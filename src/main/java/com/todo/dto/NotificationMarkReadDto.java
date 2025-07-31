package com.todo.dto;

import lombok.Data;

/**
 * 通知标记已读DTO
 */
@Data
public class NotificationMarkReadDto {
    
    /**
     * 通知ID
     */
    private Long notificationId;
    
    /**
     * 用户ID
     */
    private Long userId;
} 