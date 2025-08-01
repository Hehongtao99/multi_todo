package com.todo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoCreateDto {
    
    private String title;
    
    private String description;
    
    private String priority; // low, medium, high
    
    private Long projectId;
    
    private Long assigneeId;
    
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
}
