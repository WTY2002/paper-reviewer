package com.paper.reviewer.ai.provider;

/** Deployment-scoped model gateway. API credentials are deliberately absent. */
public interface ChatModelProvider {
    String complete(String systemPrompt, String userPrompt);
}
