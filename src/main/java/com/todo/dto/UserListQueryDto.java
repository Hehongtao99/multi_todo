package com.todo.dto;

import lombok.Data;

/**
 * 用户列表查询DTO
 */
@Data
public class UserListQueryDto {
    
    /**
     * 请求用户ID
     */
    private Long userId;
    
    /**
     * 请求用户权限
     */
    private String userAuth;
} 