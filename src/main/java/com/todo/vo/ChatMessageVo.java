package com.todo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息VO
 */
@Data
public class ChatMessageVo {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者用户名
     */
    private String senderName;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 接收者用户名
     */
    private String receiverName;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型
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
} 