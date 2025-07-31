package com.todo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectAssignDto {
    
    private Long projectId;
    
    private List<Long> userIds;
} 