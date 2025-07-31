package com.todo.service;

import com.todo.dto.NotificationSendDto;
import com.todo.dto.NotificationQueryDto;
import com.todo.dto.NotificationMarkReadDto;
import com.todo.dto.NotificationDeleteDto;
import com.todo.dto.NotificationCreateDto;
import com.todo.entity.Notification;
import com.todo.vo.NotificationVo;
import com.todo.vo.NotificationCountVo;
import com.todo.vo.OperationResultVo;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {
    
    /**
     * 发送通知
     */
    OperationResultVo sendNotification(NotificationSendDto sendDto);
    
    /**
     * 获取通知列表
     */
    List<NotificationVo> getNotifications(NotificationQueryDto queryDto);
    
    /**
     * 标记通知为已读
     */
    OperationResultVo markAsRead(NotificationMarkReadDto markReadDto);
    
    /**
     * 获取未读通知数量
     */
    NotificationCountVo getUnreadCount(NotificationQueryDto queryDto);
    
    /**
     * 删除通知
     */
    OperationResultVo deleteNotification(NotificationDeleteDto deleteDto);
    
    /**
     * 创建个人通知（内部使用）
     */
    Notification createPersonalNotification(NotificationCreateDto dto, Long senderId, String senderName);
    
    /**
     * 创建项目通知（内部使用）
     */
    Notification createProjectNotification(NotificationCreateDto dto, Long senderId, String senderName);
} 