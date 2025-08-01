package com.todo.dto;

import lombok.Data;

/**
 * 个人信息更新DTO
 */
@Data
public class ProfileUpdateDto {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 头像URL
     */
    private String avatar;
} 