package com.todo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
public class ChatMessage {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型：text-文本, image-图片, file-文件等
     */
    private String messageType;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
} 