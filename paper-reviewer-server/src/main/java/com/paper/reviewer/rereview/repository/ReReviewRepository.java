package com.paper.reviewer.rereview.repository;

import com.paper.reviewer.rereview.domain.ReReview;

import java.util.List;
import java.util.Optional;

public interface ReReviewRepository {
    ReReview save(ReReview rereview);
    ReReview update(ReReview rereview);
    Optional<ReReview> findOwnedById(long userId, long rereviewId);
    List<ReReview> findByOriginalReviewId(long userId, long reviewId);
    boolean deleteOwnedById(long userId, long rereviewId);
}
