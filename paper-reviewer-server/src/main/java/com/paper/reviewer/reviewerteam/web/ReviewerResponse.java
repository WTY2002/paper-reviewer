package com.paper.reviewer.reviewerteam.web;

import com.paper.reviewer.reviewerteam.domain.Reviewer;
import com.paper.reviewer.reviewerteam.domain.ReviewerRole;

public record ReviewerResponse(
        ReviewerRole role,
        String displayName,
        String identityDescription,
        String expertise,
        String reviewFocus
) {
    public static ReviewerResponse from(Reviewer reviewer) {
        return new ReviewerResponse(reviewer.role(), reviewer.displayName(), reviewer.identityDescription(),
                reviewer.expertise(), reviewer.reviewFocus());
    }
}
