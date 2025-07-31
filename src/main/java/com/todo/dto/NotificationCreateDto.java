package com.todo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建通知DTO
 */
@Data
public class NotificationCreateDto {
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 通知类型：system(系统通知)、project(项目通知)、personal(个人通知)
     */
    private String type;
    
    /**
     * 优先级：low(低)、normal(普通)、high(高)、urgent(紧急)
     */
    private String priority;
    
    /**
     * 接收者ID（为空表示全体用户）
     */
    private Long receiverId;
    
    /**
     * 项目ID（项目通知时使用）
     */
    private Long projectId;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 额外数据（JSON格式）
     */
    private String extraData;
    
    /**
     * 是否立即推送
     */
    private Boolean pushImmediately = true;
} 