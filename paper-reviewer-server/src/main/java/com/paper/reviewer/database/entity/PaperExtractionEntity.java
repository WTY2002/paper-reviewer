package com.paper.reviewer.database.entity;

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
