package com.todo.service;

import com.todo.dto.NotificationCreateDto;
import com.todo.entity.Notification;
import com.todo.vo.NotificationVo;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {
    
    /**
     * 创建通知
     */
    Notification createNotification(NotificationCreateDto dto, Long senderId, String senderName);
    
    /**
     * 发送系统通知给所有用户
     */
    void sendSystemNotification(NotificationCreateDto dto, Long senderId, String senderName);
    
    /**
     * 发送项目通知
     */
    void sendProjectNotification(NotificationCreateDto dto, Long senderId, String senderName);
    
    /**
     * 发送个人通知
     */
    void sendPersonalNotification(NotificationCreateDto dto, Long senderId, String senderName);
    
    /**
     * 根据用户ID获取通知列表
     */
    List<NotificationVo> getNotificationsByUserId(Long userId);
    
    /**
     * 根据项目ID获取项目通知
     */
    List<NotificationVo> getNotificationsByProjectId(Long projectId);
    
    /**
     * 获取系统通知
     */
    List<NotificationVo> getSystemNotifications();
    
    /**
     * 标记通知为已读
     */
    boolean markAsRead(Long notificationId, Long userId);
    
    /**
     * 批量标记通知为已读
     */
    boolean markAllAsRead(Long userId);
    
    /**
     * 获取未读通知数量
     */
    int getUnreadCount(Long userId);
    
    /**
     * 删除通知
     */
    boolean deleteNotification(Long notificationId);
} 