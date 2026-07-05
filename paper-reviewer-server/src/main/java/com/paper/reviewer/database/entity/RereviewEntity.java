package com.paper.reviewer.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.paper.reviewer.database.typehandler.JsonNodeTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tools.jackson.databind.JsonNode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "rereviews", autoResultMap = true)
public class RereviewEntity extends LogicalDeleteEntity {
    private Long userId;
    private Long originalReviewId;
    private Long revisedPaperId;
    private Long responsePaperId;
    private String outputLanguage;
    private String status;
    private String resultMarkdown;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode checklistJson;
    private String errorMessage;
}
