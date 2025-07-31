package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.todo.dto.TodoCreateDto;
import com.todo.dto.TodoUpdateDto;
import com.todo.dto.TodoQueryDto;
import com.todo.dto.TodoDeleteDto;
import com.todo.dto.TodoStatusUpdateDto;
import com.todo.dto.NotificationCreateDto;
import com.todo.entity.Todo;
import com.todo.entity.User;
import com.todo.entity.Project;
import com.todo.mapper.TodoMapper;
import com.todo.mapper.UserMapper;
import com.todo.mapper.ProjectMapper;
import com.todo.service.TodoService;
import com.todo.service.NotificationService;
import com.todo.vo.TodoVo;
import com.todo.vo.OperationResultVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TodoServiceImpl implements TodoService {
    
    @Autowired
    private TodoMapper todoMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public TodoVo createTodo(TodoCreateDto todoCreateDto) {
        // 权限验证：检查是否有权限创建待办事项
        if (!"admin".equals(todoCreateDto.getUserAuth())) {
            throw new RuntimeException("权限不足，只有管理员可以创建待办事项");
        }
        
        Todo todo = new Todo();
        BeanUtils.copyProperties(todoCreateDto, todo);
        todo.setCreatorId(todoCreateDto.getUserId());
        todo.setStatus("pending"); // 默认状态为待处理
        todo.setCreatedTime(LocalDateTime.now());
        todo.setUpdatedTime(LocalDateTime.now());
        
        int result = todoMapper.insert(todo);
        
        if (result <= 0) {
            throw new RuntimeException("待办事项创建失败");
        }
        
        // 发送待办创建通知
        sendTodoCreationNotification(todo, todoCreateDto.getUserId());
        
        // 转换为VO对象
        return convertToTodoVo(todo);
    }
    
    @Override
    public TodoVo updateTodo(TodoUpdateDto todoUpdateDto) {
        // 权限验证：检查是否有权限更新待办事项
        if (!"admin".equals(todoUpdateDto.getUserAuth())) {
            throw new RuntimeException("权限不足，只有管理员可以更新待办事项");
        }
        
        Todo existingTodo = todoMapper.selectById(todoUpdateDto.getId());
        if (existingTodo == null) {
            throw new RuntimeException("待办事项不存在");
        }
        
        Todo todo = new Todo();
        BeanUtils.copyProperties(todoUpdateDto, todo);
        todo.setUpdatedTime(LocalDateTime.now());
        
        int result = todoMapper.updateById(todo);
        
        if (result <= 0) {
            throw new RuntimeException("待办事项更新失败");
        }
        
        // 重新查询并转换为VO对象
        Todo updatedTodo = todoMapper.selectById(todo.getId());
        return convertToTodoVo(updatedTodo);
    }
    
    @Override
    public OperationResultVo deleteTodo(TodoDeleteDto todoDeleteDto) {
        // 权限验证：检查是否有权限删除待办事项
        if (!"admin".equals(todoDeleteDto.getUserAuth())) {
            throw new RuntimeException("权限不足，只有管理员可以删除待办事项");
        }
        
        Todo existingTodo = todoMapper.selectById(todoDeleteDto.getTodoId());
        if (existingTodo == null) {
            throw new RuntimeException("待办事项不存在");
        }
        
        int result = todoMapper.deleteById(todoDeleteDto.getTodoId());
        
        OperationResultVo resultVo = new OperationResultVo();
        if (result > 0) {
            resultVo.setSuccess(true);
            resultVo.setMessage("待办事项删除成功");
            resultVo.setData(todoDeleteDto.getTodoId());
        } else {
            resultVo.setSuccess(false);
            resultVo.setMessage("待办事项删除失败");
        }
        
        return resultVo;
    }
    
    @Override
    public List<TodoVo> getTodoList(TodoQueryDto queryDto) {
        // 根据查询条件返回不同的待办事项列表
        if (queryDto.getProjectId() != null) {
            // 根据项目ID查询
            return todoMapper.getTodosByProjectId(queryDto.getProjectId());
        } else if (queryDto.getAssigneeId() != null) {
            // 根据分配人ID查询
            return todoMapper.getTodosByAssigneeId(queryDto.getAssigneeId());
        } else if ("admin".equals(queryDto.getUserAuth())) {
            // 管理员可以查看所有待办事项
            QueryWrapper<Todo> queryWrapper = new QueryWrapper<>();
            if (queryDto.getStatus() != null) {
                queryWrapper.eq("status", queryDto.getStatus());
            }
            List<Todo> todos = todoMapper.selectList(queryWrapper);
            return todos.stream()
                       .map(this::convertToTodoVo)
                       .collect(java.util.stream.Collectors.toList());
        } else {
            // 普通用户只能查看分配给自己的待办事项
            return todoMapper.getTodosByAssigneeId(queryDto.getUserId());
        }
    }
    
    @Override
    public TodoVo getTodoDetail(TodoQueryDto queryDto) {
        if (queryDto.getTodoId() == null) {
            throw new RuntimeException("待办事项ID不能为空");
        }
        
        TodoVo todoVo = todoMapper.getTodoDetail(queryDto.getTodoId());
        
        if (todoVo == null) {
            throw new RuntimeException("待办事项不存在");
        }
        
        // 权限验证：普通用户只能查看分配给自己的待办事项详情
        if (!"admin".equals(queryDto.getUserAuth()) && 
            !queryDto.getUserId().equals(todoVo.getAssigneeId())) {
            throw new RuntimeException("权限不足，您无法查看此待办事项详情");
        }
        
        return todoVo;
    }
    
    @Override
    public TodoVo updateTodoStatus(TodoStatusUpdateDto statusUpdateDto) {
        if (statusUpdateDto.getTodoId() == null) {
            throw new RuntimeException("待办事项ID不能为空");
        }
        
        if (statusUpdateDto.getStatus() == null || statusUpdateDto.getStatus().trim().isEmpty()) {
            throw new RuntimeException("状态不能为空");
        }
        
        Todo existingTodo = todoMapper.selectById(statusUpdateDto.getTodoId());
        if (existingTodo == null) {
            throw new RuntimeException("待办事项不存在");
        }
        
        // 权限验证：管理员可以更新任何待办事项状态，普通用户只能更新分配给自己的待办事项状态
        if (!"admin".equals(statusUpdateDto.getUserAuth()) && 
            !statusUpdateDto.getUserId().equals(existingTodo.getAssigneeId())) {
            throw new RuntimeException("权限不足，您无法修改此待办事项的状态");
        }
        
        // 更新状态和修改时间
        Todo todo = new Todo();
        todo.setId(statusUpdateDto.getTodoId());
        todo.setStatus(statusUpdateDto.getStatus());
        todo.setUpdatedTime(LocalDateTime.now());
        
        int result = todoMapper.updateById(todo);
        
        if (result <= 0) {
            throw new RuntimeException("待办事项状态更新失败");
        }
        
        // 重新查询并转换为VO对象
        Todo updatedTodo = todoMapper.selectById(statusUpdateDto.getTodoId());
        return convertToTodoVo(updatedTodo);
    }
    


    /**
     * 将Todo实体转换为TodoVo
     */
    private TodoVo convertToTodoVo(Todo todo) {
        TodoVo todoVo = new TodoVo();
        BeanUtils.copyProperties(todo, todoVo);
        return todoVo;
    }
    
    /**
     * 发送待办创建通知
     */
    private void sendTodoCreationNotification(Todo todo, Long adminId) {
        try {
            // 获取管理员信息
            User admin = userMapper.selectById(adminId);
            String adminName = admin != null ? admin.getUsername() : "管理员";
            
            // 如果待办有分配人，发送个人通知
            if (todo.getAssigneeId() != null) {
                User assignee = userMapper.selectById(todo.getAssigneeId());
                if (assignee != null) {
                    NotificationCreateDto notificationDto = new NotificationCreateDto();
                    notificationDto.setTitle("新待办事项分配");
                    notificationDto.setContent(String.format("您有新的待办事项：%s。优先级：%s，截止时间：%s", 
                        todo.getTitle(), 
                        todo.getPriority() != null ? todo.getPriority() : "普通",
                        todo.getDueDate() != null ? todo.getDueDate().toString() : "未设置"));
                    notificationDto.setType("personal");
                    notificationDto.setPriority("high");
                    notificationDto.setReceiverId(todo.getAssigneeId());
                    notificationDto.setProjectId(todo.getProjectId());
                    notificationDto.setPushImmediately(true);
                    
                    // 发送个人通知
                    notificationService.createPersonalNotification(notificationDto, adminId, adminName);
                    
                    log.info("已为用户 {} 发送待办创建通知，待办：{}", assignee.getUsername(), todo.getTitle());
                }
            }
            
            // 如果待办属于某个项目，发送项目通知给项目成员
            if (todo.getProjectId() != null) {
                Project project = projectMapper.selectById(todo.getProjectId());
                if (project != null) {
                    NotificationCreateDto projectNotificationDto = new NotificationCreateDto();
                    projectNotificationDto.setTitle("项目新待办事项");
                    projectNotificationDto.setContent(String.format("项目 %s 有新的待办事项：%s", 
                        project.getProjectName(), todo.getTitle()));
                    projectNotificationDto.setType("project");
                    projectNotificationDto.setPriority("normal");
                    projectNotificationDto.setProjectId(todo.getProjectId());
                    projectNotificationDto.setPushImmediately(true);
                    
                    // 发送项目通知
                    notificationService.createProjectNotification(projectNotificationDto, adminId, adminName);
                    
                    log.info("已为项目 {} 发送待办创建通知，待办：{}", project.getProjectName(), todo.getTitle());
                }
            }
            
        } catch (Exception e) {
            log.error("发送待办创建通知失败：{}", e.getMessage(), e);
        }
    }
}
