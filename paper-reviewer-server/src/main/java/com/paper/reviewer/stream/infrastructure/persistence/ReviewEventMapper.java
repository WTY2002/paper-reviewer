package com.paper.reviewer.stream.infrastructure.persistence;

import com.paper.reviewer.common.persistence.BaseRepository;

import com.paper.reviewer.stream.infrastructure.persistence.ReviewEventEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReviewEventMapper extends BaseRepository<ReviewEventEntity> {
    @Select("SELECT id FROM reviews WHERE id = #{reviewId} FOR UPDATE")
    Long lockReview(@Param("reviewId") long reviewId);
}
