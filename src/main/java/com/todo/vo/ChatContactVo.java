package com.todo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天联系人VO
 */
@Data
public class ChatContactVo {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 最后一条消息内容
     */
    private String lastMessage;
    
    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;
    
    /**
     * 未读消息数量
     */
    private Integer unreadCount;
    
    /**
     * 在线状态
     */
    private String onlineStatus;
} 