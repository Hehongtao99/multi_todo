package com.todo.service;

import com.todo.dto.TodoCreateDto;
import com.todo.dto.TodoUpdateDto;
import com.todo.dto.TodoQueryDto;
import com.todo.dto.TodoDeleteDto;
import com.todo.dto.TodoStatusUpdateDto;
import com.todo.vo.TodoVo;
import com.todo.vo.OperationResultVo;

import java.util.List;

public interface TodoService {
    
    /**
     * 创建待办事项
     */
    TodoVo createTodo(TodoCreateDto todoCreateDto);
    
    /**
     * 更新待办事项
     */
    TodoVo updateTodo(TodoUpdateDto todoUpdateDto);
    
    /**
     * 删除待办事项
     */
    OperationResultVo deleteTodo(TodoDeleteDto todoDeleteDto);
    
    /**
     * 获取待办事项列表
     */
    List<TodoVo> getTodoList(TodoQueryDto queryDto);
    
    /**
     * 获取待办详情
     */
    TodoVo getTodoDetail(TodoQueryDto queryDto);

    /**
     * 更新待办状态
     */
    TodoVo updateTodoStatus(TodoStatusUpdateDto statusUpdateDto);
}
