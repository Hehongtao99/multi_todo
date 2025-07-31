<template>
  <div class="project-management">
    <div class="page-header">
      <h2>项目管理</h2>
      <p>{{ isAdmin ? '管理所有项目，创建和分配项目' : '查看分配给您的项目' }}</p>
    </div>

    <!-- 管理员操作区域 -->
    <el-card v-if="isAdmin" class="admin-actions">
      <template #header>
        <div class="card-header">
          <span>项目操作</span>
        </div>
      </template>
      <el-button 
        type="primary" 
        @click="showCreateDialog = true"
        icon="Plus"
      >
        创建项目
      </el-button>
    </el-card>

    <!-- 项目列表 -->
    <el-card class="project-card">
      <template #header>
        <div class="card-header">
          <span>{{ isAdmin ? '所有项目' : '我的项目' }}</span>
          <el-button 
            @click="fetchProjectList" 
            icon="Refresh" 
            size="small"
            :loading="loading"
          >
            刷新
          </el-button>
        </div>
      </template>

      <el-table 
        :data="projectList" 
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="id" label="项目ID" width="80" />
        <el-table-column prop="projectName" label="项目名称" width="200" />
        <el-table-column prop="projectDescription" label="项目描述" min-width="300">
          <template #default="scope">
            <el-tooltip :content="scope.row.projectDescription" placement="top">
              <span class="description-text">{{ scope.row.projectDescription }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="100" />
                 <el-table-column prop="assignedUsers" label="分配给" min-width="200">
           <template #default="scope">
             <div v-if="scope.row.assignedUsers && scope.row.assignedUsers.length > 0">
               <el-tag 
                 v-for="user in scope.row.assignedUsers" 
                 :key="user.userId" 
                 type="success"
                 size="small"
                 style="margin-right: 5px; margin-bottom: 2px;"
               >
                 {{ user.username }}
               </el-tag>
             </div>
             <span v-else class="unassigned">未分配</span>
           </template>
         </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdTime) }}
          </template>
        </el-table-column>
                 <el-table-column label="操作" width="200">
           <template #default="scope">
             <el-button
               type="info"
               size="small"
               @click="handleViewDetail(scope.row)"
             >
               详情
             </el-button>
             <el-button
               v-if="isAdmin"
               type="primary"
               size="small"
               @click="handleAssignUsers(scope.row)"
             >
               分配用户
             </el-button>
           </template>
         </el-table-column>
      </el-table>

      <div v-if="projectList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无项目数据" />
      </div>
    </el-card>

    <!-- 创建项目对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建新项目"
      width="600px"
    >
      <el-form 
        :model="createForm" 
        label-width="100px"
        ref="createFormRef"
      >
        <el-form-item label="项目名称" required>
          <el-input 
            v-model="createForm.projectName" 
            placeholder="请输入项目名称"
          />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input 
            v-model="createForm.projectDescription" 
            type="textarea"
            :rows="4"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleCreateProject"
            :loading="createLoading"
          >
            创建项目
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分配用户对话框 -->
    <el-dialog
      v-model="showAssignDialog"
      title="分配项目用户"
      width="600px"
    >
      <div v-if="selectedProject">
        <p>项目：<strong>{{ selectedProject.projectName }}</strong></p>
        <p>当前分配用户：</p>
        <div v-if="selectedProject.assignedUsers && selectedProject.assignedUsers.length > 0" style="margin-bottom: 15px;">
          <el-tag 
            v-for="user in selectedProject.assignedUsers" 
            :key="user.userId" 
            type="info"
            style="margin-right: 5px; margin-bottom: 5px;"
          >
            {{ user.username }}
          </el-tag>
        </div>
        <p v-else style="color: #999; font-style: italic;">暂未分配用户</p>
        
        <el-form label-width="100px">
          <el-form-item label="选择用户">
            <el-select 
              v-model="assignForm.userIds" 
              placeholder="请选择要分配的用户（可多选）"
              style="width: 100%"
              multiple
              clearable
            >
              <el-option
                v-for="user in userList"
                :key="user.id"
                :label="user.username"
                :value="user.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAssignDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleConfirmAssign"
            :loading="assignLoading"
          >
            确认分配
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProjectList, createProject, assignProject } from '../api/project'
import { getUserList } from '../api/user'

