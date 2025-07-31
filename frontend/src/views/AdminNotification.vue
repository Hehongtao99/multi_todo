<template>
  <div class="admin-notification">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>管理员通知发送</span>
        </div>
      </template>

      <el-form :model="notificationForm" :rules="rules" ref="notificationFormRef" label-width="120px">
        <el-form-item label="通知类型" prop="type">
          <el-radio-group v-model="notificationForm.type" @change="onTypeChange">
            <el-radio label="system">系统通知</el-radio>
            <el-radio label="project">项目通知</el-radio>
            <el-radio label="personal">个人通知</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="优先级" prop="priority">
          <el-select v-model="notificationForm.priority" placeholder="请选择优先级">
            <el-option label="低优先级" value="low"></el-option>
            <el-option label="普通" value="normal"></el-option>
            <el-option label="高优先级" value="high"></el-option>
            <el-option label="紧急" value="urgent"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item 
          v-if="notificationForm.type === 'project'" 
          label="目标项目" 
          prop="projectId"
        >
          <el-select 
            v-model="notificationForm.projectId" 
            placeholder="请选择项目"
            :loading="projectsLoading"
          >
            <el-option 
              v-for="project in projects" 
              :key="project.id" 
              :label="project.name" 
              :value="project.id"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item 
          v-if="notificationForm.type === 'personal'" 
          label="目标用户" 
          prop="receiverId"
        >
          <el-select 
            v-model="notificationForm.receiverId" 
            placeholder="请选择用户"
            :loading="usersLoading"
          >
            <el-option 
              v-for="user in users" 
              :key="user.id" 
              :label="user.username" 
              :value="user.id"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="通知标题" prop="title">
          <el-input 
            v-model="notificationForm.title" 
            placeholder="请输入通知标题"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="通知内容" prop="content">
          <el-input 
            v-model="notificationForm.content" 
            type="textarea" 
            placeholder="请输入通知内容"
            :rows="5"
            maxlength="1000"
            show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="过期时间">
          <el-date-picker
            v-model="notificationForm.expireTime"
            type="datetime"
            placeholder="选择过期时间（可选）"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="立即推送">
          <el-switch v-model="notificationForm.pushImmediately"></el-switch>
          <span class="ml-2 text-sm text-gray-500">开启后将立即通过WebSocket推送通知</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="sendNotification" :loading="sending">
            发送通知
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 通知历史 -->
    <el-card class="box-card mt-4">
      <template #header>
        <div class="card-header">
          <span>通知发送历史</span>
          <el-button size="small" @click="loadNotificationHistory">刷新</el-button>
        </div>
      </template>

      <el-table :data="notificationHistory" stripe v-loading="historyLoading">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="标题" min-width="200"></el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)" size="small">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发送时间" width="180"></el-table-column>
        <el-table-column prop="isPushed" label="推送状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isPushed ? 'success' : 'warning'" size="small">
              {{ row.isPushed ? '已推送' : '未推送' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import NotificationAPI from '../api/notification.js'
import ProjectAPI from '../api/project.js'
import UserAPI from '../api/user.js'

export default {
  name: 'AdminNotification',
  setup() {
    const notificationFormRef = ref()
    const sending = ref(false)
    const historyLoading = ref(false)
    const projectsLoading = ref(false)
    const usersLoading = ref(false)

    const notificationForm = reactive({
      type: 'system',
      priority: 'normal',
      projectId: null,
      receiverId: null,
      title: '',
      content: '',
      expireTime: null,
      pushImmediately: true
    })

    const rules = reactive({
      type: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
      priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
      projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
      receiverId: [{ required: true, message: '请选择用户', trigger: 'change' }],
      title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
      content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
    })

    const projects = ref([])
    const users = ref([])
    const notificationHistory = ref([])

    // 获取当前管理员信息（假设从localStorage获取）
    const currentAdmin = ref({
      id: 1,
      username: 'admin'
    })

    const onTypeChange = () => {
      // 清空相关字段
      notificationForm.projectId = null
      notificationForm.receiverId = null
    }

    const loadProjects = async () => {
      try {
        projectsLoading.value = true
        const result = await ProjectAPI.getAllProjects()
        if (result.code === 200) {
          projects.value = result.data
        }
      } catch (error) {
        console.error('加载项目列表失败:', error)
        ElMessage.error('加载项目列表失败')
      } finally {
        projectsLoading.value = false
      }
    }

    const loadUsers = async () => {
      try {
        usersLoading.value = true
        const result = await UserAPI.getAllUsers()
        if (result.code === 200) {
          users.value = result.data
        }
      } catch (error) {
        console.error('加载用户列表失败:', error)
        ElMessage.error('加载用户列表失败')
      } finally {
        usersLoading.value = false
      }
    }

    const sendNotification = async () => {
      try {
        await notificationFormRef.value.validate()
        sending.value = true

        const notificationData = {
          title: notificationForm.title,
          content: notificationForm.content,
          type: notificationForm.type,
          priority: notificationForm.priority,
          expireTime: notificationForm.expireTime,
          pushImmediately: notificationForm.pushImmediately
        }

        // 根据类型设置相应的字段
        if (notificationForm.type === 'project') {
          notificationData.projectId = notificationForm.projectId
        } else if (notificationForm.type === 'personal') {
          notificationData.receiverId = notificationForm.receiverId
        }

        let result
        switch (notificationForm.type) {
          case 'system':
            result = await NotificationAPI.sendSystemNotification(
              notificationData, 
              currentAdmin.value.id, 
              currentAdmin.value.username
            )
            break
          case 'project':
            result = await NotificationAPI.sendProjectNotification(
              notificationData, 
              currentAdmin.value.id, 
              currentAdmin.value.username
            )
            break
          case 'personal':
            result = await NotificationAPI.sendPersonalNotification(
              notificationData, 
              currentAdmin.value.id, 
              currentAdmin.value.username
            )
            break
        }

        if (result.code === 200) {
          ElMessage.success('通知发送成功')
          resetForm()
          loadNotificationHistory()
        } else {
          ElMessage.error(result.message || '通知发送失败')
        }

      } catch (error) {
        console.error('发送通知失败:', error)
        ElMessage.error('发送通知失败')
      } finally {
        sending.value = false
      }
    }

    const resetForm = () => {
      notificationFormRef.value.resetFields()
      notificationForm.type = 'system'
      notificationForm.priority = 'normal'
      notificationForm.projectId = null
      notificationForm.receiverId = null
      notificationForm.expireTime = null
      notificationForm.pushImmediately = true
    }

    const loadNotificationHistory = async () => {
      try {
        historyLoading.value = true
        // 这里需要一个获取管理员发送通知历史的API
        const result = await NotificationAPI.getSystemNotifications()
        if (result.code === 200) {
          notificationHistory.value = result.data
        }
      } catch (error) {
        console.error('加载通知历史失败:', error)
        ElMessage.error('加载通知历史失败')
      } finally {
        historyLoading.value = false
      }
    }

    const getTypeText = (type) => {
      const typeMap = {
        system: '系统',
        project: '项目',
        personal: '个人'
      }
      return typeMap[type] || type
    }

    const getTypeTagType = (type) => {
      const tagMap = {
        system: 'danger',
        project: 'warning',
        personal: 'success'
      }
      return tagMap[type] || ''
    }

    const getPriorityText = (priority) => {
      const priorityMap = {
        low: '低',
        normal: '普通',
        high: '高',
        urgent: '紧急'
      }
      return priorityMap[priority] || priority
    }

    const getPriorityTagType = (priority) => {
      const tagMap = {
        low: 'info',
        normal: '',
        high: 'warning',
        urgent: 'danger'
      }
      return tagMap[priority] || ''
    }

    onMounted(() => {
      loadProjects()
      loadUsers()
      loadNotificationHistory()
    })

    return {
      notificationFormRef,
      notificationForm,
      rules,
      sending,
      historyLoading,
      projectsLoading,
      usersLoading,
      projects,
      users,
      notificationHistory,
      onTypeChange,
      sendNotification,
      resetForm,
      loadNotificationHistory,
      getTypeText,
      getTypeTagType,
      getPriorityText,
      getPriorityTagType
    }
  }
}
</script>

<style scoped>
.admin-notification {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ml-2 {
  margin-left: 8px;
}

.mt-4 {
  margin-top: 16px;
}

.text-sm {
  font-size: 14px;
}

.text-gray-500 {
  color: #6b7280;
}
</style> 