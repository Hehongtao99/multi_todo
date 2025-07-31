package com.todo.service;

import com.todo.dto.TodoCreateDto;
import com.todo.dto.TodoUpdateDto;
import com.todo.entity.Todo;
import com.todo.vo.TodoVo;

import java.util.List;

public interface TodoService {
    
    /**
     * 创建待办事项
     */
    Todo createTodo(TodoCreateDto todoCreateDto, Long creatorId);
    
    /**
     * 更新待办事项
     */
    Todo updateTodo(TodoUpdateDto todoUpdateDto);
    
    /**
     * 删除待办事项
     */
    void deleteTodo(Long todoId);
    
    /**
     * 根据项目ID获取待办列表
     */
    List<TodoVo> getTodosByProjectId(Long projectId);
    
    /**
     * 根据分配人ID获取待办列表
     */
    List<TodoVo> getTodosByAssigneeId(Long assigneeId);
    
    /**
     * 获取待办详情
     */
    TodoVo getTodoDetail(Long todoId);
    
    /**
     * 更新待办状态
     */
    Todo updateTodoStatus(Long todoId, String status);
}
