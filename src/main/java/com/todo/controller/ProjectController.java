package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.ProjectAssignRequestDto;
import com.todo.dto.ProjectCreateRequestDto;
import com.todo.dto.ProjectDetailQueryDto;
import com.todo.dto.ProjectListQueryDto;
import com.todo.service.ProjectService;
import com.todo.vo.OperationResultVo;
import com.todo.vo.ProjectDetailVo;
import com.todo.vo.ProjectVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
@CrossOrigin(origins = "*")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    /**
     * 创建项目（仅管理员）
     */
    @PostMapping("/create")
    public Result<ProjectVo> createProject(@RequestBody ProjectCreateRequestDto requestDto) {
        ProjectVo result = projectService.createProject(requestDto);
        return Result.success("项目创建成功", result);
    }
    
    /**
     * 获取项目列表
     */
    @PostMapping("/list")
    public Result<List<ProjectVo>> getProjectList(@RequestBody ProjectListQueryDto queryDto) {
        List<ProjectVo> result = projectService.getProjectList(queryDto);
        return Result.success(result);
    }
    
    /**
     * 分配项目（仅管理员）
     */
    @PostMapping("/assign")
    public Result<OperationResultVo> assignProject(@RequestBody ProjectAssignRequestDto requestDto) {
        OperationResultVo result = projectService.assignProject(requestDto);
        return Result.success(result);
    }

    /**
     * 获取项目详情
     */
    @PostMapping("/detail")
    public Result<ProjectDetailVo> getProjectDetail(@RequestBody ProjectDetailQueryDto queryDto) {
        ProjectDetailVo result = projectService.getProjectDetail(queryDto);
        return Result.success(result);
    }
}