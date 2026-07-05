package com.paper.reviewer.ai;

import com.paper.reviewer.ai.orchestrator.*;
import com.paper.reviewer.ai.parser.AiOutputParser;
import com.paper.reviewer.ai.parser.ReviewerTeam;
import com.paper.reviewer.ai.provider.ChatModelProvider;
import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiOrchestratorsTest {
    private final AiOutputParser parser = new AiOutputParser(new ObjectMapper());

    @Test void fullReviewRunsIndependentReviewersAndKeepsStableReportOrder() {
        ConcurrentProvider provider = new ConcurrentProvider();
        var executor = Executors.newFixedThreadPool(5);
        FullReviewOrchestrator.Result result;
        try {
            result = new FullReviewOrchestrator(provider, AiStagePublisher.NOOP, parser, executor)
                    .review(1, "paper", team(), "en");
        } finally {
            executor.shutdownNow();
        }
        assertThat(result.reports()).extracting(FullReviewOrchestrator.Report::role)
                .containsExactlyElementsOf(FullReviewOrchestrator.ROLES);
        assertThat(provider.prompts).hasSize(8);
        assertThat(provider.maxActive.get()).isGreaterThan(1);
        for (String role : FullReviewOrchestrator.ROLES)
            assertThat(provider.prompts).anySatisfy(prompt -> assertThat(prompt).contains("Act only as " + role));
    }

    @Test void quickUsesOneEicCallAndProviderFailureMapsToStableError() {
        QueueProvider provider = new QueueProvider().add("# quick");
        assertThat(new QuickReviewOrchestrator(provider, AiStagePublisher.NOOP).review(2, "paper", "zh")).isEqualTo("# quick");
        assertThat(provider.prompts).singleElement().asString().contains("only as the EIC");
        ChatModelProvider broken = (system, user) -> { throw new IllegalStateException("secret provider detail"); };
        assertThatThrownBy(() -> new QuickReviewOrchestrator(broken, AiStagePublisher.NOOP).review(2, "paper", "en"))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.AI_PROVIDER_ERROR));
    }

    @Test void fieldAndRereviewProduceStructuredResults() {
        QueueProvider fieldProvider = new QueueProvider()
                .add("{\"title\":\"T\",\"language\":\"en\",\"primaryDiscipline\":\"CS\",\"secondaryDiscipline\":\"Stats\",\"researchParadigm\":\"empirical\",\"methodologyType\":\"experiment\",\"targetJournalTier\":\"Q1\",\"paperMaturity\":\"mature\"}")
                .add(teamJson());
        assertThat(new FieldAnalysisOrchestrator(fieldProvider, AiStagePublisher.NOOP, parser).analyze(3, "paper").team().reviewers()).hasSize(5);
        QueueProvider reProvider = new QueueProvider().add("{\"decision\":\"Accept\",\"items\":[],\"newIssues\":[],\"residualIssues\":[],\"resultMarkdown\":\"# ok\"}");
        assertThat(new ReReviewOrchestrator(reProvider, AiStagePublisher.NOOP, parser)
                .review(4, "old", "new", "response", "en").decision()).isEqualTo("Accept");
    }

    @Test void quickFieldAnalysisUsesOnlyOneModelCall() {
        QueueProvider provider = new QueueProvider()
                .add("{\"title\":\"T\",\"language\":\"en\",\"primaryDiscipline\":\"CS\",\"secondaryDiscipline\":\"Stats\",\"researchParadigm\":\"empirical\",\"methodologyType\":\"experiment\",\"targetJournalTier\":\"Q1\",\"paperMaturity\":\"mature\"}");

        assertThat(new FieldAnalysisOrchestrator(provider, AiStagePublisher.NOOP, parser)
                .analyzeField(3, "paper").primaryDiscipline()).isEqualTo("CS");
        assertThat(provider.prompts).hasSize(1);
    }

    private static ReviewerTeam team() {
        return new ReviewerTeam(FullReviewOrchestrator.ROLES.stream()
                .map(role -> new ReviewerTeam.Reviewer(role, "id", "expert", "focus")).toList());
    }
    private static String teamJson() {
        return "{\"reviewers\":[" + FullReviewOrchestrator.ROLES.stream()
                .map(role -> "{\"role\":\"" + role + "\",\"identityDescription\":\"id\",\"expertise\":\"expert\",\"reviewFocus\":\"focus\"}")
                .reduce((a,b) -> a + "," + b).orElseThrow() + "]}";
    }
    private static class QueueProvider implements ChatModelProvider {
        final ArrayDeque<String> outputs = new ArrayDeque<>(); final List<String> prompts = new ArrayList<>();
        QueueProvider add(String output) { outputs.add(output); return this; }
        public String complete(String system, String user) { prompts.add(user); return outputs.remove(); }
    }
    private static class ConcurrentProvider implements ChatModelProvider {
        final List<String> prompts = java.util.Collections.synchronizedList(new ArrayList<>());
        final java.util.concurrent.atomic.AtomicInteger active = new java.util.concurrent.atomic.AtomicInteger();
        final java.util.concurrent.atomic.AtomicInteger maxActive = new java.util.concurrent.atomic.AtomicInteger();
        public String complete(String system, String user) {
            int current = active.incrementAndGet(); maxActive.accumulateAndGet(current, Math::max);
            try {
                prompts.add(user);
                if (user.contains("Act only as ")) {
                    try { Thread.sleep(25); } catch (InterruptedException exception) { Thread.currentThread().interrupt(); }
                    return "# report\n```json\n{\"scores\":{\"originality\":80,\"weightedAverage\":75}}\n```";
                }
                if (user.contains("Editorial Decision")) return "# decision";
                if (user.contains("Revision Roadmap")) return "# roadmap";
                if (user.contains("Questions for Authors")) return "# questions";
                throw new IllegalArgumentException("Unexpected prompt");
            } finally { active.decrementAndGet(); }
        }
    }
}
