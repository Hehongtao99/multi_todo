package com.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String auth;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像URL
     */
    private String avatar;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}