package com.todo.vo;

import lombok.Data;

/**
 * 通知数量视图对象
 */
@Data
public class NotificationCountVo {
    
    /**
     * 未读通知数量
     */
    private Integer unreadCount;
    
    public NotificationCountVo() {}
    
    public NotificationCountVo(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
} 