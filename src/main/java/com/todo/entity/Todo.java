package com.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("todos")
public class Todo {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String description;
    
    private String status; // pending, in_progress, completed
    
    private String priority; // low, medium, high
    
    private Long projectId;
    
    private Long assigneeId;
    
    private Long creatorId;
    
    private LocalDateTime dueDate;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
