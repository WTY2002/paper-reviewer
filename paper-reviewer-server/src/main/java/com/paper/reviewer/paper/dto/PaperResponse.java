package com.paper.reviewer.paper.dto;

import com.paper.reviewer.paper.domain.Paper;

import java.time.LocalDateTime;

public record PaperResponse(Long paperId, String title, String originalFilename, long fileSize,
                            int pageCount, String language, String status, LocalDateTime createdAt) {
    public static PaperResponse from(Paper paper) {
        return new PaperResponse(paper.id(), paper.title(), paper.originalFilename(), paper.fileSize(),
                paper.pageCount(), paper.language(), paper.status(), paper.createdAt());
    }
}
