<template>
  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <p>管理系统中的所有普通用户</p>
    </div>

    <el-card class="user-card">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
        </div>
      </template>

      <el-table 
        :data="userList" 
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="auth" label="权限" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.auth === 'admin' ? 'danger' : 'success'">
              {{ scope.row.auth === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="注册时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdTime) }}
          </template>
        </el-table-column>
                 <el-table-column label="操作" width="120">
           <template #default="scope">
             <el-button 
               type="info" 
               size="small"
               @click="handleViewProjects(scope.row)"
             >
               查看项目
             </el-button>
           </template>
         </el-table-column>
      </el-table>
    </el-card>


  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList } from '../api/user'

export default {
  name: 'UserManagement',
  setup() {
    const loading = ref(false)
    const userList = ref([])

    // 获取用户列表
    const fetchUserList = async () => {
      loading.value = true
      try {
        const response = await getUserList()
        if (response.data.code === 200) {
          userList.value = response.data.data
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        ElMessage.error('获取用户列表失败')
      } finally {
        loading.value = false
      }
    }



    // 查看用户项目
    const handleViewProjects = (user) => {
      ElMessage.info(`查看用户 ${user.username} 的项目功能待开发`)
    }

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '-'
      return new Date(dateString).toLocaleString('zh-CN')
    }

    onMounted(() => {
      fetchUserList()
    })

    return {
      loading,
      userList,
      handleViewProjects,
      formatDate
    }
  }
}
</script>

<style scoped>
.user-management {
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

.user-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 