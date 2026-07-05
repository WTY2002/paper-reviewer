package com.paper.reviewer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.storage")
public record StorageProperties(String paperRoot, String exportRoot) {
}
