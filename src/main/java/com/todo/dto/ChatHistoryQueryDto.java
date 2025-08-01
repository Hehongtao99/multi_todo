package com.todo.dto;

import lombok.Data;

/**
 * 聊天记录查询DTO
 */
@Data
public class ChatHistoryQueryDto {
    
    /**
     * 请求者ID
     */
    private Long requesterId;
    
    /**
     * 请求者权限
     */
    private String requesterAuth;
    
    /**
     * 聊天对象ID
     */
    private Long chatUserId;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    private Integer size = 20;
} 