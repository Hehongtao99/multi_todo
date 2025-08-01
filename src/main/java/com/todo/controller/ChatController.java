package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.*;
import com.todo.service.ChatService;
import com.todo.service.FileUploadService;
import com.todo.vo.ChatContactVo;
import com.todo.vo.ChatMessageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    /**
     * 获取聊天记录
     */
    @PostMapping("/history")
    public Result<List<ChatMessageVo>> getChatHistory(@RequestBody ChatHistoryQueryDto queryDto) {
        System.out.println("收到获取聊天记录请求: " + queryDto);
        
        // 参数验证
        if (queryDto.getRequesterId() == null) {
            return Result.error("请求者ID不能为空");
        }
        if (queryDto.getChatUserId() == null) {
            return Result.error("聊天对象ID不能为空");
        }
        
        try {
            List<ChatMessageVo> result = chatService.getChatHistory(queryDto);
            System.out.println("返回聊天记录数量: " + result.size());
            return Result.success(result);
        } catch (Exception e) {
            System.err.println("获取聊天记录异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取聊天记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取聊天联系人列表
     */
    @PostMapping("/contacts")
    public Result<List<ChatContactVo>> getChatContacts(@RequestBody ChatContactsQueryDto queryDto) {
        List<ChatContactVo> result = chatService.getChatContacts(queryDto);
        return Result.success(result);
    }
    
    /**
     * 标记消息为已读
     */
    @PostMapping("/mark-read")
    public Result<Void> markMessagesAsRead(@RequestBody ChatMarkReadDto markReadDto) {
        chatService.markMessagesAsRead(markReadDto);
        return Result.success(null);
    }
    
    /**
     * 获取未读消息数量
     */
    @PostMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadMessageCount(@RequestBody ChatUnreadCountQueryDto queryDto) {
        Map<String, Object> result = chatService.getUnreadMessageCount(queryDto);
        return Result.success(result);
    }
    
    /**
     * 上传聊天文件
     */
    @PostMapping("/upload-file")
    public Result<Map<String, Object>> uploadChatFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        try {
            String fileUrl = fileUploadService.uploadChatFile(file, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("fileUrl", fileUrl);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("fileType", file.getContentType());
            
            // 提取存储路径（用于下载时的文件名映射）
            String storagePath = fileUrl;
            if (fileUrl.contains("/")) {
                storagePath = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            }
            result.put("storagePath", storagePath);
            result.put("originalFileName", file.getOriginalFilename());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载文件（使用原始文件名）
     */
    @GetMapping("/download-file")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam("fileUrl") String fileUrl,
            @RequestParam("originalFileName") String originalFileName) {
        try {
            // 从MinIO获取文件流 - 使用FileUploadService中的路径提取逻辑
            String fileName = fileUploadService.extractFilePathFromUrl(fileUrl);
            
            InputStream inputStream = fileUploadService.getFileStream(fileName);
            
            // 创建Resource
            InputStreamResource resource = new InputStreamResource(inputStream);
            
            // 设置响应头
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + URLEncoder.encode(originalFileName, String.valueOf(StandardCharsets.UTF_8)) + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 预览文件内容
     */
    @GetMapping("/preview-file")
    public Result<Map<String, Object>> previewFile(
            @RequestParam("fileUrl") String fileUrl,
            @RequestParam("fileType") String fileType) {
        try {
            Map<String, Object> result = fileUploadService.previewFile(fileUrl, fileType);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("文件预览失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传表情雪碧图
     */
    @PostMapping("/upload-emoji-sprite")
    public Result<Map<String, Object>> uploadEmojiSprite(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileUploadService.uploadEmojiSprite(file);
            
            Map<String, Object> result = new HashMap<>();
            result.put("fileUrl", fileUrl);
            result.put("fileName", file.getOriginalFilename());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("表情图片上传失败: " + e.getMessage());
        }
    }


} 