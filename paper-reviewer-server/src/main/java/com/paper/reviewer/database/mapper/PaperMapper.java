package com.paper.reviewer.database.mapper;

import com.paper.reviewer.database.entity.PaperEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaperMapper extends BaseRepository<PaperEntity> {
    @Select("SELECT COALESCE(SUM(file_size), 0) FROM papers WHERE user_id = #{userId} AND deleted_at IS NULL")
    long sumActiveFileSize(@Param("userId") long userId);
    @Delete("DELETE FROM papers WHERE id = #{id} AND user_id = #{userId}")
    int hardDeleteOwned(@Param("userId") long userId, @Param("id") long id);
}
