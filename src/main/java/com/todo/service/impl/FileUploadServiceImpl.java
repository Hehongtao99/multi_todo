package com.todo.service.impl;

import com.todo.config.MinioConfig;
import com.todo.service.FileUploadService;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传服务实现类
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {
    
    @Autowired
    private MinioClient minioClient;
    
    @Autowired
    private MinioConfig minioConfig;
    
    // 支持的头像文件类型
    private static final String[] ALLOWED_AVATAR_TYPES = {
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };
    
    // 聊天文件支持所有类型，不再限制
    // private static final String[] ALLOWED_CHAT_FILE_TYPES = {...}; // 已移除限制
    
    // 头像文件最大大小 (5MB)
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;
    
    // 聊天文件最大大小 (50MB) - 增加到50MB以支持更大的文件
    private static final long MAX_CHAT_FILE_SIZE = 5000 * 1024 * 1024;
    
    // 表情图片最大大小 (2MB)
    private static final long MAX_EMOJI_SIZE = 2 * 1024 * 1024;
    
    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        try {
            // 验证文件
            validateAvatarFile(file);
            
            // 确保桶存在
            ensureBucketExists();
            
            // 生成文件名
            String fileName = generateAvatarFileName(file, userId);
            
            // 上传文件
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            // 生成访问URL
            String fileUrl = generateFileUrl(fileName);
            
            log.info("头像上传成功，用户ID: {}, 文件名: {}, URL: {}", userId, fileName, fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("头像上传失败，用户ID: {}, 错误: {}", userId, e.getMessage());
            throw new RuntimeException("头像上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public String uploadChatFile(MultipartFile file, Long userId) {
        try {
            // 验证聊天文件
            validateChatFile(file);
            
            // 确保桶存在
            ensureBucketExists();
            
            // 生成文件名
            String fileName = generateChatFileName(file, userId);
            
            // 上传文件
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            // 生成访问URL
            String fileUrl = generateFileUrl(fileName);
            
            log.info("聊天文件上传成功，用户ID: {}, 文件名: {}, URL: {}", userId, fileName, fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("聊天文件上传失败，用户ID: {}, 错误: {}", userId, e.getMessage());
            throw new RuntimeException("聊天文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public String uploadEmojiSprite(MultipartFile file) {
        try {
            // 验证表情图片
            validateEmojiFile(file);
            
            // 确保桶存在
            ensureBucketExists();
            
            // 生成文件名
            String fileName = generateEmojiFileName(file);
            
            // 上传文件
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            // 生成访问URL
            String fileUrl = generateFileUrl(fileName);
            
            log.info("表情图片上传成功，文件名: {}, URL: {}", fileName, fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("表情图片上传失败，错误: {}", e.getMessage());
            throw new RuntimeException("表情图片上传失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .build()
            );
            log.info("文件删除成功: {}", fileName);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败: {}, 错误: {}", fileName, e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证头像文件
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的头像文件");
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new RuntimeException("头像文件大小不能超过5MB");
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : ALLOWED_AVATAR_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }
        
        if (!isValidType) {
            throw new RuntimeException("不支持的头像文件格式，请上传JPG、PNG、GIF或WebP格式的图片");
        }
    }
    
    /**
     * 确保桶存在
     */
    private void ensureBucketExists() {
        try {
            boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build()
            );
            
            if (!found) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build()
                );
                log.info("创建桶: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("检查或创建桶失败: {}", e.getMessage());
            throw new RuntimeException("存储服务异常");
        }
    }
    
    /**
     * 生成头像文件名
     */
    private String generateAvatarFileName(MultipartFile file, Long userId) {
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        return String.format("avatars/%s/user_%d_%s%s", dateStr, userId, uuid, extension);
    }
    
    /**
     * 验证聊天文件
     */
    private void validateChatFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的文件");
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_CHAT_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过50MB");
        }
        
        // 移除文件类型限制，支持所有类型的文件
        // 现在只验证文件大小，不验证文件类型
    }
    
    /**
     * 验证表情图片文件
     */
    private void validateEmojiFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的表情图片");
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_EMOJI_SIZE) {
            throw new RuntimeException("表情图片大小不能超过2MB");
        }
        
        // 检查文件类型（只允许图片）
        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : ALLOWED_AVATAR_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }
        
        if (!isValidType) {
            throw new RuntimeException("表情图片格式不支持，请上传JPG、PNG、GIF或WebP格式的图片");
        }
    }
    
    /**
     * 生成聊天文件名
     */
    private String generateChatFileName(MultipartFile file, Long userId) {
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        return String.format("chat-files/%s/user_%d_%s%s", dateStr, userId, uuid, extension);
    }
    
    /**
     * 生成表情图片文件名
     */
    private String generateEmojiFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        // 表情图片使用固定名称，便于前端引用
        if ("emoji-sprite.png".equals(originalFileName)) {
            return "emojis/emoji-sprite.png";
        }
        
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return String.format("emojis/emoji_%s%s", uuid, extension);
    }

    /**
     * 生成文件访问URL
     */
    private String generateFileUrl(String fileName) {
        return String.format("%s/%s/%s", 
            minioConfig.getEndpoint(), 
            minioConfig.getBucketName(), 
            fileName);
    }
    
    @Override
    public InputStream getFileStream(String fileName) throws Exception {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .build()
            );
        } catch (Exception e) {
            log.error("获取文件流失败: {}, 错误: {}", fileName, e.getMessage());
            throw new Exception("获取文件流失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> previewFile(String fileUrl, String fileType) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从URL中提取文件名（MinIO中的完整路径）
            String fileName = extractFilePathFromUrl(fileUrl);
            
            // 获取文件流
            InputStream inputStream = getFileStream(fileName);
            
            if (fileType != null && fileType.toLowerCase().startsWith("text/")) {
                // 文本文件预览
                result = previewTextFile(inputStream);
            } else if (fileName.toLowerCase().endsWith(".zip")) {
                // ZIP文件预览
                result = previewZipFile(inputStream);
            } else if (fileName.toLowerCase().endsWith(".rar")) {
                // RAR文件（暂不支持内容预览，只显示基本信息）
                result.put("type", "unsupported");
                result.put("message", "RAR文件预览暂不支持，请下载后查看");
            } else {
                // 其他文件类型
                result.put("type", "unsupported");
                result.put("message", "该文件类型不支持在线预览，请下载后查看");
            }
            
            inputStream.close();
            
        } catch (Exception e) {
            log.error("预览文件失败: {}, 错误: {}", fileUrl, e.getMessage());
            throw new Exception("预览文件失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 预览文本文件
     */
    private Map<String, Object> previewTextFile(InputStream inputStream) throws Exception {
        Map<String, Object> result = new HashMap<>();
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineCount = 0;
            int maxLines = 100; // 最多预览100行
            
            while ((line = reader.readLine()) != null && lineCount < maxLines) {
                content.append(line).append("\n");
                lineCount++;
            }
            
            result.put("type", "text");
            result.put("content", content.toString());
            result.put("lineCount", lineCount);
            result.put("isComplete", lineCount < maxLines);
            
        } catch (Exception e) {
            throw new Exception("读取文本文件失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 预览ZIP文件内容
     */
    private Map<String, Object> previewZipFile(InputStream inputStream) throws Exception {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> entries = new ArrayList<>();
        
        try (ZipInputStream zipStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            int entryCount = 0;
            int maxEntries = 50; // 最多显示50个文件
            
            while ((entry = zipStream.getNextEntry()) != null && entryCount < maxEntries) {
                Map<String, Object> entryInfo = new HashMap<>();
                entryInfo.put("name", entry.getName());
                entryInfo.put("size", entry.getSize());
                entryInfo.put("compressedSize", entry.getCompressedSize());
                entryInfo.put("isDirectory", entry.isDirectory());
                entryInfo.put("lastModified", entry.getTime());
                
                // 判断文件类型，添加是否可预览标识
                if (!entry.isDirectory()) {
                    String fileName = entry.getName().toLowerCase();
                    boolean canPreview = isFilePreviewable(fileName);
                    entryInfo.put("canPreview", canPreview);
                    entryInfo.put("fileType", getFileTypeFromName(fileName));
                }
                
                entries.add(entryInfo);
                entryCount++;
                
                zipStream.closeEntry();
            }
            
            result.put("type", "zip");
            result.put("entries", entries);
            result.put("totalEntries", entryCount);
            result.put("isComplete", entryCount < maxEntries);
            
        } catch (Exception e) {
            throw new Exception("读取ZIP文件失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 从ZIP文件中提取特定文件内容
     */
    public Map<String, Object> extractFileFromZip(String fileUrl, String entryPath) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取ZIP文件流
            String fileName = extractFilePathFromUrl(fileUrl);
            InputStream zipInputStream = getFileStream(fileName);
            
            try (ZipInputStream zipStream = new ZipInputStream(zipInputStream)) {
                ZipEntry entry;
                
                while ((entry = zipStream.getNextEntry()) != null) {
                    if (entry.getName().equals(entryPath) && !entry.isDirectory()) {
                        // 找到目标文件
                        String entryFileName = entry.getName().toLowerCase();
                        
                        if (isTextFile(entryFileName)) {
                            // 预览文本文件
                            result = previewTextFileFromStream(zipStream, entry.getSize());
                            result.put("fileName", entry.getName());
                            result.put("fileType", "text");
                        } else if (isImageFile(entryFileName)) {
                            // 图片文件
                            result.put("type", "image");
                            result.put("fileName", entry.getName());
                            result.put("message", "ZIP中的图片文件暂不支持在线预览，请下载压缩包查看");
                        } else {
                            // 其他文件类型
                            result.put("type", "unsupported");
                            result.put("fileName", entry.getName());
                            result.put("message", "该文件类型不支持在线预览");
                        }
                        
                        zipStream.closeEntry();
                        return result;
                    }
                    zipStream.closeEntry();
                }
                
                // 文件未找到
                result.put("type", "error");
                result.put("message", "在压缩包中未找到指定文件: " + entryPath);
                
            }
            
        } catch (Exception e) {
            log.error("从ZIP文件中提取文件失败: {}, 文件: {}, 错误: {}", fileUrl, entryPath, e.getMessage());
            throw new Exception("提取文件失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 判断文件是否可预览
     */
    private boolean isFilePreviewable(String fileName) {
        return isTextFile(fileName) || isImageFile(fileName);
    }
    
    /**
     * 判断是否为文本文件
     */
    private boolean isTextFile(String fileName) {
        String[] textExtensions = {".txt", ".md", ".json", ".xml", ".html", ".css", ".js", ".ts", 
                                   ".vue", ".jsx", ".tsx", ".py", ".java", ".cpp", ".c", ".h", 
                                   ".yml", ".yaml", ".properties", ".log", ".sql", ".sh", ".bat"};
        
        for (String ext : textExtensions) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String fileName) {
        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg", ".ico"};
        
        for (String ext : imageExtensions) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 根据文件名获取文件类型
     */
    private String getFileTypeFromName(String fileName) {
        if (isTextFile(fileName)) {
            return "text";
        } else if (isImageFile(fileName)) {
            return "image";
        } else if (fileName.endsWith(".pdf")) {
            return "pdf";
        } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            return "word";
        } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            return "excel";
        } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
            return "powerpoint";
        } else {
            return "other";
        }
    }
    
    /**
     * 从流中预览文本文件（限制大小）
     */
    private Map<String, Object> previewTextFileFromStream(InputStream inputStream, long fileSize) throws Exception {
        Map<String, Object> result = new HashMap<>();
        StringBuilder content = new StringBuilder();
        
        // 限制读取大小，避免内存溢出
        long maxSize = 1024 * 1024; // 1MB
        boolean isComplete = fileSize <= maxSize;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineCount = 0;
            int maxLines = 100; // 最多预览100行
            long bytesRead = 0;
            
            while ((line = reader.readLine()) != null && lineCount < maxLines && bytesRead < maxSize) {
                content.append(line).append("\n");
                lineCount++;
                bytesRead += line.getBytes(StandardCharsets.UTF_8).length + 1; // +1 for newline
            }
            
            result.put("type", "text");
            result.put("content", content.toString());
            result.put("lineCount", lineCount);
            result.put("isComplete", isComplete && lineCount < maxLines);
            result.put("fileSize", fileSize);
            
        } catch (Exception e) {
            throw new Exception("读取文本文件失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 从URL中提取MinIO中的文件路径
     */
    public String extractFilePathFromUrl(String fileUrl) {
        try {
            log.info("开始解析文件URL: {}", fileUrl);
            
            // URL格式: http://113.45.161.48:9000/admin-system/chat-files/20250801/filename.ext
            // 需要提取: chat-files/20250801/filename.ext
            
            if (fileUrl.contains(minioConfig.getBucketName() + "/")) {
                // 找到桶名称后的部分
                String[] parts = fileUrl.split(minioConfig.getBucketName() + "/");
                if (parts.length > 1) {
                    String extractedPath = parts[1];
                    log.info("成功提取文件路径（方法1）: {} -> {}", fileUrl, extractedPath);
                    return extractedPath; // 返回桶名称后的完整路径
                }
            }
            
            // 降级处理：如果无法正确解析，尝试从最后三个路径段组合
            String[] urlParts = fileUrl.split("/");
            if (urlParts.length >= 3) {
                // 取最后三段：chat-files/20250801/filename.ext
                int startIndex = Math.max(0, urlParts.length - 3);
                StringBuilder path = new StringBuilder();
                for (int i = startIndex; i < urlParts.length; i++) {
                    if (i > startIndex) {
                        path.append("/");
                    }
                    path.append(urlParts[i]);
                }
                String extractedPath = path.toString();
                log.info("使用降级方法提取文件路径（方法2）: {} -> {}", fileUrl, extractedPath);
                return extractedPath;
            }
            
            // 最后的降级：只取文件名
            String fileName = urlParts[urlParts.length - 1];
            log.warn("只能提取文件名（方法3）: {} -> {}", fileUrl, fileName);
            return fileName;
            
        } catch (Exception e) {
            log.error("解析文件URL失败: {}, 错误: {}", fileUrl, e.getMessage());
            // 如果解析失败，返回最后一个路径段
            String[] parts = fileUrl.split("/");
            String fileName = parts[parts.length - 1];
            log.warn("使用最终降级方法: {} -> {}", fileUrl, fileName);
            return fileName;
        }
    }
} 