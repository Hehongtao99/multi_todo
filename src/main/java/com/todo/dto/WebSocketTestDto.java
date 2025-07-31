package com.todo.dto;

import lombok.Data;

/**
 * WebSocket测试DTO
 */
@Data
public class WebSocketTestDto {
    
    /**
     * 消息类型
     */
    private String type;
    
    /**
     * 消息内容
     */
    private Object content;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者姓名
     */
    private String senderName;
    
    /**
     * 项目ID（项目消息时使用）
     */
    private Long projectId;
    
    /**
     * 接收者ID（点对点消息时使用）
     */
    private Long receiverId;
} 