package com.paper.reviewer.ai.parser;

import java.util.Map;

public record DimensionScores(Map<String, Integer> scores) {
    public DimensionScores {
        scores = Map.copyOf(scores);
        if (scores.values().stream().anyMatch(score -> score == null || score < 0 || score > 100))
            throw new IllegalArgumentException("Dimension scores must be between 0 and 100");
    }
}
