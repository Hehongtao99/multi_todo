import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 创建项目
export const createProject = (data) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.post('/project/create', {
    userId: userInfo.id,
    userAuth: userInfo.auth,
    projectName: data.projectName,
    projectDescription: data.projectDescription
  })
}

// 获取项目列表
export const getProjectList = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.post('/project/list', {
    userId: userInfo.id,
    userAuth: userInfo.auth
  })
}

// 分配项目给多个用户
export const assignProject = (data) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.post('/project/assign', {
    userAuth: userInfo.auth,
    projectId: data.projectId,
    userIds: data.userIds
  })
}

// 获取项目详情
export const getProjectDetail = (projectId) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.post('/project/detail', {
    projectId: projectId,
    userId: userInfo.id,
    userAuth: userInfo.auth
  })
}