package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.TodoCreateDto;
import com.todo.dto.TodoUpdateDto;
import com.todo.entity.Todo;
import com.todo.service.TodoService;
import com.todo.vo.TodoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todo")
@CrossOrigin(origins = "*")
public class TodoController {
    
    @Autowired
    private TodoService todoService;
    
    /**
     * 创建待办事项
     */
    @PostMapping("/create")
    public Result<Todo> createTodo(@RequestBody TodoCreateDto todoCreateDto,
                                  @RequestHeader("userId") Long userId) {
        try {
            Todo todo = todoService.createTodo(todoCreateDto, userId);
            return Result.success("待办事项创建成功", todo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新待办事项
     */
    @PutMapping("/update")
    public Result<Todo> updateTodo(@RequestBody TodoUpdateDto todoUpdateDto) {
        try {
            Todo todo = todoService.updateTodo(todoUpdateDto);
            return Result.success("待办事项更新成功", todo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除待办事项
     */
    @DeleteMapping("/{todoId}")
    public Result<Void> deleteTodo(@PathVariable Long todoId) {
        try {
            todoService.deleteTodo(todoId);
            return Result.success("待办事项删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据项目ID获取待办列表
     */
    @GetMapping("/project/{projectId}")
    public Result<List<TodoVo>> getTodosByProjectId(@PathVariable Long projectId) {
        try {
            List<TodoVo> todos = todoService.getTodosByProjectId(projectId);
            return Result.success(todos);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据分配人ID获取待办列表
     */
    @GetMapping("/assignee/{assigneeId}")
    public Result<List<TodoVo>> getTodosByAssigneeId(@PathVariable Long assigneeId) {
        try {
            List<TodoVo> todos = todoService.getTodosByAssigneeId(assigneeId);
            return Result.success(todos);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取待办详情
     */
    @GetMapping("/{todoId}")
    public Result<TodoVo> getTodoDetail(@PathVariable Long todoId) {
        try {
            TodoVo todo = todoService.getTodoDetail(todoId);
            return Result.success(todo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新待办状态
     */
    @PutMapping("/{todoId}/status")
    public Result<Todo> updateTodoStatus(@PathVariable Long todoId, 
                                        @RequestParam String status) {
        try {
            Todo todo = todoService.updateTodoStatus(todoId, status);
            return Result.success("状态更新成功", todo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
