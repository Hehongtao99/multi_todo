import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Dashboard from '../views/Dashboard.vue'
import UserManagement from '../views/UserManagement.vue'
import ProjectManagement from '../views/ProjectManagement.vue'
import ProjectDetail from '../views/ProjectDetail.vue'
import TodoManagement from '../views/TodoManagement.vue'
import Chat from '../views/Chat.vue'
import Profile from '../views/Profile.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    children: [
      {
        path: 'users',
        name: 'UserManagement',
        component: UserManagement
      },
      {
        path: 'projects',
        name: 'ProjectManagement',
        component: ProjectManagement
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: ProjectDetail
      },
      {
        path: 'todos',
        name: 'TodoManagement',
        component: TodoManagement
      },
      {
        path: 'chat',
        name: 'Chat',
        component: Chat
      },
      {
        path: 'profile',
        name: 'Profile',
        component: Profile
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router 