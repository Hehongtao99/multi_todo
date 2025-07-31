package com.todo.service;

import com.todo.dto.ProjectAssignDto;
import com.todo.dto.ProjectCreateDto;
import com.todo.entity.Project;
import com.todo.vo.ProjectDetailVo;
import com.todo.vo.ProjectVo;

import java.util.List;

public interface ProjectService {

    /**
     * 创建项目
     */
    Project createProject(ProjectCreateDto projectCreateDto, Long creatorId);

    /**
     * 获取所有项目（管理员）
     */
    List<ProjectVo> getAllProjects();

    /**
     * 根据分配人ID获取项目（普通用户）
     */
    List<ProjectVo> getProjectsByAssignee(Long assigneeId);

    /**
     * 分配项目
     */
    Project assignProject(ProjectAssignDto projectAssignDto);

    /**
     * 获取项目详情
     */
    ProjectDetailVo getProjectDetail(Long projectId);
}