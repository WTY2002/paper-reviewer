package com.paper.reviewer.rereview.domain;

import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record ReReview(Long id, long userId, long originalReviewId, long revisedPaperId,
                       long responsePaperId, String outputLanguage, ReReviewStatus status,
                       String resultMarkdown, JsonNode checklistJson, String errorMessage,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {

    public ReReview transitionTo(ReReviewStatus target, LocalDateTime now) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException("Invalid re-review transition: " + status + " -> " + target);
        }
        return new ReReview(id, userId, originalReviewId, revisedPaperId, responsePaperId,
                outputLanguage, target, resultMarkdown, checklistJson,
                target == ReReviewStatus.FAILED ? errorMessage : null, createdAt, now);
    }

    public ReReview complete(String markdown, JsonNode checklist, LocalDateTime now) {
        ReReview moved = transitionTo(ReReviewStatus.COMPLETED, now);
        return new ReReview(id, userId, originalReviewId, revisedPaperId, responsePaperId,
                outputLanguage, moved.status, markdown, checklist, null, createdAt, now);
    }

    public ReReview fail(String message, LocalDateTime now) {
        if (!status.canTransitionTo(ReReviewStatus.FAILED)) {
            throw new IllegalStateException("Completed or deleted re-review cannot fail");
        }
        return new ReReview(id, userId, originalReviewId, revisedPaperId, responsePaperId,
                outputLanguage, ReReviewStatus.FAILED, resultMarkdown, checklistJson, message,
                createdAt, now);
    }
}
