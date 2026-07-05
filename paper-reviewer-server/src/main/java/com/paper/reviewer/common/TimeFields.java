package com.paper.reviewer.common;

import java.time.Clock;
import java.time.Instant;

/**
 * Reusable UTC timestamp convention for database fields created_at, updated_at and deleted_at.
 * API models should expose these values as ISO-8601 instants.
 */
public class TimeFields {
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public void markCreated(Clock clock) {
        Instant now = clock.instant();
        createdAt = now;
        updatedAt = now;
    }

    public void markUpdated(Clock clock) {
        updatedAt = clock.instant();
    }

    public void markDeleted(Clock clock) {
        Instant now = clock.instant();
        deletedAt = now;
        updatedAt = now;
    }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
}
