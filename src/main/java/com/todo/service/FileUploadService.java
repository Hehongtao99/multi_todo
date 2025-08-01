package com.todo.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.Map;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {
    
    /**
     * 上传头像文件
     * @param file 文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    String uploadAvatar(MultipartFile file, Long userId);
    
    /**
     * 上传聊天文件
     * @param file 文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    String uploadChatFile(MultipartFile file, Long userId);
    
    /**
     * 上传表情图片
     * @param file 文件
     * @return 文件访问URL
     */
    String uploadEmojiSprite(MultipartFile file);
    
    /**
     * 删除文件
     * @param fileName 文件名
     * @return 是否删除成功
     */
    boolean deleteFile(String fileName);
    
    /**
     * 获取文件输入流
     * @param fileName 文件名
     * @return 文件输入流
     */
    InputStream getFileStream(String fileName) throws Exception;
    
    /**
     * 预览文件内容
     * @param fileUrl 文件URL
     * @param fileType 文件类型
     * @return 预览内容
     */
    Map<String, Object> previewFile(String fileUrl, String fileType) throws Exception;
    
    /**
     * 从URL中提取MinIO中的文件路径
     * @param fileUrl 文件URL
     * @return MinIO中的文件路径
     */
    String extractFilePathFromUrl(String fileUrl);
    
    /**
     * 从ZIP文件中提取特定文件内容
     * @param fileUrl ZIP文件URL
     * @param entryPath ZIP文件中的文件路径
     * @return 文件内容
     */
    Map<String, Object> extractFileFromZip(String fileUrl, String entryPath) throws Exception;
} 