package com.paper.reviewer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.ai")
public record AiModelProperties(String provider, Qwen qwen, OpenAi openai) {
    public record Qwen(String apiKey, String model, String baseUrl) { }
    public record OpenAi(boolean enabled, String apiKey, String model) { }
}
