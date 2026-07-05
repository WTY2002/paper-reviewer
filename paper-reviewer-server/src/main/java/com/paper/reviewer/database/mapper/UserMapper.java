package com.paper.reviewer.database.mapper;

import com.paper.reviewer.database.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseRepository<UserEntity> {
}
