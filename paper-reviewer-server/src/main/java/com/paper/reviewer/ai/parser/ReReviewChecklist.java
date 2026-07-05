package com.paper.reviewer.ai.parser;

import java.util.List;

public record ReReviewChecklist(String decision, List<Item> items, List<String> newIssues,
                                List<String> residualIssues, String resultMarkdown) {
    public ReReviewChecklist { items = List.copyOf(items); newIssues = List.copyOf(newIssues); residualIssues = List.copyOf(residualIssues); }
    public record Item(String id, String originalComment, String authorClaim, String status,
                       String revisionLocation, boolean verified, String qualityAssessment) { }
}
