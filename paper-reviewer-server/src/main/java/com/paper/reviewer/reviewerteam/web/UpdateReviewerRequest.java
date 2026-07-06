package com.paper.reviewer.reviewerteam.web;

import com.paper.reviewer.reviewerteam.domain.ReviewerRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateReviewerRequest(
        @NotNull ReviewerRole role,
        String displayName,
        @NotBlank String identityDescription,
        String expertise,
        @NotBlank String reviewFocus
) {
    public com.paper.reviewer.reviewerteam.service.UpdateReviewerCommand toCommand() {
        return new com.paper.reviewer.reviewerteam.service.UpdateReviewerCommand(
                role, displayName, identityDescription, expertise, reviewFocus);
    }
}
