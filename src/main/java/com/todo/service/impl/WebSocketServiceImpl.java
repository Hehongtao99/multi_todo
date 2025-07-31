package com.todo.service.impl;

import com.todo.dto.WebSocketMessageDto;
import com.todo.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * WebSocket服务实现类
 */
@Slf4j
@Service
public class WebSocketServiceImpl implements WebSocketService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Override
    public void handleJoinProject(WebSocketMessageDto message, SimpMessageHeaderAccessor headerAccessor) {
        log.info("用户 {} 加入项目 {}", message.getSenderName(), message.getProjectId());
        
        // 在会话中保存用户信息
        headerAccessor.getSessionAttributes().put("userId", message.getSenderId());
        headerAccessor.getSessionAttributes().put("userName", message.getSenderName());
        headerAccessor.getSessionAttributes().put("projectId", message.getProjectId());
        
        // 通知项目内其他用户
        WebSocketMessageDto notification = new WebSocketMessageDto(
            "USER_JOINED", 
            message.getSenderName() + " 加入了项目",
            message.getSenderId(),
            message.getSenderName()
        );
        notification.setProjectId(message.getProjectId());
        notification.setMessageId(UUID.randomUUID().toString());
        
        sendToProject(message.getProjectId(), notification);
    }
    
    @Override
    public void handleLeaveProject(WebSocketMessageDto message, SimpMessageHeaderAccessor headerAccessor) {
        log.info("用户 {} 离开项目 {}", message.getSenderName(), message.getProjectId());
        
        // 通知项目内其他用户
        WebSocketMessageDto notification = new WebSocketMessageDto(
            "USER_LEFT", 
            message.getSenderName() + " 离开了项目",
            message.getSenderId(),
            message.getSenderName()
        );
        notification.setProjectId(message.getProjectId());
        notification.setMessageId(UUID.randomUUID().toString());
        
        sendToProject(message.getProjectId(), notification);
    }
    
    @Override
    public void handleProjectUpdate(WebSocketMessageDto message) {
        log.info("项目 {} 有更新，发送者: {}", message.getProjectId(), message.getSenderName());
        
        message.setMessageId(UUID.randomUUID().toString());
        sendToProject(message.getProjectId(), message);
    }
    
    @Override
    public void handleUserStatusUpdate(WebSocketMessageDto message) {
        log.info("用户 {} 状态更新", message.getSenderName());
        
        message.setMessageId(UUID.randomUUID().toString());
        if (message.getProjectId() != null) {
            sendToProject(message.getProjectId(), message);
        } else {
            broadcast(message);
        }
    }
    
    @Override
    public void handleChatMessage(WebSocketMessageDto message) {
        log.info("收到聊天消息，发送者: {}, 项目: {}", message.getSenderName(), message.getProjectId());
        
        message.setMessageId(UUID.randomUUID().toString());
        
        if (message.getReceiverId() != null) {
            // 点对点消息
            sendToUser(message.getReceiverId(), message);
        } else if (message.getProjectId() != null) {
            // 项目群聊消息
            sendToProject(message.getProjectId(), message);
        } else {
            // 全局聊天消息
            broadcast(message);
        }
    }
    
    @Override
    public void sendToProject(Long projectId, WebSocketMessageDto message) {
        String destination = "/topic/project/" + projectId;
        messagingTemplate.convertAndSend(destination, message);
        log.debug("发送消息到项目 {}: {}", projectId, message.getType());
    }
    
    @Override
    public void sendToUser(Long userId, WebSocketMessageDto message) {
        String destination = "/queue/user/" + userId;
        messagingTemplate.convertAndSend(destination, message);
        log.debug("发送消息到用户 {}: {}", userId, message.getType());
    }
    
    @Override
    public void broadcast(WebSocketMessageDto message) {
        String destination = "/topic/global";
        messagingTemplate.convertAndSend(destination, message);
        log.debug("广播消息: {}", message.getType());
    }
} 