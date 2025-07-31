package com.todo.service.impl;

import com.todo.dto.NotificationCreateDto;
import com.todo.dto.WebSocketMessageDto;
import com.todo.entity.Notification;
import com.todo.mapper.NotificationMapper;
import com.todo.service.NotificationService;
import com.todo.service.WebSocketService;
import com.todo.vo.NotificationVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Override
    public Notification createNotification(NotificationCreateDto dto, Long senderId, String senderName) {
        Notification notification = new Notification();
        BeanUtils.copyProperties(dto, notification);
        
        notification.setSenderId(senderId);
        notification.setSenderName(senderName);
        notification.setIsRead(false);
        notification.setIsPushed(false);
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        
        // 设置默认优先级
        if (notification.getPriority() == null) {
            notification.setPriority("normal");
        }
        
        notificationMapper.insert(notification);
        
        // 如果需要立即推送
        if (dto.getPushImmediately() != null && dto.getPushImmediately()) {
            pushNotificationViaWebSocket(notification);
        }
        
        log.info("创建通知成功，ID: {}, 标题: {}", notification.getId(), notification.getTitle());
        return notification;
    }
    
    @Override
    public void sendSystemNotification(NotificationCreateDto dto, Long senderId, String senderName) {
        dto.setType("system");
        dto.setReceiverId(null); // 系统通知发送给所有用户
        createNotification(dto, senderId, senderName);
    }
    
    @Override
    public void sendProjectNotification(NotificationCreateDto dto, Long senderId, String senderName) {
        dto.setType("project");
        createNotification(dto, senderId, senderName);
    }
    
    @Override
    public void sendPersonalNotification(NotificationCreateDto dto, Long senderId, String senderName) {
        dto.setType("personal");
        createNotification(dto, senderId, senderName);
    }
    
    @Override
    public List<NotificationVo> getNotificationsByUserId(Long userId) {
        return notificationMapper.getNotificationsByUserId(userId);
    }
    
    @Override
    public List<NotificationVo> getNotificationsByProjectId(Long projectId) {
        return notificationMapper.getNotificationsByProjectId(projectId);
    }
    
    @Override
    public List<NotificationVo> getSystemNotifications() {
        return notificationMapper.getSystemNotifications();
    }
    
    @Override
    public boolean markAsRead(Long notificationId, Long userId) {
        int result = notificationMapper.markAsRead(notificationId, userId);
        return result > 0;
    }
    
    @Override
    public boolean markAllAsRead(Long userId) {
        int result = notificationMapper.markAllAsRead(userId);
        return result > 0;
    }
    
    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.getUnreadCount(userId);
    }
    
    @Override
    public boolean deleteNotification(Long notificationId) {
        int result = notificationMapper.deleteById(notificationId);
        return result > 0;
    }
    
    /**
     * 通过WebSocket推送通知
     */
    private void pushNotificationViaWebSocket(Notification notification) {
        WebSocketMessageDto message = new WebSocketMessageDto();
        message.setType("NOTIFICATION");
        message.setContent(notification);
        message.setSenderId(notification.getSenderId());
        message.setSenderName(notification.getSenderName());
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(System.currentTimeMillis());
        
        try {
            if ("system".equals(notification.getType())) {
                // 系统通知广播给所有用户
                webSocketService.broadcast(message);
                log.info("系统通知已广播: {}", notification.getTitle());
                
            } else if ("project".equals(notification.getType()) && notification.getProjectId() != null) {
                // 项目通知发送给项目成员
                message.setProjectId(notification.getProjectId());
                webSocketService.sendToProject(notification.getProjectId(), message);
                log.info("项目通知已发送到项目 {}: {}", notification.getProjectId(), notification.getTitle());
                
            } else if ("personal".equals(notification.getType()) && notification.getReceiverId() != null) {
                // 个人通知发送给特定用户
                message.setReceiverId(notification.getReceiverId());
                webSocketService.sendToUser(notification.getReceiverId(), message);
                log.info("个人通知已发送给用户 {}: {}", notification.getReceiverId(), notification.getTitle());
            }
            
            // 标记为已推送
            notification.setIsPushed(true);
            notification.setUpdateTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
            
        } catch (Exception e) {
            log.error("推送通知失败: {}", e.getMessage(), e);
        }
    }
} 