package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.LoginDto;
import com.todo.dto.RegisterDto;
import com.todo.entity.User;
import com.todo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterDto registerDto) {
        try {
            User user = userService.register(registerDto);
            return Result.success("注册成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody LoginDto loginDto) {
        try {
            User user = userService.login(loginDto);
            return Result.success("登录成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有普通用户列表（仅管理员）
     */
    @GetMapping("/list")
    public Result<List<User>> getUserList(@RequestHeader("userId") Long userId, 
                                         @RequestHeader("userAuth") String userAuth) {
        try {
            // 权限验证：只有管理员可以查看用户列表
            if (!"admin".equals(userAuth)) {
                return Result.error(403, "权限不足");
            }
            
            List<User> userList = userService.getUserList();
            return Result.success(userList);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
} 