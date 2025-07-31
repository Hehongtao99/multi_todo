package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.NotificationDeleteDto;
import com.todo.dto.NotificationMarkReadDto;
import com.todo.dto.NotificationQueryDto;
import com.todo.dto.NotificationSendDto;
import com.todo.service.NotificationService;
import com.todo.vo.NotificationCountVo;
import com.todo.vo.NotificationVo;
import com.todo.vo.OperationResultVo;
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
     * 发送通知（统一接口）
     */
    @PostMapping("/send")
    public Result<OperationResultVo> sendNotification(@RequestBody NotificationSendDto sendDto) {
        OperationResultVo result = notificationService.sendNotification(sendDto);
        return Result.success(result);
    }
    
    /**
     * 获取用户通知列表
     */
    @PostMapping("/user")
    public Result<List<NotificationVo>> getUserNotifications(@RequestBody NotificationQueryDto queryDto) {
        List<NotificationVo> result = notificationService.getUserNotifications(queryDto);
        return Result.success(result);
    }
    
    /**
     * 获取项目通知列表
     */
    @PostMapping("/project")
    public Result<List<NotificationVo>> getProjectNotifications(@RequestBody NotificationQueryDto queryDto) {
        List<NotificationVo> result = notificationService.getProjectNotifications(queryDto);
        return Result.success(result);
    }
    
    /**
     * 获取系统通知列表
     */
    @PostMapping("/system")
    public Result<List<NotificationVo>> getSystemNotifications() {
        List<NotificationVo> result = notificationService.getSystemNotifications();
        return Result.success(result);
    }
    
    /**
     * 标记通知为已读
     */
    @PostMapping("/mark-read")
    public Result<OperationResultVo> markAsRead(@RequestBody NotificationMarkReadDto markReadDto) {
        OperationResultVo result = notificationService.markAsRead(markReadDto);
        return Result.success(result);
    }
    
    /**
     * 批量标记所有通知为已读
     */
    @PostMapping("/mark-all-read")
    public Result<OperationResultVo> markAllAsRead(@RequestBody NotificationQueryDto queryDto) {
        OperationResultVo result = notificationService.markAllAsRead(queryDto);
        return Result.success(result);
    }
    
    /**
     * 获取未读通知数量
     */
    @PostMapping("/unread-count")
    public Result<NotificationCountVo> getUnreadCount(@RequestBody NotificationQueryDto queryDto) {
        NotificationCountVo result = notificationService.getUnreadCount(queryDto);
        return Result.success(result);
    }
    
    /**
     * 删除通知
     */
    @PostMapping("/delete")
    public Result<OperationResultVo> deleteNotification(@RequestBody NotificationDeleteDto deleteDto) {
        OperationResultVo result = notificationService.deleteNotification(deleteDto);
        return Result.success(result);
    }
} 