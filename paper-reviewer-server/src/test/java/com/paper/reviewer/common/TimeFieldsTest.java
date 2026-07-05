package com.paper.reviewer.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TimeFieldsTest {
    @Test
    void lifecycleTimestampsUseInjectedUtcClock() {
        Instant instant = Instant.parse("2026-07-04T10:00:00Z");
        Clock clock = Clock.fixed(instant, ZoneOffset.UTC);
        TimeFields fields = new TimeFields();

        fields.markCreated(clock);
        fields.markDeleted(clock);

        assertThat(fields.getCreatedAt()).isEqualTo(instant);
        assertThat(fields.getUpdatedAt()).isEqualTo(instant);
        assertThat(fields.getDeletedAt()).isEqualTo(instant);
    }
}
