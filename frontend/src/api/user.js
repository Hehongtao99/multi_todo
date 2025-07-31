import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 用户注册
export const register = (data) => {
  return api.post('/user/register', data)
}

// 用户登录
export const login = (data) => {
  return api.post('/user/login', data)
}

// 获取用户列表（仅管理员）
export const getUserList = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.post('/user/list', {
    userId: userInfo.id,
    userAuth: userInfo.auth
  })
} 