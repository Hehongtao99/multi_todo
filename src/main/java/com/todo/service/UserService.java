package com.todo.service;

import com.todo.dto.LoginDto;
import com.todo.dto.ProfileUpdateDto;
import com.todo.dto.RegisterDto;
import com.todo.dto.UserListQueryDto;
import com.todo.vo.ProfileVo;
import com.todo.vo.UserVo;

import java.util.List;

public interface UserService {
    
    /**
     * 用户注册
     */
    UserVo register(RegisterDto registerDto);
    
    /**
     * 用户登录
     */
    UserVo login(LoginDto loginDto);
    
    /**
     * 获取所有普通用户列表
     */
    List<UserVo> getUserList(UserListQueryDto queryDto);
    
    /**
     * 获取个人信息
     */
    ProfileVo getProfile(Long userId);
    
    /**
     * 更新个人信息
     */
    void updateProfile(ProfileUpdateDto dto);
} 