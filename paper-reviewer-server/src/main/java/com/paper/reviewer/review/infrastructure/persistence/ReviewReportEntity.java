package com.paper.reviewer.review.infrastructure.persistence;

import com.paper.reviewer.common.persistence.AuditEntity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.paper.reviewer.common.persistence.JsonNodeTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tools.jackson.databind.JsonNode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "review_reports", autoResultMap = true)
public class ReviewReportEntity extends AuditEntity {
    private Long reviewId;
    private String reviewerRole;
    private String contentMarkdown;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode scoresJson;
    private String status;
}
