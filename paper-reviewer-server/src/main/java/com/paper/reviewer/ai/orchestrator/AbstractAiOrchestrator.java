package com.paper.reviewer.ai.orchestrator;

import com.paper.reviewer.ai.provider.ChatModelProvider;
import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;

abstract class AbstractAiOrchestrator {
    protected final ChatModelProvider provider;
    protected final AiStagePublisher publisher;
    AbstractAiOrchestrator(ChatModelProvider provider, AiStagePublisher publisher) {
        this.provider = provider; this.publisher = publisher;
    }
    protected String call(long reviewId, String stage, String role, String system, String prompt) {
        publisher.started(reviewId, stage, role);
        try {
            String output = provider.complete(system, prompt);
            publisher.completed(reviewId, stage, role, output);
            return output;
        } catch (Exception exception) {
            publisher.failed(reviewId, stage, "AI provider request failed");
            if (exception instanceof BusinessException business && business.getErrorCode() == ErrorCode.AI_PROVIDER_ERROR) throw business;
            throw new BusinessException(ErrorCode.AI_PROVIDER_ERROR, "AI provider request failed", exception);
        }
    }
}
