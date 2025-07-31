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
    
    private LocalDateTime dueDate;
}
