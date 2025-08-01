package com.todo.dto;

import lombok.Data;

/**
 * 标记消息已读DTO
 */
@Data
public class ChatMarkReadDto {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户权限
     */
    private String userAuth;
    
    /**
     * 聊天对象ID
     */
    private Long chatUserId;
} 