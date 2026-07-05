package com.paper.reviewer.ai.orchestrator;

/** Narrow port: AI orchestration does not depend on SSE persistence or transport details. */
public interface AiStagePublisher {
    void started(long reviewId, String stage, String reviewerRole);
    void completed(long reviewId, String stage, String reviewerRole, String content);
    void failed(long reviewId, String stage, String message);

    AiStagePublisher NOOP = new AiStagePublisher() {
        public void started(long id, String stage, String role) { }
        public void completed(long id, String stage, String role, String content) { }
        public void failed(long id, String stage, String message) { }
    };
}
