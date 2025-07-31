package com.todo.dto;

import lombok.Data;

/**
 * 项目列表查询DTO
 */
@Data
public class ProjectListQueryDto {
    
    /**
     * 请求用户ID
     */
    private Long userId;
    
    /**
     * 请求用户权限
     */
    private String userAuth;
} 