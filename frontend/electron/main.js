const { app, BrowserWindow, Notification, ipcMain } = require('electron')
const path = require('path')
const isDev = process.env.NODE_ENV === 'development'

function createWindow() {
  // 创建浏览器窗口
  const mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, 'preload.js')
    },
    icon: path.join(__dirname, '../build/icon.ico'),
    title: '多人待办系统'
  })

  // 加载应用
  if (isDev) {
    mainWindow.loadURL('http://localhost:5173')
    // 开发环境下打开开发者工具
    mainWindow.webContents.openDevTools()
  } else {
    mainWindow.loadFile(path.join(__dirname, '../dist/index.html'))
  }
}

// 处理通知请求
ipcMain.handle('show-notification', async (event, options) => {
  try {
    const notification = new Notification({
      title: options.title,
      body: options.body,
      icon: options.icon,
      silent: options.silent || false,
      urgency: options.urgency || 'normal'
    })

    notification.show()
    
    return {
      success: true,
      notification: notification
    }
  } catch (error) {
    console.error('显示通知失败:', error)
    return {
      success: false,
      error: error.message
    }
  }
})

// 当Electron完成初始化并准备创建浏览器窗口时调用此方法
app.whenReady().then(createWindow)

// 当所有窗口都关闭时退出应用
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow()
  }
}) 