package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.*;
import com.todo.service.TodoService;
import com.todo.vo.TodoVo;
import com.todo.vo.OperationResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todo")
@CrossOrigin(originPatterns = "*")
public class TodoController {
    
    @Autowired
    private TodoService todoService;
    
    /**
     * 创建待办事项
     */
    @PostMapping("/create")
    public Result<TodoVo> createTodo(@RequestBody TodoCreateDto todoCreateDto) {
        try {
            TodoVo todoVo = todoService.createTodo(todoCreateDto);
            return Result.success("待办事项创建成功", todoVo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新待办事项
     */
    @PutMapping("/update")
    public Result<TodoVo> updateTodo(@RequestBody TodoUpdateDto todoUpdateDto) {
        try {
            TodoVo todoVo = todoService.updateTodo(todoUpdateDto);
            return Result.success("待办事项更新成功", todoVo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除待办事项
     */
    @DeleteMapping("/delete")
    public Result<OperationResultVo> deleteTodo(@RequestBody TodoDeleteDto todoDeleteDto) {
        try {
            OperationResultVo result = todoService.deleteTodo(todoDeleteDto);
            return Result.success("待办事项删除成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取待办事项列表
     */
    @PostMapping("/list")
    public Result<List<TodoVo>> getTodoList(@RequestBody TodoQueryDto queryDto) {
        try {
            List<TodoVo> todos = todoService.getTodoList(queryDto);
            return Result.success(todos);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取待办详情
     */
    @PostMapping("/detail")
    public Result<TodoVo> getTodoDetail(@RequestBody TodoQueryDto queryDto) {
        try {
            TodoVo todo = todoService.getTodoDetail(queryDto);
            return Result.success(todo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新待办状态
     */
    @PutMapping("/status")
    public Result<TodoVo> updateTodoStatus(@RequestBody TodoStatusUpdateDto statusUpdateDto) {
        try {
            TodoVo todoVo = todoService.updateTodoStatus(statusUpdateDto);
            return Result.success("状态更新成功", todoVo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 以下为前端兼容的GET接口 ==========

    /**
     * 根据项目ID获取待办列表 (GET接口)
     */
    @GetMapping("/project/{projectId}")
    public Result<List<TodoVo>> getTodosByProjectId(@PathVariable Long projectId) {
        try {
            TodoQueryDto queryDto = new TodoQueryDto();
            queryDto.setProjectId(projectId);
            queryDto.setUserAuth("admin"); // 临时设置，实际应该从token中解析
            List<TodoVo> todos = todoService.getTodoList(queryDto);
            return Result.success(todos);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据分配人ID获取待办列表 (GET接口)
     */
    @GetMapping("/assignee/{assigneeId}")
    public Result<List<TodoVo>> getTodosByAssigneeId(@PathVariable Long assigneeId) {
        try {
            TodoQueryDto queryDto = new TodoQueryDto();
            queryDto.setAssigneeId(assigneeId);
            queryDto.setUserAuth("admin"); // 临时设置，实际应该从token中解析
            List<TodoVo> todos = todoService.getTodoList(queryDto);
            return Result.success(todos);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取待办详情 (GET接口)
     */
    @GetMapping("/{todoId}")
    public Result<TodoVo> getTodoDetail(@PathVariable Long todoId) {
        try {
            TodoQueryDto queryDto = new TodoQueryDto();
            queryDto.setTodoId(todoId);
            queryDto.setUserAuth("admin"); // 临时设置，实际应该从token中解析
            TodoVo todo = todoService.getTodoDetail(queryDto);
            return Result.success(todo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除待办事项 (GET接口)
     */
    @DeleteMapping("/{todoId}")
    public Result<OperationResultVo> deleteTodoById(@PathVariable Long todoId) {
        try {
            TodoDeleteDto deleteDto = new TodoDeleteDto();
            deleteDto.setTodoId(todoId);
            deleteDto.setUserAuth("admin"); // 临时设置，实际应该从token中解析
            OperationResultVo result = todoService.deleteTodo(deleteDto);
            return Result.success("待办事项删除成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新待办状态 (GET接口)
     */
    @PutMapping("/{todoId}/status")
    public Result<TodoVo> updateTodoStatusById(
            @PathVariable Long todoId,
            @RequestParam String status,
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestHeader(value = "userAuth", required = false) String userAuth) {
        try {
            TodoStatusUpdateDto statusUpdateDto = new TodoStatusUpdateDto();
            statusUpdateDto.setTodoId(todoId);
            statusUpdateDto.setStatus(status);
            statusUpdateDto.setUserId(userId);
            statusUpdateDto.setUserAuth(userAuth != null ? userAuth : "admin"); // 默认admin权限
            TodoVo todoVo = todoService.updateTodoStatus(statusUpdateDto);
            return Result.success("状态更新成功", todoVo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
