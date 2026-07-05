package com.paper.reviewer.reviewerteam.domain;

import java.util.Objects;

public record Reviewer(
        ReviewerRole role,
        String displayName,
        String identityDescription,
        String expertise,
        String reviewFocus
) {
    public Reviewer {
        Objects.requireNonNull(role, "role");
        requireText(displayName, "displayName");
        requireText(identityDescription, "identityDescription");
        requireText(expertise, "expertise");
        requireText(reviewFocus, "reviewFocus");
    }

    public Reviewer edit(String identityDescription, String reviewFocus) {
        return new Reviewer(role, displayName, identityDescription, expertise, reviewFocus);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
    }
}
