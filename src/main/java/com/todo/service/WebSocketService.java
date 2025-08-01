package com.todo.service;

import com.todo.dto.WebSocketMessageDto;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

/**
 * WebSocket服务接口
 */
public interface WebSocketService {
    
    /**
     * 处理用户加入项目
     */
    void handleJoinProject(WebSocketMessageDto message, SimpMessageHeaderAccessor headerAccessor);
    
    /**
     * 处理用户离开项目
     */
    void handleLeaveProject(WebSocketMessageDto message, SimpMessageHeaderAccessor headerAccessor);
    
    /**
     * 处理项目更新消息
     */
    void handleProjectUpdate(WebSocketMessageDto message);
    
    /**
     * 处理用户状态更新
     */
    void handleUserStatusUpdate(WebSocketMessageDto message);
    
    /**
     * 处理聊天消息
     */
    void handleChatMessage(WebSocketMessageDto message);
    
    /**
     * 处理用户头像更新
     */
    void handleUserAvatarUpdate(WebSocketMessageDto message);
    
    /**
     * 向特定项目发送消息
     */
    void sendToProject(Long projectId, WebSocketMessageDto message);
    
    /**
     * 向特定用户发送消息
     */
    void sendToUser(Long userId, WebSocketMessageDto message);
    
    /**
     * 广播消息给所有在线用户
     */
    void broadcast(WebSocketMessageDto message);
} 