package com.todo.service;

import com.todo.dto.ProjectAssignRequestDto;
import com.todo.dto.ProjectCreateRequestDto;
import com.todo.dto.ProjectListQueryDto;
import com.todo.dto.ProjectDetailQueryDto;
import com.todo.vo.ProjectDetailVo;
import com.todo.vo.ProjectVo;
import com.todo.vo.OperationResultVo;

import java.util.List;

public interface ProjectService {

    /**
     * 创建项目
     */
    ProjectVo createProject(ProjectCreateRequestDto projectCreateDto);

    /**
     * 获取项目列表（根据用户权限返回不同数据）
     */
    List<ProjectVo> getProjectList(ProjectListQueryDto queryDto);

    /**
     * 分配项目
     */
    OperationResultVo assignProject(ProjectAssignRequestDto assignDto);

    /**
     * 获取项目详情
     */
    ProjectDetailVo getProjectDetail(ProjectDetailQueryDto queryDto);
}