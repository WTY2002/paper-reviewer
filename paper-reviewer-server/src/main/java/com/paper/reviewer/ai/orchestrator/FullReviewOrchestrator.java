package com.paper.reviewer.ai.orchestrator;

import com.paper.reviewer.ai.parser.AiOutputParser;
import com.paper.reviewer.ai.parser.DimensionScores;
import com.paper.reviewer.ai.parser.ReviewerTeam;
import com.paper.reviewer.ai.prompt.ReviewPrompts;
import com.paper.reviewer.ai.provider.ChatModelProvider;
import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Service
public class FullReviewOrchestrator extends AbstractAiOrchestrator {
    public static final List<String> ROLES = List.of("EIC", "METHODOLOGY", "DOMAIN", "PERSPECTIVE", "DEVILS_ADVOCATE");
    private final AiOutputParser parser;
    private final Executor reviewExecutor;
    public FullReviewOrchestrator(ChatModelProvider provider, AiStagePublisher publisher, AiOutputParser parser,
                                  @Qualifier("reviewExecutor") Executor reviewExecutor) {
        super(provider, publisher); this.parser = parser; this.reviewExecutor = reviewExecutor;
    }
    /** Reviewers remain independent but run concurrently; list order stays deterministic. */
    public Result review(long reviewId, String paperText, ReviewerTeam team, String language) {
        List<CompletableFuture<Report>> reviewerTasks = ROLES.stream()
                .map(role -> CompletableFuture.supplyAsync(() -> reviewOne(reviewId, role, paperText, team, language), reviewExecutor))
                .toList();
        List<Report> reports = reviewerTasks.stream().map(this::join).toList();
        String joined = join(reports);
        CompletableFuture<String> decisionTask = CompletableFuture.supplyAsync(() -> call(reviewId, "EDITORIAL_DECISION", null,
                ReviewPrompts.SYSTEM, ReviewPrompts.editorialDecision(joined, language)), reviewExecutor);
        CompletableFuture<String> questionsTask = CompletableFuture.supplyAsync(() -> call(reviewId, "AUTHOR_QUESTIONS", null,
                ReviewPrompts.SYSTEM, ReviewPrompts.questionsForAuthors(joined, language)), reviewExecutor);
        String decision = join(decisionTask);
        String roadmap = call(reviewId, "REVISION_ROADMAP", null, ReviewPrompts.SYSTEM, ReviewPrompts.revisionRoadmap(joined, decision, language));
        String questions = join(questionsTask);
        return new Result(reports, decision, roadmap, questions);
    }

    private Report reviewOne(long reviewId, String role, String paperText, ReviewerTeam team, String language) {
        ReviewerTeam.Reviewer reviewer = team.reviewers().stream().filter(r -> role.equals(r.role())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing reviewer role " + role));
        String markdown = call(reviewId, "REVIEWER_REPORT", role, ReviewPrompts.SYSTEM,
                ReviewPrompts.reviewer(role, reviewer.identityDescription() + "; " + reviewer.expertise() + "; " + reviewer.reviewFocus(), paperText, language));
        try { return new Report(role, parser.preserveMarkdown(markdown), parser.parseScores(markdown)); }
        catch (RuntimeException exception) { throw new BusinessException(ErrorCode.AI_PROVIDER_ERROR, "Reviewer score output was invalid", exception); }
    }

    private <T> T join(CompletableFuture<T> task) {
        try { return task.join(); }
        catch (CompletionException exception) {
            if (exception.getCause() instanceof RuntimeException runtime) throw runtime;
            throw exception;
        }
    }
    private String join(List<Report> reports) {
        StringBuilder value = new StringBuilder();
        reports.forEach(report -> value.append("\n<REPORT role=\"").append(report.role()).append("\">\n")
                .append(report.markdown()).append("\n</REPORT>\n"));
        return value.toString();
    }
    public record Report(String role, String markdown, DimensionScores scores) { }
    public record Result(List<Report> reports, String editorialDecision, String revisionRoadmap, String questionsForAuthors) {
        public Result { reports = List.copyOf(reports); }
    }
}
