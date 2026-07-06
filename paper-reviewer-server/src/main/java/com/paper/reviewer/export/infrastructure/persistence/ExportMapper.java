package com.paper.reviewer.export.infrastructure.persistence;

import com.paper.reviewer.common.persistence.BaseRepository;

import com.paper.reviewer.export.infrastructure.persistence.ExportEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExportMapper extends BaseRepository<ExportEntity> {
    @Delete("DELETE FROM exports WHERE user_id = #{userId} AND review_id = #{reviewId}")
    int hardDeleteByReview(@Param("userId") long userId, @Param("reviewId") long reviewId);
}
