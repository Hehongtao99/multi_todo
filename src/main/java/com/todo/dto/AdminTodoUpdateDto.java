package com.todo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员修改待办事项DTO
 */
@Data
public class AdminTodoUpdateDto {
    
    /**
     * 待办事项ID
     */
    private Long todoId;
    
    /**
     * 任务标题
     */
    private String title;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 任务状态：pending, in_progress, completed
     */
    private String status;
    
    /**
     * 优先级：low, medium, high
     */
    private String priority;
    
    /**
     * 分配人ID
     */
    private Long assigneeId;
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 截止时间
     */
    private LocalDateTime dueDate;
    
    /**
     * 管理员ID（操作人）
     */
    private Long adminId;
    
    /**
     * 修改原因或备注
     */
    private String updateReason;
} 