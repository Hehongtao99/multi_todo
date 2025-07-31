import axios from 'axios'

const BASE_URL = 'http://localhost:8080/api'

/**
 * 通知API服务
 */
class NotificationAPI {
  
  /**
   * 管理员发送系统通知
   */
  static async sendSystemNotification(notificationData, adminId, adminName) {
    try {
      const response = await axios.post(`${BASE_URL}/notifications/system`, notificationData, {
        params: { adminId, adminName }
      })
      return response.data
    } catch (error) {
      console.error('发送系统通知失败:', error)
      throw error
    }
  }

  /**
   * 管理员发送项目通知
   */
  static async sendProjectNotification(notificationData, adminId, adminName) {
    try {
      const response = await axios.post(`${BASE_URL}/notifications/project`, notificationData, {
        params: { adminId, adminName }
      })
      return response.data
    } catch (error) {
      console.error('发送项目通知失败:', error)
      throw error
    }
  }

  /**
   * 管理员发送个人通知
   */
  static async sendPersonalNotification(notificationData, adminId, adminName) {
    try {
      const response = await axios.post(`${BASE_URL}/notifications/personal`, notificationData, {
        params: { adminId, adminName }
      })
      return response.data
    } catch (error) {
      console.error('发送个人通知失败:', error)
      throw error
    }
  }

  /**
   * 获取用户通知列表
   */
  static async getUserNotifications(userId) {
    try {
      const response = await axios.get(`${BASE_URL}/notifications/user/${userId}`)
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
      const response = await axios.get(`${BASE_URL}/notifications/project/${projectId}`)
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
      const response = await axios.get(`${BASE_URL}/notifications/system`)
      return response.data
    } catch (error) {
      console.error('获取系统通知失败:', error)
      throw error
    }
  }

  /**
   * 标记通知为已读
   */
  static async markAsRead(notificationId, userId) {
    try {
      const response = await axios.put(`${BASE_URL}/notifications/${notificationId}/read`, null, {
        params: { userId }
      })
      return response.data
    } catch (error) {
      console.error('标记通知已读失败:', error)
      throw error
    }
  }

  /**
   * 批量标记所有通知为已读
   */
  static async markAllAsRead(userId) {
    try {
      const response = await axios.put(`${BASE_URL}/notifications/read-all`, null, {
        params: { userId }
      })
      return response.data
    } catch (error) {
      console.error('批量标记已读失败:', error)
      throw error
    }
  }

  /**
   * 获取未读通知数量
   */
  static async getUnreadCount(userId) {
    try {
      const response = await axios.get(`${BASE_URL}/notifications/unread-count/${userId}`)
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
      const response = await axios.delete(`${BASE_URL}/notifications/${notificationId}`)
      return response.data
    } catch (error) {
      console.error('删除通知失败:', error)
      throw error
    }
  }
}

export default NotificationAPI 