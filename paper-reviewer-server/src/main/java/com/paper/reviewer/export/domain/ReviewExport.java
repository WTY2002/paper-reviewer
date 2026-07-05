package com.paper.reviewer.export.domain;

import java.time.LocalDateTime;

public record ReviewExport(Long id, long userId, Long reviewId, Long rereviewId,
                           ExportType exportType, String filePath, ExportStatus status,
                           LocalDateTime createdAt, LocalDateTime updatedAt) { }
