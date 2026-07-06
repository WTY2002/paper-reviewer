package com.paper.reviewer.review.infrastructure.persistence;

import com.paper.reviewer.common.persistence.BaseRepository;

import com.paper.reviewer.review.infrastructure.persistence.ReviewEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewMapper extends BaseRepository<ReviewEntity> {
    @Delete("DELETE FROM reviews WHERE id = #{id} AND user_id = #{userId}")
    int hardDeleteOwned(@Param("userId") long userId, @Param("id") long id);
}
