package com.todo.dto;

import lombok.Data;

/**
 * 项目创建请求DTO
 */
@Data
public class ProjectCreateRequestDto {
    
    /**
     * 请求用户ID
     */
    private Long userId;
    
    /**
     * 请求用户权限
     */
    private String userAuth;
    
    /**
     * 项目名称
     */
    private String projectName;
    
    /**
     * 项目描述
     */
    private String projectDescription;
} 