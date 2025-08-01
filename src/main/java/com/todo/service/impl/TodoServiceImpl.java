package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.todo.dto.TodoCreateDto;
import com.todo.dto.TodoUpdateDto;
import com.todo.dto.TodoQueryDto;
import com.todo.dto.TodoDeleteDto;
import com.todo.dto.TodoStatusUpdateDto;
import com.todo.dto.AdminTodoUpdateDto;
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

/**
 * 待办事项服务实现类
 */
@Slf4j
@Service
public class TodoServiceImpl implements TodoService {
    
    @Autowired
    private TodoMapper todoMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private ProjectMapper projectMapper;
    
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
        
        // 设置默认开始时间和截止时间
        setDefaultTimes(todo, todoCreateDto);
        
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
        // 构建基础查询条件
        QueryWrapper<Todo> queryWrapper = new QueryWrapper<>();
        
        // 根据权限设置查询范围
        if (!"admin".equals(queryDto.getUserAuth())) {
            // 普通用户只能查看分配给自己的待办事项
            queryWrapper.eq("assignee_id", queryDto.getUserId());
        }
        
        // 项目ID过滤
        if (queryDto.getProjectId() != null) {
            queryWrapper.eq("project_id", queryDto.getProjectId());
        }
        
        // 分配人ID过滤（管理员可以指定查看特定用户的任务）
        if (queryDto.getAssigneeId() != null && "admin".equals(queryDto.getUserAuth())) {
            queryWrapper.eq("assignee_id", queryDto.getAssigneeId());
        }
        
        // 状态过滤
        if (queryDto.getStatus() != null) {
            queryWrapper.eq("status", queryDto.getStatus());
        }
        
        // 日期过滤逻辑
        addDateFilter(queryWrapper, queryDto);
        
        // 按开始时间排序
        queryWrapper.orderByAsc("start_time", "created_time");
        
        List<Todo> todos = todoMapper.selectList(queryWrapper);
        return todos.stream()
                   .map(this::convertToTodoVo)
                   .collect(java.util.stream.Collectors.toList());
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
        
        // 发送任务状态变更通知给管理员
        try {
            sendTaskStatusChangeNotification(updatedTodo, statusUpdateDto.getUserId());
        } catch (Exception e) {
            log.warn("发送任务状态变更通知失败: {}", e.getMessage());
        }
        
