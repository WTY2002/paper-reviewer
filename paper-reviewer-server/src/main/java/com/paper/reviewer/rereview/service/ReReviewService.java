package com.paper.reviewer.rereview.service;

import com.paper.reviewer.ai.orchestrator.ReReviewOrchestrator;
import com.paper.reviewer.ai.parser.ReReviewChecklist;
import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import com.paper.reviewer.extraction.repository.PaperExtractionRepository;
import com.paper.reviewer.paper.domain.Paper;
import com.paper.reviewer.paper.service.PaperService;
import com.paper.reviewer.review.domain.Review;
import com.paper.reviewer.review.domain.ReviewStatus;
import com.paper.reviewer.review.repository.ReviewReportRepository;
import com.paper.reviewer.review.service.ReviewWorkflowService;
import com.paper.reviewer.rereview.domain.ReReview;
import com.paper.reviewer.rereview.domain.ReReviewStatus;
import com.paper.reviewer.rereview.repository.ReReviewRepository;
import com.paper.reviewer.stream.domain.ReviewEventType;
import com.paper.reviewer.stream.service.ReviewEventService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class ReReviewService {
    private final ReReviewRepository rereviews; private final ReviewWorkflowService reviews;
    private final ReviewReportRepository reports; private final PaperService papers;
    private final PaperExtractionRepository extractions; private final ReReviewOrchestrator orchestrator;
    private final ReviewEventService events; private final ObjectMapper mapper; private final Clock clock;

    public ReReviewService(ReReviewRepository rereviews, ReviewWorkflowService reviews,
            ReviewReportRepository reports, PaperService papers, PaperExtractionRepository extractions,
            ReReviewOrchestrator orchestrator, ReviewEventService events, ObjectMapper mapper, Clock clock) {
        this.rereviews=rereviews;this.reviews=reviews;this.reports=reports;this.papers=papers;
        this.extractions=extractions;this.orchestrator=orchestrator;this.events=events;this.mapper=mapper;this.clock=clock;
    }

    public ReReview create(long userId, long originalReviewId, MultipartFile revisedFile,
                           MultipartFile responseFile, String outputLanguage) {
        Review original = reviews.get(userId, originalReviewId);
        if (original.status() != ReviewStatus.COMPLETED)
            throw new BusinessException(ErrorCode.REVIEW_INVALID_STATUS, "Only a completed review can be re-reviewed");
        Paper revised = null;
        try {
            revised = papers.upload(userId, revisedFile);
            Paper response = papers.upload(userId, responseFile);
            LocalDateTime now = now();
            return rereviews.save(new ReReview(null, userId, originalReviewId, revised.id(), response.id(),
                    normalizeLanguage(outputLanguage), ReReviewStatus.CREATED, null, null, null, now, now));
        } catch (RuntimeException exception) {
            if (revised != null) try { papers.delete(userId, revised.id()); } catch (RuntimeException ignored) { }
            throw exception;
        }
    }

    public ReReview start(long userId, long rereviewId) {
        ReReview rereview = get(userId, rereviewId);
        if (rereview.status() != ReReviewStatus.CREATED)
            throw new BusinessException(ErrorCode.REVIEW_INVALID_STATUS);
        Review original = reviews.get(userId, rereview.originalReviewId());
        String revisedText = extracted(rereview.revisedPaperId());
        String responseText = extracted(rereview.responsePaperId());
        String roadmap = original.revisionRoadmapMarkdown();
        if (roadmap == null || roadmap.isBlank()) roadmap = reports.findByReviewId(original.id()).stream()
                .map(report -> report.contentMarkdown()).collect(Collectors.joining("\n\n"));
        ReReview verifying = rereviews.update(rereview.transitionTo(ReReviewStatus.VERIFYING, now()));
        events.publish(original.id(), ReviewEventType.REREVIEW_STARTED, "REREVIEW", "EIC", null);
        try {
            ReReviewChecklist checklist = orchestrator.review(original.id(), roadmap, revisedText,
                    responseText, rereview.outputLanguage());
            ReReview completed = rereviews.update(verifying.complete(checklist.resultMarkdown(),
                    mapper.valueToTree(checklist), now()));
            events.publish(original.id(), ReviewEventType.REVIEW_COMPLETED, "REREVIEW_COMPLETED", null, null);
            return completed;
        } catch (RuntimeException exception) {
            String message = exception.getMessage() == null ? "Re-review failed" : exception.getMessage();
            rereviews.update(verifying.fail(message, now()));
            events.publishFailure(original.id(), "REREVIEW", message);
            throw exception;
        }
    }

    public ReReview get(long userId, long rereviewId) {
        return rereviews.findOwnedById(userId, rereviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
    }
    private String extracted(long paperId) { return extractions.findByPaperId(paperId)
            .map(value -> value.extractedText()).filter(value -> value != null && !value.isBlank())
            .orElseThrow(() -> new BusinessException(ErrorCode.PDF_EXTRACTION_FAILED)); }
    private String normalizeLanguage(String value) { return value == null || value.isBlank() ? "AUTO" : value; }
    private LocalDateTime now(){return LocalDateTime.now(clock);}
}
