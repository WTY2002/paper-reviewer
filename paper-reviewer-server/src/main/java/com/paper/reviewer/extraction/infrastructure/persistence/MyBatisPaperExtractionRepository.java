package com.paper.reviewer.extraction.infrastructure.persistence;

import com.paper.reviewer.extraction.repository.PaperExtractionRepository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paper.reviewer.extraction.infrastructure.persistence.PaperExtractionEntity;
import com.paper.reviewer.extraction.infrastructure.persistence.PaperExtractionMapper;
import com.paper.reviewer.extraction.domain.PaperExtraction;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class MyBatisPaperExtractionRepository implements PaperExtractionRepository {
    private final PaperExtractionMapper mapper;

    public MyBatisPaperExtractionRepository(PaperExtractionMapper mapper) { this.mapper = mapper; }

    @Override
    public PaperExtraction save(PaperExtraction extraction) {
        LocalDateTime now = LocalDateTime.now();
        PaperExtractionEntity entity = new PaperExtractionEntity();
        entity.setPaperId(extraction.paperId());
        entity.setExtractedText(extraction.extractedText());
        entity.setPageCount(extraction.pageCount());
        entity.setExtractionStatus(extraction.extractionStatus());
        entity.setErrorMessage(extraction.errorMessage());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<PaperExtraction> findByPaperId(long paperId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<PaperExtractionEntity>()
                .eq(PaperExtractionEntity::getPaperId, paperId))).map(this::toDomain);
    }

    private PaperExtraction toDomain(PaperExtractionEntity entity) {
        return new PaperExtraction(entity.getId(), entity.getPaperId(), entity.getExtractedText(),
                entity.getPageCount(), entity.getExtractionStatus(), entity.getErrorMessage(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
