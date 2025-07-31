package com.todo.dto;

import lombok.Data;

/**
 * 通知标记已读DTO
 */
@Data
public class NotificationMarkReadDto {
    
    /**
     * 通知ID（为空时标记所有通知为已读）
     */
    private Long notificationId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户权限
     */
    private String userAuth;
} 