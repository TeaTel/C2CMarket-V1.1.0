import axios from 'axios'

// 获取API基础URL（支持多环境配置）
function getBaseURL() {
  // 优先使用环境变量（仅在有实际值时使用）
  const envBaseUrl = import.meta.env.VITE_API_BASE_URL
  if (envBaseUrl && envBaseUrl.trim() !== '') {
    return `${envBaseUrl.trim()}/api`
  }

  // 生产环境：使用相对路径（前后端一体化部署）
  // 空字符串会让 axios 自动使用当前域名，实现同源请求
  // 例如：https://c2cmarket.store/api/xxx
  const hostname = window.location.hostname

  // 开发环境：localhost 或 127.0.0.1
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return '/api'  // 使用相对路径，由 vite proxy 转发到后端
  }

  // 其他生产环境：返回空字符串，axios 会自动使用当前 origin
  return ''
}

// 创建axios实例
const api = axios.create({
  baseURL: getBaseURL(),
  timeout: 20000, // 增加超时时间到20秒
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加JWT令牌
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理错误（增强版）
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      let message = '请求失败'
      let shouldLogout = false

      // 根据状态码生成友好的错误消息
      if (data && data.message) {
        message = data.message
      } else if (status === 400) {
        message = '请求参数错误，请检查输入'
      } else if (status === 401) {
        message = '登录已过期，请重新登录'
        shouldLogout = true
      } else if (status === 403) {
        message = '权限不足，无法执行此操作'
      } else if (status === 404) {
        message = '请求的资源不存在'
      } else if (status === 429) {
        message = '操作太频繁，请稍后再试'
      } else if (status >= 500 && status < 504) {
        // 服务器错误（包括502 Bad Gateway, 503 Service Unavailable）
        const serverErrors = {
          500: '服务器内部错误',
          502: '网关错误，服务暂时不可用',
          503: '服务维护中，请稍后重试',
          504: '网关超时，请稍后重试'
        }
        message = serverErrors[status] || '服务器繁忙，请稍后重试'
      }

      console.error(`[API Error ${status}]:`, message)

      // 如果需要登出（如token过期）
      if (shouldLogout) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        // 不在这里直接跳转，让组件自行决定如何处理
      }

      return Promise.reject({ message, status, code: status })
    } else if (error.request) {
      // 请求已发出但没有收到响应（网络问题）
      console.error('[Network Error]:', error.message)

      // 区分不同网络错误类型
      if (error.message.includes('timeout')) {
        return Promise.reject({
          message: '请求超时，请检查网络后重试',
          status: 0,
          code: 'TIMEOUT'
        })
      } else if (error.message.includes('Network Error')) {
        return Promise.reject({
          message: '网络连接失败，请检查网络设置',
          status: 0,
          code: 'NETWORK_ERROR'
        })
      }

      return Promise.reject({
        message: '网络连接失败，请检查网络设置',
        status: 0,
        code: 'NETWORK_ERROR'
      })
    } else {
      // 请求配置出错
      console.error('[Request Config Error]:', error.message)
      return Promise.reject({
        message: '请求配置错误',
        status: 0,
        code: 'CONFIG_ERROR'
      })
    }
  }
)

