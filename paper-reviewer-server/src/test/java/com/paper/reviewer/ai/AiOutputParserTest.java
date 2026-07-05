package com.paper.reviewer.ai;

import com.paper.reviewer.ai.parser.AiOutputParser;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiOutputParserTest {
    private final AiOutputParser parser = new AiOutputParser(new ObjectMapper());

    @Test void parsesFieldTeamScoresAndChecklist() {
        var field = parser.parseFieldAnalysis("```json\n{\"title\":\"T\",\"language\":\"en\",\"primaryDiscipline\":\"CS\",\"secondaryDiscipline\":\"Stats\",\"researchParadigm\":\"empirical\",\"methodologyType\":\"experiment\",\"targetJournalTier\":\"Q1\",\"paperMaturity\":\"mature\"}\n```");
        assertThat(field.primaryDiscipline()).isEqualTo("CS");
        String teamJson = "{\"reviewers\":[" + reviewer("EIC") + "," + reviewer("METHODOLOGY") + "," +
                reviewer("DOMAIN") + "," + reviewer("PERSPECTIVE") + "," + reviewer("DEVILS_ADVOCATE") + "]}";
        assertThat(parser.parseReviewerTeam(teamJson).reviewers()).hasSize(5);
        assertThat(parser.parseScores("# report\n```json\n{\"scores\":{\"originality\":81,\"weightedAverage\":78}}\n```")
                .scores()).containsEntry("originality", 81);
        var checklist = parser.parseChecklist("{\"decision\":\"Minor Revision\",\"items\":[],\"newIssues\":[],\"residualIssues\":[],\"resultMarkdown\":\"# Verification\"}");
        assertThat(checklist.resultMarkdown()).isEqualTo("# Verification");
    }

    @Test void preservesMarkdownExactlyAndRejectsOutOfRangeScores() {
        String markdown = "# Report\n\nOriginal bytes stay here.";
        assertThat(parser.preserveMarkdown(markdown)).isSameAs(markdown);
        assertThatThrownBy(() -> parser.parseScores("```json\n{\"scores\":{\"x\":101}}\n```"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test void acceptsUnfencedScoreJsonFromCompatibleModels() {
        String output = "# Report\n\nModel output.\n{\"scores\":{\"originality\":81,\"weightedAverage\":78}}";

        assertThat(parser.parseScores(output).scores())
                .containsEntry("originality", 81)
                .containsEntry("weightedAverage", 78);
    }

    private String reviewer(String role) {
        return "{\"role\":\"" + role + "\",\"identityDescription\":\"id\",\"expertise\":\"expert\",\"reviewFocus\":\"focus\"}";
    }
}
