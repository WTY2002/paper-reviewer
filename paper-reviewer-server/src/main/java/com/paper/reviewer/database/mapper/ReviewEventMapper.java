package com.paper.reviewer.database.mapper;

import com.paper.reviewer.database.entity.ReviewEventEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReviewEventMapper extends BaseRepository<ReviewEventEntity> {
    @Select("SELECT id FROM reviews WHERE id = #{reviewId} AND deleted_at IS NULL FOR UPDATE")
    Long lockReview(@Param("reviewId") long reviewId);
}
