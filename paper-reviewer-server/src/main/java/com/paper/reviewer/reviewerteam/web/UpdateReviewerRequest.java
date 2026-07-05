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
) {}
