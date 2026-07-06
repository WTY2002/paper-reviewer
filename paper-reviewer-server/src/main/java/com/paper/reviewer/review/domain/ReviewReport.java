package com.paper.reviewer.review.domain;

import tools.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public record ReviewReport(Long id, long reviewId, String reviewerRole, String contentMarkdown,
                           JsonNode scores, String status,
                           LocalDateTime createdAt, LocalDateTime updatedAt) { }