        return convertToTodoVo(updatedTodo);
    }
    
    @Override
    public TodoVo adminUpdateTodo(AdminTodoUpdateDto adminUpdateDto) {
        // 验证管理员权限
        User admin = userMapper.selectById(adminUpdateDto.getAdminId());
        if (admin == null || !"admin".equals(admin.getAuth())) {
            throw new RuntimeException("权限不足，只有管理员可以执行此操作");
        }
        
        // 检查待办事项是否存在
        Todo existingTodo = todoMapper.selectById(adminUpdateDto.getTodoId());
        if (existingTodo == null) {
            throw new RuntimeException("待办事项不存在");
        }
        
        // 记录原始值用于通知
        String originalStatus = existingTodo.getStatus();
        Long originalAssigneeId = existingTodo.getAssigneeId();
        
        // 更新待办事项
        Todo todo = new Todo();
        todo.setId(adminUpdateDto.getTodoId());
        
        // 只更新非空字段
        if (adminUpdateDto.getTitle() != null && !adminUpdateDto.getTitle().trim().isEmpty()) {
            todo.setTitle(adminUpdateDto.getTitle());
        }
        if (adminUpdateDto.getDescription() != null) {
            todo.setDescription(adminUpdateDto.getDescription());
        }
        if (adminUpdateDto.getStatus() != null && !adminUpdateDto.getStatus().trim().isEmpty()) {
            todo.setStatus(adminUpdateDto.getStatus());
        }
        if (adminUpdateDto.getPriority() != null && !adminUpdateDto.getPriority().trim().isEmpty()) {
            todo.setPriority(adminUpdateDto.getPriority());
        }
        if (adminUpdateDto.getAssigneeId() != null) {
            todo.setAssigneeId(adminUpdateDto.getAssigneeId());
        }
        if (adminUpdateDto.getProjectId() != null) {
            todo.setProjectId(adminUpdateDto.getProjectId());
        }
        if (adminUpdateDto.getStartTime() != null) {
            todo.setStartTime(adminUpdateDto.getStartTime());
        }
        if (adminUpdateDto.getDueDate() != null) {
            todo.setDueDate(adminUpdateDto.getDueDate());
        }
        
        todo.setUpdatedTime(LocalDateTime.now());
        
        int result = todoMapper.updateById(todo);
        
        if (result <= 0) {
            throw new RuntimeException("管理员修改待办事项失败");
        }
        
        // 重新查询更新后的待办事项
        Todo updatedTodo = todoMapper.selectById(adminUpdateDto.getTodoId());
        
        // 发送管理员修改通知
        try {
            sendAdminUpdateNotification(updatedTodo, admin, originalStatus, originalAssigneeId, adminUpdateDto.getUpdateReason());
        } catch (Exception e) {
            log.warn("发送管理员修改通知失败: {}", e.getMessage());
        }
        
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
    
    /**
     * 发送任务状态变更通知
     */
    private void sendTaskStatusChangeNotification(Todo todo, Long operatorUserId) {
        try {
            // 获取操作用户信息
            User operator = userMapper.selectById(operatorUserId);
            if (operator == null) {
                log.warn("操作用户不存在，ID: {}", operatorUserId);
                return;
            }
            
            // 获取任务分配者信息
            User assignee = null;
            if (todo.getAssigneeId() != null) {
                assignee = userMapper.selectById(todo.getAssigneeId());
            }
            
            // 查询所有管理员
            List<User> adminUsers = getAllAdminUsers();
            if (adminUsers.isEmpty()) {
                log.warn("没有找到管理员用户");
                return;
            }
            
            // 构建通知内容
            String statusText = getStatusText(todo.getStatus());
            String operatorName = operator.getUsername();
            String todoTitle = todo.getTitle();
            
            String notificationContent = String.format("%s将%s任务切换为%s状态", 
                operatorName, todoTitle, statusText);
            
            // 为每个管理员发送个人通知
            for (User admin : adminUsers) {
                NotificationCreateDto notificationDto = new NotificationCreateDto();
                notificationDto.setTitle("任务状态变更通知");
                notificationDto.setContent(notificationContent);
                notificationDto.setType("personal");
                notificationDto.setPriority("normal");
                notificationDto.setReceiverId(admin.getId());
                notificationDto.setPushImmediately(true);
                
                // 发送个人通知
                notificationService.createPersonalNotification(notificationDto, 
                    operatorUserId, operatorName);
                
                log.info("已向管理员 {} 发送任务状态变更通知: {}", 
                    admin.getUsername(), notificationContent);
            }
            
        } catch (Exception e) {
            log.error("发送任务状态变更通知失败：{}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取所有管理员用户
     */
    private List<User> getAllAdminUsers() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("auth", "admin");
        return userMapper.selectList(queryWrapper);
    }
    
    /**
     * 获取状态的中文描述
     */
    private String getStatusText(String status) {
        switch (status) {
            case "pending":
                return "待处理";
            case "in_progress":
                return "进行中";
            case "completed":
                return "已完成";
            default:
                return status;
        }
    }
    
    /**
     * 设置默认的开始时间和截止时间
     */
    private void setDefaultTimes(Todo todo, TodoCreateDto dto) {
        LocalDateTime now = LocalDateTime.now();
        
        // 设置开始时间：如果没有指定，默认为当天9:00
        if (dto.getStartTime() == null) {
            todo.setStartTime(now.toLocalDate().atTime(9, 0, 0));
        } else {
            todo.setStartTime(dto.getStartTime());
        }
        
        // 设置截止时间：如果没有指定，默认为当天23:59
        if (dto.getDueDate() == null) {
            todo.setDueDate(now.toLocalDate().atTime(23, 59, 59));
        } else {
            todo.setDueDate(dto.getDueDate());
        }
    }
    
    /**
     * 发送管理员修改通知
     */
    private void sendAdminUpdateNotification(Todo updatedTodo, User admin, String originalStatus, Long originalAssigneeId, String updateReason) {
        try {
            // 构建修改内容描述
            StringBuilder changeDescription = new StringBuilder();
            changeDescription.append("管理员 ").append(admin.getUsername()).append(" 修改了任务 \"").append(updatedTodo.getTitle()).append("\"");
            
            if (updateReason != null && !updateReason.trim().isEmpty()) {
                changeDescription.append("，修改原因：").append(updateReason);
            }
            
            // 如果状态发生变化，添加状态变更描述
            if (!updatedTodo.getStatus().equals(originalStatus)) {
                changeDescription.append("，状态从 ").append(getStatusText(originalStatus))
                               .append(" 变更为 ").append(getStatusText(updatedTodo.getStatus()));
            }
            
            // 如果分配人发生变化，添加分配人变更描述
            if (!java.util.Objects.equals(updatedTodo.getAssigneeId(), originalAssigneeId)) {
                User newAssignee = null;
                User oldAssignee = null;
                
                if (updatedTodo.getAssigneeId() != null) {
                    newAssignee = userMapper.selectById(updatedTodo.getAssigneeId());
                }
                if (originalAssigneeId != null) {
                    oldAssignee = userMapper.selectById(originalAssigneeId);
                }
                
                changeDescription.append("，分配人从 ")
                               .append(oldAssignee != null ? oldAssignee.getUsername() : "未分配")
                               .append(" 变更为 ")
                               .append(newAssignee != null ? newAssignee.getUsername() : "未分配");
            }
            
            // 发送通知给当前分配人（如果存在且不是管理员本人）
            if (updatedTodo.getAssigneeId() != null && !updatedTodo.getAssigneeId().equals(admin.getId())) {
                NotificationCreateDto assigneeNotification = new NotificationCreateDto();
                assigneeNotification.setTitle("任务被管理员修改");
                assigneeNotification.setContent(changeDescription.toString());
                assigneeNotification.setType("personal");
                assigneeNotification.setPriority("high");
                assigneeNotification.setReceiverId(updatedTodo.getAssigneeId());
                assigneeNotification.setProjectId(updatedTodo.getProjectId());
                assigneeNotification.setPushImmediately(true);
                
                notificationService.createPersonalNotification(assigneeNotification, admin.getId(), admin.getUsername());
                log.info("已向分配人发送管理员修改通知，任务：{}", updatedTodo.getTitle());
            }
            
            // 如果原分配人发生变化且原分配人不是当前分配人和管理员，也发送通知
            if (originalAssigneeId != null && !originalAssigneeId.equals(admin.getId()) 
                && !java.util.Objects.equals(originalAssigneeId, updatedTodo.getAssigneeId())) {
                NotificationCreateDto originalAssigneeNotification = new NotificationCreateDto();
                originalAssigneeNotification.setTitle("您的任务被重新分配");
                originalAssigneeNotification.setContent("任务 \"" + updatedTodo.getTitle() + "\" 已被管理员重新分配");
                originalAssigneeNotification.setType("personal");
                originalAssigneeNotification.setPriority("normal");
                originalAssigneeNotification.setReceiverId(originalAssigneeId);
                originalAssigneeNotification.setProjectId(updatedTodo.getProjectId());
                originalAssigneeNotification.setPushImmediately(true);
                
                notificationService.createPersonalNotification(originalAssigneeNotification, admin.getId(), admin.getUsername());
                log.info("已向原分配人发送任务重新分配通知，任务：{}", updatedTodo.getTitle());
            }
            
            // 如果任务属于某个项目，发送项目通知
            if (updatedTodo.getProjectId() != null) {
                Project project = projectMapper.selectById(updatedTodo.getProjectId());
                if (project != null) {
                    NotificationCreateDto projectNotification = new NotificationCreateDto();
                    projectNotification.setTitle("项目任务被管理员修改");
                    projectNotification.setContent("项目 " + project.getProjectName() + " 中的" + changeDescription.toString());
                    projectNotification.setType("project");
                    projectNotification.setPriority("normal");
                    projectNotification.setProjectId(updatedTodo.getProjectId());
                    projectNotification.setPushImmediately(true);
                    
                    notificationService.createProjectNotification(projectNotification, admin.getId(), admin.getUsername());
                    log.info("已发送项目任务修改通知，项目：{}，任务：{}", project.getProjectName(), updatedTodo.getTitle());
                }
            }
            
        } catch (Exception e) {
            log.error("发送管理员修改通知失败：{}", e.getMessage(), e);
        }
    }
    
    /**
     * 添加日期过滤条件
     */
    private void addDateFilter(QueryWrapper<Todo> queryWrapper, TodoQueryDto queryDto) {
        java.time.LocalDate targetDate;
        
        // 确定查询日期
        if (queryDto.getQueryDate() != null) {
            targetDate = queryDto.getQueryDate();
        } else {
            // 默认查询今天的数据
            targetDate = java.time.LocalDate.now();
        }
        
        // 如果不包括历史数据，则按日期过滤
        if (queryDto.getIncludeHistory() == null || !queryDto.getIncludeHistory()) {
            // 查询指定日期的待办事项
            // 开始时间在指定日期，或者截止时间在指定日期，或者跨越指定日期的任务
            LocalDateTime dayStart = targetDate.atStartOfDay();
            LocalDateTime dayEnd = targetDate.atTime(23, 59, 59);
            
            queryWrapper.and(wrapper -> 
                wrapper.between("start_time", dayStart, dayEnd)
                       .or()
                       .between("due_date", dayStart, dayEnd)
                       .or()
                       .and(w -> w.le("start_time", dayStart).ge("due_date", dayEnd))
            );
        }
        // 如果包括历史数据，则不添加日期过滤条件，显示所有数据
    }
}
