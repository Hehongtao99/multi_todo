package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.ProjectAssignDto;
import com.todo.dto.ProjectCreateDto;
import com.todo.entity.Project;
import com.todo.service.ProjectService;
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
    public Result<Project> createProject(@RequestBody ProjectCreateDto projectCreateDto,
                                       @RequestHeader("userId") Long userId,
                                       @RequestHeader("userAuth") String userAuth) {
        try {
            // 权限验证：只有管理员可以创建项目
            if (!"admin".equals(userAuth)) {
                return Result.error(403, "权限不足");
            }
            
            Project project = projectService.createProject(projectCreateDto, userId);
            return Result.success("项目创建成功", project);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取项目列表
     */
    @GetMapping("/list")
    public Result<List<ProjectVo>> getProjectList(@RequestHeader("userId") Long userId,
                                                 @RequestHeader("userAuth") String userAuth) {
        try {
            List<ProjectVo> projectList;
            
            if ("admin".equals(userAuth)) {
                // 管理员可以看到所有项目
                projectList = projectService.getAllProjects();
            } else {
                // 普通用户只能看到分配给自己的项目
                projectList = projectService.getProjectsByAssignee(userId);
            }
            
            return Result.success(projectList);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 分配项目（仅管理员）
     */
    @PutMapping("/assign")
    public Result<Project> assignProject(@RequestBody ProjectAssignDto projectAssignDto,
                                       @RequestHeader("userAuth") String userAuth) {
        try {
            // 权限验证：只有管理员可以分配项目
            if (!"admin".equals(userAuth)) {
                return Result.error(403, "权限不足");
            }
            
            Project project = projectService.assignProject(projectAssignDto);
            return Result.success("项目分配成功", project);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/{projectId}/detail")
    public Result<ProjectDetailVo> getProjectDetail(@PathVariable Long projectId,
                                                   @RequestHeader("userId") Long userId,
                                                   @RequestHeader("userAuth") String userAuth) {
        try {
            ProjectDetailVo projectDetail = projectService.getProjectDetail(projectId);
            return Result.success(projectDetail);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}