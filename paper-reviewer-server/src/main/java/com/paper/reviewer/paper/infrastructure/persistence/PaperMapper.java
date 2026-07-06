package com.paper.reviewer.paper.infrastructure.persistence;

import com.paper.reviewer.common.persistence.BaseRepository;

import com.paper.reviewer.paper.infrastructure.persistence.PaperEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaperMapper extends BaseRepository<PaperEntity> {
    @Select("SELECT COALESCE(SUM(file_size), 0) FROM papers WHERE user_id = #{userId}")
    long sumActiveFileSize(@Param("userId") long userId);
    @Delete("DELETE FROM papers WHERE id = #{id} AND user_id = #{userId}")
    int hardDeleteOwned(@Param("userId") long userId, @Param("id") long id);
}
