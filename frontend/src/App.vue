<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
import { onMounted } from 'vue'
import notificationUtil from './utils/notification.js'

export default {
  name: 'App',
  setup() {
    onMounted(async () => {
      // 初始化通知权限
      try {
        await notificationUtil.init()
        console.log('通知权限初始化完成')
      } catch (error) {
        console.error('通知权限初始化失败:', error)
      }
    })
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
}

body {
  margin: 0;
  padding: 0;
  height: 100vh;
  background-color: #f5f5f5;
}
</style> 