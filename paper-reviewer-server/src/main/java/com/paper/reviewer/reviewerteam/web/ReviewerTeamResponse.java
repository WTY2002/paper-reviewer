package com.paper.reviewer.reviewerteam.web;

import com.paper.reviewer.reviewerteam.domain.ReviewerTeam;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewerTeamResponse(
        long reviewId,
        String status,
        Long teamId,
        String targetVenue,
        List<ReviewerResponse> reviewers,
        LocalDateTime confirmedAt
) {
    public static ReviewerTeamResponse from(ReviewerTeam team, String status) {
        return new ReviewerTeamResponse(team.reviewId(), status, team.id(), team.targetVenue(),
                team.reviewers().stream().map(ReviewerResponse::from).toList(), team.confirmedAt());
    }
}
