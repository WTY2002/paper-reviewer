package com.paper.reviewer.reviewerteam.service;

import com.paper.reviewer.reviewerteam.domain.ReviewerTeam;
import com.paper.reviewer.stream.domain.ReviewEventType;
import com.paper.reviewer.stream.service.ReviewEventService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
public class StreamReviewerTeamEventPublisher implements ReviewerTeamEventPublisher {
    private final ReviewEventService events;
    private final ObjectMapper objectMapper;

    public StreamReviewerTeamEventPublisher(ReviewEventService events, ObjectMapper objectMapper) {
        this.events = events;
        this.objectMapper = objectMapper;
    }

    @Override
    public void generated(ReviewerTeam team) {
        events.publish(team.reviewId(), ReviewEventType.REVIEWER_TEAM_GENERATED, "TEAM_PENDING", null, payload(team));
    }

    @Override
    public void confirmed(ReviewerTeam team) {
        events.publish(team.reviewId(), ReviewEventType.REVIEWER_TEAM_CONFIRMED, "TEAM_CONFIRMED", null, payload(team));
    }

    private ObjectNode payload(ReviewerTeam team) {
        return objectMapper.createObjectNode()
                .put("teamId", team.id())
                .put("reviewerCount", team.reviewers().size())
                .put("targetVenue", team.targetVenue());
    }
}
