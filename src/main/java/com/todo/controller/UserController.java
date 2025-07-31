package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.LoginDto;
import com.todo.dto.RegisterDto;
import com.todo.dto.UserListQueryDto;
import com.todo.service.UserService;
import com.todo.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVo> register(@RequestBody RegisterDto registerDto) {
        UserVo result = userService.register(registerDto);
        return Result.success(result);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserVo> login(@RequestBody LoginDto loginDto) {
        UserVo result = userService.login(loginDto);
        return Result.success(result);
    }
    
    /**
     * 获取所有普通用户列表（仅管理员）
     */
    @PostMapping("/list")
    public Result<List<UserVo>> getUserList(@RequestBody UserListQueryDto queryDto) {
        List<UserVo> result = userService.getUserList(queryDto);
        return Result.success(result);
    }
} 