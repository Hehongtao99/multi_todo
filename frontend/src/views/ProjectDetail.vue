<template>
  <div class="project-detail">
    <!-- 项目基本信息 -->
    <el-card class="project-info-card">
      <template #header>
        <div class="card-header">
          <span>项目信息</span>
          <el-button 
            @click="$router.go(-1)" 
            icon="ArrowLeft" 
            size="small"
          >
            返回
          </el-button>
        </div>
      </template>
      
      <div v-if="projectDetail" class="project-info">
        <div class="info-row">
          <label>项目名称：</label>
          <span class="project-name">{{ projectDetail.projectName }}</span>
        </div>
        <div class="info-row">
          <label>项目描述：</label>
          <span>{{ projectDetail.projectDescription || '暂无描述' }}</span>
        </div>
        <div class="info-row">
          <label>创建人：</label>
          <span>{{ projectDetail.creatorName }}</span>
        </div>
        <div class="info-row">
          <label>创建时间：</label>
          <span>{{ formatDate(projectDetail.createdTime) }}</span>
        </div>
      </div>
    </el-card>

    <!-- 分配用户列表 -->
    <el-card class="users-card">
      <template #header>
        <div class="card-header">
          <span>分配用户</span>
        </div>
      </template>
      
      <div v-if="projectDetail && projectDetail.assignedUsers && projectDetail.assignedUsers.length > 0">
        <el-tag 
          v-for="user in projectDetail.assignedUsers" 
          :key="user.userId" 
          type="success"
          size="large"
          class="user-tag"
        >
          {{ user.username }}
        </el-tag>
      </div>
      <div v-else class="empty-state">
        <el-empty description="暂无分配用户" />
      </div>
    </el-card>

    <!-- 待办事项管理 -->
    <el-card class="todos-card">
      <template #header>
        <div class="card-header">
          <span>待办事项</span>
          <el-button 
            v-if="isAdmin"
            type="primary" 
            @click="showCreateTodoDialog = true"
            icon="Plus"
            size="small"
          >
            创建待办
          </el-button>
        </div>
      </template>

      <!-- 待办事项列表 -->
      <el-table 
        :data="todos" 
        style="width: 100%"
        v-loading="todosLoading"
      >
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="description" label="描述" min-width="250">
          <template #default="scope">
            <el-tooltip :content="scope.row.description" placement="top">
              <span class="description-text">{{ scope.row.description || '暂无描述' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag 
              :type="getStatusType(scope.row.status)"
              size="small"
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag 
              :type="getPriorityType(scope.row.priority)"
              size="small"
            >
              {{ getPriorityText(scope.row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="分配给" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="scope">
            {{ formatDetailedDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止日期" width="180">
          <template #default="scope">
            {{ formatDetailedDateTime(scope.row.dueDate) }}
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
              type="primary" 
              size="small"
              @click="handleUpdateStatus(scope.row)"
            >
              更新状态
            </el-button>
            <el-button 
              v-if="isAdmin"
              type="warning" 
              size="small"
              @click="handleAdminEditTodo(scope.row)"
            >
              管理员编辑
            </el-button>
            <el-button 
              v-if="isAdmin"
              type="danger" 
              size="small"
              @click="handleDeleteTodo(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="todos.length === 0 && !todosLoading" class="empty-state">
        <el-empty description="暂无待办事项" />
      </div>
    </el-card>

    <!-- 创建待办对话框 -->
    <el-dialog
      v-model="showCreateTodoDialog"
      title="创建待办事项"
      width="600px"
    >
      <el-form 
        :model="todoForm" 
        label-width="100px"
        ref="todoFormRef"
      >
        <el-form-item label="标题" required>
          <el-input 
            v-model="todoForm.title" 
            placeholder="请输入待办标题"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="todoForm.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入待办描述"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="todoForm.priority" placeholder="请选择优先级">
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
          </el-select>
        </el-form-item>
        <el-form-item label="分配给">
          <el-select 
            v-model="todoForm.assigneeId" 
            placeholder="请选择分配用户"
            clearable
          >
            <el-option
              v-for="user in assignedUsers"
              :key="user.userId"
              :label="user.username"
              :value="user.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="todoForm.startTime"
            type="datetime"
            placeholder="选择开始时间（默认今天9:00）"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker
            v-model="todoForm.dueDate"
            type="datetime"
            placeholder="选择截止日期（默认今天23:59）"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateTodoDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleCreateTodo"
            :loading="createTodoLoading"
          >
            创建
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 更新状态对话框 -->
    <el-dialog
      v-model="showStatusDialog"
      title="更新待办状态"
      width="400px"
    >
      <div v-if="selectedTodo">
        <p>待办：<strong>{{ selectedTodo.title }}</strong></p>
        <p>当前状态：<el-tag :type="getStatusType(selectedTodo.status)">{{ getStatusText(selectedTodo.status) }}</el-tag></p>
        
        <el-form label-width="80px">
          <el-form-item label="新状态">
            <el-select v-model="newStatus" placeholder="请选择新状态">
              <el-option label="待处理" value="pending" />
              <el-option label="进行中" value="in_progress" />
              <el-option label="已完成" value="completed" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showStatusDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleConfirmUpdateStatus"
            :loading="updateStatusLoading"
          >
            确认更新
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 管理员编辑待办对话框 -->
    <el-dialog
      v-model="showAdminEditDialog"
      title="管理员编辑待办事项"
      width="600px"
    >
      <el-form 
        :model="adminEditForm" 
        label-width="100px"
        ref="adminEditFormRef"
      >
        <el-form-item label="标题" required>
          <el-input 
            v-model="adminEditForm.title" 
            placeholder="请输入待办标题"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="adminEditForm.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入待办描述"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="adminEditForm.priority" placeholder="请选择优先级">
                <el-option label="低" value="low" />
                <el-option label="中" value="medium" />
                <el-option label="高" value="high" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="adminEditForm.status" placeholder="请选择状态">
                <el-option label="待处理" value="pending" />
                <el-option label="进行中" value="in_progress" />
                <el-option label="已完成" value="completed" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分配给">
              <el-select 
                v-model="adminEditForm.assigneeId" 
                placeholder="请选择分配用户"
                clearable
              >
                <el-option
                  v-for="user in assignedUsers"
                  :key="user.userId"
                  :label="user.username"
                  :value="user.userId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目">
              <el-input :value="projectDetail?.projectName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker
                v-model="adminEditForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止时间">
              <el-date-picker
                v-model="adminEditForm.dueDate"
                type="datetime"
                placeholder="选择截止时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="修改原因">
          <el-input 
            v-model="adminEditForm.updateReason" 
            type="textarea"
            :rows="2"
            placeholder="请说明修改原因（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAdminEditDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleConfirmAdminEdit"
            :loading="adminEditLoading"
          >
            确认修改
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProjectDetail } from '../api/project'
import { createTodo, updateTodoStatus, deleteTodo, adminUpdateTodo } from '../api/todo'
import { formatDateForDisplay, formatDetailedDateTime } from '../utils/dateUtils'
import webSocketService from '../api/websocket.js'

export default {
  name: 'ProjectDetail',
  setup() {
    const route = useRoute()
    const projectId = route.params.id
    
    const loading = ref(false)
    const todosLoading = ref(false)
    const projectDetail = ref(null)
    const todos = ref([])
    const assignedUsers = ref([])
    
    const showCreateTodoDialog = ref(false)
    const showStatusDialog = ref(false)
    const showAdminEditDialog = ref(false)
    const createTodoLoading = ref(false)
    const updateStatusLoading = ref(false)
    const adminEditLoading = ref(false)
    
    const selectedTodo = ref(null)
    const newStatus = ref('')
    const adminEditFormRef = ref(null)
    
    const todoForm = ref({
      title: '',
      description: '',
      priority: 'medium',
      assigneeId: null,
      startTime: null,
      dueDate: null,
      projectId: parseInt(projectId)
    })

    const adminEditForm = ref({
      id: null,
      title: '',
      description: '',
      priority: 'medium',
      status: 'pending',
      assigneeId: null,
      projectId: parseInt(projectId),
      startTime: null,
      dueDate: null,
      updateReason: ''
    })

    // 获取当前用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    
    // 判断是否为管理员
    const isAdmin = computed(() => userInfo.auth === 'admin')

    // 获取项目详情
    const fetchProjectDetail = async () => {
      loading.value = true
      try {
        const response = await getProjectDetail(projectId)
        if (response.data.code === 200) {
          projectDetail.value = response.data.data
          todos.value = response.data.data.todos || []
          assignedUsers.value = response.data.data.assignedUsers || []
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        ElMessage.error('获取项目详情失败')
      } finally {
        loading.value = false
      }
    }

    // 创建待办事项
    const handleCreateTodo = async () => {
      if (!todoForm.value.title.trim()) {
        ElMessage.warning('请输入待办标题')
        return
      }

      createTodoLoading.value = true
      try {
        const response = await createTodo(todoForm.value)
        if (response.data.code === 200) {
          ElMessage.success('待办事项创建成功')
          showCreateTodoDialog.value = false
          todoForm.value = {
            title: '',
            description: '',
            priority: 'medium',
            assigneeId: null,
            startTime: null,
            dueDate: null,
            projectId: parseInt(projectId)
          }
          fetchProjectDetail() // 重新获取项目详情
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        ElMessage.error('待办事项创建失败')
      } finally {
        createTodoLoading.value = false
      }
    }

    // 处理更新状态
    const handleUpdateStatus = (todo) => {
      selectedTodo.value = todo
      newStatus.value = todo.status
      showStatusDialog.value = true
    }

    // 确认更新状态
    const handleConfirmUpdateStatus = async () => {
      updateStatusLoading.value = true
      try {
        const response = await updateTodoStatus(selectedTodo.value.id, newStatus.value)
        if (response.data.code === 200) {
          ElMessage.success('状态更新成功')
          showStatusDialog.value = false
          fetchProjectDetail() // 重新获取项目详情
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        ElMessage.error('状态更新失败')
      } finally {
        updateStatusLoading.value = false
      }
    }

    // 删除待办事项
    const handleDeleteTodo = async (todo) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除待办事项"${todo.title}"吗？`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )
        
        const response = await deleteTodo(todo.id)
        if (response.data.code === 200) {
          ElMessage.success('待办事项删除成功')
          fetchProjectDetail() // 重新获取项目详情
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('待办事项删除失败')
        }
      }
    }

    // 管理员编辑待办事项
    const handleAdminEditTodo = (todo) => {
      if (!isAdmin.value) {
        ElMessage.warning('只有管理员可以使用此功能')
        return
      }

      // 填充表单数据
      adminEditForm.value = {
        id: todo.id,
        title: todo.title,
        description: todo.description,
        priority: todo.priority,
        status: todo.status,
        assigneeId: todo.assigneeId,
        projectId: parseInt(projectId),
        startTime: todo.startTime,
        dueDate: todo.dueDate,
        updateReason: ''
      }
      
      showAdminEditDialog.value = true
    }

    // 确认管理员编辑
    const handleConfirmAdminEdit = async () => {
      if (!adminEditForm.value.title?.trim()) {
        ElMessage.warning('请输入待办标题')
        return
      }

      adminEditLoading.value = true
      try {
        const response = await adminUpdateTodo(adminEditForm.value)
        if (response.data.code === 200) {
          ElMessage.success('管理员修改成功')
          showAdminEditDialog.value = false
          fetchProjectDetail() // 重新获取项目详情
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        ElMessage.error('管理员修改失败')
      } finally {
        adminEditLoading.value = false
      }
    }

    // 获取状态类型
    const getStatusType = (status) => {
      const statusMap = {
        'pending': 'warning',
        'in_progress': 'primary',
        'completed': 'success'
      }
      return statusMap[status] || 'info'
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        'pending': '待处理',
        'in_progress': '进行中',
        'completed': '已完成'
      }
      return statusMap[status] || status
    }

    // 获取优先级类型
    const getPriorityType = (priority) => {
      const priorityMap = {
        'low': 'info',
        'medium': 'warning',
        'high': 'danger'
      }
      return priorityMap[priority] || 'info'
    }

    // 获取优先级文本
    const getPriorityText = (priority) => {
      const priorityMap = {
        'low': '低',
        'medium': '中',
        'high': '高'
      }
      return priorityMap[priority] || priority
    }

    // 格式化日期
    const formatDate = formatDateForDisplay

    // WebSocket连接和通知处理
    const initWebSocket = async () => {
      try {
        if (!webSocketService.connected) {
          await webSocketService.connect(userInfo)
        }
        
        // 加入项目频道
        webSocketService.joinProject(parseInt(projectId))
        
        // 监听通知事件
        window.addEventListener('notificationReceived', handleNotificationReceived)
        
      } catch (error) {
        console.error('WebSocket连接失败:', error)
      }
    }
    
    // 处理接收到的通知
    const handleNotificationReceived = (event) => {
      const notification = event.detail
      console.log('ProjectDetail 收到通知:', notification)
      
      // 如果是待办相关通知，静默刷新项目详情（不显示额外消息，因为已经有系统通知了）
      if (notification.type === 'personal' && 
          notification.title === '新待办事项分配') {
        fetchProjectDetail()
      }
      
      // 如果是项目通知且是当前项目，静默刷新详情
      if (notification.type === 'project' && 
          notification.projectId === parseInt(projectId)) {
        fetchProjectDetail()
      }
    }

    onMounted(async () => {
      fetchProjectDetail()
      await initWebSocket()
    })
    
    onUnmounted(() => {
      // 清理事件监听器
      window.removeEventListener('notificationReceived', handleNotificationReceived)
    })

    return {
      loading,
      todosLoading,
      projectDetail,
      todos,
      assignedUsers,
      showCreateTodoDialog,
      showStatusDialog,
      showAdminEditDialog,
      createTodoLoading,
      updateStatusLoading,
      adminEditLoading,
      selectedTodo,
      newStatus,
      todoForm,
      adminEditForm,
      adminEditFormRef,
      isAdmin,
      handleCreateTodo,
      handleUpdateStatus,
      handleConfirmUpdateStatus,
      handleDeleteTodo,
      handleAdminEditTodo,
      handleConfirmAdminEdit,
      getStatusType,
      getStatusText,
      getPriorityType,
      getPriorityText,
      formatDate,
      formatDetailedDateTime
    }
  }
}
</script>

<style scoped>
.project-detail {
  padding: 20px;
}

.project-info-card,
.users-card,
.todos-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-info .info-row {
  margin-bottom: 15px;
  display: flex;
  align-items: flex-start;
}

.project-info .info-row label {
  font-weight: bold;
  min-width: 100px;
  color: #666;
}

.project-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.user-tag {
  margin-right: 10px;
  margin-bottom: 8px;
}

.description-text {
  display: inline-block;
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
