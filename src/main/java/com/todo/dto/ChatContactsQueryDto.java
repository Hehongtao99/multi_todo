package com.todo.dto;

import lombok.Data;

/**
 * 聊天联系人查询DTO
 */
@Data
public class ChatContactsQueryDto {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户权限
     */
    private String userAuth;
} 