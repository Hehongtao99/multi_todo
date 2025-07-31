package com.todo.dto;

import lombok.Data;

@Data
public class RegisterDto {
    
    private String username;
    
    private String password;
    
    private String confirmPassword;
    
    /**
     * 操作者权限，用于权限验证
     */
    private String userAuth;
} 