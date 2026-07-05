package com.paper.reviewer.stream.service;

import com.paper.reviewer.stream.domain.ReviewEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class SseConnectionManager {
    public static final String SSE_EVENT_NAME = "REVIEW_EVENT";
    private static final Logger log = LoggerFactory.getLogger(SseConnectionManager.class);
    private static final long TIMEOUT_MILLIS = 30L * 60L * 1000L;

    private final ConcurrentHashMap<Long, Channel> channels = new ConcurrentHashMap<>();

    /** Registers before replaying while publication is blocked, so no event can fall into a replay/live gap. */
    public SseEmitter subscribe(long reviewId, Supplier<List<ReviewEvent>> historySupplier) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);
        emitter.onCompletion(() -> remove(reviewId, emitter));
        emitter.onTimeout(() -> remove(reviewId, emitter));
        emitter.onError(error -> remove(reviewId, emitter));

        channels.compute(reviewId, (id, existing) -> {
            Channel channel = existing == null ? new Channel() : existing;
            channel.emitters.add(emitter);
            for (ReviewEvent event : historySupplier.get()) {
                if (!send(emitter, event)) {
                    channel.emitters.remove(emitter);
                    break;
                }
            }
            return channel.emitters.isEmpty() ? null : channel;
        });
        return emitter;
    }

    public void publish(ReviewEvent event) {
        channels.computeIfPresent(event.reviewId(), (id, channel) -> {
            channel.emitters.removeIf(emitter -> !send(emitter, event));
            return channel.emitters.isEmpty() ? null : channel;
        });
    }

    int connectionCount(long reviewId) {
        Channel channel = channels.get(reviewId);
        return channel == null ? 0 : channel.emitters.size();
    }

    private void remove(long reviewId, SseEmitter emitter) {
        channels.computeIfPresent(reviewId, (id, channel) -> {
            channel.emitters.remove(emitter);
            return channel.emitters.isEmpty() ? null : channel;
        });
    }

    private boolean send(SseEmitter emitter, ReviewEvent event) {
        try {
            emitter.send(SseEmitter.event()
                    .id(Long.toString(event.sequence()))
                    .name(SSE_EVENT_NAME)
                    .data(event));
            return true;
        } catch (IOException | IllegalStateException exception) {
            log.debug("Removing closed SSE connection for review {}", event.reviewId(), exception);
            // A failed send means the servlet response is already unusable. Calling
            // complete() here attempts another flush and can throw
            // AsyncRequestNotUsableException, leaking a client disconnect into the
            // review transaction. Returning false removes it from the channel.
            return false;
        }
    }

    private static final class Channel {
        private final Set<SseEmitter> emitters = new HashSet<>();
    }
}
