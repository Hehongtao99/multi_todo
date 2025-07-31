<template>
  <div class="register-container">
    <div class="register-box">
      <h2 class="register-title">用户注册</h2>
      <el-form ref="registerForm" :model="registerData" class="register-form">
        <el-form-item>
          <el-input
            v-model="registerData.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="registerData.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="registerData.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
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
            @click="handleRegister"
            :loading="loading"
          >
            注册
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="login-link">
            已有账号？
            <el-link type="primary" @click="goToLogin">立即登录</el-link>
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
import { register } from '../api/user'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    
    const registerData = reactive({
      username: '',
      password: '',
      confirmPassword: ''
    })

    const handleRegister = async () => {
      // 简单检查两次密码是否一致
      if (registerData.password !== registerData.confirmPassword) {
        ElMessage.error('两次输入的密码不一致')
        return
      }

      loading.value = true
      try {
        const response = await register(registerData)
        if (response.data.code === 200) {
          ElMessage.success('注册成功，请登录')
          router.push('/login')
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        ElMessage.error('注册失败，请检查网络连接')
      } finally {
        loading.value = false
      }
    }

    const goToLogin = () => {
      router.push('/login')
    }

    return {
      registerData,
      loading,
      handleRegister,
      goToLogin
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  width: 400px;
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
  font-weight: bold;
}

.register-form {
  margin-top: 20px;
}

.login-link {
  text-align: center;
  margin-top: 10px;
}
</style> 