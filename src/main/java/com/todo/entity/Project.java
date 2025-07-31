package com.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("projects")
public class Project {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String projectName;
    
    private String projectDescription;
    
    private Long creatorId;
    

    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
} 