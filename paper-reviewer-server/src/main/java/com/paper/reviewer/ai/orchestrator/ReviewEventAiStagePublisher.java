package com.paper.reviewer.ai.orchestrator;

import com.paper.reviewer.stream.domain.ReviewEventType;
import com.paper.reviewer.stream.service.ReviewEventService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class ReviewEventAiStagePublisher implements AiStagePublisher {
    private final ReviewEventService events;
    private final ObjectMapper objectMapper;
    public ReviewEventAiStagePublisher(ReviewEventService events, ObjectMapper objectMapper) {
        this.events = events; this.objectMapper = objectMapper;
    }
    public void started(long id, String stage, String role) {
        events.publish(id, startType(stage), stage, role, objectMapper.createObjectNode());
    }
    public void completed(long id, String stage, String role, String content) {
        events.publish(id, completeType(stage), stage, role, objectMapper.createObjectNode().put("content", content));
    }
    public void failed(long id, String stage, String message) { events.publishFailure(id, stage, message); }

    private ReviewEventType startType(String stage) {
        if ("FIELD_ANALYSIS".equals(stage)) return ReviewEventType.FIELD_ANALYSIS_STARTED;
        if ("EDITORIAL_DECISION".equals(stage)) return ReviewEventType.EDITORIAL_DECISION_STARTED;
        if ("REVISION_ROADMAP".equals(stage)) return ReviewEventType.REVISION_ROADMAP_STARTED;
        if ("AUTHOR_QUESTIONS".equals(stage)) return ReviewEventType.AUTHOR_QUESTIONS_STARTED;
        return ReviewEventType.REVIEWER_REPORT_STARTED;
    }
    private ReviewEventType completeType(String stage) {
        if ("FIELD_ANALYSIS".equals(stage)) return ReviewEventType.FIELD_ANALYSIS_COMPLETED;
        if ("REVIEWER_TEAM".equals(stage)) return ReviewEventType.REVIEWER_TEAM_GENERATED;
        return ReviewEventType.REVIEWER_REPORT_COMPLETED;
    }
}
