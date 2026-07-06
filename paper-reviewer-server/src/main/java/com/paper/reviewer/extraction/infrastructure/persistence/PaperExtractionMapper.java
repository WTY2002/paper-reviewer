package com.paper.reviewer.extraction.infrastructure.persistence;

import com.paper.reviewer.common.persistence.BaseRepository;

import com.paper.reviewer.extraction.infrastructure.persistence.PaperExtractionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaperExtractionMapper extends BaseRepository<PaperExtractionEntity> {
}
