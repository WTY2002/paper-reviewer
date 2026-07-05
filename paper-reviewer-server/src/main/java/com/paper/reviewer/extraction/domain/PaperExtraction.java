package com.paper.reviewer.extraction.domain;

import java.time.LocalDateTime;

public record PaperExtraction(Long id, Long paperId, String extractedText, int pageCount,
                              String extractionStatus, String errorMessage,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
}
