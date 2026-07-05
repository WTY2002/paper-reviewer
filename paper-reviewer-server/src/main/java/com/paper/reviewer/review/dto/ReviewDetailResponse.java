package com.paper.reviewer.review.dto;

import com.paper.reviewer.review.domain.Review;
import com.paper.reviewer.review.domain.ReviewReport;
import tools.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.List;

public record ReviewDetailResponse(long reviewId, long paperId, String paperTitle, String reviewType, String status,
        String sourceLanguage, String outputLanguage, JsonNode fieldAnalysis,
        String editorialDecisionMarkdown, String revisionRoadmapMarkdown,
        String authorQuestionsMarkdown, String errorMessage, List<ReviewReport> reports,
        LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static ReviewDetailResponse from(Review r, List<ReviewReport> reports) {
        return from(r, null, reports);
    }
    public static ReviewDetailResponse from(Review r, String paperTitle, List<ReviewReport> reports) {
        return new ReviewDetailResponse(r.id(), r.paperId(), paperTitle, r.reviewType().name(), r.status().name(),
                r.sourceLanguage(), r.outputLanguage(), r.fieldAnalysis(), r.editorialDecisionMarkdown(),
                r.revisionRoadmapMarkdown(), r.authorQuestionsMarkdown(), r.errorMessage(), List.copyOf(reports),
                r.createdAt(), r.updatedAt());
    }
}
