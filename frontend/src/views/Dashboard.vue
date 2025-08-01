<template>
  <div class="dashboard-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="250px" class="sidebar">
        <div class="logo">
          <h2>多人待办系统</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          @select="handleMenuSelect"
        >
          <el-menu-item index="projects">
            <el-icon><Folder /></el-icon>
            <span>项目管理</span>
          </el-menu-item>
          <el-menu-item index="todos">
            <el-icon><List /></el-icon>
            <span>待办事项</span>
          </el-menu-item>
          <el-menu-item v-if="userInfo.auth === 'admin'" index="users">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>实时聊天</span>
          </el-menu-item>
          <el-menu-item index="profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主要内容区域 -->
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="header">
          <div class="header-left">
            <span class="welcome-text">欢迎回来，{{ userInfo.username }}！</span>
          </div>
          <div class="header-right">
            <el-button v-if="showNotificationButton" @click="requestNotificationPermission" type="info" size="small" plain style="margin-right: 15px;">
              开启消息通知
            </el-button>
            <el-dropdown @command="handleUserCommand">
              <span class="user-dropdown">
                <el-icon><User /></el-icon>
                {{ userInfo.username }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主要内容 -->
        <el-main class="main-content">
          <router-view v-if="currentView" />
          <div v-else class="content-placeholder">
            <el-card class="welcome-card">
              <h3>欢迎使用多人待办系统</h3>
              <p>您当前的权限级别：<el-tag :type="userInfo.auth === 'admin' ? 'danger' : 'success'">{{ userInfo.auth === 'admin' ? '管理员' : '普通用户' }}</el-tag></p>
              <div class="feature-list">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-card class="feature-card" @click="handleMenuSelect('projects')">
                      <div class="feature-icon">
                        <el-icon><Folder /></el-icon>
                      </div>
                      <h4>项目管理</h4>
                      <p>管理您的项目，组织待办事项</p>
                    </el-card>
                  </el-col>
                  <el-col :span="12">
                    <el-card class="feature-card" @click="handleMenuSelect('todos')">
                      <div class="feature-icon">
                        <el-icon><List /></el-icon>
                      </div>
                      <h4>待办事项</h4>
                      <p>创建和管理您的待办任务</p>
                    </el-card>
                  </el-col>
                  <el-col v-if="userInfo.auth === 'admin'" :span="12">
                    <el-card class="feature-card" @click="handleMenuSelect('users')">
                      <div class="feature-icon">
                        <el-icon><UserFilled /></el-icon>
                      </div>
                      <h4>用户管理</h4>
                      <p>管理系统用户和权限</p>
                    </el-card>
                  </el-col>
                  <el-col :span="12">
                    <el-card class="feature-card" @click="handleMenuSelect('chat')">
                      <div class="feature-icon">
                        <el-icon><ChatDotRound /></el-icon>
                      </div>
                      <h4>实时聊天</h4>
                      <p>{{ userInfo.auth === 'admin' ? '与普通用户进行实时聊天' : '与管理员进行实时聊天' }}</p>
                    </el-card>
                  </el-col>
                </el-row>
              </div>
            </el-card>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import notificationUtil from '../utils/notification.js'

export default {
  name: 'Dashboard',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const userInfo = ref({})
    const activeMenu = ref('')
    const showNotificationButton = ref(false)

    // 是否显示子路由内容
    const currentView = computed(() => {
      return route.path !== '/dashboard'
    })

    onMounted(() => {
      // 从本地存储获取用户信息
      const userData = localStorage.getItem('userInfo')
      if (userData) {
        userInfo.value = JSON.parse(userData)
      } else {
        // 如果没有用户信息，跳转到登录页
        router.push('/login')
      }
      
      // 检查通知权限状态
      checkNotificationPermission()

      // 设置默认激活菜单
      if (route.path.includes('/projects')) {
        activeMenu.value = 'projects'
      } else if (route.path.includes('/users')) {
        activeMenu.value = 'users'
      } else if (route.path.includes('/todos')) {
        activeMenu.value = 'todos'
      } else if (route.path.includes('/chat')) {
        activeMenu.value = 'chat'
      }
    })

    const handleUserCommand = (command) => {
      if (command === 'logout') {
        // 触发storage事件，让App.vue知道用户已退出
        window.dispatchEvent(new StorageEvent('storage', {
          key: 'userInfo',
          oldValue: localStorage.getItem('userInfo'),
          newValue: null,
          storageArea: localStorage
        }))
        
        localStorage.removeItem('userInfo')
        ElMessage.success('退出成功')
        router.push('/login')
      } else if (command === 'profile') {
        router.push('/dashboard/profile')
      }
    }

    const handleMenuSelect = (key) => {
      activeMenu.value = key
      switch (key) {
        case 'projects':
          router.push('/dashboard/projects')
          break
        case 'users':
          router.push('/dashboard/users')
          break
        case 'todos':
          router.push('/dashboard/todos')
          break
        case 'chat':
          router.push('/dashboard/chat')
          break
        case 'profile':
          router.push('/dashboard/profile')
          break
        default:
          break
      }
    }

    // 检查通知权限状态
    const checkNotificationPermission = () => {
      const permissionStatus = notificationUtil.getPermissionStatus()
      // 只有当权限状态为default（未决定）时才显示按钮
      showNotificationButton.value = permissionStatus === 'default'
    }
    
    // 请求通知权限
    const requestNotificationPermission = async () => {
      try {
        const granted = await notificationUtil.requestPermission()
        if (granted) {
          ElMessage.success('通知权限已开启')
          showNotificationButton.value = false
        } else {
          ElMessage.warning('您拒绝了通知权限，将无法收到消息提醒')
        }
      } catch (error) {
        console.error('请求通知权限失败:', error)
        ElMessage.error('请求通知权限失败')
      }
    }
    
    return {
      userInfo,
      activeMenu,
      currentView,
      handleUserCommand,
      handleMenuSelect,
      ChatDotRound,
      showNotificationButton,
      requestNotificationPermission
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  border-right: 1px solid #e6e6e6;
}

.logo {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #434c5e;
}

.logo h2 {
  color: #fff;
  margin: 0;
  font-size: 18px;
}

.sidebar-menu {
  border: none;
  height: calc(100vh - 80px);
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.welcome-text {
  font-size: 16px;
  color: #333;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.main-content {
  background-color: #f5f5f5;
  padding: 20px;
}

.content-placeholder {
  height: 100%;
}

.welcome-card {
  text-align: center;
  padding: 20px;
}

.welcome-card h3 {
  margin-bottom: 20px;
  color: #333;
}

.feature-list {
  margin-top: 30px;
}

.feature-card {
  text-align: center;
  padding: 20px;
  height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.feature-icon {
  font-size: 40px;
  color: #409EFF;
  margin-bottom: 15px;
}

.feature-card h4 {
  margin: 10px 0;
  color: #333;
}

.feature-card p {
  color: #666;
  font-size: 14px;
}

.feature-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style> 