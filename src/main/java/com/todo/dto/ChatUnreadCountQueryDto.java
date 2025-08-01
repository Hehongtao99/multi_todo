package com.todo.dto;

import lombok.Data;

/**
 * 获取未读消息数量DTO
 */
@Data
public class ChatUnreadCountQueryDto {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户权限
     */
    private String userAuth;
} 