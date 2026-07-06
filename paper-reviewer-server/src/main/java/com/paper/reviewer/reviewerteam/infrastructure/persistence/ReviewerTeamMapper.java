package com.paper.reviewer.reviewerteam.infrastructure.persistence;

import com.paper.reviewer.common.persistence.BaseRepository;

import com.paper.reviewer.reviewerteam.infrastructure.persistence.ReviewerTeamEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewerTeamMapper extends BaseRepository<ReviewerTeamEntity> {
}
