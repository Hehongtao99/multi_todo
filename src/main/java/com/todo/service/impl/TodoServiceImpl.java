package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.todo.dto.TodoCreateDto;
import com.todo.dto.TodoUpdateDto;
import com.todo.entity.Todo;
import com.todo.mapper.TodoMapper;
import com.todo.service.TodoService;
import com.todo.vo.TodoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {
    
    @Autowired
    private TodoMapper todoMapper;
    
    @Override
    public Todo createTodo(TodoCreateDto todoCreateDto, Long creatorId) {
        Todo todo = new Todo();
        BeanUtils.copyProperties(todoCreateDto, todo);
        todo.setCreatorId(creatorId);
        todo.setStatus("pending"); // 默认状态为待处理
        todo.setCreatedTime(LocalDateTime.now());
        todo.setUpdatedTime(LocalDateTime.now());
        
        todoMapper.insert(todo);
        return todo;
    }
    
    @Override
    public Todo updateTodo(TodoUpdateDto todoUpdateDto) {
        Todo existingTodo = todoMapper.selectById(todoUpdateDto.getId());
        if (existingTodo == null) {
            throw new RuntimeException("待办事项不存在");
        }
        
        Todo todo = new Todo();
        BeanUtils.copyProperties(todoUpdateDto, todo);
        todo.setUpdatedTime(LocalDateTime.now());
        
        todoMapper.updateById(todo);
        return todoMapper.selectById(todo.getId());
    }
    
    @Override
    public void deleteTodo(Long todoId) {
        Todo existingTodo = todoMapper.selectById(todoId);
        if (existingTodo == null) {
            throw new RuntimeException("待办事项不存在");
        }
        
        todoMapper.deleteById(todoId);
    }
    
    @Override
    public List<TodoVo> getTodosByProjectId(Long projectId) {
        return todoMapper.getTodosByProjectId(projectId);
    }
    
    @Override
    public List<TodoVo> getTodosByAssigneeId(Long assigneeId) {
        return todoMapper.getTodosByAssigneeId(assigneeId);
    }
    
    @Override
    public TodoVo getTodoDetail(Long todoId) {
        return todoMapper.getTodoDetail(todoId);
    }
    
    @Override
    public Todo updateTodoStatus(Long todoId, String status) {
        Todo existingTodo = todoMapper.selectById(todoId);
        if (existingTodo == null) {
            throw new RuntimeException("待办事项不存在");
        }
        
        existingTodo.setStatus(status);
        existingTodo.setUpdatedTime(LocalDateTime.now());
        
        todoMapper.updateById(existingTodo);
        return existingTodo;
    }
}
