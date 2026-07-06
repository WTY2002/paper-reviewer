package com.paper.reviewer.reviewerteam.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateReviewerTeamRequest(
        @Size(max = 500) String targetVenue,
        @NotNull @Size(min = 5, max = 5) List<@Valid UpdateReviewerRequest> reviewers
) {
    public com.paper.reviewer.reviewerteam.service.UpdateReviewerTeamCommand toCommand() {
        return new com.paper.reviewer.reviewerteam.service.UpdateReviewerTeamCommand(
                targetVenue, reviewers.stream().map(UpdateReviewerRequest::toCommand).toList());
    }
}
