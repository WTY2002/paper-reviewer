package com.paper.reviewer.user.infrastructure.persistence;

import com.paper.reviewer.common.persistence.AuditEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class UserEntity extends AuditEntity {
    private String email;
    private String passwordHash;
    private String displayName;
    private String defaultOutputLanguage;
}
