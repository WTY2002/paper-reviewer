package com.paper.reviewer.review.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewStatusTest {
    @Test void fullRequiresTeamCheckpoint() {
        assertThat(ReviewStatus.ANALYZING.canTransitionTo(ReviewType.FULL, ReviewStatus.TEAM_PENDING)).isTrue();
        assertThat(ReviewStatus.ANALYZING.canTransitionTo(ReviewType.FULL, ReviewStatus.REVIEWING)).isFalse();
        assertThat(ReviewStatus.TEAM_PENDING.canTransitionTo(ReviewType.FULL, ReviewStatus.TEAM_CONFIRMED)).isTrue();
        assertThat(ReviewStatus.TEAM_CONFIRMED.canTransitionTo(ReviewType.FULL, ReviewStatus.REVIEWING)).isTrue();
        assertThat(ReviewStatus.REVIEWING.canTransitionTo(ReviewType.FULL, ReviewStatus.COMPLETED)).isTrue();
    }

    @Test void quickHasNoTeamCheckpoint() {
        assertThat(ReviewStatus.ANALYZING.canTransitionTo(ReviewType.QUICK, ReviewStatus.REVIEWING)).isTrue();
        assertThat(ReviewStatus.ANALYZING.canTransitionTo(ReviewType.QUICK, ReviewStatus.TEAM_PENDING)).isFalse();
        assertThat(ReviewStatus.REVIEWING.canTransitionTo(ReviewType.QUICK, ReviewStatus.COMPLETED)).isTrue();
    }

    @Test void onlyNonTerminalReviewsCanFail() {
        assertThat(ReviewStatus.ANALYZING.canTransitionTo(ReviewType.FULL, ReviewStatus.FAILED)).isTrue();
        assertThat(ReviewStatus.REVIEWING.canTransitionTo(ReviewType.QUICK, ReviewStatus.FAILED)).isTrue();
        assertThat(ReviewStatus.COMPLETED.canTransitionTo(ReviewType.FULL, ReviewStatus.FAILED)).isFalse();
        assertThat(ReviewStatus.FAILED.canTransitionTo(ReviewType.QUICK, ReviewStatus.FAILED)).isFalse();
    }
}
