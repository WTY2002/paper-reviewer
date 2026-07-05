package com.paper.reviewer.paper.domain;

import java.time.LocalDateTime;

public record Paper(Long id, Long userId, String title, String originalFilename, String filePath,
                    long fileSize, int pageCount, String language, String status,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
}
