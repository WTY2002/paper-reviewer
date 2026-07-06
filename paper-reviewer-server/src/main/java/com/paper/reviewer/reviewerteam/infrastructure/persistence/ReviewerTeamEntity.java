package com.paper.reviewer.reviewerteam.infrastructure.persistence;

import com.paper.reviewer.common.persistence.AuditEntity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.paper.reviewer.common.persistence.JsonNodeTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "reviewer_teams", autoResultMap = true)
public class ReviewerTeamEntity extends AuditEntity {
    private Long reviewId;
    private String targetVenue;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode teamJson;
    private LocalDateTime confirmedAt;
}
