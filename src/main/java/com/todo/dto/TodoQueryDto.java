package com.todo.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 待办事项查询DTO
 */
@Data
public class TodoQueryDto {
    
    /**
     * 请求用户ID
     */
    private Long userId;
    
    /**
     * 请求用户权限
     */
    private String userAuth;
    
    /**
     * 项目ID（可选）
     */
    private Long projectId;
    
    /**
     * 分配人ID（可选）
     */
    private Long assigneeId;
    
    /**
     * 待办事项ID（用于查询详情）
     */
    private Long todoId;
    
    /**
     * 状态过滤（可选）
     */
    private String status;
    
    /**
     * 查询日期（可选，默认为今天）
     */
    private LocalDate queryDate;
    
    /**
     * 是否查询历史数据（默认false，只查询今天的）
     */
    private Boolean includeHistory;
} 