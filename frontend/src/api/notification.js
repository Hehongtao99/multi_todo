import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

/**
 * 通知API服务
 */
class NotificationAPI {
  
  /**
   * 发送通知
   */
  static async sendNotification(notificationData) {
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const response = await api.post('/notifications/send', {
        userId: userInfo.id,
        userAuth: userInfo.auth,
        senderName: userInfo.username,
        ...notificationData
      })
      return response.data
    } catch (error) {
      console.error('发送通知失败:', error)
      throw error
    }
  }

  /**
   * 获取通知列表
   */
  static async getNotifications(queryParams = {}) {
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const response = await api.post('/notifications/list', {
        userId: userInfo.id,
        userAuth: userInfo.auth,
        ...queryParams
      })
      return response.data
    } catch (error) {
      console.error('获取通知列表失败:', error)
      throw error
    }
  }

  /**
   * 获取用户通知列表
   */
  static async getUserNotifications(userId = null) {
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const response = await api.post('/notifications/list', {
        userId: userId || userInfo.id,
        userAuth: userInfo.auth
      })
      return response.data
    } catch (error) {
      console.error('获取用户通知失败:', error)
      throw error
    }
  }

  /**
   * 获取项目通知列表
   */
  static async getProjectNotifications(projectId) {
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const response = await api.post('/notifications/list', {
        userId: userInfo.id,
        userAuth: userInfo.auth,
        projectId: projectId,
        type: 'project'
      })
      return response.data
    } catch (error) {
      console.error('获取项目通知失败:', error)
      throw error
    }
  }

  /**
   * 获取系统通知列表
   */
  static async getSystemNotifications() {
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const response = await api.post('/notifications/list', {
        userId: userInfo.id,
        userAuth: userInfo.auth,
        type: 'system'
      })
      return response.data
    } catch (error) {
      console.error('获取系统通知失败:', error)
      throw error
    }
  }

  /**
   * 标记通知为已读
   */
  static async markAsRead(notificationId) {
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const response = await api.post('/notifications/mark-read', {
        notificationId: notificationId,
        userId: userInfo.id,
        userAuth: userInfo.auth
      })
      return response.data
    } catch (error) {
      console.error('标记通知已读失败:', error)
      throw error
    }
  }

  /**
   * 获取未读通知数量
   */
  static async getUnreadCount() {
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const response = await api.post('/notifications/unread-count', {
        userId: userInfo.id,
        userAuth: userInfo.auth
      })
      return response.data
    } catch (error) {
      console.error('获取未读通知数量失败:', error)
      throw error
    }
  }

  /**
   * 删除通知
   */
  static async deleteNotification(notificationId) {
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const response = await api.post('/notifications/delete', {
        notificationId: notificationId,
        userId: userInfo.id,
        userAuth: userInfo.auth
      })
      return response.data
    } catch (error) {
      console.error('删除通知失败:', error)
      throw error
    }
  }

  /**
   * 发送系统通知（管理员专用）
   */
  static async sendSystemNotification(notificationData) {
    return this.sendNotification({
      type: 'system',
      priority: 'normal',
      ...notificationData
    })
  }

  /**
   * 发送项目通知（管理员专用）
   */
  static async sendProjectNotification(notificationData) {
    return this.sendNotification({
      type: 'project',
      priority: 'normal',
      ...notificationData
    })
  }

  /**
   * 发送个人通知（管理员专用）
   */
  static async sendPersonalNotification(notificationData) {
    return this.sendNotification({
      type: 'personal',
      priority: 'normal',
      ...notificationData
    })
  }
}

export default NotificationAPI 