import { ElMessage } from 'element-plus'

class GlobalMessageHandler {
  constructor() {
    this.router = null
  }

  /**
   * 设置路由实例
   */
  setRouter(router) {
    this.router = router
  }

  /**
   * 显示聊天消息提醒
   */
  showChatMessageAlert(message, currentUser) {
    // 只有当前路由不是聊天页面时才显示弹窗提醒
    if (this.router && this.router.currentRoute.value.path !== '/chat') {
      // 只对接收到的消息（非自己发送的）显示提醒
      if (message.senderId !== currentUser.id) {
        // 使用更简洁的消息提示
        ElMessage({
          message: `${message.senderName}: ${message.content}`,
          type: 'info',
          duration: 3000,
          showClose: true,
          offset: 60,
          customClass: 'chat-message-alert',
          onClick: () => {
            // 点击消息跳转到聊天页面
            if (this.router) {
              this.router.push('/chat')
            }
          }
        })
      }
    }
  }

  /**
   * 显示用户状态变化提醒
   */
  showUserStatusAlert(message) {
    // 暂时禁用用户状态变化提醒，避免路由切换时的大量提示
    // 可以根据需要重新开启
    /*
    if (message.content && message.content.status === 'online') {
      ElMessage({
        message: `${message.senderName} 上线了`,
        type: 'success',
        duration: 2000,
        offset: 60,
        showClose: false,
        customClass: 'user-status-alert'
      })
    }
    */
  }
}

// 创建全局实例
const globalMessageHandler = new GlobalMessageHandler()

export default globalMessageHandler 