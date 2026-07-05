package com.paper.reviewer.review.dto;

import com.paper.reviewer.review.domain.Review;
import java.time.LocalDateTime;

public record ReviewSummaryResponse(long reviewId, long paperId, String paperTitle, String reviewType, String status,
                                    String outputLanguage, String errorMessage,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static ReviewSummaryResponse from(Review review) {
        return from(review, null);
    }
    public static ReviewSummaryResponse from(Review review, String paperTitle) {
        return new ReviewSummaryResponse(review.id(), review.paperId(), paperTitle, review.reviewType().name(),
                review.status().name(), review.outputLanguage(), review.errorMessage(),
                review.createdAt(), review.updatedAt());
    }
}
