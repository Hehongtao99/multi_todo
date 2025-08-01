import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 获取聊天记录
export const getChatHistory = (data) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const requestData = {
    ...data,
    requesterId: userInfo.id,
    requesterAuth: userInfo.auth
  }
  
  console.log('发送聊天历史请求:', requestData)
  
  return api.post('/chat/history', requestData)
}

// 获取用户的聊天联系人列表
export const getChatContacts = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.post('/chat/contacts', {
    userId: userInfo.id,
    userAuth: userInfo.auth
  })
}

// 标记消息为已读
export const markMessagesAsRead = (data) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.post('/chat/mark-read', {
    ...data,
    userId: userInfo.id,
    userAuth: userInfo.auth
  })
}

// 获取未读消息数量
export const getUnreadMessageCount = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return api.post('/chat/unread-count', {
    userId: userInfo.id,
    userAuth: userInfo.auth
  })
}

// 上传聊天文件
export const uploadChatFile = (file, userId) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('userId', userId)
  
  return api.post('/chat/upload-file', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传表情雪碧图
export const uploadEmojiSprite = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return api.post('/chat/upload-emoji-sprite', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 下载文件（使用原始文件名）
export const downloadChatFile = (fileUrl, originalFileName) => {
  const params = new URLSearchParams()
  params.append('fileUrl', fileUrl)
  params.append('originalFileName', originalFileName)
  
  return api.get(`/chat/download-file?${params.toString()}`, {
    responseType: 'blob'
  })
}

// 预览文件内容
export const previewChatFile = (fileUrl, fileType) => {
  const params = new URLSearchParams()
  params.append('fileUrl', fileUrl)
  params.append('fileType', fileType)
  
  return api.get(`/chat/preview-file?${params.toString()}`)
} 