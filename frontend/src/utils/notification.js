/**
 * Windows通知工具类
 * 封装浏览器通知API和Electron通知API
 */
class NotificationUtil {
  
  constructor() {
    this.permission = null
    this.isElectron = this.checkElectronEnvironment()
    this.recentNotifications = new Map() // 用于去重的通知记录
    // 不在构造函数中调用init()，避免自动请求权限
  }

  /**
   * 检查是否在Electron环境中
   */
  checkElectronEnvironment() {
    return typeof window !== 'undefined' && 
           window.electronAPI !== undefined && 
           window.electronAPI.isElectron === true
  }

  /**
   * 初始化通知权限
   */
  async init() {
    if (this.isElectron) {
      // Electron环境下，通知权限通常已经获得
      this.permission = 'granted'
      return true
    } else {
      // 浏览器环境下，检查通知权限但不主动请求
      if (Notification.permission === 'granted') {
        this.permission = 'granted'
        return true
      } else if (Notification.permission === 'denied') {
        this.permission = 'denied'
        return false
      } else {
        this.permission = 'default'
        return false
      }
    }
  }

  /**
   * 请求通知权限
   */
  async requestPermission() {
    if (!('Notification' in window)) {
      console.warn('此浏览器不支持桌面通知')
      return false
    }

    if (Notification.permission === 'granted') {
      this.permission = 'granted'
      return true
    } else if (Notification.permission === 'denied') {
      console.warn('通知权限被拒绝')
      return false
    } else {
      // 请求权限
      const permission = await Notification.requestPermission()
      this.permission = permission
      return permission === 'granted'
    }
  }

  /**
   * 显示通知
   */
  async showNotification(options) {
    const {
      title = '新通知',
      message = '',
      icon = null,
      priority = 'normal',
      tag = null,
      requireInteraction = false,
      silent = false,
      onClick = null,
      onClose = null
    } = options

    // 去重检查：防止短时间内显示相同的通知
    const notificationKey = `${title}-${message}`
    const now = Date.now()
    const lastShown = this.recentNotifications.get(notificationKey)
    
    if (lastShown && (now - lastShown) < 3000) { // 3秒内不重复显示相同通知
      console.log('跳过重复通知:', title)
      return null
    }
    
    this.recentNotifications.set(notificationKey, now)
    
    // 清理过期的通知记录（保留最近10分钟的记录）
    for (const [key, timestamp] of this.recentNotifications.entries()) {
      if (now - timestamp > 600000) { // 10分钟
        this.recentNotifications.delete(key)
      }
    }

    // 检查权限
    if (this.permission !== 'granted') {
      const hasPermission = await this.requestPermission()
      if (!hasPermission) {
        console.warn('无法显示通知：权限不足')
        return null
      }
    }

    try {
      if (this.isElectron && window.electronAPI && window.electronAPI.showNotification) {
        // 桌面应用：使用Electron的通知API
        console.log('使用Electron通知')
        return await this.showElectronNotification({
          title,
          message,
          icon,
          priority,
          silent,
          onClick,
          onClose
        })
      } else {
        // 浏览器环境：使用浏览器的通知API
        console.log('使用浏览器通知')
        return await this.showBrowserNotification({
          title,
          message,
          icon,
          tag,
          requireInteraction,
          silent,
          onClick,
          onClose
        })
      }
    } catch (error) {
      console.error('显示通知失败:', error)
      // 桌面应用不降级，浏览器应用可以考虑其他提示方式
      if (!this.isElectron) {
        console.warn('浏览器通知失败，可能需要用户授权或浏览器不支持')
      }
      return null
    }
  }

