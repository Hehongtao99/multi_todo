import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 创建项目
export const createProject = (data) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.post('/project/create', data, {
    headers: {
      'userId': userInfo.id,
      'userAuth': userInfo.auth
    }
  })
}

// 获取项目列表
export const getProjectList = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.get('/project/list', {
    headers: {
      'userId': userInfo.id,
      'userAuth': userInfo.auth
    }
  })
}

// 分配项目给多个用户
export const assignProject = (data) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.put('/project/assign', data, {
    headers: {
      'userId': userInfo.id,
      'userAuth': userInfo.auth
    }
  })
} 