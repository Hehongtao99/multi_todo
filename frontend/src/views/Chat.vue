<template>
  <div class="chat-container">
    <div class="chat-layout">
      <!-- 左侧用户列表 -->
      <div class="user-list">
        <div class="user-list-header">
          <h3>{{ currentUser.auth === 'admin' ? '普通用户' : '管理员' }}</h3>
          <el-button @click="refreshUserList" :icon="Refresh" size="small" text>刷新</el-button>
        </div>
        <div class="user-list-content">
          <div
            v-for="user in userList"
            :key="user.id"
            :class="['user-item', { active: selectedUser?.id === user.id }]"
            @click="selectUser(user)"
          >
            <el-avatar :size="40" :src="user.avatar" class="user-avatar">
              {{ getUserDisplayName(user).charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="user-info">
              <div class="user-name">{{ getUserDisplayName(user) }}</div>
              <div class="user-status" :class="getUserStatusClass(user.id)">
                {{ getUserStatusText(user.id) }}
              </div>
            </div>
            <div v-if="getUnreadCount(user.id) > 0" class="unread-badge">
              {{ getUnreadCount(user.id) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="chat-area">
        <div v-if="!selectedUser" class="no-chat-selected">
          <el-empty description="请选择一个用户开始聊天" />
        </div>
        <div v-else class="chat-content">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <el-avatar :size="32" :src="selectedUser.avatar">
              {{ getUserDisplayName(selectedUser).charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="chat-user-info">
              <div class="chat-user-name">{{ getUserDisplayName(selectedUser) }}</div>
              <div class="chat-user-status" :class="getUserStatusClass(selectedUser.id)">
                {{ getUserStatusText(selectedUser.id) }}
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
          <div class="message-list" ref="messageListRef">
            <div
              v-for="message in getCurrentMessages()"
              :key="message.id"
              :class="['message-item', { 'own-message': message.senderId === currentUser.id }]"
            >
              <!-- 对方消息 -->
              <div v-if="message.senderId !== currentUser.id" class="message-wrapper">
                <el-avatar 
                  :size="36" 
                  :src="getUserAvatar(message.senderId)"
                  class="message-avatar"
                >
                  {{ getUserDisplayName(getUserById(message.senderId)).charAt(0).toUpperCase() }}
                </el-avatar>
                <div class="message-info">
                  <div class="message-sender">{{ getUserDisplayName(getUserById(message.senderId)) }}</div>
                  <div class="message-content">
                    <div v-if="isFileMessage(message.content)" class="message-file">
                      <FilePreview :fileInfo="getFileInfo(message.content)" />
                    </div>
                    <div v-else class="message-text" v-html="parseMessageContent(message.content)"></div>
                    <div class="message-time">{{ formatTime(message.timestamp) }}</div>
                  </div>
                </div>
              </div>
              
              <!-- 自己的消息 -->
              <div v-else class="message-wrapper own">
                <div class="message-info">
                  <div class="message-content">
                    <div v-if="isFileMessage(message.content)" class="message-file">
                      <FilePreview :fileInfo="getFileInfo(message.content)" />
                    </div>
                    <div v-else class="message-text" v-html="parseMessageContent(message.content)"></div>
                    <div class="message-time">{{ formatTime(message.timestamp) }}</div>
                  </div>
                </div>
                <el-avatar 
                  :size="36" 
                  :src="currentUser.avatar"
                  class="message-avatar own-avatar"
                >
                  {{ getUserDisplayName(currentUser).charAt(0).toUpperCase() }}
                </el-avatar>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="message-input">
            <div class="input-toolbar">
              <EmojiPicker @select="insertEmoji" />
              <el-upload
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleFileSelect"
                action=""
                :http-request="() => {}"
              >
                <el-button text size="small">
                  📎 文件
                </el-button>
              </el-upload>
            </div>
            <el-input
              v-model="messageInput"
              placeholder="输入消息..."
              @keyup.enter="sendMessage"
              :maxlength="500"
              show-word-limit
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 4 }"
            >
              <template #append>
                <el-button type="primary" @click="sendMessage" :disabled="!messageInput.trim()">
                  发送
                </el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import { Refresh, Document } from '@element-plus/icons-vue'
import { getUserList } from '../api/user.js'
import { getChatContacts, getChatHistory, uploadChatFile } from '../api/chat.js'
import { parseEmojis } from '../utils/emojiConfig.js'
import EmojiPicker from '../components/EmojiPicker.vue'
import FilePreview from '../components/FilePreview.vue'
import webSocketService from '../api/websocket.js'

export default {
  name: 'Chat',
  components: {
    Refresh,
    EmojiPicker,
    FilePreview
  },
  setup() {
    const userList = ref([])
    const selectedUser = ref(null)
    const messageInput = ref('')
    const messageListRef = ref(null)
    const currentUser = ref({})
    
    // 聊天消息存储，按用户ID分组
    const chatMessages = reactive({})
    
    // 用户在线状态 - 使用ref确保响应式
    const userOnlineStatus = ref({})
    
    // 未读消息计数
    const unreadCounts = reactive({})

        // 初始化
    onMounted(async () => {
      console.log('Chat页面挂载开始')
      
      // 获取当前用户信息
      const userData = localStorage.getItem('userInfo')
      if (userData) {
        currentUser.value = JSON.parse(userData)
        console.log('当前用户:', currentUser.value)
      }

      // 注册本地状态变化监听器（优先注册）
      registerLocalStatusHandlers()
      
      // 注册WebSocket消息处理器
      registerWebSocketHandlers()

      // 加载用户列表
      await loadUserList()
      
      // 强制刷新当前用户状态
      await forceRefreshCurrentUserStatus()

      // 确保WebSocket连接（由App.vue全局管理，这里只是确保连接状态）
      if (!webSocketService.isConnected()) {
        try {
          await webSocketService.connect(currentUser.value)
        } catch (error) {
          ElMessage.error('WebSocket连接失败')
        }
      }

      // 统一进行状态同步，避免多次重复请求
      if (webSocketService.isConnected()) {
        try {
          await webSocketService.syncUserStatus()
          console.log('Chat页面状态同步完成')
        } catch (error) {
          console.error('Chat页面状态同步失败:', error)
          // 降级处理：单独请求状态
          setTimeout(() => {
            webSocketService.requestOnlineUserStatus(true)
          }, 1000)
        }
      }
      
      // 启动定期状态检查
      startStatusCheck()
      
      console.log('Chat页面挂载完成')
    })

    // 使用路由守卫监听页面进入
    const router = useRouter()
    
    // 监听路由变化，当进入聊天页面时刷新状态
    const handleRouteChange = () => {
      if (router.currentRoute.value.name === 'Chat') {
        console.log('检测到进入Chat页面')
        // 确保当前用户状态为在线
        if (currentUser.value) {
          userOnlineStatus.value[currentUser.value.id] = 'online'
          userOnlineStatus.value = { ...userOnlineStatus.value }
        }
      }
    }
    
    // 监听路由变化（简化处理）
    watch(() => router.currentRoute.value.name, handleRouteChange, { immediate: true })

    onUnmounted(() => {
      // 清理WebSocket消息处理器（不断开连接，由全局管理）
      webSocketService.offMessage('CHAT_MESSAGE', handleChatMessage)
      webSocketService.offMessage('USER_STATUS', handleUserStatus)
      webSocketService.offMessage('USER_AVATAR_UPDATE', handleUserAvatarUpdate)
      
      // 清理本地状态监听器
      window.removeEventListener('userStatusChange', handleLocalStatusChange)
      
      // 清理定期检查
      stopStatusCheck()
    })

    // 加载用户列表
    const loadUserList = async () => {
      try {
        const response = await getUserList()
        if (response.data.code === 200) {
          userList.value = response.data.data
          // 初始化聊天消息存储和未读计数
          userList.value.forEach(user => {
            if (!chatMessages[user.id]) {
              chatMessages[user.id] = []
            }
            if (!unreadCounts[user.id]) {
              unreadCounts[user.id] = 0
            }
            // 初始化用户状态
            if (currentUser.value && user.id === currentUser.value.id) {
              // 当前用户总是设为在线
              userOnlineStatus.value[user.id] = 'online'
              console.log(`设置当前用户${user.id}为在线状态`)
            } else {
              // 其他用户默认为离线，等待WebSocket更新
              if (userOnlineStatus.value[user.id] === undefined) {
                userOnlineStatus.value[user.id] = 'offline'
              }
            }
          })
          
          // 强制触发响应式更新
          userOnlineStatus.value = { ...userOnlineStatus.value }
          
          console.log('用户列表加载完成，当前状态:', userOnlineStatus.value)
          
          // 请求用户在线状态更新
          if (webSocketService.isConnected()) {
            setTimeout(() => {
              webSocketService.requestOnlineUserStatus()
            }, 600)
          }
        } else {
          ElMessage.error(response.data.message || '获取用户列表失败')
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
        ElMessage.error('获取用户列表失败')
      }
    }

    // 刷新用户列表
    const refreshUserList = () => {
      // 立即设置当前用户为在线状态
      if (currentUser.value) {
        userOnlineStatus.value[currentUser.value.id] = 'online'
        userOnlineStatus.value = { ...userOnlineStatus.value }
      }
      
      loadUserList()
      
      // 手动刷新时请求用户状态更新
      if (webSocketService.isConnected()) {
        setTimeout(() => {
          webSocketService.requestOnlineUserStatus()
        }, 200)
      }
    }

    // 加载聊天历史记录
    const loadChatHistory = async (chatUserId) => {
      try {
        console.log(`开始加载用户${chatUserId}的聊天历史记录`)
        
        const response = await getChatHistory({
          chatUserId: chatUserId,
          page: 1,
          size: 50  // 加载最近50条消息
        })
        
        console.log('聊天历史记录API响应:', response.data)
        
        if (response.data.code === 200) {
          const messages = response.data.data || []
          console.log(`收到${messages.length}条历史消息:`, messages)
          
          // 将历史消息添加到聊天记录中（如果还没有的话）
          if (!chatMessages[chatUserId]) {
            chatMessages[chatUserId] = []
          }
          
          // 清空现有消息，用历史记录替换
          chatMessages[chatUserId] = messages.map(msg => {
            console.log('处理历史消息:', msg)
            return {
              id: msg.id,
              senderId: msg.senderId,
              senderName: msg.senderName,
              content: msg.content,
              timestamp: msg.createdTime ? new Date(msg.createdTime).getTime() : Date.now()
            }
          }).sort((a, b) => a.timestamp - b.timestamp)  // 按时间正序排列
          
          console.log(`用户${chatUserId}的聊天记录已更新:`, chatMessages[chatUserId])
          console.log(`加载了用户${chatUserId}的${messages.length}条历史消息`)
        } else {
          console.warn('加载聊天历史失败:', response.data.message)
          ElMessage.warning(response.data.message || '加载聊天历史失败')
        }
      } catch (error) {
        console.error('加载聊天历史失败:', error)
        ElMessage.error('加载聊天历史失败，请重试')
      }
    }

    // 注册WebSocket消息处理器
    const registerWebSocketHandlers = () => {
      webSocketService.onMessage('CHAT_MESSAGE', handleChatMessage)
      webSocketService.onMessage('USER_STATUS', handleUserStatus)
      webSocketService.onMessage('USER_AVATAR_UPDATE', handleUserAvatarUpdate)
    }

    // 注册本地状态变化监听器
    const registerLocalStatusHandlers = () => {
      window.addEventListener('userStatusChange', handleLocalStatusChange)
    }

    // 处理本地状态变化（立即更新，不等待WebSocket）
    const handleLocalStatusChange = (event) => {
      const { userId, status, immediate, source } = event.detail
      if (immediate && userId) {
        console.log(`处理本地状态变化: 用户${userId} -> ${status} (来源: ${source || 'unknown'})`)
        
        // 确保状态对象存在
        if (!userOnlineStatus.value) {
          userOnlineStatus.value = {}
        }
        
        // 检查是否需要更新（避免不必要的响应式触发）
        const currentStatus = userOnlineStatus.value[userId]
        if (currentStatus !== status) {
          // 更新状态
          userOnlineStatus.value[userId] = status
          
          // 强制触发响应式更新
          userOnlineStatus.value = { ...userOnlineStatus.value }
          
          console.log(`用户${userId}状态已更新为${status}，当前状态:`, userOnlineStatus.value[userId])
        } else {
          console.log(`用户${userId}状态无变化，跳过更新`)
        }
      }
    }

    // 处理聊天消息
    const handleChatMessage = (message) => {
      console.log('收到聊天消息:', message)
      
      // 确定消息对应的用户ID（发送者或接收者）
      let chatUserId
      if (message.senderId === currentUser.value.id) {
        // 当前用户发送的消息
        chatUserId = message.receiverId
      } else {
        // 接收到的消息
        chatUserId = message.senderId
      }

      // 添加消息到对应用户的聊天记录
      if (chatUserId && chatMessages[chatUserId]) {
        const chatMsg = {
          id: message.messageId || Date.now(),
          senderId: message.senderId,
          senderName: message.senderName,
          content: message.content,
          timestamp: message.timestamp
        }
        
        // 检查是否已存在相同的消息（防止重复）
        const existingMessage = chatMessages[chatUserId].find(msg => 
          msg.id === chatMsg.id || 
          (msg.senderId === chatMsg.senderId && 
           msg.content === chatMsg.content && 
           Math.abs(msg.timestamp - chatMsg.timestamp) < 1000)
        )
        
        if (!existingMessage) {
          chatMessages[chatUserId].push(chatMsg)
          
          // 如果不是当前用户发送的消息且不是当前选中的用户，增加未读计数
          if (message.senderId !== currentUser.value.id && 
              (!selectedUser.value || selectedUser.value.id !== chatUserId)) {
            unreadCounts[chatUserId]++
          }
          
          // 滚动到底部
          nextTick(() => {
            scrollToBottom()
          })
        }
      }
    }

    // 处理用户状态更新
    const handleUserStatus = (message) => {
      console.log('收到用户状态更新:', message)
      if (message.senderId && message.content?.status) {
        const oldStatus = userOnlineStatus.value[message.senderId]
        userOnlineStatus.value[message.senderId] = message.content.status
        console.log(`用户${message.senderId}状态从${oldStatus}更新为${message.content.status}`)
        
        // 强制触发响应式更新
        userOnlineStatus.value = { ...userOnlineStatus.value }
      }
    }

    // 处理用户头像更新
    const handleUserAvatarUpdate = (message) => {
      console.log('收到用户头像更新:', message)
      if (message.senderId && message.content) {
        // 更新用户列表中对应用户的头像和姓名
        const userIndex = userList.value.findIndex(user => user.id === message.senderId)
        if (userIndex !== -1) {
          if (message.content.avatar) {
            userList.value[userIndex].avatar = message.content.avatar
          }
          if (message.content.realName) {
            userList.value[userIndex].realName = message.content.realName
          }
        }
        
        // 如果当前选中的用户是更新头像的用户，也要更新
        if (selectedUser.value && selectedUser.value.id === message.senderId) {
          if (message.content.avatar) {
            selectedUser.value.avatar = message.content.avatar
          }
          if (message.content.realName) {
            selectedUser.value.realName = message.content.realName
          }
        }
      }
    }

    // 选择用户
    const selectUser = async (user) => {
      selectedUser.value = user
      // 清除该用户的未读计数
      unreadCounts[user.id] = 0
      
      // 加载与该用户的聊天历史记录
      await loadChatHistory(user.id)
      
      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
    }

    // 插入表情
    const insertEmoji = (emojiCode) => {
      messageInput.value += emojiCode
    }

    // 文件选择前验证
    const beforeFileUpload = (file) => {
      console.log('文件验证:', file.name, '大小:', file.size)
      const isLt50M = file.size / 1024 / 1024 < 50
      if (!isLt50M) {
        ElMessage.error('文件大小不能超过 50MB!')
        return false
      }
      return true
    }

    // 处理文件选择
    const handleFileSelect = async (file, fileList) => {
      console.log('文件选择事件触发:', file)
      
      if (!selectedUser.value) {
        ElMessage.warning('请先选择聊天对象')
        return
      }

      if (!file || !file.raw) {
        console.error('文件对象不正确:', file)
        ElMessage.error('文件选择失败')
        return
      }

      // 验证文件大小
      if (!beforeFileUpload(file.raw)) {
        return
      }

      const loading = ElLoading.service({
        lock: true,
        text: '文件上传中...',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      try {
        console.log('开始上传文件:', file.name, '用户ID:', currentUser.value.id)
        const response = await uploadChatFile(file.raw, currentUser.value.id)
        console.log('文件上传响应:', response)
        
        if (response.data.code === 200) {
          const fileInfo = response.data.data
          
          // 构造文件消息内容
          const fileMessage = `FILE:${JSON.stringify({
            fileName: fileInfo.fileName,
            fileUrl: fileInfo.fileUrl,
            fileSize: fileInfo.fileSize,
            fileType: fileInfo.fileType
          })}`
          
          console.log('发送文件消息:', fileMessage)
          
          // 通过WebSocket发送文件消息
          webSocketService.sendChatMessage(fileMessage, selectedUser.value.id)
          
          ElMessage.success('文件发送成功')
        } else {
          ElMessage.error(response.data.message || '文件上传失败')
        }
      } catch (error) {
        console.error('文件上传失败:', error)
        ElMessage.error('文件上传失败: ' + error.message)
      } finally {
        loading.close()
      }
    }

    // 发送消息
    const sendMessage = () => {
      if (!messageInput.value.trim() || !selectedUser.value) {
        return
      }

      const content = messageInput.value.trim()
      
      // 通过WebSocket发送消息
      webSocketService.sendChatMessage(content, selectedUser.value.id)
      
      // 清空输入框
      messageInput.value = ''
    }

    // 获取当前选中用户的消息列表
    const getCurrentMessages = () => {
      if (!selectedUser.value) return []
      return chatMessages[selectedUser.value.id] || []
    }

    // 格式化时间
    const formatTime = (timestamp) => {
      const date = new Date(timestamp)
      const now = new Date()
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
      
      if (msgDate.getTime() === today.getTime()) {
        // 今天的消息只显示时间
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      } else {
        // 其他日期显示日期和时间
        return date.toLocaleString('zh-CN', { 
          month: '2-digit', 
          day: '2-digit', 
          hour: '2-digit', 
          minute: '2-digit' 
        })
      }
    }

    // 获取用户状态样式
    const getUserStatusClass = (userId) => {
      // 当前用户始终显示为在线
      if (currentUser.value && userId === currentUser.value.id) {
        return 'online'
      }
      const status = userOnlineStatus.value[userId] || 'offline'
      return status === 'online' ? 'online' : 'offline'
    }

    // 获取用户状态文本
    const getUserStatusText = (userId) => {
      // 当前用户始终显示为在线
      if (currentUser.value && userId === currentUser.value.id) {
        return '在线'
      }
      const status = userOnlineStatus.value[userId] || 'offline'
      return status === 'online' ? '在线' : '离线'
    }

    // 获取未读消息数量
    const getUnreadCount = (userId) => {
      return unreadCounts[userId] || 0
    }

    // 获取用户显示名称
    const getUserDisplayName = (user) => {
      return user.realName || user.username || `用户${user.id}`
    }

    // 获取用户头像
    const getUserAvatar = (userId) => {
      const user = userList.value.find(u => u.id === userId)
      return user ? user.avatar : ''
    }

    // 获取用户详情
    const getUserById = (userId) => {
      return userList.value.find(u => u.id === userId) || { id: userId, username: `用户${userId}` }
    }

    // 判断是否为文件消息
    const isFileMessage = (content) => {
      return content && content.startsWith('FILE:')
    }

    // 获取文件信息
    const getFileInfo = (content) => {
      if (!isFileMessage(content)) return null
      try {
        return JSON.parse(content.substring(5)) // 移除"FILE:"前缀
      } catch (error) {
        console.error('解析文件信息失败:', error)
        return null
      }
    }

    // 解析消息内容（处理表情）
    const parseMessageContent = (content) => {
      if (isFileMessage(content)) return content
      return parseEmojis(content)
    }

    // 滚动到底部
    const scrollToBottom = () => {
      if (messageListRef.value) {
        messageListRef.value.scrollTop = messageListRef.value.scrollHeight
      }
    }

    // 定期状态检查
    let statusCheckInterval = null
    
    const startStatusCheck = () => {
      // 每15秒检查一次状态
      statusCheckInterval = setInterval(() => {
        // 确保当前用户始终为在线状态
        if (currentUser.value) {
          const needsUpdate = userOnlineStatus.value[currentUser.value.id] !== 'online'
          if (needsUpdate) {
            userOnlineStatus.value[currentUser.value.id] = 'online'
            userOnlineStatus.value = { ...userOnlineStatus.value }
            console.log('定期检查：更新当前用户为在线状态')
          }
        }
        
        // 请求最新的用户状态
        if (webSocketService.isConnected()) {
          webSocketService.requestOnlineUserStatus()
        }
      }, 15000)
    }
    
    const stopStatusCheck = () => {
      if (statusCheckInterval) {
        clearInterval(statusCheckInterval)
        statusCheckInterval = null
      }
    }

    // 强制刷新当前用户状态
    const forceRefreshCurrentUserStatus = async () => {
      if (currentUser.value) {
        console.log('强制刷新当前用户状态:', currentUser.value.id)
        
        // 立即设置为在线状态
        userOnlineStatus.value[currentUser.value.id] = 'online'
        userOnlineStatus.value = { ...userOnlineStatus.value }
        
        // 发送WebSocket状态更新
        if (webSocketService.isConnected()) {
          webSocketService.sendUserStatus('online')
        }
        
        // 触发本地状态更新事件
        window.dispatchEvent(new CustomEvent('userStatusChange', {
          detail: {
            userId: currentUser.value.id,
            status: 'online',
            immediate: true,
            source: 'force_refresh'
          }
        }))
        
        console.log('当前用户状态已强制更新为在线')
      }
    }

    return {
      userList,
      selectedUser,
      messageInput,
      messageListRef,
      currentUser,
      Refresh,
      Document,
      refreshUserList,
      selectUser,
      sendMessage,
      insertEmoji,
      handleFileSelect,
      getCurrentMessages,
      formatTime,
      getUserStatusClass,
      getUserStatusText,
      getUnreadCount,
      getUserDisplayName,
      getUserAvatar,
      getUserById,
      isFileMessage,
      getFileInfo,
      parseMessageContent
    }
  }
}
</script>

<style scoped>
.chat-container {
  height: 100%;
  padding: 20px;
}

.chat-layout {
  display: flex;
  height: calc(100vh - 160px);
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 用户列表样式 */
.user-list {
  width: 300px;
  border-right: 1px solid #e8e8e8;
  background: #fafafa;
}

.user-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e8e8e8;
  background: #fff;
}

.user-list-header h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.user-list-content {
  height: calc(100% - 65px);
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
  position: relative;
}

.user-item:hover {
  background: #f5f5f5;
}

.user-item.active {
  background: #e6f7ff;
  border-left: 3px solid #409EFF;
}

.user-avatar {
  margin-right: 12px;
  background: #409EFF;
  color: #fff;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.user-status {
  font-size: 12px;
}

.user-status.online {
  color: #52c41a;
}

.user-status.offline {
  color: #999;
}

.unread-badge {
  background: #f56c6c;
  color: #fff;
  border-radius: 10px;
  min-width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  padding: 0 6px;
}

/* 聊天区域样式 */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.no-chat-selected {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #fafafa;
}

.chat-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e8e8e8;
  background: #fff;
}

.chat-user-info {
  margin-left: 12px;
}

.chat-user-name {
  font-size: 16px;
  color: #333;
  margin-bottom: 4px;
}

.chat-user-status {
  font-size: 12px;
}

.chat-user-status.online {
  color: #52c41a;
}

.chat-user-status.offline {
  color: #999;
}

/* 消息列表样式 */
.message-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f8f9fa;
}

.message-item {
  margin-bottom: 10px;
  display: flex;
}

.message-item.own-message {
  justify-content: flex-end;
}

.message-wrapper {
  display: flex;
  align-items: center;
  max-width: 75%;
}

.message-wrapper.own {
  flex-direction: row;
  margin-left: auto;
  justify-content: flex-end;
}

.message-avatar {
  margin: 0 8px;
  background: #409EFF;
  color: #fff;
  flex-shrink: 0;
}

.message-wrapper.own .message-avatar.own-avatar {
  margin-left: 8px;
  margin-right: 0;
}

.message-wrapper.own .message-info {
  margin-right: 0;
  flex: none;
}

.message-info {
  flex: 1;
  min-width: 0;
}

.message-sender {
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
  padding: 0 12px;
}

.message-wrapper.own .message-sender {
  text-align: right;
  color: #b3d1ff;
}

.message-content {
  background: #fff;
  border-radius: 12px;
  padding: 6px 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  position: relative;
}

.message-wrapper.own .message-content {
  background: #409EFF;
  color: #fff;
}

/* 添加聊天气泡的小三角 */
.message-content::before {
  content: '';
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  left: -8px;
  width: 0;
  height: 0;
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
  border-right: 8px solid #fff;
}

.message-wrapper.own .message-content::before {
  left: auto;
  right: -8px;
  border-left: 8px solid #409EFF;
  border-right: none;
}

.message-text {
  margin-bottom: 3px;
  word-wrap: break-word;
  line-height: 1.3;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
  text-align: right;
}

/* 输入区域样式 */
.message-input {
  padding: 20px;
  border-top: 1px solid #e8e8e8;
  background: #fff;
}

.input-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding: 8px 0;
}

.message-file {
  margin: 4px 0;
}

/* 表情样式 */
.emoji-sprite {
  width: 20px;
  height: 20px;
  background-image: url('http://113.45.161.48:9000/admin-system/emojis/emoji-sprite.png');
  background-repeat: no-repeat;
  display: inline-block;
  vertical-align: middle;
}

/* 表情位置定义 */
.emoji-smile { background-position: 0 0; }
.emoji-laugh { background-position: -24px 0; }
.emoji-wink { background-position: -48px 0; }
.emoji-kiss { background-position: -72px 0; }
.emoji-heart-eyes { background-position: -96px 0; }
.emoji-cool { background-position: -120px 0; }
.emoji-cry { background-position: 0 -24px; }
.emoji-angry { background-position: -24px -24px; }
.emoji-surprised { background-position: -48px -24px; }
.emoji-confused { background-position: -72px -24px; }
.emoji-shy { background-position: -96px -24px; }
.emoji-tired { background-position: -120px -24px; }
.emoji-rage { background-position: 0 -48px; }
.emoji-sick { background-position: -24px -48px; }
.emoji-thinking { background-position: -48px -48px; }
.emoji-neutral { background-position: -72px -48px; }
.emoji-tongue { background-position: -96px -48px; }
.emoji-sleep { background-position: -120px -48px; }
.emoji-thumbs-up { background-position: 0 -72px; }
.emoji-thumbs-down { background-position: -24px -72px; }
.emoji-clap { background-position: -48px -72px; }
.emoji-pray { background-position: -72px -72px; }
.emoji-victory { background-position: -96px -72px; }
.emoji-heart { background-position: -120px -72px; }

/* 滚动条样式 */
.user-list-content::-webkit-scrollbar,
.message-list::-webkit-scrollbar {
  width: 6px;
}

.user-list-content::-webkit-scrollbar-track,
.message-list::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.user-list-content::-webkit-scrollbar-thumb,
.message-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.user-list-content::-webkit-scrollbar-thumb:hover,
.message-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style> 