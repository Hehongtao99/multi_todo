<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
import { onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import notificationUtil from './utils/notification.js'
import webSocketService from './api/websocket.js'
import globalMessageHandler from './utils/globalMessage.js'

export default {
  name: 'App',
  setup() {
    const router = useRouter()

    onMounted(async () => {
      // 初始化通知工具，但不主动请求权限
      try {
        await notificationUtil.init()
        console.log('通知权限检查完成')
      } catch (error) {
        console.error('通知权限检查失败:', error)
      }

      // 设置全局消息处理器的路由实例
      globalMessageHandler.setRouter(router)

      // 检查用户登录状态并初始化WebSocket
      initializeWebSocket()

      // 注册全局消息处理器
      registerGlobalMessageHandlers()
    })

    onUnmounted(() => {
      // 应用卸载时断开WebSocket连接
      if (webSocketService.isConnected()) {
        webSocketService.disconnect()
      }
    })

    // 初始化WebSocket连接
    const initializeWebSocket = async () => {
      const userData = localStorage.getItem('userInfo')
      if (userData) {
        try {
          const user = JSON.parse(userData)
          if (!webSocketService.isConnected()) {
            await webSocketService.connect(user)
            console.log('WebSocket连接已建立')
            
            // 立即触发本地状态更新事件
            window.dispatchEvent(new CustomEvent('userStatusChange', {
              detail: {
                userId: user.id,
                status: 'online',
                immediate: true
              }
            }))
          }
        } catch (error) {
          console.error('WebSocket连接失败:', error)
        }
      }
    }

    // 注册全局消息处理器
    const registerGlobalMessageHandlers = () => {
      // 聊天消息处理器
      webSocketService.onMessage('CHAT_MESSAGE', handleGlobalChatMessage)
      
      // 用户状态更新处理器
      webSocketService.onMessage('USER_STATUS', handleGlobalUserStatus)
      
      // 头像更新处理器
      webSocketService.onMessage('USER_AVATAR_UPDATE', handleGlobalAvatarUpdate)
    }

    // 处理全局聊天消息
    const handleGlobalChatMessage = (message) => {
      const currentUser = JSON.parse(localStorage.getItem('userInfo') || '{}')
      globalMessageHandler.showChatMessageAlert(message, currentUser)
    }

    // 处理全局用户状态更新
    const handleGlobalUserStatus = (message) => {
      console.log('全局用户状态更新:', message)
      globalMessageHandler.showUserStatusAlert(message)
    }

    // 处理全局头像更新
    const handleGlobalAvatarUpdate = (message) => {
      console.log('全局头像更新:', message)
    }

    // 监听路由变化，确保WebSocket连接状态
    watch(() => router.currentRoute.value, (to, from) => {
      // 路由变化时不断开WebSocket连接，保持在线状态
      const userData = localStorage.getItem('userInfo')
      if (userData) {
        if (!webSocketService.isConnected()) {
          // 如果用户已登录但WebSocket未连接，重新连接
          initializeWebSocket()
        } else {
          // 只在切换到聊天页面时进行状态同步
          if (to.path.includes('/chat') && from.path !== to.path) {
            console.log('路由切换到聊天页面，进行状态同步')
            
            // 使用新的同步方法
            webSocketService.syncUserStatus().then(() => {
              console.log('状态同步完成')
            }).catch(error => {
              console.error('状态同步失败:', error)
            })
          } else {
            // 其他路由切换时只发送基本状态
            const user = JSON.parse(userData)
            window.dispatchEvent(new CustomEvent('userStatusChange', {
              detail: {
                userId: user.id,
                status: 'online',
                immediate: true,
                source: 'route_change'
              }
            }))
          }
        }
      }
    })

    // 监听存储变化，处理登录/退出
    window.addEventListener('storage', (e) => {
      if (e.key === 'userInfo') {
        if (e.newValue) {
          // 用户登录，连接WebSocket
          initializeWebSocket()
        } else {
          // 用户退出，断开WebSocket
          if (webSocketService.isConnected()) {
            webSocketService.disconnect()
          }
        }
      }
    })

    return {}
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
}

body {
  margin: 0;
  padding: 0;
  height: 100vh;
  background-color: #f5f5f5;
}

/* 聊天消息提醒样式 */
.chat-message-alert {
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  background-color: #ecf5ff;
  border-color: #d9ecff;
  color: #409EFF;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.chat-message-alert:hover {
  background-color: #d9ecff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 用户状态提醒样式 */
.user-status-alert {
  font-size: 13px;
  padding: 8px 16px;
  background-color: #f0f9eb;
  border-color: #e1f3d8;
  color: #67c23a;
}
</style> 