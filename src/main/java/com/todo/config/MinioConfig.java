package com.todo.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {
    
    /**
     * MinIO服务器地址
     */
    private String endpoint = "http://113.45.161.48:9000";
    
    /**
     * 访问密钥
     */
    private String accessKey = "hht5002342003";
    
    /**
     * 秘密密钥
     */
    private String secretKey = "hht5002342003";
    
    /**
     * 桶名称
     */
    private String bucketName = "admin-system";
    
    /**
     * 创建MinIO客户端
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
} 