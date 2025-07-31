package com.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.todo.entity.Todo;
import com.todo.vo.TodoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TodoMapper extends BaseMapper<Todo> {
    
    /**
     * 根据项目ID获取待办列表（包含用户信息）
     */
    List<TodoVo> getTodosByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 根据分配人ID获取待办列表
     */
    List<TodoVo> getTodosByAssigneeId(@Param("assigneeId") Long assigneeId);
    
    /**
     * 获取待办详情（包含用户信息）
     */
    TodoVo getTodoDetail(@Param("todoId") Long todoId);
}
