/**
 * WebSocket调试工具
 * 用于监控和调试WebSocket状态变化
 */

class WebSocketDebug {
  constructor() {
    this.statusHistory = []
    this.eventLog = []
    this.isEnabled = false
  }

  /**
   * 启用调试模式
   */
  enable() {
    this.isEnabled = true
    this.setupEventListeners()
    console.log('🔧 WebSocket调试模式已启用')
  }

  /**
   * 禁用调试模式
   */
  disable() {
    this.isEnabled = false
    console.log('🔧 WebSocket调试模式已禁用')
  }

  /**
   * 设置事件监听器
   */
  setupEventListeners() {
    if (!this.isEnabled) return

    // 监听用户状态变化事件
    window.addEventListener('userStatusChange', (event) => {
      this.logEvent('用户状态变化', event.detail)
    })

    // 监听通知接收事件
    window.addEventListener('notificationReceived', (event) => {
      this.logEvent('通知接收', event.detail)
    })
  }

  /**
   * 记录事件
   */
  logEvent(type, data) {
    if (!this.isEnabled) return

    const event = {
      type,
      data,
      timestamp: new Date().toISOString(),
      time: Date.now()
    }

    this.eventLog.push(event)
    
    // 只保留最近100条记录
    if (this.eventLog.length > 100) {
      this.eventLog.shift()
    }

    console.log(`🔍 [${event.timestamp}] ${type}:`, data)
  }

  /**
   * 记录状态变化
   */
  logStatusChange(userId, oldStatus, newStatus, source) {
    if (!this.isEnabled) return

    const change = {
      userId,
      oldStatus,
      newStatus,
      source,
      timestamp: new Date().toISOString(),
      time: Date.now()
    }

    this.statusHistory.push(change)
    
    // 只保留最近50条记录
    if (this.statusHistory.length > 50) {
      this.statusHistory.shift()
    }

    console.log(`📊 状态变化: 用户${userId} ${oldStatus} -> ${newStatus} (来源: ${source})`)
  }

  /**
   * 获取事件日志
   */
  getEventLog() {
    return this.eventLog
  }

  /**
   * 获取状态历史
   */
  getStatusHistory() {
    return this.statusHistory
  }

  /**
   * 清除日志
   */
  clearLogs() {
    this.eventLog = []
    this.statusHistory = []
    console.log('🧹 调试日志已清除')
  }

  /**
   * 打印调试报告
   */
  printReport() {
    if (!this.isEnabled) {
      console.log('⚠️ 调试模式未启用')
      return
    }

    console.group('📋 WebSocket调试报告')
    
    console.group('📊 状态变化历史')
    this.statusHistory.forEach((change, index) => {
      console.log(`${index + 1}. [${change.timestamp}] 用户${change.userId}: ${change.oldStatus} -> ${change.newStatus} (${change.source})`)
    })
    console.groupEnd()

    console.group('🔍 事件日志')
    this.eventLog.forEach((event, index) => {
      console.log(`${index + 1}. [${event.timestamp}] ${event.type}:`, event.data)
    })
    console.groupEnd()

    console.groupEnd()
  }
}

// 创建全局调试实例
const webSocketDebug = new WebSocketDebug()

// 在开发环境中自动启用
if (process.env.NODE_ENV === 'development') {
  webSocketDebug.enable()
}

// 添加到全局对象，方便控制台调用
if (typeof window !== 'undefined') {
  window.wsDebug = webSocketDebug
}

export default webSocketDebug 