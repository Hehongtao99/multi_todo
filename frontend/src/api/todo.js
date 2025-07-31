import axios from 'axios'
import { formatDateForBackend } from '../utils/dateUtils'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 创建待办事项
export const createTodo = (data) => {
  return api.post('/todo/create', {
    title: data.title,
    description: data.description,
    priority: data.priority,
    projectId: data.projectId,
    assigneeId: data.assigneeId,
    dueDate: formatDateForBackend(data.dueDate)
  }, {
    headers: {
      'userId': JSON.parse(localStorage.getItem('userInfo') || '{}').id
    }
  })
}

// 更新待办事项
export const updateTodo = (data) => {
  return api.put('/todo/update', {
    id: data.id,
    title: data.title,
    description: data.description,
    status: data.status,
    priority: data.priority,
    assigneeId: data.assigneeId,
    dueDate: formatDateForBackend(data.dueDate)
  })
}

// 删除待办事项
export const deleteTodo = (todoId) => {
  return api.delete(`/todo/${todoId}`)
}

// 获取待办列表 - 根据项目ID
export const getTodosByProjectId = (projectId) => {
  return api.get(`/todo/project/${projectId}`)
}

// 根据分配人ID获取待办列表
export const getTodosByAssigneeId = (assigneeId) => {
  return api.get(`/todo/assignee/${assigneeId}`)
}

// 获取待办详情
export const getTodoDetail = (todoId) => {
  return api.get(`/todo/${todoId}`)
}

// 更新待办状态
export const updateTodoStatus = (todoId, status) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.put(`/todo/${todoId}/status`, null, {
    params: { status },
    headers: {
      'userId': userInfo.id,
      'userAuth': userInfo.auth
    }
  })
}

// 获取所有待办事项列表（管理员用）
export const getAllTodos = (queryParams = {}) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.post('/todo/list', {
    userId: userInfo.id,
    userAuth: userInfo.auth,
    ...queryParams
  })
}
