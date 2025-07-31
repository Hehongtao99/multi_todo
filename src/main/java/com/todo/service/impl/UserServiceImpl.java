package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.todo.dto.LoginDto;
import com.todo.dto.RegisterDto;
import com.todo.entity.User;
import com.todo.mapper.UserMapper;
import com.todo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public User register(RegisterDto registerDto) {
        // 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", registerDto.getUsername());
        User existUser = userMapper.selectOne(queryWrapper);
        
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(registerDto.getPassword());
        user.setAuth("user"); // 默认为普通用户
        
        userMapper.insert(user);
        return user;
    }
    
    @Override
    public User login(LoginDto loginDto) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginDto.getUsername())
                   .eq("password", loginDto.getPassword());
        
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        return user;
    }
    
    @Override
    public List<User> getUserList() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("auth", "user");
        return userMapper.selectList(queryWrapper);
    }
} 