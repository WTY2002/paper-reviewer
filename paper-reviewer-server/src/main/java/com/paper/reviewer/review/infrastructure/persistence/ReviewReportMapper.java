package com.paper.reviewer.review.infrastructure.persistence;

import com.paper.reviewer.common.persistence.BaseRepository;

import com.paper.reviewer.review.infrastructure.persistence.ReviewReportEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewReportMapper extends BaseRepository<ReviewReportEntity> {
}
