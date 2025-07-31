package com.todo.dto;

import lombok.Data;

/**
 * 通知查询DTO
 */
@Data
public class NotificationQueryDto {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 项目ID
     */
    private Long projectId;
} 