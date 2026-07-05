package com.paper.reviewer.extraction.repository;

import com.paper.reviewer.extraction.domain.PaperExtraction;

import java.util.Optional;

public interface PaperExtractionRepository {
    PaperExtraction save(PaperExtraction extraction);
    Optional<PaperExtraction> findByPaperId(long paperId);
}
