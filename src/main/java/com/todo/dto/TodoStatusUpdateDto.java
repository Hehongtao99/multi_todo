package com.todo.dto;

import lombok.Data;

/**
 * 待办事项状态更新DTO
 */
@Data
public class TodoStatusUpdateDto {
    
    /**
     * 待办事项ID
     */
    private Long todoId;
    
    /**
     * 新状态
     */
    private String status;
    
    /**
     * 请求用户ID
     */
    private Long userId;
    
    /**
     * 请求用户权限
     */
    private String userAuth;
}
