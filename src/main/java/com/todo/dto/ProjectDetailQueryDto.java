package com.todo.dto;

import lombok.Data;

/**
 * 项目详情查询DTO
 */
@Data
public class ProjectDetailQueryDto {
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 请求用户ID
     */
    private Long userId;
    
    /**
     * 请求用户权限
     */
    private String userAuth;
} 