export default {
  name: 'ProjectManagement',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const projectList = ref([])
    const userList = ref([])
    const showCreateDialog = ref(false)
    const showAssignDialog = ref(false)
    const createLoading = ref(false)
    const assignLoading = ref(false)
    const selectedProject = ref(null)

    const createForm = ref({
      projectName: '',
      projectDescription: ''
    })

    const assignForm = ref({
      userIds: []
    })

    // 获取当前用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    
    // 判断是否为管理员
    const isAdmin = computed(() => userInfo.auth === 'admin')

    // 获取项目列表
    const fetchProjectList = async () => {
      loading.value = true
      try {
        const response = await getProjectList()
        if (response.data.code === 200) {
          projectList.value = response.data.data
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        ElMessage.error('获取项目列表失败')
      } finally {
        loading.value = false
      }
    }

    // 获取用户列表（仅管理员需要）
    const fetchUserList = async () => {
      if (!isAdmin.value) return
      
      try {
        const response = await getUserList()
        if (response.data.code === 200) {
          userList.value = response.data.data
        }
      } catch (error) {
        ElMessage.error('获取用户列表失败')
      }
    }

    // 创建项目
    const handleCreateProject = async () => {
      if (!createForm.value.projectName.trim()) {
        ElMessage.warning('请输入项目名称')
        return
      }

      createLoading.value = true
      try {
        const response = await createProject(createForm.value)
        if (response.data.code === 200) {
          ElMessage.success('项目创建成功')
          showCreateDialog.value = false
          createForm.value = {
            projectName: '',
            projectDescription: ''
          }
          fetchProjectList()
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        ElMessage.error('项目创建失败')
      } finally {
        createLoading.value = false
      }
    }

    // 处理分配用户
    const handleAssignUsers = (project) => {
      selectedProject.value = project
      // 设置当前已分配的用户ID
      if (project.assignedUsers && project.assignedUsers.length > 0) {
        assignForm.value.userIds = project.assignedUsers.map(user => user.userId)
      } else {
        assignForm.value.userIds = []
      }
      showAssignDialog.value = true
    }

    // 确认分配用户
    const handleConfirmAssign = async () => {
      assignLoading.value = true
      try {
        const response = await assignProject({
          projectId: selectedProject.value.id,
          userIds: assignForm.value.userIds
        })
        
        if (response.data.code === 200) {
          ElMessage.success('用户分配成功')
          showAssignDialog.value = false
          assignForm.value.userIds = []
          fetchProjectList()
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        ElMessage.error('用户分配失败')
      } finally {
        assignLoading.value = false
      }
    }

    // 查看项目详情
    const handleViewDetail = (project) => {
      router.push(`/dashboard/projects/${project.id}`)
    }

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '-'
      return new Date(dateString).toLocaleString('zh-CN')
    }

    // WebSocket连接和通知处理
    const initWebSocket = async () => {
      try {
        if (!webSocketService.connected) {
          await webSocketService.connect(userInfo)
        }
        
        // 监听通知事件
        window.addEventListener('notificationReceived', handleNotificationReceived)
        
      } catch (error) {
        console.error('WebSocket连接失败:', error)
      }
    }
    
    // 处理接收到的通知
    const handleNotificationReceived = (event) => {
      const notification = event.detail
      console.log('ProjectManagement 收到通知:', notification)
      
      // 如果是项目分配通知，静默刷新项目列表（不显示额外消息，因为已经有系统通知了）
      if (notification.type === 'personal' && 
          notification.title === '项目分配通知') {
        fetchProjectList()
      }
      
      // 如果是项目相关通知，也可以刷新列表
      if (notification.type === 'project') {
        fetchProjectList()
      }
    }
    
    onMounted(async () => {
      fetchProjectList()
      fetchUserList()
      await initWebSocket()
    })
    
    onUnmounted(() => {
      // 清理事件监听器
      window.removeEventListener('notificationReceived', handleNotificationReceived)
    })

    return {
      loading,
      projectList,
      userList,
      showCreateDialog,
      showAssignDialog,
      createLoading,
      assignLoading,
      selectedProject,
      createForm,
      assignForm,
      isAdmin,
      fetchProjectList,
      handleCreateProject,
      handleAssignUsers,
      handleConfirmAssign,
      handleViewDetail,
      formatDate
    }
  }
}
</script>

<style scoped>
.project-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #333;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.admin-actions {
  margin-bottom: 20px;
}

.project-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.description-text {
  display: inline-block;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unassigned {
  color: #999;
  font-style: italic;
}

.empty-state {
  padding: 40px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 