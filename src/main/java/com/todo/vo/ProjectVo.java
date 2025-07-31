package com.todo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectVo {
    
    private Long id;
    
    private String projectName;
    
    private String projectDescription;
    
    private Long creatorId;
    
    private String creatorName;
    
    private List<ProjectUserVo> assignedUsers;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
    
    @Data
    public static class ProjectUserVo {
        private Long userId;
        private String username;
        private LocalDateTime assignedTime;
    }
} 