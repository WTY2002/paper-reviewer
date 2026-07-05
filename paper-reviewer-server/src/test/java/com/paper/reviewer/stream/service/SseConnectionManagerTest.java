package com.paper.reviewer.stream.service;

import com.paper.reviewer.stream.domain.ReviewEvent;
import com.paper.reviewer.stream.domain.ReviewEventType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class SseConnectionManagerTest {
    @Test
    void removesDisconnectedEmitterWithoutBreakingEventPublication() {
        SseConnectionManager manager = new SseConnectionManager();
        var emitter = manager.subscribe(42L, List::of);
        emitter.completeWithError(new IOException("browser disconnected"));
        ReviewEvent event = new ReviewEvent(1L, 42L, ReviewEventType.REVIEWER_REPORT_STARTED,
                "REVIEWING", "EIC", 1L, new ObjectMapper().createObjectNode(), LocalDateTime.now());

        assertThatCode(() -> manager.publish(event)).doesNotThrowAnyException();
        assertThat(manager.connectionCount(42L)).isZero();
    }
}
