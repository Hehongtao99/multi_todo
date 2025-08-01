<template>
  <div class="todo-kanban">
    <!-- 页面标题和操作栏 -->
    <div class="kanban-header">
      <div class="header-left">
        <h2>待办事项看板</h2>
        <div class="header-stats">
          <span class="stat-item">
            <span class="stat-label">总计</span>
            <span class="stat-value">{{ todos.length }}</span>
          </span>
          <span class="stat-item">
            <span class="stat-label">进行中</span>
            <span class="stat-value">{{ getStatusCount('in_progress') }}</span>
          </span>
          <span class="stat-item">
            <span class="stat-label">已完成</span>
            <span class="stat-value">{{ getStatusCount('completed') }}</span>
          </span>
        </div>
      </div>
      <div class="header-right">
        <el-button 
          v-if="userInfo.auth === 'admin'" 
          type="primary" 
          @click="showCreateDialog = true"
          class="create-btn"
        >
          <el-icon><Plus /></el-icon>
          新建任务
        </el-button>
      </div>
    </div>

    <!-- 筛选和搜索栏 -->
    <div class="kanban-filters">
      <div class="filter-group">
        <el-select v-model="filters.projectId" placeholder="选择项目" clearable @change="loadTodos" class="filter-select">
          <el-option label="全部项目" value="" />
          <el-option 
            v-for="project in projects" 
            :key="project.id" 
            :label="project.name" 
            :value="project.id" 
          />
        </el-select>
        <el-select v-model="filters.priority" placeholder="优先级" clearable @change="loadTodos" class="filter-select">
          <el-option label="全部" value="" />
          <el-option label="高" value="high" />
          <el-option label="中" value="medium" />
          <el-option label="低" value="low" />
        </el-select>
        <el-date-picker
          v-model="filters.queryDate"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="loadTodos"
          clearable
          class="filter-select"
        />
        <el-switch
          v-model="filters.includeHistory"
          @change="loadTodos"
          active-text="显示历史"
          inactive-text="仅今日"
          class="history-switch"
        />
        <el-input
          v-model="filters.keyword"
          placeholder="搜索任务..."
          @input="debounceSearch"
          clearable
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 甘特图看板 -->
    <div class="kanban-board" v-loading="loading">
      <div class="kanban-columns">
        <!-- 待处理列 -->
        <div class="kanban-column">
          <div class="column-header pending">
            <div class="column-title">
              <span class="column-icon">⏳</span>
              <span>待处理</span>
              <span class="column-count">{{ getPendingTodos.length }}</span>
            </div>
          </div>
          <div class="column-content">
            <div v-if="getPendingTodos.length === 0" class="empty-column">
              <div class="empty-icon">📝</div>
              <div class="empty-text">暂无待处理任务</div>
              <div class="drop-hint">拖拽任务到此处</div>
            </div>
            <draggable
              v-model="pendingTodosData"
              group="todos"
              @change="onDragChange"
              item-key="id"
              class="draggable-list"
              data-status="pending"
              :animation="200"
              ghost-class="ghost-card"
              chosen-class="chosen-card"
              drag-class="drag-card"
              :disabled="!canDragTodos"
            >
              <template #item="{ element: todo, index }">
                <div 
                  class="todo-card draggable-card"
                  @click="showTodoDetail(todo)"
                  :data-status="todo.status"
                  :data-id="todo.id"
                >
                  <div class="card-header">
                    <div class="card-priority" :class="todo.priority">
                      <span class="priority-dot"></span>
                    </div>
                    <div class="card-actions">
                      <div class="drag-handle" @click.stop>
                        <span class="drag-icon">⋮⋮</span>
                      </div>
                      <el-dropdown @command="(command) => handleTodoAction(command, todo)" trigger="click" @click.stop>
                        <el-button type="text" size="small" class="action-btn">
                          <el-icon><MoreFilled /></el-icon>
                        </el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item command="view">查看详情</el-dropdown-item>
                            <el-dropdown-item 
                              v-if="canEditTodo(todo)" 
                              command="edit"
                            >
                              编辑
                            </el-dropdown-item>
                            <el-dropdown-item 
                              v-if="userInfo.auth === 'admin'" 
                              command="admin-edit"
                            >
                              管理员编辑
                            </el-dropdown-item>
                            <el-dropdown-item 
                              v-if="canEditTodo(todo)" 
                              command="start"
                            >
                              开始处理
                            </el-dropdown-item>
                            <el-dropdown-item 
                              v-if="canDeleteTodo(todo)" 
                              command="delete" 
                              divided
                            >
                              删除
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                  </div>
                  <div class="card-title">{{ todo.title }}</div>
                  <div class="card-description">
                    {{ todo.description || '暂无描述' }}
                  </div>
                  <div class="card-times">
                    <div class="time-item">
                      <span class="time-icon">📅</span>
                      <span class="time-label">开始</span>
                      <span class="time-value">{{ formatDetailedDateTime(todo.startTime) }}</span>
                    </div>
                    <div class="time-item">
                      <span class="time-icon">⏰</span>
                      <span class="time-label">结束</span>
                      <span class="time-value">{{ formatDetailedDateTime(todo.dueDate) }}</span>
                    </div>
                  </div>
                  <div class="card-priority-badge">
                    <el-tag 
                      :type="getPriorityType(todo.priority)" 
                      size="small"
                      effect="plain"
                    >
                      {{ getPriorityText(todo.priority) }}优先级
                    </el-tag>
                  </div>
                </div>
              </template>
            </draggable>
          </div>
        </div>

        <!-- 进行中列 -->
        <div class="kanban-column">
          <div class="column-header in-progress">
            <div class="column-title">
              <span class="column-icon">🔄</span>
              <span>进行中</span>
              <span class="column-count">{{ getInProgressTodos.length }}</span>
            </div>
          </div>
          <div class="column-content">
            <div v-if="getInProgressTodos.length === 0" class="empty-column">
              <div class="empty-icon">⚡</div>
              <div class="empty-text">暂无进行中任务</div>
              <div class="drop-hint">拖拽任务到此处</div>
            </div>
            <draggable
              v-model="inProgressTodosData"
              group="todos"
              @change="onDragChange"
              item-key="id"
              class="draggable-list"
              data-status="in_progress"
              :animation="200"
              ghost-class="ghost-card"
              chosen-class="chosen-card"
              drag-class="drag-card"
              :disabled="!canDragTodos"
            >
              <template #item="{ element: todo, index }">
                <div 
                  class="todo-card in-progress draggable-card"
                  @click="showTodoDetail(todo)"
                  :data-status="todo.status"
                  :data-id="todo.id"
                >
                  <div class="card-header">
                    <div class="card-priority" :class="todo.priority">
                      <span class="priority-dot"></span>
                    </div>
                    <div class="card-actions">
                      <div class="drag-handle" @click.stop>
                        <span class="drag-icon">⋮⋮</span>
                      </div>
                      <el-dropdown @command="(command) => handleTodoAction(command, todo)" trigger="click" @click.stop>
                        <el-button type="text" size="small" class="action-btn">
                          <el-icon><MoreFilled /></el-icon>
                        </el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item command="view">查看详情</el-dropdown-item>
                            <el-dropdown-item 
                              v-if="canEditTodo(todo)" 
                              command="edit"
                            >
                              编辑
                            </el-dropdown-item>
                            <el-dropdown-item 
                              v-if="userInfo.auth === 'admin'" 
                              command="admin-edit"
                            >
                              管理员编辑
                            </el-dropdown-item>
                            <el-dropdown-item 
                              v-if="canEditTodo(todo)" 
                              command="complete"
                            >
                              标记完成
                            </el-dropdown-item>
                            <el-dropdown-item 
                              v-if="canEditTodo(todo)" 
                              command="pause"
                            >
                              暂停
                            </el-dropdown-item>
                            <el-dropdown-item 
                              v-if="canDeleteTodo(todo)" 
                              command="delete" 
                              divided
                            >
                              删除
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                  </div>
                  <div class="card-title">{{ todo.title }}</div>
                  <div class="card-description">
                    {{ todo.description || '暂无描述' }}
                  </div>
                  <div class="card-times">
                    <div class="time-item">
                      <span class="time-icon">📅</span>
                      <span class="time-label">开始</span>
                      <span class="time-value">{{ formatDetailedDateTime(todo.startTime) }}</span>
                    </div>
                    <div class="time-item">
                      <span class="time-icon">⏰</span>
                      <span class="time-label">结束</span>
                      <span class="time-value">{{ formatDetailedDateTime(todo.dueDate) }}</span>
                    </div>
                  </div>
                  <div class="card-priority-badge">
                    <el-tag 
                      :type="getPriorityType(todo.priority)" 
                      size="small"
                      effect="plain"
                    >
                      {{ getPriorityText(todo.priority) }}优先级
                    </el-tag>
                  </div>
                  <div class="progress-indicator">
                    <div class="progress-bar">
                      <div class="progress-fill"></div>
                    </div>
                  </div>
                </div>
              </template>
            </draggable>
          </div>
        </div>

        <!-- 已完成列 -->
        <div class="kanban-column">
          <div class="column-header completed">
            <div class="column-title">
              <span class="column-icon">✅</span>
              <span>已完成</span>
              <span class="column-count">{{ getCompletedTodos.length }}</span>
            </div>
          </div>
          <div class="column-content">
            <div v-if="getCompletedTodos.length === 0" class="empty-column">
              <div class="empty-icon">🎉</div>
              <div class="empty-text">暂无已完成任务</div>
              <div class="drop-hint">拖拽任务到此处</div>
            </div>
            <draggable
              v-model="completedTodosData"
              group="todos"
              @change="onDragChange"
              item-key="id"
              class="draggable-list"
              data-status="completed"
              :animation="200"
              ghost-class="ghost-card"
              chosen-class="chosen-card"
              drag-class="drag-card"
              :disabled="!canDragTodos"
            >
              <template #item="{ element: todo, index }">
                <div 
                  class="todo-card completed draggable-card"
                  @click="showTodoDetail(todo)"
                  :data-status="todo.status"
                  :data-id="todo.id"
                >
                  <div class="card-header">
                    <div class="card-priority" :class="todo.priority">
                      <span class="priority-dot"></span>
                    </div>
                    <div class="card-actions">
                      <div class="drag-handle" @click.stop>
                        <span class="drag-icon">⋮⋮</span>
                      </div>
                      <el-dropdown @command="(command) => handleTodoAction(command, todo)" trigger="click" @click.stop>
                        <el-button type="text" size="small" class="action-btn">
                          <el-icon><MoreFilled /></el-icon>
                        </el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item command="view">查看详情</el-dropdown-item>
                            <el-dropdown-item 
                              v-if="userInfo.auth === 'admin'" 
                              command="admin-edit"
                            >
                              管理员编辑
                            </el-dropdown-item>
                            <el-dropdown-item 
                              v-if="canEditTodo(todo)" 
                              command="reopen"
                            >
                              重新打开
                            </el-dropdown-item>
                            <el-dropdown-item 
                              v-if="canDeleteTodo(todo)" 
                              command="delete" 
                              divided
                            >
                              删除
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                  </div>
                  <div class="card-title completed-title">{{ todo.title }}</div>
                  <div class="card-description">
                    {{ todo.description || '暂无描述' }}
                  </div>
                  <div class="card-times">
                    <div class="time-item">
                      <span class="time-icon">📅</span>
                      <span class="time-label">开始</span>
                      <span class="time-value">{{ formatDetailedDateTime(todo.startTime) }}</span>
                    </div>
                    <div class="time-item">
                      <span class="time-icon">⏰</span>
                      <span class="time-label">结束</span>
                      <span class="time-value">{{ formatDetailedDateTime(todo.dueDate) }}</span>
                    </div>
                  </div>
                  <div class="card-priority-badge">
                    <el-tag 
                      :type="getPriorityType(todo.priority)" 
                      size="small"
                      effect="plain"
                    >
                      {{ getPriorityText(todo.priority) }}优先级
                    </el-tag>
                  </div>
                </div>
              </template>
            </draggable>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建/编辑待办事项对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="getDialogTitle()"
      width="600px"
      @close="resetForm"
      class="todo-dialog"
    >
      <el-form
        ref="todoFormRef"
        :model="todoForm"
        :rules="todoFormRules"
        label-width="100px"
        class="todo-form"
      >
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="todoForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="todoForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入详细描述"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="todoForm.priority" placeholder="选择优先级">
                <el-option label="高优先级" value="high" />
                <el-option label="中优先级" value="medium" />
                <el-option label="低优先级" value="low" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属项目" prop="projectId">
              <el-select v-model="todoForm.projectId" placeholder="选择项目">
                <el-option 
                  v-for="project in projects" 
                  :key="project.id" 
                  :label="project.name" 
                  :value="project.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="指派给" prop="assigneeId">
              <el-select v-model="todoForm.assigneeId" placeholder="选择负责人">
                <el-option 
                  v-for="user in users" 
                  :key="user.id" 
                  :label="user.username" 
                  :value="user.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="todoForm.startTime"
                type="datetime"
                placeholder="选择开始时间（默认今天9:00）"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="截止日期" prop="dueDate">
              <el-date-picker
                v-model="todoForm.dueDate"
                type="datetime"
                placeholder="选择截止日期（默认今天23:59）"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="isAdminEditing">
            <el-form-item label="任务状态" prop="status">
              <el-select v-model="todoForm.status" placeholder="选择状态">
                <el-option label="待处理" value="pending" />
                <el-option label="进行中" value="in_progress" />
                <el-option label="已完成" value="completed" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="isAdminEditing">
          <el-col :span="24">
            <el-form-item label="修改原因" prop="updateReason">
              <el-input
                v-model="todoForm.updateReason"
                type="textarea"
                :rows="2"
                placeholder="请说明修改原因（可选）"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTodo" :loading="saving">
          {{ editingTodo ? '更新任务' : '创建任务' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 待办事项详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="任务详情"
      width="700px"
      class="detail-dialog"
    >
      <div v-if="selectedTodo" class="todo-detail">
        <div class="detail-header">
          <div class="detail-title">
            <h3>{{ selectedTodo.title }}</h3>
            <div class="detail-badges">
              <el-tag :type="getPriorityType(selectedTodo.priority)" size="small">
                {{ getPriorityText(selectedTodo.priority) }}优先级
              </el-tag>
              <el-tag :type="getStatusType(selectedTodo.status)" size="small">
                {{ getStatusText(selectedTodo.status) }}
              </el-tag>
            </div>
          </div>
        </div>
        
        <div class="detail-content">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="detail-section">
                <h4>基本信息</h4>
                <div class="info-grid">
                  <div class="info-item">
                    <span class="info-label">所属项目</span>
                    <span class="info-value">{{ selectedTodo.projectName || '未分配' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">负责人</span>
                    <span class="info-value">{{ selectedTodo.assigneeName || '未分配' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">创建人</span>
                    <span class="info-value">{{ selectedTodo.creatorName || '未知' }}</span>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-section">
                <h4>时间信息</h4>
                <div class="info-grid">
                  <div class="info-item">
                    <span class="info-label">创建时间</span>
                    <span class="info-value">{{ formatDetailedDateTime(selectedTodo.createdTime) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">开始时间</span>
                    <span class="info-value">{{ formatDetailedDateTime(selectedTodo.startTime) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">截止时间</span>
                    <span class="info-value">{{ formatDetailedDateTime(selectedTodo.dueDate) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">剩余时间</span>
                    <span class="info-value" :class="getTimelineStatus(selectedTodo.dueDate)">
                      {{ getTimelineText(selectedTodo.dueDate) }}
                    </span>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
          
          <div class="detail-section" v-if="selectedTodo.description">
            <h4>任务描述</h4>
            <div class="description-content">
              {{ selectedTodo.description }}
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import draggable from 'vuedraggable'
import {
  createTodo,
  updateTodo,
  deleteTodo,
  getTodosByProjectId,
  getTodosByAssigneeId,
  getTodoDetail,
  updateTodoStatus,
  getAllTodos,
  getTodosByDate,
  adminUpdateTodo
} from '../api/todo'
import { getProjectList } from '../api/project'
import { getUserList } from '../api/user'
import { formatDateForDisplay, formatDetailedDateTime } from '../utils/dateUtils'

export default {
  name: 'TodoManagement',
  components: {
    draggable
  },
  setup() {
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
    const loading = ref(false)
    const saving = ref(false)
    const todos = ref([])
    const projects = ref([])
    const users = ref([])
    
    // 拖拽相关的响应式数据
    const pendingTodosData = ref([])
    const inProgressTodosData = ref([])
    const completedTodosData = ref([])
    
    // 筛选条件
    const filters = reactive({
      status: '',
      priority: '',
      projectId: '',
      keyword: '',
      queryDate: null,
      includeHistory: false
    })
    
    // 对话框状态
    const showCreateDialog = ref(false)
    const showDetailDialog = ref(false)
    const editingTodo = ref(null)
    const selectedTodo = ref(null)
    const isAdminEditing = ref(false)
    
    // 表单数据
    const todoForm = reactive({
      title: '',
      description: '',
      priority: 'medium',
      projectId: '',
      assigneeId: '',
      startTime: '',
      dueDate: '',
      status: 'pending',
      updateReason: ''
    })
    
    // 表单验证规则
    const todoFormRules = {
      title: [
        { required: true, message: '请输入任务标题', trigger: 'blur' }
      ],
      priority: [
        { required: true, message: '请选择优先级', trigger: 'change' }
      ],
      projectId: [
        { required: true, message: '请选择项目', trigger: 'change' }
      ],
      assigneeId: [
        { required: true, message: '请选择负责人', trigger: 'change' }
      ]
    }
    
    const todoFormRef = ref()

    // 计算属性 - 按状态分组的待办事项
    const getPendingTodos = computed(() => {
      return todos.value.filter(todo => todo.status === 'pending')
    })

    const getInProgressTodos = computed(() => {
      return todos.value.filter(todo => todo.status === 'in_progress')
    })

    const getCompletedTodos = computed(() => {
      return todos.value.filter(todo => todo.status === 'completed')
    })

    // 获取状态数量
    const getStatusCount = (status) => {
      return todos.value.filter(todo => todo.status === status).length
    }

    // 是否可以拖拽
    const canDragTodos = computed(() => {
      return userInfo.value.auth === 'admin' || todos.value.some(todo => canEditTodo(todo))
    })

    // 监听todos变化，更新拖拽数据
    watch(todos, (newTodos) => {
      pendingTodosData.value = newTodos.filter(todo => todo.status === 'pending')
      inProgressTodosData.value = newTodos.filter(todo => todo.status === 'in_progress')
      completedTodosData.value = newTodos.filter(todo => todo.status === 'completed')
    }, { immediate: true })

    // 拖拽变化处理
    const onDragChange = async (evt) => {
      console.log('拖拽事件:', evt) // 调试日志

      if (evt.added) {
        // 任务被拖拽到新的列
        const addedTodo = evt.added.element
        let newStatus = null

        // 更可靠的状态确定方法：通过当前拖拽数据数组来判断
        if (pendingTodosData.value.includes(addedTodo)) {
          newStatus = 'pending'
        } else if (inProgressTodosData.value.includes(addedTodo)) {
          newStatus = 'in_progress'
        } else if (completedTodosData.value.includes(addedTodo)) {
          newStatus = 'completed'
        }

        // 如果上述方法失败，使用原有方法
        if (!newStatus) {
          newStatus = getStatusFromColumn(evt.added.newIndex, evt.to)
        }

        console.log(`任务 ${addedTodo.title} 从 ${addedTodo.status} 移动到 ${newStatus}`) // 调试日志

        if (addedTodo.status !== newStatus) {
          // 检查权限
          if (!canEditTodo(addedTodo)) {
            ElMessage.warning('您没有权限移动此任务')
            // 重新加载数据以还原状态
            loadTodos()
            return
          }

          try {
            await updateTodoStatus(addedTodo.id, newStatus)
            // 更新本地数据
            addedTodo.status = newStatus
            ElMessage.success(`任务已移动到${getStatusText(newStatus)}`)

            // 确保数据同步：重新加载数据
            setTimeout(() => {
              loadTodos()
            }, 500)
          } catch (error) {
            console.error('更新任务状态失败:', error)
            ElMessage.error('移动任务失败')
            // 重新加载数据以还原状态
            loadTodos()
          }
        }
      }
    }

    // 根据拖拽目标确定新状态
    const getStatusFromColumn = (index, targetElement) => {
      // 方法1：通过draggable组件的data-status属性确定
      let draggableList = targetElement
      if (!draggableList.classList.contains('draggable-list')) {
        draggableList = targetElement.closest('.draggable-list')
      }

      if (draggableList && draggableList.dataset.status) {
        return draggableList.dataset.status
      }

      // 方法2：通过查找父元素的类名来确定目标列
      let column = targetElement
      while (column && !column.classList.contains('kanban-column')) {
        column = column.parentElement
      }

      if (column) {
        if (column.querySelector('.column-header.pending')) {
          return 'pending'
        } else if (column.querySelector('.column-header.in-progress')) {
          return 'in_progress'
        } else if (column.querySelector('.column-header.completed')) {
          return 'completed'
        }
      }

      return 'pending' // 默认状态
    }

    // 初始化数据
    onMounted(() => {
      loadProjects()
      loadUsers()
      loadTodos()
    })

    // 加载项目列表
    const loadProjects = async () => {
      try {
        const response = await getProjectList({
          userId: userInfo.value.id,
          userAuth: userInfo.value.auth
        })
        projects.value = response.data.data || []
      } catch (error) {
        console.error('加载项目列表失败:', error)
      }
    }

    // 加载用户列表
    const loadUsers = async () => {
      try {
        if (userInfo.value.auth === 'admin') {
          const response = await getUserList({
            userId: userInfo.value.id,
            userAuth: userInfo.value.auth
          })
          users.value = response.data.data || []
        }
      } catch (error) {
        console.error('加载用户列表失败:', error)
      }
    }

    // 加载待办事项列表
    const loadTodos = async () => {
      loading.value = true
      try {
        let response
        
        // 使用新的按日期查询API
        const dateStr = filters.queryDate || 'today'
        const options = {
          includeHistory: filters.includeHistory,
          projectId: filters.projectId,
          status: filters.status
        }
        
        // 如果是管理员且没有指定分配人，则查询所有任务
        if (userInfo.value.auth === 'admin') {
          // 管理员可以看到所有待办事项或特定分配人的任务
          response = await getTodosByDate(dateStr, options)
        } else {
          // 普通用户只能看到分配给自己的任务
          options.assigneeId = userInfo.value.id
          response = await getTodosByDate(dateStr, options)
        }

        let todoList = response.data.data || []

        // 应用前端筛选条件（关键词搜索和优先级）
        if (filters.keyword) {
          const keyword = filters.keyword.toLowerCase()
          todoList = todoList.filter(todo =>
            todo.title.toLowerCase().includes(keyword) ||
            (todo.description && todo.description.toLowerCase().includes(keyword))
          )
        }
        
        if (filters.priority) {
          todoList = todoList.filter(todo => todo.priority === filters.priority)
        }

        todos.value = todoList
      } catch (error) {
        console.error('加载待办事项失败:', error)
        ElMessage.error('加载待办事项失败')
      } finally {
        loading.value = false
      }
    }

    // 防抖搜索
    let searchTimer = null
    const debounceSearch = () => {
      clearTimeout(searchTimer)
      searchTimer = setTimeout(() => {
        loadTodos()
      }, 500)
    }

    // 权限检查
    const canEditTodo = (todo) => {
      return userInfo.value.auth === 'admin' || todo.assigneeId === userInfo.value.id
    }

    const canDeleteTodo = (todo) => {
      return userInfo.value.auth === 'admin'
    }

    // 获取优先级类型
    const getPriorityType = (priority) => {
      const types = {
        high: 'danger',
        medium: 'warning',
        low: 'info'
      }
      return types[priority] || 'info'
    }

    // 获取优先级文本
    const getPriorityText = (priority) => {
      const texts = {
        high: '高',
        medium: '中',
        low: '低'
      }
      return texts[priority] || '未知'
    }

    // 获取状态类型
    const getStatusType = (status) => {
      const types = {
        pending: 'info',
        in_progress: 'warning',
        completed: 'success'
      }
      return types[status] || 'info'
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const texts = {
        pending: '待处理',
        in_progress: '进行中',
        completed: '已完成'
      }
      return texts[status] || '未知'
    }

    // 格式化短日期
    const formatShortDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      const now = new Date()
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      const targetDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
      
      const diffTime = targetDate.getTime() - today.getTime()
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      
      if (diffDays === 0) return '今天'
      if (diffDays === 1) return '明天'
      if (diffDays === -1) return '昨天'
      if (diffDays > 0 && diffDays <= 7) return `${diffDays}天后`
      if (diffDays < 0 && diffDays >= -7) return `${Math.abs(diffDays)}天前`
      
      return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
    }

    // 格式化短日期时间
    const formatShortDateTime = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      
      return `${year}-${month}-${day}`
    }

    // 获取时间轴状态
    const getTimelineStatus = (dateString) => {
      if (!dateString) return 'normal'
      const date = new Date(dateString)
      const now = new Date()
      const diffTime = date.getTime() - now.getTime()
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      
      if (diffDays < 0) return 'overdue'
      if (diffDays <= 1) return 'urgent'
      if (diffDays <= 3) return 'warning'
      return 'normal'
    }

    // 获取时间轴文本
    const getTimelineText = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      const now = new Date()
      const diffTime = date.getTime() - now.getTime()
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      
      if (diffDays < 0) return `逾期${Math.abs(diffDays)}天`
      if (diffDays === 0) return '今天到期'
      if (diffDays === 1) return '明天到期'
      return `${diffDays}天后到期`
    }

    // 显示待办事项详情
    const showTodoDetail = async (todo) => {
      try {
        const response = await getTodoDetail(todo.id)
        selectedTodo.value = response.data.data
        showDetailDialog.value = true
      } catch (error) {
        console.error('获取待办详情失败:', error)
        ElMessage.error('获取待办详情失败')
      }
    }

    // 处理待办事项操作
    const handleTodoAction = async (command, todo) => {
      switch (command) {
        case 'view':
          showTodoDetail(todo)
          break
        case 'edit':
          editTodo(todo)
          break
        case 'admin-edit':
          adminEditTodo(todo)
          break
        case 'delete':
          deleteTodoItem(todo)
          break
        case 'start':
          await updateTodoStatusAction(todo, 'in_progress')
          break
        case 'complete':
          await updateTodoStatusAction(todo, 'completed')
          break
        case 'pause':
          await updateTodoStatusAction(todo, 'pending')
          break
        case 'reopen':
          await updateTodoStatusAction(todo, 'pending')
          break
      }
    }

    // 更新待办事项状态
    const updateTodoStatusAction = async (todo, newStatus) => {
      if (!canEditTodo(todo)) {
        ElMessage.warning('您没有权限修改此任务')
        return
      }

      try {
        await updateTodoStatus(todo.id, newStatus)
        ElMessage.success('状态更新成功')
        loadTodos()
      } catch (error) {
        console.error('更新状态失败:', error)
        ElMessage.error('状态更新失败')
      }
    }

    // 编辑待办事项
    const editTodo = (todo) => {
      if (!canEditTodo(todo)) {
        ElMessage.warning('您没有权限编辑此任务')
        return
      }

      editingTodo.value = todo
      isAdminEditing.value = false
      Object.assign(todoForm, {
        title: todo.title,
        description: todo.description,
        priority: todo.priority,
        projectId: todo.projectId,
        assigneeId: todo.assigneeId,
        startTime: todo.startTime,
        dueDate: todo.dueDate,
        status: 'pending',
        updateReason: ''
      })
      showCreateDialog.value = true
    }

    // 管理员编辑待办事项
    const adminEditTodo = (todo) => {
      if (userInfo.value.auth !== 'admin') {
        ElMessage.warning('只有管理员可以使用此功能')
        return
      }

      editingTodo.value = todo
      isAdminEditing.value = true
      Object.assign(todoForm, {
        title: todo.title,
        description: todo.description,
        priority: todo.priority,
        projectId: todo.projectId,
        assigneeId: todo.assigneeId,
        startTime: todo.startTime,
        dueDate: todo.dueDate,
        status: todo.status,
        updateReason: ''
      })
      showCreateDialog.value = true
    }

    // 获取对话框标题
    const getDialogTitle = () => {
      if (!editingTodo.value) {
        return '新建任务'
      }
      return isAdminEditing.value ? '管理员编辑任务' : '编辑任务'
    }

    // 删除待办事项
    const deleteTodoItem = async (todo) => {
      if (!canDeleteTodo(todo)) {
        ElMessage.warning('您没有权限删除此任务')
        return
      }

      try {
        await ElMessageBox.confirm(
          `确定要删除任务"${todo.title}"吗？`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        await deleteTodo(todo.id)
        ElMessage.success('删除成功')
        loadTodos()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          ElMessage.error('删除失败')
        }
      }
    }

    // 保存待办事项
    const saveTodo = async () => {
      if (!todoFormRef.value) return

      try {
        await todoFormRef.value.validate()
        saving.value = true

        if (editingTodo.value) {
          // 更新待办事项
          if (isAdminEditing.value) {
            // 管理员编辑
            await adminUpdateTodo({
              id: editingTodo.value.id,
              ...todoForm
            })
            ElMessage.success('管理员修改成功')
          } else {
            // 普通编辑
            await updateTodo({
              id: editingTodo.value.id,
              ...todoForm
            })
            ElMessage.success('更新成功')
          }
        } else {
          // 创建待办事项
          await createTodo(todoForm)
          ElMessage.success('创建成功')
        }

        showCreateDialog.value = false
        loadTodos()
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      } finally {
        saving.value = false
      }
    }

    // 重置表单
    const resetForm = () => {
      editingTodo.value = null
      isAdminEditing.value = false
      Object.assign(todoForm, {
        title: '',
        description: '',
        priority: 'medium',
        projectId: '',
        assigneeId: '',
        startTime: '',
        dueDate: '',
        status: 'pending',
        updateReason: ''
      })
      if (todoFormRef.value) {
        todoFormRef.value.clearValidate()
      }
    }

    // 格式化日期
    const formatDate = (dateString) => {
      return formatDateForDisplay(dateString)
    }

    return {
      userInfo,
      loading,
      saving,
      todos,
      projects,
      users,
      filters,
      showCreateDialog,
      showDetailDialog,
      editingTodo,
      selectedTodo,
      isAdminEditing,
      todoForm,
      todoFormRules,
      todoFormRef,
      // 拖拽相关
      pendingTodosData,
      inProgressTodosData,
      completedTodosData,
      canDragTodos,
      onDragChange,
      getPendingTodos,
      getInProgressTodos,
      getCompletedTodos,
      getStatusCount,
      formatDate,
      formatShortDate,
      formatShortDateTime,
      formatDetailedDateTime,
      getTimelineStatus,
      getTimelineText,
      loadTodos,
      debounceSearch,
      canEditTodo,
      canDeleteTodo,
      getPriorityType,
      getPriorityText,
      getStatusType,
      getStatusText,
      showTodoDetail,
      handleTodoAction,
      updateTodoStatusAction,
      editTodo,
      adminEditTodo,
      getDialogTitle,
      deleteTodoItem,
      saveTodo,
      resetForm
    }
  }
}
</script>

<style scoped>
.todo-kanban {
  padding: 24px;
  background: #f8f9fa;
  min-height: 100vh;
}

/* 页面头部 */
.kanban-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  background: white;
  padding: 20px 24px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.header-left h2 {
  margin: 0 0 12px 0;
  color: #1f2937;
  font-size: 24px;
  font-weight: 600;
}

.header-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.create-btn {
  border-radius: 6px;
  height: 36px;
  padding: 0 16px;
  font-weight: 500;
}

/* 筛选栏 */
.kanban-filters {
  margin-bottom: 24px;
  background: white;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.filter-group {
  display: flex;
  gap: 16px;
  align-items: center;
}

.filter-select {
  width: 160px;
}

.search-input {
  width: 280px;
}

/* 看板布局 */
.kanban-board {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  min-height: 600px;
}

.kanban-columns {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 24px;
  height: 100%;
}

.kanban-column {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  min-height: 500px;
}

/* 列头部 */
.column-header {
  margin-bottom: 16px;
  padding: 12px 16px;
  border-radius: 6px;
  background: white;
  border-left: 4px solid;
}

.column-header.pending {
  border-left-color: #6b7280;
}

.column-header.in-progress {
  border-left-color: #f59e0b;
}

.column-header.completed {
  border-left-color: #10b981;
}

.column-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1f2937;
}

.column-icon {
  font-size: 16px;
}

.column-count {
  background: #e5e7eb;
  color: #6b7280;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  margin-left: auto;
}

/* 列内容 */
.column-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 400px;
}

.empty-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #9ca3af;
  border: 2px dashed #e5e7eb;
  border-radius: 8px;
  margin-top: 20px;
  transition: all 0.3s ease;
}

.empty-column:hover {
  border-color: #d1d5db;
  background: #f9fafb;
}

.empty-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.empty-text {
  font-size: 14px;
  margin-bottom: 4px;
}

.drop-hint {
  font-size: 12px;
  color: #9ca3af;
  font-style: italic;
}

/* 拖拽列表 */
.draggable-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 100px;
}

/* 任务卡片 */
.todo-card {
  background: white;
  border-radius: 6px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.draggable-card {
  cursor: grab;
}

.draggable-card:active {
  cursor: grabbing;
}

.todo-card:hover {
  border-color: #d1d5db;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.todo-card.in-progress {
  border-left: 3px solid #f59e0b;
}

.todo-card.completed {
  opacity: 0.8;
}

/* 拖拽状态样式 */
.ghost-card {
  opacity: 0.5;
  background: #f3f4f6;
  border: 2px dashed #9ca3af;
}

.chosen-card {
  transform: rotate(5deg);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.drag-card {
  transform: rotate(5deg);
  opacity: 0.9;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-priority {
  display: flex;
  align-items: center;
}

.priority-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.card-priority.high .priority-dot {
  background: #ef4444;
}

.card-priority.medium .priority-dot {
  background: #f59e0b;
}

.card-priority.low .priority-dot {
  background: #6b7280;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.drag-handle {
  opacity: 0;
  transition: opacity 0.2s ease;
  cursor: grab;
  padding: 4px;
  border-radius: 4px;
  color: #9ca3af;
}

.drag-handle:hover {
  color: #6b7280;
  background: #f3f4f6;
}

.drag-handle:active {
  cursor: grabbing;
}

.drag-icon {
  font-size: 14px;
  line-height: 1;
  user-select: none;
}

.action-btn {
  opacity: 0;
  transition: opacity 0.2s ease;
}

.todo-card:hover .action-btn,
.todo-card:hover .drag-handle {
  opacity: 1;
}

.card-title {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 8px;
  line-height: 1.3;
  font-size: 13px;
}

.card-title.completed-title {
  text-decoration: line-through;
  color: #6b7280;
}

.card-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.meta-label {
  color: #6b7280;
}

.meta-value {
  color: #374151;
  font-weight: 500;
}

/* 时间轴 */
.card-timeline {
  border-top: 1px solid #f3f4f6;
  padding-top: 12px;
}

.timeline-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.timeline-icon {
  font-size: 14px;
}

.timeline-text {
  color: #6b7280;
  flex: 1;
}

.timeline-status {
  font-weight: 500;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.timeline-status.normal {
  background: #f3f4f6;
  color: #6b7280;
}

.timeline-status.warning {
  background: #fef3c7;
  color: #d97706;
}

.timeline-status.urgent {
  background: #fee2e2;
  color: #dc2626;
}

.timeline-status.overdue {
  background: #fecaca;
  color: #b91c1c;
}

.timeline-status.completed {
  background: #d1fae5;
  color: #059669;
}

/* 新卡片样式 */
.card-description {
  color: #6b7280;
  font-size: 12px;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 32px;
}

.card-times {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
  padding: 6px 8px;
  background: #f9fafb;
  border-radius: 3px;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
}

.time-icon {
  font-size: 12px;
  min-width: 14px;
}

.time-label {
  color: #6b7280;
  min-width: 30px;
  font-size: 10px;
}

.time-value {
  color: #374151;
  font-weight: 500;
  flex: 1;
  font-size: 10px;
  line-height: 1.2;
}

.card-priority-badge {
  display: flex;
  justify-content: flex-end;
}

.card-priority-badge .el-tag {
  font-size: 10px;
  padding: 1px 4px;
  height: 18px;
}

/* 进度指示器 */
.progress-indicator {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background: #f3f4f6;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #f59e0b, #d97706);
  width: 60%;
  border-radius: 2px;
  animation: progress-pulse 2s ease-in-out infinite;
}

@keyframes progress-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

/* 对话框样式 */
.todo-dialog .el-dialog__header {
  padding: 24px 24px 0;
}

.todo-dialog .el-dialog__body {
  padding: 24px;
}

.todo-form .el-form-item {
  margin-bottom: 20px;
}

.detail-dialog .el-dialog__body {
  padding: 24px;
}

.detail-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.detail-title {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.detail-title h3 {
  margin: 0;
  color: #1f2937;
  font-size: 20px;
  font-weight: 600;
  flex: 1;
  margin-right: 16px;
}

.detail-badges {
  display: flex;
  gap: 8px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  color: #374151;
  font-size: 16px;
  font-weight: 600;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f9fafb;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #6b7280;
  font-size: 14px;
}

.info-value {
  color: #1f2937;
  font-weight: 500;
  font-size: 14px;
}

.description-content {
  background: #f9fafb;
  padding: 16px;
  border-radius: 6px;
  line-height: 1.6;
  color: #374151;
  white-space: pre-wrap;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .kanban-columns {
    grid-template-columns: 1fr 1fr;
    gap: 16px;
  }
  
  .kanban-column:last-child {
    grid-column: 1 / -1;
  }
}

@media (max-width: 768px) {
  .todo-kanban {
    padding: 16px;
  }
  
  .kanban-columns {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-select,
.search-input,
.history-switch {
  width: 100%;
}
  
  .kanban-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-stats {
    justify-content: space-around;
    width: 100%;
  }
  
  /* 移动端拖拽优化 */
  .drag-handle {
    opacity: 1;
  }
}
</style>
