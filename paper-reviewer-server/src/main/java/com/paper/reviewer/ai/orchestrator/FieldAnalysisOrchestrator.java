package com.paper.reviewer.ai.orchestrator;

import com.paper.reviewer.ai.parser.AiOutputParser;
import com.paper.reviewer.ai.parser.FieldAnalysis;
import com.paper.reviewer.ai.parser.ReviewerTeam;
import com.paper.reviewer.ai.prompt.ReviewPrompts;
import com.paper.reviewer.ai.provider.ChatModelProvider;
import org.springframework.stereotype.Service;

@Service
public class FieldAnalysisOrchestrator extends AbstractAiOrchestrator {
    private final AiOutputParser parser;
    public FieldAnalysisOrchestrator(ChatModelProvider provider, AiStagePublisher publisher, AiOutputParser parser) {
        super(provider, publisher); this.parser = parser;
    }
    public FieldAnalysis analyzeField(long reviewId, String paperText) {
        return analyzeFieldWithRaw(reviewId, paperText).field();
    }
    private ParsedField analyzeFieldWithRaw(long reviewId, String paperText) {
        String fieldRaw = call(reviewId, "FIELD_ANALYSIS", null, ReviewPrompts.SYSTEM, ReviewPrompts.fieldAnalysis(paperText));
        return new ParsedField(parse(reviewId, "FIELD_ANALYSIS", () -> parser.parseFieldAnalysis(fieldRaw)), fieldRaw);
    }
    public Result analyze(long reviewId, String paperText) {
        ParsedField parsedField = analyzeFieldWithRaw(reviewId, paperText);
        FieldAnalysis field = parsedField.field();
        String teamRaw = call(reviewId, "REVIEWER_TEAM", null, ReviewPrompts.SYSTEM,
                ReviewPrompts.reviewerTeam(paperText, parsedField.raw()));
        ReviewerTeam team = parse(reviewId, "REVIEWER_TEAM", () -> parser.parseReviewerTeam(teamRaw));
        return new Result(field, team);
    }
    private record ParsedField(FieldAnalysis field, String raw) { }
    private <T> T parse(long id, String stage, java.util.concurrent.Callable<T> parserCall) {
        try { return parserCall.call(); }
        catch (Exception exception) {
            publisher.failed(id, stage, "AI structured output was invalid");
            throw new com.paper.reviewer.common.BusinessException(com.paper.reviewer.common.ErrorCode.AI_PROVIDER_ERROR,
                    "AI structured output was invalid", exception);
        }
    }
    public record Result(FieldAnalysis field, ReviewerTeam team) { }
}
