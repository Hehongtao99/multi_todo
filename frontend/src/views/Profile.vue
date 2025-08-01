<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      
      <div class="profile-content">
        <!-- 头像部分 -->
        <div class="avatar-section">
          <div class="avatar-container">
            <el-avatar 
              :size="120" 
              :src="profileData.avatar" 
              class="profile-avatar"
            >
              {{ getAvatarText() }}
            </el-avatar>
            <div class="avatar-actions">
              <el-button 
                type="primary" 
                size="small" 
                @click="showAvatarDialog = true"
                :icon="Edit"
              >
                更换头像
              </el-button>
            </div>
          </div>
        </div>

        <!-- 信息表单 -->
        <div class="info-section">
          <el-form 
            :model="profileData" 
            :rules="rules" 
            ref="profileFormRef" 
            label-width="100px"
            class="profile-form"
          >
            <el-form-item label="用户名">
              <el-input v-model="profileData.username" disabled />
            </el-form-item>
            
            <el-form-item label="姓名" prop="realName">
              <el-input 
                v-model="profileData.realName" 
                placeholder="请输入您的真实姓名"
                maxlength="20"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="权限">
              <el-tag :type="profileData.auth === 'admin' ? 'danger' : 'info'">
                {{ profileData.auth === 'admin' ? '管理员' : '普通用户' }}
              </el-tag>
            </el-form-item>
            
            <el-form-item label="注册时间">
              <el-input :value="formatTime(profileData.createdTime)" disabled />
            </el-form-item>
          </el-form>
          
          <div class="form-actions">
            <el-button type="primary" @click="updateProfile" :loading="loading">
              保存修改
            </el-button>
            <el-button @click="resetForm">
              重置
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 头像设置对话框 -->
    <el-dialog
      v-model="showAvatarDialog"
      title="设置头像"
      width="600px"
      @close="resetAvatarDialog"
    >
      <div class="avatar-dialog-content">
        <div class="upload-section">
          <h4>上传头像</h4>
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="handleAvatarUpload"
            accept="image/*"
            drag
          >
            <div class="upload-area">
              <el-icon class="avatar-uploader-icon" v-if="!uploadedAvatarUrl">
                <Plus />
              </el-icon>
              <img v-if="uploadedAvatarUrl" :src="uploadedAvatarUrl" class="uploaded-avatar" />
              <div class="upload-text">
                <p v-if="!uploadedAvatarUrl">点击或拖拽图片到此处上传</p>
                <p v-else>点击重新上传</p>
                <p class="upload-tip">支持 JPG、PNG、GIF 格式，文件大小不超过 5MB</p>
              </div>
            </div>
          </el-upload>
        </div>
        
        <el-divider>或</el-divider>
        
        <div class="preset-section">
          <h4>选择预设头像</h4>
          <div class="preset-avatars">
            <div 
              v-for="(avatar, index) in presetAvatars" 
              :key="index"
              :class="['preset-avatar-item', { active: selectedAvatar === avatar }]"
              @click="selectedAvatar = avatar"
            >
              <el-avatar :size="60" :src="avatar">
                {{ getAvatarText() }}
              </el-avatar>
            </div>
          </div>
        </div>
        
        <div class="custom-avatar">
          <h4>自定义头像URL</h4>
          <el-input 
            v-model="customAvatarUrl"
            placeholder="请输入头像图片链接"
            @input="selectedAvatar = customAvatarUrl"
          />
          <div class="avatar-preview" v-if="customAvatarUrl">
            <h5>预览</h5>
            <el-avatar :size="80" :src="customAvatarUrl">
              {{ getAvatarText() }}
            </el-avatar>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAvatarDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmAvatar">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Plus } from '@element-plus/icons-vue'
import { getProfile, updateProfile as updateProfileApi, uploadAvatar } from '../api/profile.js'
import webSocketService from '../api/websocket.js'

