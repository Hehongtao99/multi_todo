package com.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.todo.entity.Project;
import com.todo.vo.ProjectVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
    
    /**
     * 获取所有项目（带用户名）
     */
    List<ProjectVo> selectAllProjectsWithUserNames();
    
    /**
     * 根据分配人ID获取项目（带用户名）
     */
    List<ProjectVo> selectProjectsByAssigneeWithUserNames(@Param("assigneeId") Long assigneeId);
} 