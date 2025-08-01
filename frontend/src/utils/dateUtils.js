/**
 * 日期工具函数
 */

/**
 * 格式化日期为后端期望的格式 (yyyy-MM-dd HH:mm:ss)
 * @param {Date|string} date - 日期对象或日期字符串
 * @returns {string} 格式化后的日期字符串
 */
export const formatDateForBackend = (date) => {
  if (!date) return null
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return null
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 格式化日期为显示格式
 * @param {string} dateString - 日期字符串
 * @returns {string} 格式化后的显示字符串
 */
export const formatDateForDisplay = (dateString) => {
  if (!dateString) return '-'
  
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return '-'
    
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    return '-'
  }
}

/**
 * 将Element Plus的日期选择器值转换为后端格式
 * @param {Date} date - Element Plus日期选择器的值
 * @returns {string|null} 后端格式的日期字符串
 */
export const convertElementDateToBackend = (date) => {
  if (!date) return null
  return formatDateForBackend(date)
}

/**
 * 获取当前时间的后端格式字符串
 * @returns {string} 当前时间的后端格式字符串
 */
export const getCurrentTimeForBackend = () => {
  return formatDateForBackend(new Date())
}

/**
 * 格式化日期时间为详细格式（包含具体时间）
 * @param {string} dateString - 日期字符串
 * @returns {string} 格式化后的详细时间字符串
 */
export const formatDetailedDateTime = (dateString) => {
  if (!dateString) return '未设置'
  
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return '无效时间'
    
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}`
  } catch (error) {
    return '时间错误'
  }
}

/**
 * 格式化日期时间为简短格式（仅显示时间）
 * @param {string} dateString - 日期字符串
 * @returns {string} 格式化后的时间字符串
 */
export const formatTimeOnly = (dateString) => {
  if (!dateString) return '未设置'
  
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return '无效时间'
    
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    
    return `${hours}:${minutes}`
  } catch (error) {
    return '时间错误'
  }
} 