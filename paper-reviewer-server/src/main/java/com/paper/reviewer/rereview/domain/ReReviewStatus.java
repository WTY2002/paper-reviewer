package com.paper.reviewer.rereview.domain;

public enum ReReviewStatus {
    CREATED, UPLOADING, EXTRACTING, VERIFYING, COMPLETED, FAILED, DELETED;

    public boolean canTransitionTo(ReReviewStatus target) {
        if (target == FAILED) return this != COMPLETED && this != FAILED && this != DELETED;
        if (target == DELETED) return this != DELETED;
        return switch (this) {
            case CREATED -> target == VERIFYING || target == UPLOADING;
            case UPLOADING -> target == EXTRACTING;
            case EXTRACTING -> target == CREATED || target == VERIFYING;
            case VERIFYING -> target == COMPLETED;
            default -> false;
        };
    }
}
