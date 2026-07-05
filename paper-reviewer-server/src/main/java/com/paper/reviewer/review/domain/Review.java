package com.paper.reviewer.review.domain;

import tools.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public record Review(Long id, long userId, long paperId, ReviewType reviewType, ReviewStatus status,
                     String sourceLanguage, String outputLanguage, JsonNode fieldAnalysis,
                     String editorialDecisionMarkdown, String revisionRoadmapMarkdown,
                     String authorQuestionsMarkdown, String errorMessage,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
    public Review transitionTo(ReviewStatus target, LocalDateTime now) {
        if (!status.canTransitionTo(reviewType, target))
            throw new IllegalStateException("Invalid review transition: " + status + " -> " + target);
        return new Review(id, userId, paperId, reviewType, target, sourceLanguage, outputLanguage,
                fieldAnalysis, editorialDecisionMarkdown, revisionRoadmapMarkdown,
                authorQuestionsMarkdown, target == ReviewStatus.FAILED ? errorMessage : null, createdAt, now);
    }

    public Review withAnalysis(JsonNode analysis, String sourceLanguage, LocalDateTime now) {
        return new Review(id, userId, paperId, reviewType, status, sourceLanguage, outputLanguage,
                analysis, editorialDecisionMarkdown, revisionRoadmapMarkdown, authorQuestionsMarkdown,
                errorMessage, createdAt, now);
    }

    public Review complete(String decision, String roadmap, String questions, LocalDateTime now) {
        Review moved = transitionTo(ReviewStatus.COMPLETED, now);
        return new Review(id, userId, paperId, reviewType, moved.status, sourceLanguage, outputLanguage,
                fieldAnalysis, decision, roadmap, questions, null, createdAt, now);
    }

    public Review fail(String message, LocalDateTime now) {
        if (!status.canTransitionTo(reviewType, ReviewStatus.FAILED))
            throw new IllegalStateException("Completed review cannot fail");
        return new Review(id, userId, paperId, reviewType, ReviewStatus.FAILED, sourceLanguage,
                outputLanguage, fieldAnalysis, editorialDecisionMarkdown, revisionRoadmapMarkdown,
                authorQuestionsMarkdown, message, createdAt, now);
    }
}
