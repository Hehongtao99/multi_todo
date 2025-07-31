package com.todo.dto;

import lombok.Data;

/**
 * WebSocket消息传输对象
 */
@Data
public class WebSocketMessageDto {
    
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
     * 项目ID（如果是项目相关消息）
     */
    private Long projectId;
    
    /**
     * 接收者ID（点对点消息时使用）
     */
    private Long receiverId;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 消息ID
     */
    private String messageId;
    
    public WebSocketMessageDto() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public WebSocketMessageDto(String type, Object content) {
        this();
        this.type = type;
        this.content = content;
    }
    
    public WebSocketMessageDto(String type, Object content, Long senderId, String senderName) {
        this(type, content);
        this.senderId = senderId;
        this.senderName = senderName;
    }
} 