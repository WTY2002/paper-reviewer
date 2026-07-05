package com.paper.reviewer.review.domain;

import java.util.EnumSet;

public enum ReviewStatus {
    CREATED, ANALYZING, TEAM_PENDING, TEAM_CONFIRMED, REVIEWING, VERIFYING, COMPLETED, FAILED;

    public boolean canTransitionTo(ReviewType type, ReviewStatus target) {
        if (target == FAILED) return this != COMPLETED && this != FAILED;
        return switch (type) {
            case FULL -> switch (this) {
                case CREATED -> target == ANALYZING;
                case ANALYZING -> target == TEAM_PENDING;
                case TEAM_PENDING -> target == TEAM_CONFIRMED;
                case TEAM_CONFIRMED -> target == REVIEWING;
                case REVIEWING -> target == COMPLETED;
                default -> false;
            };
            case QUICK -> switch (this) {
                case CREATED -> target == ANALYZING;
                case ANALYZING -> target == REVIEWING;
                case REVIEWING -> target == COMPLETED;
                default -> false;
            };
            case REREVIEW -> switch (this) {
                case CREATED -> target == VERIFYING;
                case VERIFYING -> target == COMPLETED;
                default -> false;
            };
        };
    }

    public static EnumSet<ReviewStatus> terminal() { return EnumSet.of(COMPLETED, FAILED); }
}
