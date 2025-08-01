<template>
  <div class="file-preview">
    <!-- 图片预览 -->
    <div v-if="isImage" class="image-preview">
      <el-image
        :src="fileInfo.fileUrl"
        fit="cover"
        style="max-width: 200px; max-height: 150px; border-radius: 8px; cursor: pointer;"
        :preview-src-list="[fileInfo.fileUrl]"
        preview-teleported
        @click="previewFile"
      >
        <template #error>
          <div class="image-error">
            <el-icon><Picture /></el-icon>
            <span>图片加载失败</span>
          </div>
        </template>
      </el-image>
      <div class="file-name">{{ getDisplayFileName() }}</div>
    </div>

    <!-- 其他文件预览 -->
    <div v-else class="file-download">
      <div class="file-icon" @click="previewFile">
        <el-icon size="32">
          <component :is="getFileIcon()" />
        </el-icon>
      </div>
      <div class="file-details" @click="previewFile">
        <div class="file-name">{{ getDisplayFileName() }}</div>
        <div class="file-size">{{ formatFileSize(fileInfo.fileSize) }}</div>
        <div class="file-type">{{ getFileTypeDescription() }}</div>
      </div>
      <el-button 
        type="primary" 
        size="small" 
        @click.stop="downloadFile"
        :icon="Download"
      >
        下载
      </el-button>
    </div>

    <!-- 文件预览对话框 -->
    <el-dialog
      v-model="showPreview"
      :title="getDisplayFileName()"
      width="80%"
      center
      @close="closePreview"
    >
      <div class="file-preview-dialog">
        <div class="file-info-detail">
          <div class="file-icon-large">
            <el-icon size="64">
              <component :is="getFileIcon()" />
            </el-icon>
          </div>
          <div class="file-details-large">
            <h3>{{ getDisplayFileName() }}</h3>
            <p><strong>文件大小:</strong> {{ formatFileSize(fileInfo.fileSize) }}</p>
            <p><strong>文件类型:</strong> {{ getFileTypeDescription() }}</p>
            <p><strong>MIME类型:</strong> {{ fileInfo.fileType || '未知' }}</p>
          </div>
        </div>
        
        <!-- 文件内容预览区域 -->
        <div v-if="previewContent" class="content-preview">
          <!-- 文本文件预览 -->
          <div v-if="previewContent.type === 'text'" class="text-preview">
            <h4>文件内容预览：</h4>
            <div class="text-content">
              <pre>{{ previewContent.content }}</pre>
            </div>
            <div v-if="!previewContent.isComplete" class="preview-warning">
              <el-alert
                title="仅显示前100行内容，完整内容请下载查看"
                type="info"
                :closable="false"
              />
            </div>
          </div>
          
          <!-- ZIP文件预览 -->
          <div v-else-if="previewContent.type === 'zip'" class="zip-preview">
            <h4>压缩包内容：</h4>
            <div class="zip-entries">
              <el-table :data="previewContent.entries" style="width: 100%" max-height="400">
                <el-table-column prop="name" label="文件名" width="300" show-overflow-tooltip />
                <el-table-column prop="size" label="大小" width="100">
                  <template #default="scope">
                    {{ scope.row.isDirectory ? '-' : formatFileSize(scope.row.size) }}
                  </template>
                </el-table-column>
                <el-table-column prop="isDirectory" label="类型" width="80">
                  <template #default="scope">
                    {{ scope.row.isDirectory ? '文件夹' : '文件' }}
                  </template>
                </el-table-column>
                <el-table-column prop="lastModified" label="修改时间" width="180">
                  <template #default="scope">
                    {{ formatDate(scope.row.lastModified) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-if="!previewContent.isComplete" class="preview-warning">
              <el-alert
                :title="`仅显示前${previewContent.totalEntries}个条目，完整内容请下载查看`"
                type="info"
                :closable="false"
              />
            </div>
          </div>
          
          <!-- 不支持预览的文件 -->
          <div v-else class="unsupported-preview">
            <h4>文件预览：</h4>
            <div class="unsupported-content">
              <el-alert
                :title="previewContent.message"
                type="warning"
                :closable="false"
              />
            </div>
          </div>
        </div>
        
        <!-- 加载状态 -->
        <div v-if="previewLoading" class="preview-loading">
          <el-loading text="正在加载文件预览..." />
        </div>
        
        <!-- 如果是PDF文件，可以提供在线预览选项 -->
        <div v-if="isPdfFile" class="pdf-preview">
          <h4>PDF预览：</h4>
          <p>您可以直接在浏览器中打开PDF文件</p>
          <el-button type="primary" @click="openInBrowser">在浏览器中打开</el-button>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closePreview">关闭</el-button>
          <el-button type="primary" @click="downloadFile" :icon="Download">
            下载文件
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { 
  Download, 
  Document, 
  Picture,
  VideoPlay,
  Headset,
  Files,
  FolderOpened,
  Edit,
  Reading,
  Monitor,
  Star
} from '@element-plus/icons-vue'
import { downloadChatFile, previewChatFile } from '../api/chat.js'

export default {
  name: 'FilePreview',
  components: {
    Download,
    Document,
    Picture,
    VideoPlay,
    Headset,
    Files,
    FolderOpened,
    Edit,
    Reading,
    Monitor,
    Star
  },
  props: {
    fileInfo: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const showPreview = ref(false)
    const previewContent = ref(null)
    const previewLoading = ref(false)

    // 判断是否为图片
    const isImage = computed(() => {
      const imageTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp', 'image/svg+xml', 'image/bmp', 'image/tiff']
      return imageTypes.includes(props.fileInfo.fileType?.toLowerCase())
    })

    // 判断是否为文本文件
    const isTextFile = computed(() => {
      const textTypes = ['text/plain', 'text/html', 'text/css', 'text/javascript', 'application/json', 'text/xml']
      const textExtensions = ['.txt', '.md', '.json', '.xml', '.html', '.css', '.js', '.ts', '.vue', '.jsx', '.tsx']
      return textTypes.includes(props.fileInfo.fileType?.toLowerCase()) || 
             textExtensions.some(ext => getDisplayFileName().toLowerCase().endsWith(ext))
    })

    // 判断是否为PDF文件
    const isPdfFile = computed(() => {
      return props.fileInfo.fileType?.toLowerCase() === 'application/pdf' || 
             getDisplayFileName().toLowerCase().endsWith('.pdf')
    })

    // 获取显示的文件名（优先使用原始文件名）
    const getDisplayFileName = () => {
      return props.fileInfo.originalFileName || props.fileInfo.fileName || '未知文件'
    }

    // 获取文件图标
    const getFileIcon = () => {
      const fileName = getDisplayFileName()
      const fileType = props.fileInfo.fileType || ''
      const lowerType = fileType.toLowerCase()
      const lowerName = fileName.toLowerCase()
      
      // 图片文件
      if (lowerType.startsWith('image/')) {
        return Picture
      }
      
      // 视频文件
      if (lowerType.startsWith('video/') || ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.mkv'].some(ext => lowerName.endsWith(ext))) {
        return VideoPlay
      }
      
      // 音频文件
      if (lowerType.startsWith('audio/') || ['.mp3', '.wav', '.flac', '.aac', '.ogg'].some(ext => lowerName.endsWith(ext))) {
        return Headset
      }
      
      // PDF文件
      if (lowerType === 'application/pdf' || lowerName.endsWith('.pdf')) {
        return Reading
      }
      
      // Office文档
      if (lowerType.includes('word') || lowerType.includes('document') || ['.doc', '.docx'].some(ext => lowerName.endsWith(ext))) {
        return Edit
      }
      
      if (lowerType.includes('excel') || lowerType.includes('spreadsheet') || ['.xls', '.xlsx'].some(ext => lowerName.endsWith(ext))) {
        return Monitor
      }
      
      if (lowerType.includes('powerpoint') || lowerType.includes('presentation') || ['.ppt', '.pptx'].some(ext => lowerName.endsWith(ext))) {
        return Star
      }
      
      // 压缩文件
      if (lowerType.includes('zip') || lowerType.includes('rar') || lowerType.includes('compressed') || 
          ['.zip', '.rar', '.7z', '.tar', '.gz'].some(ext => lowerName.endsWith(ext))) {
        return FolderOpened
      }
      
      // 代码/文本文件
      if (lowerType.startsWith('text/') || ['.txt', '.md', '.json', '.xml', '.html', '.css', '.js', '.ts', '.vue', '.jsx', '.tsx', '.py', '.java', '.cpp', '.c'].some(ext => lowerName.endsWith(ext))) {
        return Files
      }
      
      // 默认文档图标
      return Document
    }

    // 获取文件类型描述
    const getFileTypeDescription = () => {
      const fileName = getDisplayFileName()
      const fileType = props.fileInfo.fileType || ''
      const lowerType = fileType.toLowerCase()
      const lowerName = fileName.toLowerCase()
      
      if (lowerType.startsWith('image/')) return '图片文件'
      if (lowerType.startsWith('video/')) return '视频文件'
      if (lowerType.startsWith('audio/')) return '音频文件'
      if (lowerType === 'application/pdf') return 'PDF文档'
      if (lowerType.includes('word') || lowerType.includes('document')) return 'Word文档'
      if (lowerType.includes('excel') || lowerType.includes('spreadsheet')) return 'Excel表格'
      if (lowerType.includes('powerpoint') || lowerType.includes('presentation')) return 'PowerPoint演示文稿'
      if (lowerType.includes('zip') || lowerType.includes('compressed')) return '压缩文件'
      if (lowerType.startsWith('text/')) return '文本文件'
      
      // 根据文件扩展名判断
      if (lowerName.endsWith('.txt')) return '文本文件'
      if (lowerName.endsWith('.md')) return 'Markdown文档'
      if (lowerName.endsWith('.json')) return 'JSON数据'
      if (lowerName.endsWith('.xml')) return 'XML文档'
      if (lowerName.endsWith('.html')) return 'HTML网页'
      if (lowerName.endsWith('.css')) return 'CSS样式表'
      if (['.js', '.ts', '.vue', '.jsx', '.tsx'].some(ext => lowerName.endsWith(ext))) return '代码文件'
      
      return '文件'
    }

    // 格式化文件大小
    const formatFileSize = (bytes) => {
      if (!bytes) return '0 B'
      
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    // 格式化日期
    const formatDate = (timestamp) => {
      if (!timestamp) return '未知'
      const date = new Date(timestamp)
      return date.toLocaleString('zh-CN')
    }

    // 预览文件
    const previewFile = async () => {
      // 如果是图片，Element Plus的el-image已经处理了预览
      if (isImage.value) {
        return
      }
      
      // 显示预览对话框
      showPreview.value = true
      
      // 如果支持内容预览，则加载内容
      if (isTextFile.value || getDisplayFileName().toLowerCase().endsWith('.zip') || getDisplayFileName().toLowerCase().endsWith('.rar')) {
        previewLoading.value = true
        previewContent.value = null
        
        try {
          const response = await previewChatFile(props.fileInfo.fileUrl, props.fileInfo.fileType)
          if (response.data.code === 200) {
            previewContent.value = response.data.data
          } else {
            ElMessage.error('文件预览失败: ' + response.data.message)
          }
        } catch (error) {
          console.error('文件预览失败:', error)
          ElMessage.error('文件预览失败')
        } finally {
          previewLoading.value = false
        }
      }
    }

    // 关闭预览
    const closePreview = () => {
      showPreview.value = false
      previewContent.value = null
    }

    // 在浏览器中打开文件
    const openInBrowser = () => {
      window.open(props.fileInfo.fileUrl, '_blank')
    }

    // 下载文件（使用原始文件名）
    const downloadFile = async () => {
      try {
        const originalFileName = getDisplayFileName()
        const response = await downloadChatFile(props.fileInfo.fileUrl, originalFileName)
        
        // 创建下载链接
        const blob = new Blob([response.data])
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = originalFileName
        link.target = '_blank'
        
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        
        // 清理URL对象
        window.URL.revokeObjectURL(url)
        
        ElMessage.success('文件下载成功')
      } catch (error) {
        console.error('下载失败:', error)
        ElMessage.error('下载失败，您可以右键点击链接选择"另存为"')
      }
    }

    return {
      showPreview,
      previewContent,
      previewLoading,
      isImage,
      isTextFile,
      isPdfFile,
      getDisplayFileName,
      getFileIcon,
      getFileTypeDescription,
      formatFileSize,
      formatDate,
      previewFile,
      closePreview,
      openInBrowser,
      downloadFile,
      Download,
      Picture
    }
  }
}
</script>

<style scoped>
.file-preview {
  max-width: 250px;
}

.image-preview {
  text-align: center;
}

.image-preview .file-name {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
  word-break: break-all;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100px;
  background: #f5f5f5;
  border-radius: 8px;
  color: #999;
}

.image-error span {
  margin-top: 8px;
  font-size: 12px;
}

.file-download {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  gap: 12px;
}

.file-icon {
  color: #409eff;
  flex-shrink: 0;
  cursor: pointer;
  transition: color 0.2s;
}

.file-icon:hover {
  color: #66b1ff;
}

.file-details {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.file-details:hover .file-name {
  color: #409eff;
}

.file-name {
  font-size: 14px;
  color: #333;
  word-break: break-all;
  margin-bottom: 4px;
  transition: color 0.2s;
}

.file-size {
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}

.file-type {
  font-size: 11px;
  color: #666;
}

/* 预览对话框样式 */
.file-preview-dialog {
  text-align: center;
}

.file-info-detail {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.file-icon-large {
  color: #409eff;
  flex-shrink: 0;
}

.file-details-large {
  flex: 1;
  text-align: left;
}

.file-details-large h3 {
  margin: 0 0 10px 0;
  color: #333;
  word-break: break-all;
}

.file-details-large p {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

.content-preview {
  margin: 20px 0;
  text-align: left;
}

.text-preview h4,
.zip-preview h4,
.unsupported-preview h4 {
  margin-bottom: 15px;
  color: #333;
  text-align: left;
}

.text-content {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  max-height: 400px;
  overflow-y: auto;
  font-family: 'Courier New', monospace;
}

.text-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-size: 13px;
  line-height: 1.4;
}

.zip-entries {
  margin-bottom: 15px;
}

.preview-warning {
  margin-top: 15px;
}

.preview-loading {
  padding: 40px;
  text-align: center;
}

.pdf-preview {
  margin: 20px 0;
  text-align: left;
}

.pdf-preview h4 {
  margin-bottom: 10px;
  color: #333;
}

.unsupported-content {
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style> 