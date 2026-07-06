package com.paper.reviewer.reviewerteam.service;

import java.util.List;

public record UpdateReviewerTeamCommand(String targetVenue, List<UpdateReviewerCommand> reviewers) {
    public UpdateReviewerTeamCommand {
        reviewers = List.copyOf(reviewers);
    }
}