  /**
   * 显示Electron通知
   */
  async showElectronNotification(options) {
    const { title, message, icon, priority, silent, onClick, onClose } = options
    
    const notificationOptions = {
      title,
      body: message,
      icon: icon || this.getDefaultIcon(),
      silent: silent || false,
      urgency: this.mapPriorityToUrgency(priority)
    }

    try {
      const result = await window.electronAPI.showNotification(notificationOptions)
      
      if (result.success) {
        console.log('Electron通知发送成功')
        return result.notification
      } else {
        throw new Error(result.error)
      }
    } catch (error) {
      console.error('显示Electron通知失败:', error)
      // 对于桌面应用，不降级到浏览器通知，直接返回错误
      throw error
    }
  }

  /**
   * 显示浏览器通知
   */
  async showBrowserNotification(options) {
    const { title, message, icon, tag, requireInteraction, silent, onClick, onClose } = options

    const notificationOptions = {
      body: message,
      icon: icon || this.getDefaultIcon(),
      tag: tag,
      requireInteraction: requireInteraction || false,
      silent: silent || false
    }

    const notification = new Notification(title, notificationOptions)

    // 绑定事件处理器
    if (onClick) {
      notification.onclick = onClick
    }
    if (onClose) {
      notification.onclose = onClose
    }

    // 自动关闭通知（如果不要求用户交互）
    if (!requireInteraction) {
      setTimeout(() => {
        notification.close()
      }, 5000) // 5秒后自动关闭
    }

    return notification
  }

  /**
   * 显示系统通知
   */
  async showSystemNotification(notification) {
    return await this.showNotification({
      title: `系统通知: ${notification.title}`,
      message: notification.content,
      priority: notification.priority,
      tag: `system-${notification.id}`,
      requireInteraction: notification.priority === 'urgent',
      onClick: () => {
        console.log('点击了系统通知:', notification.id)
        // 可以在这里跳转到通知详情页面
        this.handleNotificationClick(notification)
      }
    })
  }

  /**
   * 显示项目通知
   */
  async showProjectNotification(notification) {
    return await this.showNotification({
      title: `项目通知: ${notification.title}`,
      message: notification.content,
      priority: notification.priority,
      tag: `project-${notification.id}`,
      requireInteraction: notification.priority === 'urgent' || notification.priority === 'high',
      onClick: () => {
        console.log('点击了项目通知:', notification.id)
        this.handleNotificationClick(notification)
      }
    })
  }

  /**
   * 显示个人通知
   */
  async showPersonalNotification(notification) {
    return await this.showNotification({
      title: `个人通知: ${notification.title}`,
      message: notification.content,
      priority: notification.priority,
      tag: `personal-${notification.id}`,
      requireInteraction: notification.priority === 'urgent',
      onClick: () => {
        console.log('点击了个人通知:', notification.id)
        this.handleNotificationClick(notification)
      }
    })
  }

  /**
   * 处理通知点击事件
   */
  handleNotificationClick(notification) {
    // 聚焦应用窗口
    if (this.isElectron && window.electronAPI && window.electronAPI.focusWindow) {
      window.electronAPI.focusWindow()
    } else {
      window.focus()
    }

    // 触发自定义事件，让应用处理通知点击
    const event = new CustomEvent('notificationClick', {
      detail: notification
    })
    window.dispatchEvent(event)
  }

  /**
   * 获取默认图标
   */
  getDefaultIcon() {
    // 可以设置应用的默认图标路径
    return '/favicon.ico'
  }

  /**
   * 将优先级映射为Electron的urgency
   */
  mapPriorityToUrgency(priority) {
    switch (priority) {
      case 'low':
        return 'low'
      case 'normal':
        return 'normal'
      case 'high':
        return 'normal'
      case 'urgent':
        return 'critical'
      default:
        return 'normal'
    }
  }

  /**
   * 检查通知权限状态
   */
  getPermissionStatus() {
    if (this.isElectron) {
      return 'granted' // Electron通常有权限
    } else {
      return Notification.permission
    }
  }

  /**
   * 是否支持通知
   */
  isSupported() {
    return this.isElectron || ('Notification' in window)
  }
}

// 创建全局通知工具实例
const notificationUtil = new NotificationUtil()

export default notificationUtil 