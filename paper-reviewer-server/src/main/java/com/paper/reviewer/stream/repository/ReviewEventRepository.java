package com.paper.reviewer.stream.repository;

import com.paper.reviewer.stream.domain.ReviewEvent;

import java.util.List;

public interface ReviewEventRepository {
    void lockReview(long reviewId);

    long nextSequence(long reviewId);

    ReviewEvent save(ReviewEvent event);

    List<ReviewEvent> findByReviewId(long reviewId);
}
