package com.paper.reviewer.reviewerteam.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewerTeamTest {
    @Test
    void rejectsAddingOrRemovingReviewers() {
        List<Reviewer> four = reviewers().subList(0, 4);
        assertThatThrownBy(() -> team(four)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactly five");

        List<Reviewer> six = new ArrayList<>(reviewers());
        six.add(reviewer(ReviewerRole.EIC));
        assertThatThrownBy(() -> team(six)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactly five");
    }

    @Test
    void rejectsChangingOrDuplicatingRoles() {
        List<Reviewer> changed = new ArrayList<>(reviewers());
        changed.set(0, reviewer(ReviewerRole.DOMAIN));
        assertThatThrownBy(() -> team(changed)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("each required role exactly once");
    }

    @Test
    void confirmedTeamCannotBeEditedOrConfirmedAgain() {
        LocalDateTime now = LocalDateTime.parse("2026-07-04T12:00:00");
        ReviewerTeam confirmed = team(reviewers()).confirm(now);
        assertThatThrownBy(() -> confirmed.edit("Venue", reviewers(), now.plusMinutes(1)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("locked");
        assertThatThrownBy(() -> confirmed.confirm(now.plusMinutes(1)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("already confirmed");
    }

    private ReviewerTeam team(List<Reviewer> reviewers) {
        LocalDateTime now = LocalDateTime.parse("2026-07-04T11:00:00");
        return new ReviewerTeam(1L, 2L, "Venue", reviewers, null, now, now);
    }

    static List<Reviewer> reviewers() {
        return List.of(ReviewerRole.values()).stream().map(ReviewerTeamTest::reviewer).toList();
    }

    static Reviewer reviewer(ReviewerRole role) {
        return new Reviewer(role, role + " reviewer", role + " identity", role + " expertise", role + " focus");
    }
}
