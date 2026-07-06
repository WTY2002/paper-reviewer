package com.paper.reviewer.paper.infrastructure.persistence;

import com.paper.reviewer.common.persistence.AuditEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("papers")
public class PaperEntity extends AuditEntity {
    private Long userId;
    private String title;
    private String originalFilename;
    private String filePath;
    private Long fileSize;
    private Integer pageCount;
    private String language;
    private String status;
}
