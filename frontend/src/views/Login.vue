<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">用户登录</h2>
      <el-form ref="loginForm" :model="loginData" class="login-form">
        <el-form-item>
          <el-input
            v-model="loginData.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="loginData.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            @click="handleLogin"
            :loading="loading"
          >
            登录
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="register-link">
            还没有账号？
            <el-link type="primary" @click="goToRegister">立即注册</el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/user'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    
    const loginData = reactive({
      username: '',
      password: ''
    })

    const handleLogin = async () => {
      loading.value = true
      try {
        const response = await login(loginData)
        if (response.data.code === 200) {
          ElMessage.success('登录成功')
          // 保存用户信息到本地存储
          localStorage.setItem('userInfo', JSON.stringify(response.data.data))
          
          // 触发storage事件，让App.vue知道用户已登录
          window.dispatchEvent(new StorageEvent('storage', {
            key: 'userInfo',
            newValue: JSON.stringify(response.data.data),
            storageArea: localStorage
          }))
          
          router.push('/dashboard')
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        ElMessage.error('登录失败，请检查网络连接')
      } finally {
        loading.value = false
      }
    }

    const goToRegister = () => {
      router.push('/register')
    }

    return {
      loginData,
      loading,
      handleLogin,
      goToRegister
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  width: 400px;
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
  font-weight: bold;
}

.login-form {
  margin-top: 20px;
}

.register-link {
  text-align: center;
  margin-top: 10px;
}
</style> 