package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.WebSocketMessageDto;
import com.todo.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * WebSocket测试控制器
 * 提供WebSocket功能测试接口
 */
@RestController
@RequestMapping("/api/websocket")
@CrossOrigin(origins = "*")
public class WebSocketTestController {
    
    @Autowired
    private WebSocketService webSocketService;
    
    /**
     * 测试向项目发送消息
     */
    @PostMapping("/test/project/{projectId}")
    public Result<String> testProjectMessage(@PathVariable Long projectId, 
                                           @RequestBody WebSocketMessageDto message) {
        message.setProjectId(projectId);
        webSocketService.sendToProject(projectId, message);
        return Result.success("消息发送成功");
    }
    
    /**
     * 测试向用户发送消息
     */
    @PostMapping("/test/user/{userId}")
    public Result<String> testUserMessage(@PathVariable Long userId, 
                                        @RequestBody WebSocketMessageDto message) {
        message.setReceiverId(userId);
        webSocketService.sendToUser(userId, message);
        return Result.success("消息发送成功");
    }
    
    /**
     * 测试广播消息
     */
    @PostMapping("/test/broadcast")
    public Result<String> testBroadcast(@RequestBody WebSocketMessageDto message) {
        webSocketService.broadcast(message);
        return Result.success("广播消息发送成功");
    }
    
    /**
     * 获取WebSocket连接状态
     */
    @GetMapping("/status")
    public Result<String> getWebSocketStatus() {
        return Result.success("WebSocket服务正常运行");
    }
} 