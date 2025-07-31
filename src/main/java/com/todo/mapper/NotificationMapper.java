package com.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.todo.entity.Notification;
import com.todo.vo.NotificationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知Mapper接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    
    /**
     * 根据用户ID获取通知列表
     */
    List<NotificationVo> getNotificationsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据项目ID获取项目通知
     */
    List<NotificationVo> getNotificationsByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 获取系统通知
     */
    List<NotificationVo> getSystemNotifications();
    
    /**
     * 标记通知为已读
     */
    int markAsRead(@Param("notificationId") Long notificationId, @Param("userId") Long userId);
    
    /**
     * 批量标记通知为已读
     */
    int markAllAsRead(@Param("userId") Long userId);
    
    /**
     * 获取未读通知数量
     */
    int getUnreadCount(@Param("userId") Long userId);
} 