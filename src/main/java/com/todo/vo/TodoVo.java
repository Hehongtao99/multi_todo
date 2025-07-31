package com.todo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoVo {
    
    private Long id;
    
    private String title;
    
    private String description;
    
    private String status;
    
    private String priority;
    
    private Long projectId;
    
    private String projectName;
    
    private Long assigneeId;
    
    private String assigneeName;
    
    private Long creatorId;
    
    private String creatorName;
    
    private LocalDateTime dueDate;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
