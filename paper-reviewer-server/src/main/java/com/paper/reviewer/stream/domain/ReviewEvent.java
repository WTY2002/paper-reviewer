package com.paper.reviewer.stream.domain;

import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record ReviewEvent(
        Long id,
        Long reviewId,
        ReviewEventType type,
        String stage,
        String reviewerRole,
        Long sequence,
        JsonNode payload,
        LocalDateTime createdAt
) {
}
