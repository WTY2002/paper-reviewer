package com.paper.reviewer.review.repository;

import com.paper.reviewer.review.domain.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    Review save(Review review);
    Review update(Review review);
    Optional<Review> findOwnedById(long userId, long reviewId);
    List<Review> findAllOwnedBy(long userId);
    boolean deleteOwnedById(long userId, long reviewId);
}
