package com.paper.reviewer.database.mapper;

import com.paper.reviewer.database.entity.ExportEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExportMapper extends BaseRepository<ExportEntity> {
    @Delete("DELETE FROM exports WHERE user_id = #{userId} AND review_id = #{reviewId}")
    int hardDeleteByReview(@Param("userId") long userId, @Param("reviewId") long reviewId);
    @Delete("DELETE FROM exports WHERE user_id = #{userId} AND id = #{id}")
    int hardDeleteOwned(@Param("userId") long userId, @Param("id") long id);
}
