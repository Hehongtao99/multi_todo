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
