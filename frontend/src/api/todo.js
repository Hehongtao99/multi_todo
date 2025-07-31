import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 创建待办事项
export const createTodo = (data) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.post('/todo/create', data, {
    headers: {
      'userId': userInfo.id,
      'userAuth': userInfo.auth
    }
  })
}

// 更新待办事项
export const updateTodo = (data) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.put('/todo/update', data, {
    headers: {
      'userId': userInfo.id,
      'userAuth': userInfo.auth
    }
  })
}

// 删除待办事项
export const deleteTodo = (todoId) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.delete(`/todo/${todoId}`, {
    headers: {
      'userId': userInfo.id,
      'userAuth': userInfo.auth
    }
  })
}

// 根据项目ID获取待办列表
export const getTodosByProjectId = (projectId) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.get(`/todo/project/${projectId}`, {
    headers: {
      'userId': userInfo.id,
      'userAuth': userInfo.auth
    }
  })
}

// 根据分配人ID获取待办列表
export const getTodosByAssigneeId = (assigneeId) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.get(`/todo/assignee/${assigneeId}`, {
    headers: {
      'userId': userInfo.id,
      'userAuth': userInfo.auth
    }
  })
}

// 获取待办详情
export const getTodoDetail = (todoId) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.get(`/todo/${todoId}`, {
    headers: {
      'userId': userInfo.id,
      'userAuth': userInfo.auth
    }
  })
}

// 更新待办状态
export const updateTodoStatus = (todoId, status) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.put(`/todo/${todoId}/status?status=${status}`, {}, {
    headers: {
      'userId': userInfo.id,
      'userAuth': userInfo.auth
    }
  })
}
