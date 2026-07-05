package com.paper.reviewer.ai.orchestrator;

import com.paper.reviewer.ai.parser.AiOutputParser;
import com.paper.reviewer.ai.parser.ReReviewChecklist;
import com.paper.reviewer.ai.prompt.ReviewPrompts;
import com.paper.reviewer.ai.provider.ChatModelProvider;
import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class ReReviewOrchestrator extends AbstractAiOrchestrator {
    private final AiOutputParser parser;
    public ReReviewOrchestrator(ChatModelProvider provider, AiStagePublisher publisher, AiOutputParser parser) {
        super(provider, publisher); this.parser = parser;
    }
    public ReReviewChecklist review(long reviewId, String originalRoadmap, String revisedPaper, String authorResponse, String language) {
        String output = call(reviewId, "REREVIEW", "EIC", ReviewPrompts.SYSTEM,
                ReviewPrompts.reReview(originalRoadmap, revisedPaper, authorResponse, language));
        try { return parser.parseChecklist(output); }
        catch (RuntimeException exception) { throw new BusinessException(ErrorCode.AI_PROVIDER_ERROR, "Re-review checklist output was invalid", exception); }
    }
}
