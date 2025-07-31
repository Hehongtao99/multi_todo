package com.todo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectDetailVo {
    
    private Long id;
    
    private String projectName;
    
    private String projectDescription;
    
    private Long creatorId;
    
    private String creatorName;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
    
    private List<AssignedUser> assignedUsers;
    
    private List<TodoVo> todos;
    
    @Data
    public static class AssignedUser {
        private Long userId;
        private String username;
        private LocalDateTime assignedTime;
    }
}
