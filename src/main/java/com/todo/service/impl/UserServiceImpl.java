package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.todo.dto.LoginDto;
import com.todo.dto.ProfileUpdateDto;
import com.todo.dto.RegisterDto;
import com.todo.dto.UserListQueryDto;
import com.todo.entity.User;
import com.todo.mapper.UserMapper;
import com.todo.service.UserService;
import com.todo.vo.ProfileVo;
import com.todo.vo.UserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public UserVo register(RegisterDto registerDto) {
        // 权限验证：检查是否有权限注册用户
        if (!"admin".equals(registerDto.getUserAuth())) {
            throw new RuntimeException("权限不足，只有管理员可以注册用户");
        }
        
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
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());
        
        int result = userMapper.insert(user);
        
        if (result <= 0) {
            throw new RuntimeException("用户注册失败");
        }
        
        // 返回新创建的用户信息
        return convertToUserVo(user);
    }
    
    @Override
    public UserVo login(LoginDto loginDto) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginDto.getUsername())
                   .eq("password", loginDto.getPassword());
        
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 转换为VO对象
        return convertToUserVo(user);
    }
    
    @Override
    public List<UserVo> getUserList(UserListQueryDto queryDto) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        
        if ("admin".equals(queryDto.getUserAuth())) {
            // 管理员可以查看所有普通用户
            queryWrapper.eq("auth", "user");
        } else {
            // 普通用户可以查看所有管理员
            queryWrapper.eq("auth", "admin");
        }
        
        List<User> users = userMapper.selectList(queryWrapper);
        
        // 转换为VO对象列表
        return users.stream()
                   .map(this::convertToUserVo)
                   .collect(Collectors.toList());
    }
    
    /**
     * 将User实体转换为UserVo
     */
    private UserVo convertToUserVo(User user) {
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        // 不返回密码等敏感信息
        return userVo;
    }
    
    @Override
    public ProfileVo getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        ProfileVo profileVo = new ProfileVo();
        BeanUtils.copyProperties(user, profileVo);
        return profileVo;
    }
    
    @Override
    public void updateProfile(ProfileUpdateDto dto) {
        if (dto.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 更新用户信息
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", dto.getUserId());
        
        User updateUser = new User();
        updateUser.setRealName(dto.getRealName());
        updateUser.setAvatar(dto.getAvatar());
        updateUser.setUpdatedTime(LocalDateTime.now());
        
        int result = userMapper.update(updateUser, updateWrapper);
        if (result == 0) {
            throw new RuntimeException("更新个人信息失败");
        }
    }
} 