package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.todo.dto.ProjectAssignDto;
import com.todo.dto.ProjectCreateDto;
import com.todo.entity.Project;
import com.todo.entity.ProjectUser;
import com.todo.mapper.ProjectMapper;
import com.todo.mapper.ProjectUserMapper;
import com.todo.mapper.TodoMapper;
import com.todo.service.ProjectService;
import com.todo.vo.ProjectDetailVo;
import com.todo.vo.ProjectVo;
import com.todo.vo.TodoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectUserMapper projectUserMapper;

    @Autowired
    private TodoMapper todoMapper;
    
    @Override
    public Project createProject(ProjectCreateDto projectCreateDto, Long creatorId) {
        Project project = new Project();
        project.setProjectName(projectCreateDto.getProjectName());
        project.setProjectDescription(projectCreateDto.getProjectDescription());
        project.setCreatorId(creatorId);
        
        projectMapper.insert(project);
        return project;
    }
    
    @Override
    public List<ProjectVo> getAllProjects() {
        return projectMapper.selectAllProjectsWithUserNames();
    }
    
    @Override
    public List<ProjectVo> getProjectsByAssignee(Long assigneeId) {
        return projectMapper.selectProjectsByAssigneeWithUserNames(assigneeId);
    }
    
    @Override
    @Transactional
    public Project assignProject(ProjectAssignDto projectAssignDto) {
        Project project = projectMapper.selectById(projectAssignDto.getProjectId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 先删除该项目的所有用户分配
        QueryWrapper<ProjectUser> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("project_id", projectAssignDto.getProjectId());
        projectUserMapper.delete(deleteWrapper);
        
        // 重新分配用户
        if (projectAssignDto.getUserIds() != null && !projectAssignDto.getUserIds().isEmpty()) {
            for (Long userId : projectAssignDto.getUserIds()) {
                ProjectUser projectUser = new ProjectUser();
                projectUser.setProjectId(projectAssignDto.getProjectId());
                projectUser.setUserId(userId);
                projectUserMapper.insert(projectUser);
            }
        }
        
        return project;
    }

    @Override
    public ProjectDetailVo getProjectDetail(Long projectId) {
        // 获取项目基本信息
        ProjectDetailVo projectDetail = projectMapper.selectProjectDetailById(projectId);
        if (projectDetail == null) {
            throw new RuntimeException("项目不存在");
        }

        // 获取分配的用户列表
        List<ProjectDetailVo.AssignedUser> assignedUsers = projectMapper.selectAssignedUsersByProjectId(projectId);
        projectDetail.setAssignedUsers(assignedUsers);

        // 获取项目的待办事项列表
        List<TodoVo> todos = todoMapper.getTodosByProjectId(projectId);
        projectDetail.setTodos(todos);

        return projectDetail;
    }
}