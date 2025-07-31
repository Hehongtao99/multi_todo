package com.todo.utils;

import com.todo.entity.User;
import com.todo.entity.Project;
import com.todo.entity.Todo;
import com.todo.entity.Notification;
import com.todo.vo.UserVo;
import com.todo.vo.ProjectVo;
import com.todo.vo.TodoVo;
import com.todo.vo.NotificationVo;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 实体对象与VO对象转换工具类
 */
public class EntityVoConverter {
    
    /**
     * 将User实体转换为UserVo
     */
    public static UserVo convertToUserVo(User user) {
        if (user == null) {
            return null;
        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        // 不返回密码等敏感信息
        return userVo;
    }
    
    /**
     * 将User实体列表转换为UserVo列表
     */
    public static List<UserVo> convertToUserVoList(List<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream()
                   .map(EntityVoConverter::convertToUserVo)
                   .collect(Collectors.toList());
    }
    
    /**
     * 将Project实体转换为ProjectVo
     */
    public static ProjectVo convertToProjectVo(Project project) {
        if (project == null) {
            return null;
        }
        ProjectVo projectVo = new ProjectVo();
        BeanUtils.copyProperties(project, projectVo);
        return projectVo;
    }
    
    /**
     * 将Project实体列表转换为ProjectVo列表
     */
    public static List<ProjectVo> convertToProjectVoList(List<Project> projects) {
        if (projects == null) {
            return null;
        }
        return projects.stream()
                      .map(EntityVoConverter::convertToProjectVo)
                      .collect(Collectors.toList());
    }
    
    /**
     * 将Todo实体转换为TodoVo
     */
    public static TodoVo convertToTodoVo(Todo todo) {
        if (todo == null) {
            return null;
        }
        TodoVo todoVo = new TodoVo();
        BeanUtils.copyProperties(todo, todoVo);
        return todoVo;
    }
    
    /**
     * 将Todo实体列表转换为TodoVo列表
     */
    public static List<TodoVo> convertToTodoVoList(List<Todo> todos) {
        if (todos == null) {
            return null;
        }
        return todos.stream()
                   .map(EntityVoConverter::convertToTodoVo)
                   .collect(Collectors.toList());
    }
    
    /**
     * 将Notification实体转换为NotificationVo
     */
    public static NotificationVo convertToNotificationVo(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationVo notificationVo = new NotificationVo();
        BeanUtils.copyProperties(notification, notificationVo);
        return notificationVo;
    }
    
    /**
     * 将Notification实体列表转换为NotificationVo列表
     */
    public static List<NotificationVo> convertToNotificationVoList(List<Notification> notifications) {
        if (notifications == null) {
            return null;
        }
        return notifications.stream()
                          .map(EntityVoConverter::convertToNotificationVo)
                          .collect(Collectors.toList());
    }
} 