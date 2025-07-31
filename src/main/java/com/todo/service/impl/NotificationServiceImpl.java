package com.todo.service.impl;

import com.todo.dto.NotificationSendDto;
import com.todo.dto.NotificationQueryDto;
import com.todo.dto.NotificationMarkReadDto;
import com.todo.dto.NotificationDeleteDto;
import com.todo.dto.NotificationCreateDto;
import com.todo.dto.WebSocketMessageDto;
import com.todo.entity.Notification;
import com.todo.mapper.NotificationMapper;
import com.todo.service.NotificationService;
import com.todo.service.WebSocketService;
import com.todo.vo.NotificationVo;
import com.todo.vo.NotificationCountVo;
import com.todo.vo.OperationResultVo;
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
    public OperationResultVo sendNotification(NotificationSendDto sendDto) {
        // 权限验证：检查是否有权限发送通知
        if (!"admin".equals(sendDto.getUserAuth())) {
            throw new RuntimeException("权限不足，只有管理员可以发送通知");
        }
        
        // 创建通知DTO
        NotificationCreateDto createDto = new NotificationCreateDto();
        BeanUtils.copyProperties(sendDto, createDto);
        
        // 根据通知类型发送
        Notification notification;
        try {
            if ("system".equals(sendDto.getType())) {
                notification = createSystemNotification(createDto, sendDto.getUserId(), sendDto.getSenderName());
            } else if ("project".equals(sendDto.getType())) {
                notification = createProjectNotification(createDto, sendDto.getUserId(), sendDto.getSenderName());
            } else if ("personal".equals(sendDto.getType())) {
                notification = createPersonalNotification(createDto, sendDto.getUserId(), sendDto.getSenderName());
            } else {
                throw new RuntimeException("不支持的通知类型：" + sendDto.getType());
            }
            
            // 返回成功结果
            OperationResultVo resultVo = new OperationResultVo();
            resultVo.setSuccess(true);
            resultVo.setMessage("通知发送成功");
            resultVo.setData(notification.getId());
            
            return resultVo;
            
        } catch (Exception e) {
            log.error("发送通知失败: {}", e.getMessage(), e);
            throw new RuntimeException("通知发送失败：" + e.getMessage());
        }
    }
    
    @Override
    public List<NotificationVo> getNotifications(NotificationQueryDto queryDto) {
        // 根据查询类型返回不同的通知列表
        if ("system".equals(queryDto.getType())) {
            return notificationMapper.getSystemNotifications();
        } else if ("project".equals(queryDto.getType()) && queryDto.getProjectId() != null) {
            return notificationMapper.getNotificationsByProjectId(queryDto.getProjectId());
        } else if ("personal".equals(queryDto.getType()) && queryDto.getUserId() != null) {
            return notificationMapper.getNotificationsByUserId(queryDto.getUserId());
        } else {
            // 默认返回用户的所有通知
            return notificationMapper.getNotificationsByUserId(queryDto.getUserId());
        }
    }
    
    @Override
    public OperationResultVo markAsRead(NotificationMarkReadDto markReadDto) {
        int result;
        
        if (markReadDto.getNotificationId() != null) {
            // 标记单个通知为已读
            result = notificationMapper.markAsRead(markReadDto.getNotificationId(), markReadDto.getUserId());
        } else {
            // 标记所有通知为已读
            result = notificationMapper.markAllAsRead(markReadDto.getUserId());
        }
        
        OperationResultVo resultVo = new OperationResultVo();
        if (result > 0) {
            resultVo.setSuccess(true);
            resultVo.setMessage("标记成功");
            resultVo.setData(result);
        } else {
            resultVo.setSuccess(false);
            resultVo.setMessage("标记失败");
        }
        
        return resultVo;
    }
    
    @Override
    public NotificationCountVo getUnreadCount(NotificationQueryDto queryDto) {
        int count = notificationMapper.getUnreadCount(queryDto.getUserId());
        
        NotificationCountVo countVo = new NotificationCountVo();
        countVo.setUserId(queryDto.getUserId());
        countVo.setUnreadCount(count);
        
        return countVo;
    }
    
    @Override
    public OperationResultVo deleteNotification(NotificationDeleteDto deleteDto) {
        // 权限验证：检查是否有权限删除通知
        if (!"admin".equals(deleteDto.getUserAuth())) {
            throw new RuntimeException("权限不足，只有管理员可以删除通知");
        }
        
        int result = notificationMapper.deleteById(deleteDto.getNotificationId());
        
        OperationResultVo resultVo = new OperationResultVo();
        if (result > 0) {
            resultVo.setSuccess(true);
            resultVo.setMessage("通知删除成功");
            resultVo.setData(deleteDto.getNotificationId());
        } else {
            resultVo.setSuccess(false);
            resultVo.setMessage("通知删除失败");
        }
        
        return resultVo;
    }
    
    /**
     * 创建系统通知
     */
    private Notification createSystemNotification(NotificationCreateDto dto, Long senderId, String senderName) {
        dto.setType("system");
        dto.setReceiverId(null); // 系统通知发送给所有用户
        return createNotification(dto, senderId, senderName);
    }
    

    
    /**
     * 创建个人通知
     */
    @Override
    public Notification createPersonalNotification(NotificationCreateDto dto, Long senderId, String senderName) {
        dto.setType("personal");
        return createNotification(dto, senderId, senderName);
    }
    
    /**
     * 创建项目通知
     */
    @Override
    public Notification createProjectNotification(NotificationCreateDto dto, Long senderId, String senderName) {
        dto.setType("project");
        return createNotification(dto, senderId, senderName);
    }
    
    /**
     * 创建通知（内部方法）
     */
    private Notification createNotification(NotificationCreateDto dto, Long senderId, String senderName) {
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