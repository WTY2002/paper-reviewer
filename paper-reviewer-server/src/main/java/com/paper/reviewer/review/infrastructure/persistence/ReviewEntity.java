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
@TableName(value = "reviews", autoResultMap = true)
public class ReviewEntity extends AuditEntity {
    private Long userId;
    private Long paperId;
    private String reviewType;
    private String status;
    private String sourceLanguage;
    private String outputLanguage;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode fieldAnalysisJson;
    private String editorialDecisionMarkdown;
    private String revisionRoadmapMarkdown;
    private String authorQuestionsMarkdown;
    private String errorMessage;
}
