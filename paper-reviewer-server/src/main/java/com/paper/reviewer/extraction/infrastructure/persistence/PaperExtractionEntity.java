package com.paper.reviewer.extraction.infrastructure.persistence;

import com.paper.reviewer.common.persistence.AuditEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("paper_extractions")
public class PaperExtractionEntity extends AuditEntity {
    private Long paperId;
    private String extractedText;
    private Integer pageCount;
    private String extractionStatus;
    private String errorMessage;
}
