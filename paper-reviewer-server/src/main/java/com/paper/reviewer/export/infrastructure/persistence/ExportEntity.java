package com.paper.reviewer.export.infrastructure.persistence;

import com.paper.reviewer.common.persistence.AuditEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exports")
public class ExportEntity extends AuditEntity {
    private Long userId;
    private Long reviewId;
    private Long rereviewId;
    private String exportType;
    private String filePath;
    private String status;
}
