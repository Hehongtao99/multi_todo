package com.todo.controller;

import com.todo.dto.WebSocketMessageDto;
import com.todo.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * WebSocket控制器
 * 处理WebSocket消息
 */
@Controller
public class WebSocketController {
    
    @Autowired
    private WebSocketService webSocketService;
    
    /**
     * 处理用户加入项目
     */
    @MessageMapping("/project.join")
    public void joinProject(@Payload WebSocketMessageDto message, SimpMessageHeaderAccessor headerAccessor) {
        webSocketService.handleJoinProject(message, headerAccessor);
    }
    
    /**
     * 处理用户离开项目
     */
    @MessageMapping("/project.leave")
    public void leaveProject(@Payload WebSocketMessageDto message, SimpMessageHeaderAccessor headerAccessor) {
        webSocketService.handleLeaveProject(message, headerAccessor);
    }
    
    /**
     * 处理项目更新消息
     */
    @MessageMapping("/project.update")
    public void projectUpdate(@Payload WebSocketMessageDto message) {
        webSocketService.handleProjectUpdate(message);
    }
    
    /**
     * 处理用户状态更新
     */
    @MessageMapping("/user.status")
    public void userStatus(@Payload WebSocketMessageDto message) {
        webSocketService.handleUserStatusUpdate(message);
    }
    
    /**
     * 处理聊天消息
     */
    @MessageMapping("/chat.message")
    public void chatMessage(@Payload WebSocketMessageDto message) {
        webSocketService.handleChatMessage(message);
    }
} 