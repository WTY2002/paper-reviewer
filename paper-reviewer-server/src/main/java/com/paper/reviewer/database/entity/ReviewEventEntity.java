package com.paper.reviewer.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.paper.reviewer.database.typehandler.JsonNodeTypeHandler;
import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

@Data
@TableName(value = "review_events", autoResultMap = true)
public class ReviewEventEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reviewId;
    private String eventType;
    private String stage;
    private String reviewerRole;
    private Long sequenceNo;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode eventPayload;
    private LocalDateTime createdAt;
}
