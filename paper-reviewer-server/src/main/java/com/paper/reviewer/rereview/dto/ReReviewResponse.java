package com.paper.reviewer.rereview.dto;

import com.paper.reviewer.rereview.domain.ReReview;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record ReReviewResponse(long rereviewId, long originalReviewId, long revisedPaperId,
                               long responsePaperId, String outputLanguage, String status,
                               String resultMarkdown, JsonNode checklist, String errorMessage,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static ReReviewResponse from(ReReview rereview) {
        return new ReReviewResponse(rereview.id(), rereview.originalReviewId(), rereview.revisedPaperId(),
                rereview.responsePaperId(), rereview.outputLanguage(), rereview.status().name(),
                rereview.resultMarkdown(), rereview.checklistJson(), rereview.errorMessage(),
                rereview.createdAt(), rereview.updatedAt());
    }
}
