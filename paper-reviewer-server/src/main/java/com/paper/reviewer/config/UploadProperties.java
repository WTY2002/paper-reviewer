package com.paper.reviewer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.upload")
public record UploadProperties(long maxFileSizeMb, int maxPageCount, long maxUserStorageMb) {
}
