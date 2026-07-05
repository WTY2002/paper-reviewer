package com.paper.reviewer.database.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class LogicalDeleteEntity extends AuditEntity {
    @TableLogic(value = "NULL", delval = "CURRENT_TIMESTAMP")
    private LocalDateTime deletedAt;
}
