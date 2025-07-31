package com.todo.service;

import com.todo.dto.LoginDto;
import com.todo.dto.RegisterDto;
import com.todo.entity.User;

import java.util.List;

public interface UserService {
    
    /**
     * 用户注册
     */
    User register(RegisterDto registerDto);
    
    /**
     * 用户登录
     */
    User login(LoginDto loginDto);
    
    /**
     * 获取所有普通用户列表
     */
    List<User> getUserList();
} 