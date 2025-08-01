import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 获取个人信息
export const getProfile = (data) => {
  return api.post('/profile/info', data)
}

// 更新个人信息
export const updateProfile = (data) => {
  return api.post('/profile/update', data)
}

// 上传头像
export const uploadAvatar = (file, userId) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('userId', userId)
  
  return api.post('/profile/upload-avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 30000 // 上传文件超时时间延长到30秒
  })
} 