export default {
  name: 'Profile',
  components: {
    Edit, Plus
  },
  setup() {
    const profileFormRef = ref(null)
    const loading = ref(false)
    const showAvatarDialog = ref(false)
    const selectedAvatar = ref('')
    const customAvatarUrl = ref('')
    const uploadedAvatarUrl = ref('') // 新增：用于上传头像的预览
    
    const profileData = reactive({
      id: null,
      username: '',
      realName: '',
      avatar: '',
      auth: '',
      createdTime: null,
      updatedTime: null
    })
    
    const originalData = reactive({})
    
    // 预设头像列表
    const presetAvatars = [
      'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
      'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png',
      'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      'https://fuss10.elemecdn.com/a/3f/3302e58f9a181d2509f3dc0fa68b0jpeg.jpeg',
      'https://fuss10.elemecdn.com/1/34/19aa98b1fcb2781c4fba33d850549jpeg.jpeg',
      'https://fuss10.elemecdn.com/0/6f/e35ff375812e6b0020b6b4e8f9583jpeg.jpeg'
    ]
    
    // 表单验证规则
    const rules = {
      realName: [
        { max: 20, message: '姓名长度不能超过20个字符', trigger: 'blur' }
      ]
    }
    
    // 初始化
    onMounted(async () => {
      await loadProfile()
    })
    
    // 加载个人信息
    const loadProfile = async () => {
      try {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        const response = await getProfile({ userId: userInfo.id })
        
        if (response.data.code === 200) {
          Object.assign(profileData, response.data.data)
          Object.assign(originalData, response.data.data)
        } else {
          ElMessage.error(response.data.message || '获取个人信息失败')
        }
      } catch (error) {
        console.error('加载个人信息失败:', error)
        ElMessage.error('加载个人信息失败')
      }
    }
    
    // 更新个人信息
    const updateProfile = async () => {
      if (!profileFormRef.value) return
      
      try {
        await profileFormRef.value.validate()
        loading.value = true
        
        const response = await updateProfileApi({
          userId: profileData.id,
          realName: profileData.realName,
          avatar: profileData.avatar
        })
        
        if (response.data.code === 200) {
          ElMessage.success('个人信息更新成功')
          
          // 更新本地存储的用户信息
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
          const oldAvatar = userInfo.avatar
          userInfo.realName = profileData.realName
          userInfo.avatar = profileData.avatar
          localStorage.setItem('userInfo', JSON.stringify(userInfo))
          
          // 如果头像发生变化，通过WebSocket通知其他用户
          if (oldAvatar !== profileData.avatar && webSocketService.isConnected()) {
            webSocketService.sendUserAvatarUpdate(profileData.avatar, profileData.realName)
          }
          
          // 更新原始数据
          Object.assign(originalData, profileData)
        } else {
          ElMessage.error(response.data.message || '更新个人信息失败')
        }
      } catch (error) {
        console.error('更新个人信息失败:', error)
        ElMessage.error('更新个人信息失败')
      } finally {
        loading.value = false
      }
    }
    
    // 重置表单
    const resetForm = () => {
      Object.assign(profileData, originalData)
    }
    
    // 获取头像文字
    const getAvatarText = () => {
      return profileData.realName 
        ? profileData.realName.charAt(0).toUpperCase()
        : profileData.username.charAt(0).toUpperCase()
    }
    
    // 确认头像选择
    const confirmAvatar = () => {
      let avatarUrl = ''
      
      // 优先使用上传的头像
      if (uploadedAvatarUrl.value) {
        avatarUrl = uploadedAvatarUrl.value
      } else if (selectedAvatar.value) {
        avatarUrl = selectedAvatar.value
      }
      
      if (avatarUrl) {
        profileData.avatar = avatarUrl
        showAvatarDialog.value = false
        resetAvatarDialog()
      } else {
        ElMessage.warning('请选择或上传一个头像')
      }
    }
    
    // 重置头像对话框
    const resetAvatarDialog = () => {
      selectedAvatar.value = ''
      customAvatarUrl.value = ''
      uploadedAvatarUrl.value = '' // 重置上传预览
    }
    
    // 格式化时间
    const formatTime = (timestamp) => {
      if (!timestamp) return ''
      return new Date(timestamp).toLocaleString('zh-CN')
    }

    // 上传头像前的钩子
    const beforeAvatarUpload = (rawFile) => {
      const isJPGPNG = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png'
      const isLt5M = rawFile.size / 1024 / 1024 < 5

      if (!isJPGPNG) {
        ElMessage.error('头像必须是 JPG/PNG 格式!')
      }
      if (!isLt5M) {
        ElMessage.error('头像大小不能超过 5MB!')
      }
      return isJPGPNG && isLt5M
    }

    // 处理上传头像
    const handleAvatarUpload = async (options) => {
      try {
        const response = await uploadAvatar(options.file, profileData.id)
        if (response.data.code === 200) {
          uploadedAvatarUrl.value = response.data.data
          selectedAvatar.value = response.data.data
          ElMessage.success('头像上传成功')

          // 发送WebSocket消息通知其他用户头像更新
          if (webSocketService.isConnected()) {
            webSocketService.sendUserAvatarUpdate(response.data.data, profileData.realName)
          }
        } else {
          ElMessage.error(response.data.message || '头像上传失败')
        }
      } catch (error) {
        console.error('头像上传失败:', error)
        ElMessage.error('头像上传失败')
      }
    }
    
    return {
      profileFormRef,
      loading,
      showAvatarDialog,
      selectedAvatar,
      customAvatarUrl,
      uploadedAvatarUrl, // 暴露给模板
      profileData,
      presetAvatars,
      rules,
      Edit,
      loadProfile,
      updateProfile,
      resetForm,
      getAvatarText,
      confirmAvatar,
      resetAvatarDialog,
      formatTime,
      beforeAvatarUpload, // 暴露给模板
      handleAvatarUpload // 暴露给模板
    }
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}

.profile-content {
  display: flex;
  gap: 40px;
}

.avatar-section {
  flex: 0 0 200px;
  text-align: center;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.profile-avatar {
  background: #409EFF;
  color: #fff;
  font-size: 36px;
  font-weight: 600;
  border: 4px solid #f0f0f0;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.info-section {
  flex: 1;
}

.profile-form {
  margin-bottom: 30px;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-start;
}

/* 头像对话框样式 */
.avatar-dialog-content {
  padding: 20px 0;
}

.upload-section, .preset-section {
  margin-bottom: 20px;
}

.upload-section h4, .preset-section h4 {
  margin-bottom: 15px;
  font-size: 16px;
}

.avatar-uploader {
  width: 100%;
}

.upload-area {
  width: 100%;
  height: 200px;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  text-align: center;
  position: relative;
  overflow: hidden;
  background-color: #fafafa;
  transition: border-color 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-area:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.uploaded-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
}

.upload-text {
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.upload-text p {
  margin: 5px 0;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
}

.preset-avatars {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.preset-avatar-item {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.preset-avatar-item:hover {
  border-color: #409EFF;
  background: #f0f9ff;
}

.preset-avatar-item.active {
  border-color: #409EFF;
  background: #e6f7ff;
}

.custom-avatar {
  margin-top: 20px;
}

.avatar-preview {
  margin-top: 15px;
  text-align: center;
}

.avatar-preview h5 {
  margin-bottom: 10px;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
    gap: 30px;
  }
  
  .avatar-section {
    flex: none;
  }
  
  .preset-avatars {
    grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
    gap: 10px;
  }
}
</style> 