// ============================================
// 用户相关API
// ============================================
export const userApi = {
  register(data) {
    return api.post('/v2/users/register', data)
  },

  login(data) {
    return api.post('/v2/users/login', data)
  },

  getUserInfo() {
    return api.get('/v2/users/info')
  },

  updateProfile(data) {
    return api.put('/v2/users/profile', data)
  },

  changePassword(data) {
    return api.put('/v2/users/password', data)
  },

  uploadAvatar(formData) {
    return api.post('/v2/users/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

// ============================================
// 商品相关API
// ============================================
export const productApi = {
  getProducts(params) {
    return api.get('/products', { params })
  },

  getProductDetail(id) {
    return api.get(`/products/${id}`)
  },

  createProduct(data) {
    return api.post('/products', data)
  },

  updateProduct(id, data) {
    return api.put(`/products/${id}`, data)
  },

  deleteProduct(id) {
    return api.delete(`/products/${id}`)
  },

  getMyProducts() {
    return api.get('/products/my')
  },

  uploadImage(formData) {
    return api.post('/products/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  searchProducts(keyword) {
    return api.get('/products/search', { params: { keyword } })
  }
}

// ============================================
// 订单相关API
// ============================================
export const orderApi = {
  createOrder(data) {
    return api.post('/orders', data)
  },

  getOrderDetail(id) {
    return api.get(`/orders/${id}`)
  },

  getBuyerOrders(params) {
    return api.get('/orders/buyer', { params })
  },

  getSellerOrders(params) {
    return api.get('/orders/seller', { params })
  },

  updateOrderStatus(id, status) {
    return api.put(`/orders/${id}/status`, { status })
  },

  cancelOrder(id) {
    return api.put(`/orders/${id}/cancel`)
  },

  confirmOrder(id) {
    return api.put(`/orders/${id}/confirm`)
  }
}

// ============================================
// 消息/聊天相关API（增强版）
// ============================================
export const messageApi = {
  // 发送消息
  sendMessage(data) {
    return api.post('/messages', data)
  },

  // 获取对话记录（分页）
  getConversation(otherUserId, params = {}) {
    return api.get(`/messages/conversation/${otherUserId}`, { params })
  },

  // 获取联系人/会话列表
  getContacts() {
    return api.get('/messages/contacts')
  },

  // 获取未读消息列表
  getUnreadMessages() {
    return api.get('/messages/unread')
  },

  // 标记单条消息为已读
  markAsRead(messageId) {
    return api.put(`/messages/${messageId}/read`)
  },

  // 标记整个对话为已读
  markConversationAsRead(senderId) {
    return api.put(`/messages/conversation/${senderId}/read`)
  },

  // 获取未读消息数量
  getUnreadCount() {
    return api.get('/messages/unread/count')
  },

  // 删除消息
  deleteMessage(messageId) {
    return api.delete(`/messages/${messageId}`)
  },

  // 撤回消息（发送后2分钟内）
  recallMessage(messageId) {
    return api.put(`/messages/${messageId}/recall`)
  },

  // 获取最近的消息预览（用于首页展示）
  getRecentMessages(limit = 10) {
    return api.get('/messages/recent', { params: { limit } })
  },

  // 创建会话（如果不存在则创建）
  createConversation(userId, productId) {
    return api.post('/messages/conversation', { userId, productId })
  },

  // 获取与某用户的会话ID
  getConversationId(otherUserId) {
    return api.get(`/messages/conversation-id/${otherUserId}`)
  }
}

// ============================================
// 分类相关API
// ============================================
export const categoryApi = {
  getCategories(params) {
    return api.get('/v2/categories', { params })
  },

  getCategoryTree() {
    return api.get('/v2/categories/tree')
  },

  getCategoryDetail(id) {
    return api.get(`/v2/categories/${id}`)
  },

  createCategory(data) {
    return api.post('/v2/categories', data)
  },

  updateCategory(id, data) {
    return api.put(`/v2/categories/${id}`, data)
  },

  deleteCategory(id) {
    return api.delete(`/v2/categories/${id}`)
  },

  getCategoryProducts(categoryId, params) {
    return api.get(`/v2/categories/${categoryId}/products`, { params })
  }
}

// ============================================
// WebSocket连接管理（用于实时聊天）
// ============================================
class WebSocketManager {
  constructor() {
    this.ws = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 3000
    this.listeners = new Map()
    this.isConnected = false
  }

  connect(token) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('WebSocket already connected')
      return
    }

    const hostname = window.location.hostname
    const isDev = hostname === 'localhost' || hostname === '127.0.0.1'
    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
    
    // 开发环境：使用 localhost:8080
    // 生产环境：使用当前域名的 wss/ws 协议（前后端一体化部署）
    const wsUrl = import.meta.env.VITE_WS_URL || 
      (isDev 
        ? `ws://localhost:8080/ws/chat`
        : `${protocol}://${hostname}/ws/chat`
      )

    try {
      this.ws = new WebSocket(`${wsUrl}?token=${token}`)

      this.ws.onopen = () => {
        console.log('WebSocket connected')
        this.isConnected = true
        this.reconnectAttempts = 0
        this.emit('connected')
      }

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          this.emit('message', data)

          // 根据消息类型分发事件
          if (data.type) {
            this.emit(data.type, data)
          }
        } catch (e) {
          console.error('Failed to parse WebSocket message:', e)
        }
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error)
        this.emit('error', error)
      }

      this.ws.onclose = () => {
        console.log('WebSocket disconnected')
        this.isConnected = false
        this.emit('disconnected')

        // 自动重连
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++
          console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
          setTimeout(() => this.connect(token), this.reconnectDelay * this.reconnectAttempts)
        }
      }
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error)
    }
  }

  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
      this.isConnected = false
    }
  }

  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
      return true
    }
    console.warn('WebSocket is not connected')
    return false
  }

  // 发送聊天消息
  sendMessage(content, receiverId, productId = null) {
    return this.send({
      type: 'chat',
      content,
      receiverId,
      productId,
      timestamp: Date.now()
    })
  }

  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  off(event, callback) {
    if (this.listeners.has(event)) {
      const callbacks = this.listeners.get(event)
      const index = callbacks.indexOf(callback)
      if (index > -1) {
        callbacks.splice(index, 1)
      }
    }
  }

  emit(event, data) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).forEach(callback => {
        try {
          callback(data)
        } catch (e) {
          console.error(`Error in event handler for ${event}:`, e)
        }
      })
    }
  }
}

// 创建全局WebSocket实例
export const wsManager = new WebSocketManager()

export default api
