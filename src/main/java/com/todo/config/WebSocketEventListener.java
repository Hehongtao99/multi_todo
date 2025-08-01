package com.todo.config;

import com.todo.dto.WebSocketMessageDto;
import com.todo.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket事件监听器
 * 处理WebSocket连接和断开事件
 */
@Slf4j
@Component
public class WebSocketEventListener {
    
    @Autowired
    private WebSocketService webSocketService;
    
    /**
     * 处理WebSocket连接事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket连接建立，会话ID: {}", sessionId);
        
        // 注意：此时用户信息可能还没有设置到session中，需要在用户发送第一条消息时处理
    }
    
    /**
     * 处理WebSocket断开连接事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // 从会话属性中获取用户信息
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String userName = (String) headerAccessor.getSessionAttributes().get("userName");
        Long projectId = (Long) headerAccessor.getSessionAttributes().get("projectId");
        
        log.info("WebSocket连接断开，会话ID: {}, 用户: {} (ID: {})", sessionId, userName, userId);
        
        // 发送用户离线状态更新
        if (userId != null && userName != null) {
            java.util.Map<String, Object> statusContent = new java.util.HashMap<>();
            statusContent.put("status", "offline");
            
            WebSocketMessageDto statusMessage = new WebSocketMessageDto(
                "USER_STATUS", 
                statusContent,
                userId,
                userName
            );
            
            // 先处理状态更新（从在线列表中移除）
            webSocketService.handleUserStatusUpdate(statusMessage);
            
            // 再广播离线状态
            webSocketService.broadcast(statusMessage);
            
            log.info("用户 {} 离线状态已广播", userName);
        }
        
        // 如果用户在项目中，通知其他用户
        if (userId != null && userName != null && projectId != null) {
            WebSocketMessageDto notification = new WebSocketMessageDto(
                "USER_DISCONNECTED", 
                userName + " 已断开连接",
                userId,
                userName
            );
            notification.setProjectId(projectId);
            
            webSocketService.sendToProject(projectId, notification);
        }
    }
} 