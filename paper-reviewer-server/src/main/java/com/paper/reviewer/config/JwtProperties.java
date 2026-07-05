package com.paper.reviewer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMinutes) {
    public JwtProperties {
        if (secret == null || secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("app.jwt.secret must contain at least 32 bytes");
        }
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("app.jwt.expiration-minutes must be positive");
        }
    }
}
