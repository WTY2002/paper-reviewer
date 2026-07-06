package com.paper.reviewer.review.service;

import com.paper.reviewer.ai.orchestrator.FieldAnalysisOrchestrator;
import com.paper.reviewer.ai.orchestrator.FullReviewOrchestrator;
import com.paper.reviewer.ai.orchestrator.QuickReviewOrchestrator;
import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import com.paper.reviewer.extraction.repository.PaperExtractionRepository;
import com.paper.reviewer.paper.domain.Paper;
import com.paper.reviewer.paper.repository.PaperRepository;
import com.paper.reviewer.review.domain.*;
import com.paper.reviewer.review.repository.ReviewReportRepository;
import com.paper.reviewer.review.repository.ReviewRepository;
import com.paper.reviewer.reviewerteam.domain.Reviewer;
import com.paper.reviewer.reviewerteam.domain.ReviewerRole;
import com.paper.reviewer.reviewerteam.repository.ReviewerTeamRepository;
import com.paper.reviewer.reviewerteam.service.ReviewerTeamService;
import com.paper.reviewer.stream.domain.ReviewEventType;
import com.paper.reviewer.stream.service.ReviewEventService;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewWorkflowService {
    private final ReviewRepository reviews;
    private final ReviewReportRepository reports;
    private final PaperRepository papers;
    private final PaperExtractionRepository extractions;
    private final ReviewerTeamRepository teams;
    private final ReviewerTeamService teamService;
    private final FieldAnalysisOrchestrator fieldAnalysis;
    private final FullReviewOrchestrator fullReview;
    private final QuickReviewOrchestrator quickReview;
    private final ReviewEventService events;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ReviewWorkflowService(ReviewRepository reviews, ReviewReportRepository reports,
            PaperRepository papers, PaperExtractionRepository extractions, ReviewerTeamRepository teams,
            ReviewerTeamService teamService, FieldAnalysisOrchestrator fieldAnalysis,
            FullReviewOrchestrator fullReview, QuickReviewOrchestrator quickReview,
            ReviewEventService events, ObjectMapper objectMapper, Clock clock) {
        this.reviews = reviews; this.reports = reports; this.papers = papers; this.extractions = extractions;
        this.teams = teams; this.teamService = teamService; this.fieldAnalysis = fieldAnalysis;
        this.fullReview = fullReview; this.quickReview = quickReview; this.events = events;
        this.objectMapper = objectMapper; this.clock = clock;
    }

    public Review createAndAnalyze(long userId, long paperId, ReviewType type, String outputLanguage) {
        if (type == ReviewType.REREVIEW)
            throw new BusinessException(ErrorCode.REVIEW_INVALID_STATUS, "REREVIEW must use the re-review endpoint");
        Paper paper = papers.findOwnedById(userId, paperId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAPER_NOT_FOUND));
        String paperText = extractions.findByPaperId(paperId)
                .filter(value -> "COMPLETED".equals(value.extractionStatus()) || "EXTRACTED".equals(value.extractionStatus()))
                .map(value -> value.extractedText())
                .filter(value -> value != null && !value.isBlank())
                .orElseThrow(() -> new BusinessException(ErrorCode.PDF_EXTRACTION_FAILED,
                        "Paper text is not available for review"));
        LocalDateTime now = now();
        Review review = reviews.save(new Review(null, userId, paperId, type, ReviewStatus.ANALYZING,
                paper.language(), normalizeLanguage(outputLanguage), null, null, null, null, null, now, now));
        try {
            com.paper.reviewer.ai.parser.FieldAnalysis analyzedField;
            FieldAnalysisOrchestrator.Result result = null;
            if (type == ReviewType.FULL) {
                result = fieldAnalysis.analyze(review.id(), paperText);
                analyzedField = result.field();
            } else {
                analyzedField = fieldAnalysis.analyzeField(review.id(), paperText);
            }
            review = reviews.update(review.withAnalysis(objectMapper.valueToTree(analyzedField),
                    analyzedField.language(), now()));
            if (type == ReviewType.FULL) {
                com.paper.reviewer.reviewerteam.domain.ReviewerTeam generated = toDomainTeam(review.id(), result.team());
                teamService.saveGeneratedTeam(review.id(), generated.targetVenue(), generated.reviewers());
                return owned(userId, review.id());
            }
            review = reviews.update(review.transitionTo(ReviewStatus.REVIEWING, now()));
            events.publish(review.id(), ReviewEventType.QUICK_REVIEW_STARTED, "QUICK_REVIEW", "EIC", null);
            String markdown = quickReview.review(review.id(), paperText, review.outputLanguage());
            saveReport(review.id(), "EIC", markdown, null);
            review = reviews.update(review.complete(null, null, null, now()));
            events.publish(review.id(), ReviewEventType.REVIEW_COMPLETED, "COMPLETED", null, null);
            return review;
        } catch (RuntimeException exception) {
            fail(review, exception);
            throw exception;
        }
    }

    public Review start(long userId, long reviewId) {
        Review review = owned(userId, reviewId);
        if (review.reviewType() != ReviewType.FULL || review.status() != ReviewStatus.TEAM_CONFIRMED)
            throw new BusinessException(review.reviewType() == ReviewType.FULL
                    ? ErrorCode.REVIEW_TEAM_NOT_CONFIRMED : ErrorCode.REVIEW_INVALID_STATUS);
        com.paper.reviewer.reviewerteam.domain.ReviewerTeam team = teams.findByReviewId(reviewId)
                .filter(com.paper.reviewer.reviewerteam.domain.ReviewerTeam::confirmed)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_TEAM_NOT_CONFIRMED));
        String paperText = extractions.findByPaperId(review.paperId()).map(value -> value.extractedText())
                .filter(value -> value != null && !value.isBlank())
                .orElseThrow(() -> new BusinessException(ErrorCode.PDF_EXTRACTION_FAILED));
        review = reviews.update(review.transitionTo(ReviewStatus.REVIEWING, now()));
        events.publish(review.id(), ReviewEventType.FULL_REVIEW_STARTED, "FULL_REVIEW", null, null);
        try {
            FullReviewOrchestrator.Result result = fullReview.review(review.id(), paperText,
                    toAiTeam(team), review.outputLanguage());
            for (FullReviewOrchestrator.Report report : result.reports())
                saveReport(review.id(), report.role(), report.markdown(), objectMapper.valueToTree(report.scores().scores()));
            review = reviews.update(review.complete(result.editorialDecision(), result.revisionRoadmap(),
                    result.questionsForAuthors(), now()));
            events.publish(review.id(), ReviewEventType.REVIEW_COMPLETED, "COMPLETED", null, null);
            return review;
        } catch (RuntimeException exception) {
            fail(review, exception);
            throw exception;
        }
    }

    public List<Review> list(long userId) { return reviews.findAllOwnedBy(userId); }
    public Review get(long userId, long reviewId) { return owned(userId, reviewId); }
    public String paperTitle(long userId, long paperId) {
        return papers.findOwnedById(userId, paperId).map(Paper::title)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAPER_NOT_FOUND));
    }
    public List<ReviewReport> getReports(long userId, long reviewId) {
        owned(userId, reviewId); return reports.findByReviewId(reviewId);
    }
    private Review owned(long userId, long reviewId) {
        return reviews.findOwnedById(userId, reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private void saveReport(long reviewId, String role, String markdown, tools.jackson.databind.JsonNode scores) {
        LocalDateTime now = now();
        reports.save(new ReviewReport(null, reviewId, role, markdown, scores, "COMPLETED", now, now));
    }

    private void fail(Review review, RuntimeException exception) {
        String message = exception.getMessage() == null ? "Review generation failed" : exception.getMessage();
        try {
            reviews.update(review.fail(message, now()));
            events.publishFailure(review.id(), review.status().name(), message);
        } catch (RuntimeException persistenceFailure) {
            exception.addSuppressed(persistenceFailure);
        }
    }

    private com.paper.reviewer.reviewerteam.domain.ReviewerTeam toDomainTeam(long reviewId,
            com.paper.reviewer.ai.parser.ReviewerTeam source) {
        List<Reviewer> reviewers = source.reviewers().stream().map(r -> {
            ReviewerRole role = ReviewerRole.valueOf(r.role());
            return new Reviewer(role, displayName(role), r.identityDescription(), r.expertise(), r.reviewFocus());
        }).toList();
        LocalDateTime now = now();
        return new com.paper.reviewer.reviewerteam.domain.ReviewerTeam(null, reviewId, null, reviewers,
                null, now, now);
    }

    private com.paper.reviewer.ai.parser.ReviewerTeam toAiTeam(
            com.paper.reviewer.reviewerteam.domain.ReviewerTeam source) {
        return new com.paper.reviewer.ai.parser.ReviewerTeam(source.reviewers().stream()
                .map(r -> new com.paper.reviewer.ai.parser.ReviewerTeam.Reviewer(r.role().name(),
                        r.identityDescription(), r.expertise(), r.reviewFocus())).toList());
    }

    private String displayName(ReviewerRole role) {
        return switch (role) {
            case EIC -> "Editor-in-Chief";
            case METHODOLOGY -> "Methodology Reviewer";
            case DOMAIN -> "Domain Reviewer";
            case PERSPECTIVE -> "Perspective Reviewer";
            case DEVILS_ADVOCATE -> "Devil's Advocate";
        };
    }

    private String normalizeLanguage(String language) {
        return language == null || language.isBlank() ? "AUTO" : language;
    }
    private LocalDateTime now() { return LocalDateTime.now(clock); }
}
