package com.paper.reviewer.database.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exports")
public class ExportEntity extends LogicalDeleteEntity {
    private Long userId;
    private Long reviewId;
    private Long rereviewId;
    private String exportType;
    private String filePath;
    private String status;
}
