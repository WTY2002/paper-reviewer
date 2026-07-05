package com.paper.reviewer.review.service;

import com.paper.reviewer.ai.orchestrator.FieldAnalysisOrchestrator;
import com.paper.reviewer.ai.orchestrator.FullReviewOrchestrator;
import com.paper.reviewer.ai.orchestrator.QuickReviewOrchestrator;
import com.paper.reviewer.ai.parser.FieldAnalysis;
import com.paper.reviewer.ai.parser.DimensionScores;
import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import com.paper.reviewer.extraction.domain.PaperExtraction;
import com.paper.reviewer.extraction.repository.PaperExtractionRepository;
import com.paper.reviewer.paper.domain.Paper;
import com.paper.reviewer.paper.repository.PaperRepository;
import com.paper.reviewer.review.domain.*;
import com.paper.reviewer.review.repository.ReviewReportRepository;
import com.paper.reviewer.review.repository.ReviewRepository;
import com.paper.reviewer.reviewerteam.repository.ReviewerTeamRepository;
import com.paper.reviewer.reviewerteam.service.ReviewerTeamService;
import com.paper.reviewer.stream.service.ReviewEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReviewWorkflowServiceTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-04T08:00:00Z"), ZoneOffset.UTC);
    private MemoryReviews reviews;
    private MemoryReports reports;
    private PaperRepository papers;
    private PaperExtractionRepository extractions;
    private ReviewerTeamService teamService;
    private ReviewerTeamRepository teamRepository;
    private FieldAnalysisOrchestrator field;
    private FullReviewOrchestrator full;
    private QuickReviewOrchestrator quick;
    private ReviewEventService events;
    private ReviewWorkflowService service;

    @BeforeEach void setUp() {
        reviews = new MemoryReviews(); reports = new MemoryReports();
        papers = mock(PaperRepository.class); extractions = mock(PaperExtractionRepository.class);
        teamService = mock(ReviewerTeamService.class); field = mock(FieldAnalysisOrchestrator.class);
        teamRepository = mock(ReviewerTeamRepository.class);
        full = mock(FullReviewOrchestrator.class); quick = mock(QuickReviewOrchestrator.class);
        events = mock(ReviewEventService.class);
        service = new ReviewWorkflowService(reviews, reports, papers, extractions,
                teamRepository, teamService, field, full, quick, events,
                new ObjectMapper(), clock);
        when(papers.findOwnedById(7, 11)).thenReturn(Optional.of(new Paper(11L, 7L, "Paper", "p.pdf",
                "p", 12, 1, "en", "EXTRACTED", LocalDateTime.now(clock), LocalDateTime.now(clock))));
        when(extractions.findByPaperId(11)).thenReturn(Optional.of(new PaperExtraction(2L, 11L, "paper text", 1,
                "COMPLETED", null, LocalDateTime.now(clock), LocalDateTime.now(clock))));
        FieldAnalysis analysis = new FieldAnalysis("Paper", "en", "CS", "AI", "empirical",
                "experiment", "Q1", "draft");
        var aiTeam = new com.paper.reviewer.ai.parser.ReviewerTeam(List.of(
                aiReviewer("EIC"), aiReviewer("METHODOLOGY"), aiReviewer("DOMAIN"),
                aiReviewer("PERSPECTIVE"), aiReviewer("DEVILS_ADVOCATE")));
        when(field.analyze(anyLong(), eq("paper text"))).thenReturn(new FieldAnalysisOrchestrator.Result(analysis, aiTeam));
        when(field.analyzeField(anyLong(), eq("paper text"))).thenReturn(analysis);
    }

    @Test void quickReviewCompletesWithOnlyEicReport() {
        when(quick.review(anyLong(), eq("paper text"), eq("en"))).thenReturn("# Quick assessment");

        Review result = service.createAndAnalyze(7, 11, ReviewType.QUICK, "en");

        assertThat(result.status()).isEqualTo(ReviewStatus.COMPLETED);
        assertThat(reports.values).singleElement().satisfies(report -> {
            assertThat(report.reviewerRole()).isEqualTo("EIC");
            assertThat(report.contentMarkdown()).contains("Quick assessment");
        });
        verifyNoInteractions(full, teamService);
        verify(field).analyzeField(result.id(), "paper text");
        verify(field, never()).analyze(anyLong(), anyString());
        verify(events).publish(eq(result.id()), eq(com.paper.reviewer.stream.domain.ReviewEventType.REVIEW_COMPLETED),
                eq("COMPLETED"), isNull(), isNull());
    }

    @Test void quickFailureIsPersistedAndPublished() {
        when(quick.review(anyLong(), anyString(), anyString())).thenThrow(new RuntimeException("provider down"));

        assertThatThrownBy(() -> service.createAndAnalyze(7, 11, ReviewType.QUICK, "en"))
                .isInstanceOf(RuntimeException.class).hasMessage("provider down");

        Review failed = reviews.values.values().iterator().next();
        assertThat(failed.status()).isEqualTo(ReviewStatus.FAILED);
        assertThat(failed.errorMessage()).isEqualTo("provider down");
        verify(events).publishFailure(failed.id(), "REVIEWING", "provider down");
    }

    @Test void fullCannotStartBeforeConfirmedAndOtherUsersCannotReadOrDelete() {
        LocalDateTime now = LocalDateTime.now(clock);
        Review pending = reviews.save(new Review(null, 7, 11, ReviewType.FULL, ReviewStatus.TEAM_PENDING,
                "en", "en", null, null, null, null, null, now, now));

        assertThatThrownBy(() -> service.start(7, pending.id())).isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException)e).getErrorCode()).isEqualTo(ErrorCode.REVIEW_TEAM_NOT_CONFIRMED);
        assertThatThrownBy(() -> service.get(8, pending.id())).isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException)e).getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
        assertThatThrownBy(() -> service.delete(8, pending.id())).isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException)e).getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test void confirmedFullReviewPersistsFiveIndependentReportsAndSynthesis() {
        LocalDateTime now = LocalDateTime.now(clock);
        Review confirmed = reviews.save(new Review(null, 7, 11, ReviewType.FULL, ReviewStatus.TEAM_CONFIRMED,
                "en", "en", null, null, null, null, null, now, now));
        List<com.paper.reviewer.reviewerteam.domain.Reviewer> members = Arrays.stream(
                com.paper.reviewer.reviewerteam.domain.ReviewerRole.values()).map(role ->
                new com.paper.reviewer.reviewerteam.domain.Reviewer(role, role.name(), "identity", "expertise", "focus")).toList();
        var team = new com.paper.reviewer.reviewerteam.domain.ReviewerTeam(4L, confirmed.id(), "Journal", members,
                now, now, now);
        when(teamRepository.findByReviewId(confirmed.id())).thenReturn(Optional.of(team));
        List<FullReviewOrchestrator.Report> generated = FullReviewOrchestrator.ROLES.stream()
                .map(role -> new FullReviewOrchestrator.Report(role, "# " + role,
                        new DimensionScores(Map.of("quality", 80)))).toList();
        when(full.review(eq(confirmed.id()), eq("paper text"), any(), eq("en")))
                .thenReturn(new FullReviewOrchestrator.Result(generated, "decision", "roadmap", "questions"));

        Review completed = service.start(7, confirmed.id());

        assertThat(completed.status()).isEqualTo(ReviewStatus.COMPLETED);
        assertThat(completed.editorialDecisionMarkdown()).isEqualTo("decision");
        assertThat(completed.revisionRoadmapMarkdown()).isEqualTo("roadmap");
        assertThat(completed.authorQuestionsMarkdown()).isEqualTo("questions");
        assertThat(reports.values).hasSize(5).extracting(ReviewReport::reviewerRole)
                .containsExactlyElementsOf(FullReviewOrchestrator.ROLES);
        verifyNoInteractions(quick);
    }

    private com.paper.reviewer.ai.parser.ReviewerTeam.Reviewer aiReviewer(String role) {
        return new com.paper.reviewer.ai.parser.ReviewerTeam.Reviewer(role, role + " identity", role + " expertise", role + " focus");
    }

    private static class MemoryReviews implements ReviewRepository {
        long sequence; Map<Long, Review> values = new LinkedHashMap<>();
        public Review save(Review r) { Review saved = copy(r, ++sequence); values.put(saved.id(), saved); return saved; }
        public Review update(Review r) { values.put(r.id(), r); return r; }
        public Optional<Review> findOwnedById(long userId, long id) { return Optional.ofNullable(values.get(id)).filter(r -> r.userId() == userId); }
        public List<Review> findAllOwnedBy(long userId) { return values.values().stream().filter(r -> r.userId() == userId).toList(); }
        public boolean deleteOwnedById(long userId, long id) { return findOwnedById(userId, id).map(r -> values.remove(id) != null).orElse(false); }
        private Review copy(Review r, long id) { return new Review(id, r.userId(), r.paperId(), r.reviewType(), r.status(), r.sourceLanguage(), r.outputLanguage(), r.fieldAnalysis(), r.editorialDecisionMarkdown(), r.revisionRoadmapMarkdown(), r.authorQuestionsMarkdown(), r.errorMessage(), r.createdAt(), r.updatedAt()); }
    }
    private static class MemoryReports implements ReviewReportRepository {
        List<ReviewReport> values = new ArrayList<>();
        public ReviewReport save(ReviewReport r) { values.add(r); return r; }
        public List<ReviewReport> findByReviewId(long id) { return values.stream().filter(r -> r.reviewId() == id).toList(); }
    }
}
