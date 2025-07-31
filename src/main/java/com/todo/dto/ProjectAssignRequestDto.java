package com.todo.dto;

import lombok.Data;

import java.util.List;

/**
 * 项目分配请求DTO
 */
@Data
public class ProjectAssignRequestDto {
    
    /**
     * 请求用户权限
     */
    private String userAuth;
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 分配的用户ID列表
     */
    private List<Long> userIds;
} 