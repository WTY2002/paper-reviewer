package com.paper.reviewer.stream.service;

import com.paper.reviewer.stream.domain.ReviewEvent;
import com.paper.reviewer.stream.domain.ReviewEventType;
import com.paper.reviewer.stream.repository.ReviewEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewEventService {
    private final ReviewEventRepository repository;
    private final SseConnectionManager connectionManager;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ReviewEventService(ReviewEventRepository repository, SseConnectionManager connectionManager,
                              ObjectMapper objectMapper, Clock clock) {
        this.repository = repository;
        this.connectionManager = connectionManager;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    /** The review row lock serializes sequence allocation for one review across application instances. */
    @Transactional
    public ReviewEvent publish(long reviewId, ReviewEventType type, String stage,
                               String reviewerRole, JsonNode payload) {
        repository.lockReview(reviewId);
        long sequence = repository.nextSequence(reviewId);
        ReviewEvent saved = repository.save(new ReviewEvent(null, reviewId, type, stage, reviewerRole,
                sequence, payload, LocalDateTime.now(clock)));
        publishAfterCommit(saved);
        return saved;
    }

    @Transactional
    public ReviewEvent publishFailure(long reviewId, String stage, String message) {
        return publish(reviewId, ReviewEventType.REVIEW_FAILED, stage, null,
                objectMapper.createObjectNode().put("message", message));
    }

    @Transactional(readOnly = true)
    public List<ReviewEvent> history(long reviewId) {
        return repository.findByReviewId(reviewId);
    }

    private void publishAfterCommit(ReviewEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            connectionManager.publish(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                connectionManager.publish(event);
            }
        });
    }
}
