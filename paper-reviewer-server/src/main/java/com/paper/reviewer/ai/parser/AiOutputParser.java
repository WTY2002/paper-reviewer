package com.paper.reviewer.ai.parser;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiOutputParser {
    private static final Pattern JSON_FENCE = Pattern.compile("```(?:json)?\\s*(\\{.*?})\\s*```", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private final ObjectMapper objectMapper;

    public AiOutputParser(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    public String preserveMarkdown(String output) { return output; }
    public FieldAnalysis parseFieldAnalysis(String output) { return read(output, FieldAnalysis.class); }
    public ReviewerTeam parseReviewerTeam(String output) { return read(output, ReviewerTeam.class); }
    public ReReviewChecklist parseChecklist(String output) { return read(output, ReReviewChecklist.class); }

    public DimensionScores parseScores(String markdown) {
        Matcher matcher = JSON_FENCE.matcher(markdown);
        while (matcher.find()) {
            DimensionScores scores = tryParseScores(matcher.group(1));
            if (scores != null) return scores;
        }

        // Some OpenAI-compatible models occasionally omit the Markdown fence even when asked.
        // Accept a trailing/raw JSON object as long as it contains a valid score map.
        try {
            DimensionScores scores = tryParseScores(extractJson(markdown));
            if (scores != null) return scores;
        } catch (RuntimeException ignored) { /* report the stable parser error below */ }
        throw new IllegalArgumentException("No valid dimension scores JSON found");
    }

    private DimensionScores tryParseScores(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode scores = node.has("scores") ? node.get("scores") : node;
            if (scores == null || !scores.isObject()) return null;
            Map<String, Integer> values = objectMapper.convertValue(scores, new TypeReference<>() { });
            return values.isEmpty() ? null : new DimensionScores(values);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private <T> T read(String output, Class<T> type) {
        try { return objectMapper.readValue(extractJson(output), type); }
        catch (RuntimeException exception) { throw new IllegalArgumentException("Invalid AI structured output", exception); }
    }

    static String extractJson(String output) {
        String trimmed = output.trim();
        if (trimmed.startsWith("{")) return trimmed;
        Matcher matcher = JSON_FENCE.matcher(output);
        if (matcher.find()) return matcher.group(1);
        int start = output.indexOf('{'), end = output.lastIndexOf('}');
        if (start >= 0 && end > start) return output.substring(start, end + 1);
        throw new IllegalArgumentException("AI output does not contain JSON");
    }
}
