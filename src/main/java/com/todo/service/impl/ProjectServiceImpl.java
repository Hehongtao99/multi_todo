package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.todo.dto.NotificationCreateDto;
import com.todo.dto.ProjectAssignRequestDto;
import com.todo.dto.ProjectCreateRequestDto;
import com.todo.dto.ProjectDetailQueryDto;
import com.todo.dto.ProjectListQueryDto;
import com.todo.entity.Project;
import com.todo.entity.ProjectUser;
import com.todo.entity.User;
import com.todo.mapper.ProjectMapper;
import com.todo.mapper.ProjectUserMapper;
import com.todo.mapper.TodoMapper;
import com.todo.mapper.UserMapper;
import com.todo.service.NotificationService;
import com.todo.service.ProjectService;
import com.todo.vo.OperationResultVo;
import com.todo.vo.ProjectDetailVo;
import com.todo.vo.ProjectVo;
import com.todo.vo.TodoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectUserMapper projectUserMapper;

    @Autowired
    private TodoMapper todoMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public ProjectVo createProject(ProjectCreateRequestDto projectCreateDto) {
        // 权限验证：检查是否有权限创建项目
        if (!"admin".equals(projectCreateDto.getUserAuth())) {
            throw new RuntimeException("权限不足，只有管理员可以创建项目");
        }
        
        Project project = new Project();
        project.setProjectName(projectCreateDto.getProjectName());
        project.setProjectDescription(projectCreateDto.getProjectDescription());
        project.setCreatorId(projectCreateDto.getUserId());
        project.setCreatedTime(LocalDateTime.now());
        project.setUpdatedTime(LocalDateTime.now());
        
        int result = projectMapper.insert(project);
        
        if (result <= 0) {
            throw new RuntimeException("项目创建失败");
        }
        
        // 转换为VO对象
        return convertToProjectVo(project);
    }
    
    @Override
    public List<ProjectVo> getProjectList(ProjectListQueryDto queryDto) {
        // 根据用户权限返回不同的项目列表
        if ("admin".equals(queryDto.getUserAuth())) {
            // 管理员可以查看所有项目
            return projectMapper.selectAllProjectsWithUserNames();
        } else {
            // 普通用户只能查看分配给自己的项目
            return projectMapper.selectProjectsByAssigneeWithUserNames(queryDto.getUserId());
        }
    }
    
    @Override
    @Transactional
    public OperationResultVo assignProject(ProjectAssignRequestDto assignDto) {
        // 权限验证：检查是否有权限分配项目
        if (!"admin".equals(assignDto.getUserAuth())) {
            throw new RuntimeException("权限不足，只有管理员可以分配项目");
        }
        
        Project project = projectMapper.selectById(assignDto.getProjectId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 先删除该项目的所有用户分配
        QueryWrapper<ProjectUser> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("project_id", assignDto.getProjectId());
        projectUserMapper.delete(deleteWrapper);
        
        // 重新分配用户
        if (assignDto.getUserIds() != null && !assignDto.getUserIds().isEmpty()) {
            for (Long userId : assignDto.getUserIds()) {
                ProjectUser projectUser = new ProjectUser();
                projectUser.setProjectId(assignDto.getProjectId());
                projectUser.setUserId(userId);
                projectUserMapper.insert(projectUser);
            }
            
            // 发送项目分配通知给所有被分配的用户
            sendProjectAssignmentNotifications(project, assignDto.getUserIds(), assignDto.getUserId());
        }
        
        // 返回操作结果
        OperationResultVo resultVo = new OperationResultVo();
        resultVo.setSuccess(true);
        resultVo.setMessage("项目分配成功");
        resultVo.setData(assignDto.getProjectId());
        
        return resultVo;
    }

    @Override
    public ProjectDetailVo getProjectDetail(ProjectDetailQueryDto queryDto) {
        // 权限验证：检查是否有权限查看项目详情
        if ("admin".equals(queryDto.getUserAuth())) {
            // 管理员可以查看所有项目详情
        } else {
            // 普通用户只能查看分配给自己的项目详情
            List<ProjectVo> userProjects = projectMapper.selectProjectsByAssigneeWithUserNames(queryDto.getUserId());
            boolean hasAccess = userProjects.stream()
                    .anyMatch(p -> p.getId().equals(queryDto.getProjectId()));
            
            if (!hasAccess) {
                throw new RuntimeException("权限不足，您无法查看此项目详情");
            }
        }
        
        // 获取项目基本信息
        ProjectDetailVo projectDetail = projectMapper.selectProjectDetailById(queryDto.getProjectId());
        if (projectDetail == null) {
            throw new RuntimeException("项目不存在");
        }

        // 获取分配的用户列表
        List<ProjectDetailVo.AssignedUser> assignedUsers = projectMapper.selectAssignedUsersByProjectId(queryDto.getProjectId());
        projectDetail.setAssignedUsers(assignedUsers);

        // 获取项目的待办事项列表
        List<TodoVo> todos = todoMapper.getTodosByProjectId(queryDto.getProjectId());
        projectDetail.setTodos(todos);

        return projectDetail;
    }
    
    /**
     * 将Project实体转换为ProjectVo
     */
    private ProjectVo convertToProjectVo(Project project) {
        ProjectVo projectVo = new ProjectVo();
        BeanUtils.copyProperties(project, projectVo);
        return projectVo;
    }
    
    /**
     * 发送项目分配通知
     */
    private void sendProjectAssignmentNotifications(Project project, List<Long> userIds, Long adminId) {
        try {
            // 获取管理员信息
            User admin = userMapper.selectById(adminId);
            String adminName = admin != null ? admin.getUsername() : "管理员";
            
            // 为每个被分配的用户发送个人通知
            for (Long userId : userIds) {
                User user = userMapper.selectById(userId);
                if (user != null) {
                    NotificationCreateDto notificationDto = new NotificationCreateDto();
                    notificationDto.setTitle("项目分配通知");
                    notificationDto.setContent(String.format("您已被分配到项目：%s。请及时查看项目详情并开始工作。", project.getProjectName()));
                    notificationDto.setType("personal");
                    notificationDto.setPriority("high");
                    notificationDto.setReceiverId(userId);
                    notificationDto.setProjectId(project.getId());
                    notificationDto.setPushImmediately(true);
                    
                    // 发送通知
                    notificationService.createPersonalNotification(notificationDto, adminId, adminName);
                    
                    log.info("已为用户 {} 发送项目分配通知，项目：{}", user.getUsername(), project.getProjectName());
                }
            }
            
        } catch (Exception e) {
            log.error("发送项目分配通知失败：{}", e.getMessage(), e);
        }
    }
}