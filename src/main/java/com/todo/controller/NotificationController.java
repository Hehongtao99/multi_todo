package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.NotificationCreateDto;
import com.todo.entity.Notification;
import com.todo.service.NotificationService;
import com.todo.vo.NotificationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 管理员发送系统通知
     */
    @PostMapping("/system")
    public Result<Notification> sendSystemNotification(@RequestBody NotificationCreateDto dto,
                                                      @RequestParam Long adminId,
                                                      @RequestParam String adminName) {
        notificationService.sendSystemNotification(dto, adminId, adminName);
        return Result.success("系统通知发送成功");
    }
    
    /**
     * 管理员发送项目通知
     */
    @PostMapping("/project")
    public Result<Notification> sendProjectNotification(@RequestBody NotificationCreateDto dto,
                                                       @RequestParam Long adminId,
                                                       @RequestParam String adminName) {
        notificationService.sendProjectNotification(dto, adminId, adminName);
        return Result.success("项目通知发送成功");
    }
    
    /**
     * 管理员发送个人通知
     */
    @PostMapping("/personal")
    public Result<Notification> sendPersonalNotification(@RequestBody NotificationCreateDto dto,
                                                        @RequestParam Long adminId,
                                                        @RequestParam String adminName) {
        notificationService.sendPersonalNotification(dto, adminId, adminName);
        return Result.success("个人通知发送成功");
    }
    
    /**
     * 获取用户通知列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<NotificationVo>> getUserNotifications(@PathVariable Long userId) {
        List<NotificationVo> notifications = notificationService.getNotificationsByUserId(userId);
        return Result.success(notifications);
    }
    
    /**
     * 获取项目通知列表
     */
    @GetMapping("/project/{projectId}")
    public Result<List<NotificationVo>> getProjectNotifications(@PathVariable Long projectId) {
        List<NotificationVo> notifications = notificationService.getNotificationsByProjectId(projectId);
        return Result.success(notifications);
    }
    
    /**
     * 获取系统通知列表
     */
    @GetMapping("/system")
    public Result<List<NotificationVo>> getSystemNotifications() {
        List<NotificationVo> notifications = notificationService.getSystemNotifications();
        return Result.success(notifications);
    }
    
    /**
     * 标记通知为已读
     */
    @PutMapping("/{notificationId}/read")
    public Result<String> markAsRead(@PathVariable Long notificationId, @RequestParam Long userId) {
        boolean success = notificationService.markAsRead(notificationId, userId);
        if (success) {
            return Result.success("标记已读成功");
        } else {
            return Result.error("标记已读失败");
        }
    }
    
    /**
     * 批量标记所有通知为已读
     */
    @PutMapping("/read-all")
    public Result<String> markAllAsRead(@RequestParam Long userId) {
        boolean success = notificationService.markAllAsRead(userId);
        if (success) {
            return Result.success("全部标记已读成功");
        } else {
            return Result.error("全部标记已读失败");
        }
    }
    
    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count/{userId}")
    public Result<Integer> getUnreadCount(@PathVariable Long userId) {
        int count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }
    
    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    public Result<String> deleteNotification(@PathVariable Long notificationId) {
        boolean success = notificationService.deleteNotification(notificationId);
        if (success) {
            return Result.success("删除通知成功");
        } else {
            return Result.error("删除通知失败");
        }
    }
} 