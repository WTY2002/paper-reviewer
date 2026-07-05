package com.paper.reviewer.database.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class UserEntity extends LogicalDeleteEntity {
    private String email;
    private String passwordHash;
    private String displayName;
    private String defaultOutputLanguage;
}
