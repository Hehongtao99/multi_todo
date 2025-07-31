import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import notificationUtil from '../utils/notification.js'

class WebSocketService {
  constructor() {
    this.client = null
    this.connected = false
    this.currentUser = null
    this.currentProject = null
    this.messageHandlers = new Map()
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.subscriptions = new Map() // 存储订阅引用，用于取消订阅
  }

  /**
   * 连接WebSocket
   */
  connect(user) {
    return new Promise((resolve, reject) => {
      this.currentUser = user
      
      this.client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
        connectHeaders: {},
        debug: (str) => {
          console.log('WebSocket调试:', str)
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: (frame) => {
          console.log('WebSocket连接成功:', frame)
          this.connected = true
          this.reconnectAttempts = 0
          this.subscribeToTopics()
          resolve(frame)
        },
        onStompError: (frame) => {
          console.error('WebSocket STOMP错误:', frame.headers['message'])
          console.error('详细信息:', frame.body)
          this.connected = false
          reject(frame)
        },
        onWebSocketError: (error) => {
          console.error('WebSocket连接错误:', error)
          this.connected = false
          reject(error)
        },
        onDisconnect: () => {
          console.log('WebSocket连接断开')
          this.connected = false
          this.attemptReconnect()
        }
      })

      this.client.activate()
    })
  }

  /**
   * 清理所有订阅
   */
  clearSubscriptions() {
    for (const [key, subscription] of this.subscriptions.entries()) {
      if (subscription && subscription.unsubscribe) {
        subscription.unsubscribe()
      }
    }
    this.subscriptions.clear()
  }

  /**
   * 断开WebSocket连接
   */
  disconnect() {
    if (this.client && this.connected) {
      // 离开当前项目
      if (this.currentProject) {
        this.leaveProject()
      }
      
      // 清理所有订阅
      this.clearSubscriptions()
      
      this.client.deactivate()
      this.connected = false
      this.currentUser = null
      this.currentProject = null
    }
  }

  /**
   * 订阅主题
   */
  subscribeToTopics() {
    if (!this.client || !this.connected) return

    // 清理之前的订阅
    this.clearSubscriptions()

    // 订阅全局消息
    const globalSub = this.client.subscribe('/topic/global', (message) => {
      this.handleMessage('global', JSON.parse(message.body))
    })
    this.subscriptions.set('global', globalSub)

    // 订阅个人消息
    if (this.currentUser && this.currentUser.id) {
      const personalSub = this.client.subscribe(`/queue/user/${this.currentUser.id}`, (message) => {
        this.handleMessage('personal', JSON.parse(message.body))
      })
      this.subscriptions.set('personal', personalSub)
    }
  }

  /**
   * 加入项目
   */
  joinProject(projectId) {
    if (!this.client || !this.connected || !this.currentUser) {
      console.error('WebSocket未连接或用户信息缺失')
      return
    }

    // 如果已经在当前项目中，不需要重复加入
    if (this.currentProject === projectId) {
      console.log(`已经在项目 ${projectId} 中`)
      return
    }

    // 如果之前在其他项目中，先离开
    if (this.currentProject && this.currentProject !== projectId) {
      this.leaveProject()
    }

    this.currentProject = projectId

    // 取消之前的项目订阅（如果存在）
    const existingProjectSub = this.subscriptions.get('project')
    if (existingProjectSub) {
      existingProjectSub.unsubscribe()
    }

    // 订阅项目消息
    const projectSub = this.client.subscribe(`/topic/project/${projectId}`, (message) => {
      this.handleMessage('project', JSON.parse(message.body))
    })
    this.subscriptions.set('project', projectSub)

    // 发送加入项目消息
    const message = {
      type: 'JOIN_PROJECT',
      content: '加入项目',
      senderId: this.currentUser.id,
      senderName: this.currentUser.username,
      projectId: projectId,
      timestamp: Date.now()
    }

    this.client.publish({
      destination: '/app/project.join',
      body: JSON.stringify(message)
    })

    console.log(`已加入项目 ${projectId}`)
  }

  /**
   * 离开项目
   */
  leaveProject() {
    if (!this.client || !this.connected || !this.currentUser || !this.currentProject) {
      return
    }

    const message = {
      type: 'LEAVE_PROJECT',
      content: '离开项目',
      senderId: this.currentUser.id,
      senderName: this.currentUser.username,
      projectId: this.currentProject,
      timestamp: Date.now()
    }

    this.client.publish({
      destination: '/app/project.leave',
      body: JSON.stringify(message)
    })

    this.currentProject = null
  }

  /**
   * 发送项目更新消息
   */
  sendProjectUpdate(updateData) {
    if (!this.client || !this.connected || !this.currentUser || !this.currentProject) {
      console.error('WebSocket未连接或项目信息缺失')
      return
    }

    const message = {
      type: 'PROJECT_UPDATE',
      content: updateData,
      senderId: this.currentUser.id,
      senderName: this.currentUser.username,
      projectId: this.currentProject,
      timestamp: Date.now()
    }

    this.client.publish({
      destination: '/app/project.update',
      body: JSON.stringify(message)
    })
  }

  /**
   * 发送聊天消息
   */
  sendChatMessage(content, receiverId = null) {
    if (!this.client || !this.connected || !this.currentUser) {
      console.error('WebSocket未连接或用户信息缺失')
      return
    }

    const message = {
      type: 'CHAT_MESSAGE',
      content: content,
      senderId: this.currentUser.id,
      senderName: this.currentUser.username,
      projectId: this.currentProject,
      receiverId: receiverId,
      timestamp: Date.now()
    }

    this.client.publish({
      destination: '/app/chat.message',
      body: JSON.stringify(message)
    })
  }

  /**
   * 发送用户状态更新
   */
  sendUserStatus(status) {
    if (!this.client || !this.connected || !this.currentUser) {
      console.error('WebSocket未连接或用户信息缺失')
      return
    }

    const message = {
      type: 'USER_STATUS',
      content: { status: status },
      senderId: this.currentUser.id,
      senderName: this.currentUser.username,
      projectId: this.currentProject,
      timestamp: Date.now()
    }

    this.client.publish({
      destination: '/app/user.status',
      body: JSON.stringify(message)
    })
  }

  /**
   * 处理接收到的消息
   */
  handleMessage(channel, message) {
    console.log(`收到${channel}消息:`, message)
    
    // 处理通知消息
    if (message.type === 'NOTIFICATION') {
      this.handleNotificationMessage(message.content)
    }
    
    // 调用注册的消息处理器
    const handlers = this.messageHandlers.get(message.type) || []
    handlers.forEach(handler => {
      try {
        handler(message, channel)
      } catch (error) {
        console.error('消息处理器执行错误:', error)
      }
    })

    // 调用通用消息处理器
    const globalHandlers = this.messageHandlers.get('*') || []
    globalHandlers.forEach(handler => {
      try {
        handler(message, channel)
      } catch (error) {
        console.error('全局消息处理器执行错误:', error)
      }
    })
  }

  /**
   * 处理通知消息
   */
  async handleNotificationMessage(notification) {
    console.log('收到通知:', notification)
    
    try {
      // 只显示一次系统通知，根据通知类型显示不同的标题
      let notificationTitle = notification.title
      switch (notification.type) {
        case 'system':
          notificationTitle = `系统通知: ${notification.title}`
          break
        case 'project':
          notificationTitle = `项目通知: ${notification.title}`
          break
        case 'personal':
          notificationTitle = `个人通知: ${notification.title}`
          break
      }

      // 只显示一次通知
      await notificationUtil.showNotification({
        title: notificationTitle,
        message: notification.content,
        priority: notification.priority,
        tag: `${notification.type}-${notification.id || Date.now()}` // 使用 tag 防止重复通知
      })

      // 触发通知接收事件供页面组件处理业务逻辑
      const event = new CustomEvent('notificationReceived', {
        detail: notification
      })
      window.dispatchEvent(event)

    } catch (error) {
      console.error('处理通知消息失败:', error)
    }
  }

  /**
   * 注册消息处理器
   */
  onMessage(messageType, handler) {
    if (!this.messageHandlers.has(messageType)) {
      this.messageHandlers.set(messageType, [])
    }
    this.messageHandlers.get(messageType).push(handler)
  }

  /**
   * 移除消息处理器
   */
  offMessage(messageType, handler) {
    const handlers = this.messageHandlers.get(messageType)
    if (handlers) {
      const index = handlers.indexOf(handler)
      if (index > -1) {
        handlers.splice(index, 1)
      }
    }
  }

  /**
   * 尝试重连
   */
  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts && this.currentUser) {
      this.reconnectAttempts++
      console.log(`尝试重连WebSocket，第${this.reconnectAttempts}次`)
      
      setTimeout(() => {
        this.connect(this.currentUser).catch(error => {
          console.error('重连失败:', error)
        })
      }, 3000 * this.reconnectAttempts)
    } else {
      console.error('WebSocket重连次数已达上限')
    }
  }

  /**
   * 检查连接状态
   */
  isConnected() {
    return this.connected && this.client && this.client.connected
  }

  /**
   * 获取当前用户
   */
  getCurrentUser() {
    return this.currentUser
  }

  /**
   * 获取当前项目
   */
  getCurrentProject() {
    return this.currentProject
  }
}

// 创建全局WebSocket服务实例
const webSocketService = new WebSocketService()

export default webSocketService 