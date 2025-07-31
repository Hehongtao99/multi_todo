package com.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
@TableName("notifications")
public class Notification {
    
    /**
     * 通知ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 通知类型：system(系统通知)、project(项目通知)、personal(个人通知)
     */
    private String type;
    
    /**
     * 优先级：low(低)、normal(普通)、high(高)、urgent(紧急)
     */
    private String priority;
    
    /**
     * 发送者ID（管理员ID）
     */
    private Long senderId;
    
    /**
     * 发送者姓名
     */
    private String senderName;
    
    /**
     * 接收者ID（为空表示全体用户）
     */
    private Long receiverId;
    
    /**
     * 项目ID（项目通知时使用）
     */
    private Long projectId;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 是否已推送
     */
    private Boolean isPushed;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 额外数据（JSON格式）
     */
    private String extraData;
} 