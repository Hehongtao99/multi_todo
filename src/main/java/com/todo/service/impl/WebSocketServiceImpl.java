package com.todo.service.impl;

import com.todo.dto.WebSocketMessageDto;
import com.todo.service.ChatService;
import com.todo.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务实现类
 */
@Slf4j
@Service
public class WebSocketServiceImpl implements WebSocketService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ChatService chatService;
    
    // 在线用户状态管理
    private final Map<Long, String> onlineUsers = new ConcurrentHashMap<>();
    
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
        
        // 清除会话中的项目信息
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().remove("projectId");
        }
    }
    
    @Override
    public void handleProjectUpdate(WebSocketMessageDto message) {
        log.info("收到项目更新消息，发送者: {}, 项目: {}", message.getSenderName(), message.getProjectId());
        
        message.setMessageId(UUID.randomUUID().toString());
        
        if (message.getProjectId() != null) {
            sendToProject(message.getProjectId(), message);
        } else {
            broadcast(message);
        }
    }
    
    @Override
    public void handleUserStatusUpdate(WebSocketMessageDto message) {
        log.info("收到用户状态更新，发送者: {}, 状态: {}", message.getSenderName(), message.getContent());
        
        message.setMessageId(UUID.randomUUID().toString());
        
        // 处理状态请求
        if (message.getContent() instanceof Map) {
            Map<String, Object> content = (Map<String, Object>) message.getContent();
            
            // 如果是请求所有在线用户状态
            if ("request_all".equals(content.get("action"))) {
                log.info("用户{}请求所有在线用户状态，当前在线用户数: {}", message.getSenderName(), onlineUsers.size());
                
                // 确保请求者自己在在线列表中
                if (message.getSenderId() != null) {
                    onlineUsers.put(message.getSenderId(), "online");
                }
                
                // 发送当前所有在线用户的状态给请求者
                for (Map.Entry<Long, String> entry : onlineUsers.entrySet()) {
                    Map<String, Object> statusContent = new HashMap<>();
                    statusContent.put("status", entry.getValue());
                    
                    WebSocketMessageDto statusMessage = new WebSocketMessageDto(
                        "USER_STATUS", 
                        statusContent,
                        entry.getKey(),
                        "系统" // 这里可以优化为实际用户名
                    );
                    statusMessage.setMessageId(UUID.randomUUID().toString());
                    
                    sendToUser(message.getSenderId(), statusMessage);
                }
                
                return;
            }
            
            // 更新用户在线状态
            String status = (String) content.get("status");
            if (status != null && message.getSenderId() != null) {
                String oldStatus = onlineUsers.get(message.getSenderId());
                
                if ("online".equals(status)) {
                    onlineUsers.put(message.getSenderId(), status);
                    if (!"online".equals(oldStatus)) {
                        log.info("用户 {} 状态更新: {} -> {}", message.getSenderId(), oldStatus, status);
                    }
                } else if ("offline".equals(status)) {
                    onlineUsers.remove(message.getSenderId());
                    log.info("用户 {} 下线，从在线列表中移除", message.getSenderId());
                }
            }
        }
        
        // 广播用户状态给所有用户
        broadcast(message);
    }
    
    @Override
    public void handleUserAvatarUpdate(WebSocketMessageDto message) {
        log.info("收到用户头像更新，发送者: {}, 内容: {}", message.getSenderName(), message.getContent());
        
        message.setMessageId(UUID.randomUUID().toString());
        
        // 广播头像更新消息给所有用户
        broadcast(message);
    }
    
    @Override
    public void handleChatMessage(WebSocketMessageDto message) {
        log.info("收到聊天消息，发送者: {}, 接收者: {}", message.getSenderName(), message.getReceiverId());
        
        message.setMessageId(UUID.randomUUID().toString());
        
        // 保存聊天消息到数据库
        if (message.getReceiverId() != null && message.getSenderId() != null) {
            try {
                // 从消息内容中获取消息类型，默认为text
                String messageType = "text";
                String content = message.getContent().toString();
                
                // 检查是否为文件消息
                if (content.startsWith("FILE:")) {
                    messageType = "file";
                } else if (content.contains(":") && content.contains("emoji")) {
                    messageType = "emoji";
                }
                
                chatService.saveChatMessage(
                    message.getSenderId(), 
                    message.getReceiverId(), 
                    content,
                    messageType
                );
                log.info("聊天消息已保存到数据库，类型: {}", messageType);
            } catch (Exception e) {
                log.error("保存聊天消息失败: {}", e.getMessage());
            }
        }
        
        if (message.getReceiverId() != null) {
            // 点对点消息，发送给接收者
            sendToUser(message.getReceiverId(), message);
            // 同时发送给发送者确认
            sendToUser(message.getSenderId(), message);
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