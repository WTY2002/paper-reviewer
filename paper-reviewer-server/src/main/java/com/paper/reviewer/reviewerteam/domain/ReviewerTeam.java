package com.paper.reviewer.reviewerteam.domain;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record ReviewerTeam(
        Long id,
        long reviewId,
        String targetVenue,
        List<Reviewer> reviewers,
        LocalDateTime confirmedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public ReviewerTeam {
        reviewers = List.copyOf(Objects.requireNonNull(reviewers, "reviewers"));
        validateFiveRoles(reviewers);
    }

    public boolean confirmed() { return confirmedAt != null; }

    public ReviewerTeam edit(String targetVenue, List<Reviewer> editedReviewers, LocalDateTime now) {
        if (confirmed()) throw new IllegalStateException("Confirmed reviewer team is locked");
        validateImmutableFields(editedReviewers);
        List<Reviewer> updated = reviewers.stream()
                .map(existing -> {
                    Reviewer edited = byRole(editedReviewers, existing.role());
                    return existing.edit(edited.identityDescription(), edited.reviewFocus());
                }).toList();
        return new ReviewerTeam(id, reviewId, targetVenue, updated, null, createdAt, now);
    }

    public ReviewerTeam confirm(LocalDateTime now) {
        if (confirmed()) throw new IllegalStateException("Reviewer team is already confirmed");
        return new ReviewerTeam(id, reviewId, targetVenue, reviewers, now, createdAt, now);
    }

    private void validateImmutableFields(List<Reviewer> edited) {
        validateFiveRoles(edited);
        for (Reviewer current : reviewers) {
            Reviewer candidate = byRole(edited, current.role());
            if (!current.displayName().equals(candidate.displayName())
                    || !current.expertise().equals(candidate.expertise())) {
                throw new IllegalArgumentException("displayName and expertise cannot be changed");
            }
        }
    }

    private static Reviewer byRole(List<Reviewer> reviewers, ReviewerRole role) {
        return reviewers.stream().filter(reviewer -> reviewer.role() == role).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing reviewer role: " + role));
    }

    public static void validateFiveRoles(List<Reviewer> reviewers) {
        if (reviewers.size() != ReviewerRole.values().length) {
            throw new IllegalArgumentException("Reviewer team must contain exactly five reviewers");
        }
        Set<ReviewerRole> roles = reviewers.stream().map(Reviewer::role)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ReviewerRole.class)));
        if (roles.size() != reviewers.size() || !roles.equals(EnumSet.allOf(ReviewerRole.class))) {
            throw new IllegalArgumentException("Reviewer team must contain each required role exactly once");
        }
    }
}
