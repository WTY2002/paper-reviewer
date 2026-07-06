package com.paper.reviewer.user.infrastructure.persistence;

import com.paper.reviewer.common.persistence.BaseRepository;

import com.paper.reviewer.user.infrastructure.persistence.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseRepository<UserEntity> {
}
