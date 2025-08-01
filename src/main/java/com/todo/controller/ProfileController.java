package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.ProfileUpdateDto;
import com.todo.service.FileUploadService;
import com.todo.service.UserService;
import com.todo.vo.ProfileVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    /**
     * 获取个人信息
     */
    @PostMapping("/info")
    public Result<ProfileVo> getProfile(@RequestBody ProfileUpdateDto dto) {
        log.info("获取个人信息，用户ID: {}", dto.getUserId());
        try {
            ProfileVo profile = userService.getProfile(dto.getUserId());
            return Result.success(profile);
        } catch (Exception e) {
            log.error("获取个人信息失败: {}", e.getMessage());
            return Result.error("获取个人信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新个人信息
     */
    @PostMapping("/update")
    public Result<String> updateProfile(@RequestBody ProfileUpdateDto dto) {
        log.info("更新个人信息，用户ID: {}, 姓名: {}", dto.getUserId(), dto.getRealName());
        try {
            userService.updateProfile(dto);
            return Result.success("个人信息更新成功");
        } catch (Exception e) {
            log.error("更新个人信息失败: {}", e.getMessage());
            return Result.error("更新个人信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传头像
     */
    @PostMapping("/upload-avatar")
    public Result<String> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        log.info("上传头像，用户ID: {}, 文件名: {}", userId, file.getOriginalFilename());
        try {
            String avatarUrl = fileUploadService.uploadAvatar(file, userId);
            return Result.success(avatarUrl);
        } catch (Exception e) {
            log.error("上传头像失败: {}", e.getMessage());
            return Result.error("上传头像失败: " + e.getMessage());
        }
    }
} 