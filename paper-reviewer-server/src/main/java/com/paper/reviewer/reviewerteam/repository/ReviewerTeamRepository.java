package com.paper.reviewer.reviewerteam.repository;

import com.paper.reviewer.reviewerteam.domain.ReviewerTeam;

import java.util.Optional;

public interface ReviewerTeamRepository {
    ReviewerTeam save(ReviewerTeam team);
    Optional<ReviewerTeam> findByReviewId(long reviewId);
}
