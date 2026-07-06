package com.paper.reviewer.reviewerteam.service;

import com.paper.reviewer.reviewerteam.domain.ReviewerRole;

public record UpdateReviewerCommand(
        ReviewerRole role,
        String displayName,
        String identityDescription,
        String expertise,
        String reviewFocus
) { }
