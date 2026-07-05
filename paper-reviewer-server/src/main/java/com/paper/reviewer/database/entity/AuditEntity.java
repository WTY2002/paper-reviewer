package com.paper.reviewer.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class AuditEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
