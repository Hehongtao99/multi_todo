import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    host: '0.0.0.0'
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets'
  },
  define: {
    // 修复 @stomp/stompjs 在浏览器环境中的 global 未定义问题
    global: 'globalThis',
    // 修复可能的 process 未定义问题
    'process.env': {}
  }
})