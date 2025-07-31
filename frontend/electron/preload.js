const { contextBridge, ipcRenderer } = require('electron')

// 安全地暴露 Electron API 给渲染进程
contextBridge.exposeInMainWorld('electronAPI', {
  // 显示通知
  showNotification: async (options) => {
    return await ipcRenderer.invoke('show-notification', options)
  },
  
  // 检查是否在 Electron 环境
  isElectron: true,
  
  // 获取平台信息
  platform: process.platform
}) 