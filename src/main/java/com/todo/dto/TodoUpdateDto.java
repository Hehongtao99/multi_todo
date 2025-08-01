package com.todo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoUpdateDto {
    
    private Long id;
    
    private String title;
    
    private String description;
    
    private String status; // pending, in_progress, completed
    
    private String priority; // low, medium, high
    
    private Long assigneeId;
    
    private Long projectId;
    
    private LocalDateTime startTime;
    
    private LocalDateTime dueDate;
    
    /**
     * 请求用户ID
     */
    private Long userId;
    
    /**
     * 请求用户权限
     */
    private String userAuth;
    
    /**
     * 是否强制更新（管理员权限）
     */
    private Boolean forceUpdate = false;
}
