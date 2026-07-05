package com.paper.reviewer.reviewerteam.service;

import java.util.Optional;

/** Narrow Review boundary used by the team module. */
public interface ReviewerTeamReviewAccess {
    Optional<ReviewState> findOwned(long reviewId, long userId);
    Optional<ReviewState> find(long reviewId);
    boolean transition(long reviewId, String expectedStatus, String newStatus);

    record ReviewState(long reviewId, long userId, String status) {}
}
