package com.paper.reviewer.ai.parser;

import java.util.List;

public record ReviewerTeam(List<Reviewer> reviewers) {
    public record Reviewer(String role, String identityDescription, String expertise, String reviewFocus) { }

    public ReviewerTeam {
        reviewers = List.copyOf(reviewers);
        if (reviewers.size() != 5) throw new IllegalArgumentException("Reviewer team must contain exactly five members");
    }
}
