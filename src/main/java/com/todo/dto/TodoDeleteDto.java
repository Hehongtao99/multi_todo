package com.todo.dto;

import lombok.Data;

/**
 * 待办事项删除DTO
 */
@Data
public class TodoDeleteDto {
    
    /**
     * 待办事项ID
     */
    private Long todoId;
    
    /**
     * 请求用户ID
     */
    private Long userId;
    
    /**
     * 请求用户权限
     */
    private String userAuth;
} 