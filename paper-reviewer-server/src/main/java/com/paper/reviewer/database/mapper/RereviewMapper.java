package com.paper.reviewer.database.mapper;

import com.paper.reviewer.database.entity.RereviewEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RereviewMapper extends BaseRepository<RereviewEntity> {
    @Delete("DELETE FROM rereviews WHERE id = #{id} AND user_id = #{userId}")
    int hardDeleteOwned(@Param("userId") long userId, @Param("id") long id);
}
