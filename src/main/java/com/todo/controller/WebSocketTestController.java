package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.WebSocketTestDto;
import com.todo.service.WebSocketService;
import com.todo.vo.OperationResultVo;
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
    @PostMapping("/test/project")
    public Result<OperationResultVo> testProjectMessage(@RequestBody WebSocketTestDto testDto) {
        OperationResultVo result = webSocketService.testProjectMessage(testDto);
        return Result.success(result);
    }
    
    /**
     * 测试向用户发送消息
     */
    @PostMapping("/test/user")
    public Result<OperationResultVo> testUserMessage(@RequestBody WebSocketTestDto testDto) {
        OperationResultVo result = webSocketService.testUserMessage(testDto);
        return Result.success(result);
    }
    
    /**
     * 测试广播消息
     */
    @PostMapping("/test/broadcast")
    public Result<OperationResultVo> testBroadcast(@RequestBody WebSocketTestDto testDto) {
        OperationResultVo result = webSocketService.testBroadcast(testDto);
        return Result.success(result);
    }
    
    /**
     * 获取WebSocket连接状态
     */
    @PostMapping("/status")
    public Result<OperationResultVo> getWebSocketStatus() {
        OperationResultVo result = webSocketService.getWebSocketStatus();
        return Result.success(result);
    }
} 