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