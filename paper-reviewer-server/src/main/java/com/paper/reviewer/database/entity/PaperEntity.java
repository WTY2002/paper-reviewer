package com.paper.reviewer.database.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("papers")
public class PaperEntity extends LogicalDeleteEntity {
    private Long userId;
    private String title;
    private String originalFilename;
    private String filePath;
    private Long fileSize;
    private Integer pageCount;
    private String language;
    private String status;
}
