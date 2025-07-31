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
     * 用户权限
     */
    private String userAuth;
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 通知类型 (system, project, personal)
     */
    private String type;
    
    /**
     * 通知ID（用于查询特定通知）
     */
    private Long notificationId;
    
    /**
     * 是否只查询未读通知
     */
    private Boolean unreadOnly;
} 