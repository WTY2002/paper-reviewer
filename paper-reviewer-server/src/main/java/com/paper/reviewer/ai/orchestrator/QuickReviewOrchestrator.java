package com.paper.reviewer.ai.orchestrator;

import com.paper.reviewer.ai.prompt.ReviewPrompts;
import com.paper.reviewer.ai.provider.ChatModelProvider;
import org.springframework.stereotype.Service;

@Service
public class QuickReviewOrchestrator extends AbstractAiOrchestrator {
    public QuickReviewOrchestrator(ChatModelProvider provider, AiStagePublisher publisher) { super(provider, publisher); }
    public String review(long reviewId, String paperText, String language) {
        return call(reviewId, "QUICK_REVIEW", "EIC", ReviewPrompts.SYSTEM, ReviewPrompts.quickReview(paperText, language));
    }
}
