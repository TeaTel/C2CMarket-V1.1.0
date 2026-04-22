<template>
  <div class="messages">
    <NavBar />
    
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1 class="page-title">消息中心</h1>
          <div class="unread-count" v-if="unreadCount > 0">
            <span class="count-badge">{{ unreadCount }}</span>
            条未读消息
          </div>
        </div>
        
        <div class="messages-container">
          <!-- 联系人列表 -->
          <div class="contacts-sidebar">
            <div class="contacts-header">
              <h3>联系人</h3>
              <button @click="refreshContacts" class="refresh-btn" :disabled="loadingContacts">
                {{ loadingContacts ? '刷新中...' : '刷新' }}
              </button>
            </div>
            
            <div v-if="loadingContacts" class="loading-contacts">加载中...</div>
            
            <div v-else-if="contacts.length === 0" class="empty-contacts">
              <div class="empty-icon">💬</div>
              <p>暂无联系人</p>
            </div>
            
            <div v-else class="contacts-list">
              <div
                v-for="contact in contacts"
                :key="contact.userId"
                @click="selectContact(contact)"
                :class="{ active: selectedContact && selectedContact.userId === contact.userId }"
                class="contact-item"
              >
                <div class="contact-avatar">
                  <img :src="contact.avatar || '/default-avatar.png'" :alt="contact.username" />
                  <span v-if="contact.unreadCount > 0" class="unread-dot"></span>
                </div>
                <div class="contact-info">
                  <div class="contact-name">{{ contact.username }}</div>
                  <div class="contact-last-message">{{ contact.lastMessage || '暂无消息' }}</div>
                </div>
                <div class="contact-meta">
                  <div class="contact-time">{{ formatTime(contact.lastMessageTime) }}</div>
                  <div v-if="contact.unreadCount > 0" class="contact-unread">
                    {{ contact.unreadCount }}
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 聊天窗口 -->
          <div class="chat-window">
            <div v-if="!selectedContact" class="no-chat-selected">
              <div class="welcome-icon">💬</div>
              <h3>选择联系人开始聊天</h3>
              <p>点击左侧的联系人列表开始对话</p>
            </div>
            
            <div v-else class="chat-container">
              <!-- 聊天头部 -->
              <div class="chat-header">
                <div class="chat-partner">
                  <div class="partner-avatar">
                    <img :src="selectedContact.avatar || '/default-avatar.png'" :alt="selectedContact.username" />
                  </div>
                  <div class="partner-info">
                    <h4>{{ selectedContact.username }}</h4>
                    <div class="partner-status">
                      <span class="status-dot online"></span>
                      在线
                    </div>
                  </div>
                </div>
                <button @click="clearChat" class="clear-chat-btn">清空聊天记录</button>
              </div>
              
              <!-- 聊天消息 -->
              <div class="chat-messages" ref="messagesContainer">
                <div v-if="loadingMessages" class="loading-messages">加载消息中...</div>
                
                <div v-else-if="messages.length === 0" class="no-messages">
                  <p>还没有消息，开始对话吧！</p>
                </div>
                
                <div v-else class="messages-list">
                  <div
                    v-for="message in messages"
                    :key="message.id"
                    :class="{
                      'message-sent': message.senderId === currentUser.id,
                      'message-received': message.senderId !== currentUser.id
                    }"
                    class="message-item"
                  >
                    <div class="message-avatar" v-if="message.senderId !== currentUser.id">
                      <img :src="selectedContact.avatar || '/default-avatar.png'" :alt="selectedContact.username" />
                    </div>
                    <div class="message-content">
                      <div class="message-text">{{ message.content }}</div>
                      <div class="message-meta">
                        <span class="message-time">{{ formatMessageTime(message.createdAt) }}</span>
                        <span v-if="message.senderId === currentUser.id" class="message-status">
                          {{ message.isRead ? '已读' : '未读' }}
                        </span>
                      </div>
                    </div>
                    <div class="message-avatar" v-if="message.senderId === currentUser.id">
                      <img :src="currentUser.avatar || '/default-avatar.png'" :alt="currentUser.username" />
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 消息输入框 -->
              <div class="chat-input">
                <textarea
                  v-model="newMessage"
                  @keydown.enter.exact.prevent="sendMessage"
                  placeholder="输入消息..."
                  rows="3"
                ></textarea>
                <div class="input-actions">
                  <button
                    @click="sendMessage"
                    :disabled="!newMessage.trim() || sendingMessage"
                    class="send-btn"
                  >
                    {{ sendingMessage ? '发送中...' : '发送' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import NavBar from '../components/NavBar.vue'
import { messageApi } from '../services/api'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const contacts = ref([])
const selectedContact = ref(null)
const messages = ref([])
const newMessage = ref('')
const loadingContacts = ref(true)
const loadingMessages = ref(false)
const sendingMessage = ref(false)
const unreadCount = ref(0)
const messagesContainer = ref(null)

const currentUser = computed(() => authStore.currentUser)

onMounted(async () => {
  await Promise.all([loadContacts(), loadUnreadCount()])
  
  // 检查URL参数，如果有to参数，自动选择联系人
  const toUserId = route.query.to
  if (toUserId) {
    const contact = contacts.value.find(c => c.userId === parseInt(toUserId))
    if (contact) {
      selectContact(contact)
    }
  }
})

async function loadContacts() {
  try {
    loadingContacts.value = true
    const response = await messageApi.getContacts()
    if (response.code === 200) {
      contacts.value = response.data || []
    }
  } catch (error) {
    console.error('加载联系人失败:', error)
  } finally {
    loadingContacts.value = false
  }
}

async function loadUnreadCount() {
  try {
    const response = await messageApi.getUnreadCount()
    if (response.code === 200) {
      unreadCount.value = response.data || 0
    }
  } catch (error) {
    console.error('加载未读消息数失败:', error)
  }
}

async function selectContact(contact) {
  selectedContact.value = contact
  await loadMessages(contact.userId)
  
  // 标记对话为已读
  if (contact.unreadCount > 0) {
    await markConversationAsRead(contact.userId)
    contact.unreadCount = 0
    updateUnreadCount()
  }
  
  // 滚动到底部
  scrollToBottom()
}

async function loadMessages(userId) {
  try {
    loadingMessages.value = true
    const response = await messageApi.getConversation(userId)
    if (response.code === 200) {
      messages.value = response.data || []
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  } finally {
    loadingMessages.value = false
  }
}

async function sendMessage() {
  if (!newMessage.value.trim() || !selectedContact.value) {
    return
  }

  try {
    sendingMessage.value = true
    
    const messageData = {
      receiverId: selectedContact.value.userId,
      content: newMessage.value.trim()
    }
    
    // 如果有商品ID参数，添加到消息中
    const productId = route.query.product
    if (productId) {
      messageData.productId = productId
    }
    
    const response = await messageApi.sendMessage(messageData)
    if (response.code === 200) {
      // 添加新消息到列表
      const newMsg = {
        id: Date.now(), // 临时ID
        senderId: currentUser.value.id,
        receiverId: selectedContact.value.userId,
        content: newMessage.value.trim(),
        createdAt: new Date().toISOString(),
        isRead: false
      }
      
      messages.value.push(newMsg)
      newMessage.value = ''
      
      // 更新联系人最后消息
      const contact = contacts.value.find(c => c.userId === selectedContact.value.userId)
      if (contact) {
        contact.lastMessage = newMsg.content
        contact.lastMessageTime = newMsg.createdAt
      }
      
      // 滚动到底部
      scrollToBottom()
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    alert('发送消息失败，请稍后重试')
  } finally {
    sendingMessage.value = false
  }
}

async function markConversationAsRead(userId) {
  try {
    await messageApi.markConversationAsRead(userId)
  } catch (error) {
    console.error('标记对话已读失败:', error)
  }
}

async function refreshContacts() {
  await Promise.all([loadContacts(), loadUnreadCount()])
}

function clearChat() {
  if (!confirm('确定要清空聊天记录吗？此操作不可撤销。')) {
    return
  }
  messages.value = []
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  // 如果是今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  
  // 如果是昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }
  
  // 其他情况显示日期
  return date.toLocaleDateString('zh-CN')
}

function formatMessageTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

function updateUnreadCount() {
  unreadCount.value = contacts.value.reduce((total, contact) => total + (contact.unreadCount || 0), 0)
}

// 监听消息列表变化，自动滚动
watch(messages, () => {
  scrollToBottom()
}, { deep: true })
</script>

<style scoped>
.messages {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 2rem 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 1rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  color: #333;
  margin: 0;
}

.unread-count {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #666;
  font-size: 0.9rem;
}

.count-badge {
  background-color: #f44336;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 10px;
  font-size: 0.8rem;
  font-weight: 500;
}

.messages-container {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 2rem;
  height: calc(100vh - 200px);
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.contacts-sidebar {
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.contacts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #eee;
}

.contacts-header h3 {
  font-size: 1.2rem;
  color: #333;
  margin: 0;
}

.refresh-btn {
  padding: 0.25rem 0.75rem;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.8rem;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.refresh-btn:hover:not(:disabled) {
  background: #e0e0e0;
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-contacts {
  padding: 2rem;
  text-align: center;
  color: #666;
}

.empty-contacts {
  padding: 2rem;
  text-align: center;
  color: #666;
}

.empty-contacts .empty-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.contacts-list {
  flex: 1;
  overflow-y: auto;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f5f5f5;
}

.contact-item:hover {
  background-color: #f9f9f9;
}

.contact-item.active {
  background-color: #e3f2fd;
}

.contact-avatar {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background: #f5f5f5;
}

.contact-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.unread-dot {
  position: absolute;
  top: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background-color: #f44336;
  border-radius: 50%;
  border: 2px solid white;
}

.contact-info {
  flex: 1;
  min-width: 0;
}

.contact-name {
  font-size: 0.9rem;
  font-weight: 500;
  color: #333;
  margin-bottom: 0.25rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.contact-last-message {
  font-size: 0.8rem;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.contact-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.25rem;
}

.contact-time {
  font-size: 0.7rem;
  color: #999;
}

.contact-unread {
  background-color: #f44336;
  color: white;
  font-size: 0.7rem;
  padding: 0.1rem 0.4rem;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
}

.chat-window {
  display: flex;
  flex-direction: column;
}

.no-chat-selected {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
}

.welcome-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.no-chat-selected h3 {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 0.5rem;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #eee;
}

.chat-partner {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.partner-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background: #f5f5f5;
}

.partner-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.partner-info h4 {
  font-size: 1rem;
  color: #333;
  margin: 0 0 0.25rem 0;
}

.partner-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: #666;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.online {
  background-color: #4CAF50;
}

.clear-chat-btn {
  padding: 0.5rem 1rem;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.8rem;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-chat-btn:hover {
  background: #e0e0e0;
}

.chat-messages {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
  background: #f9f9f9;
}

.loading-messages {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.no-messages {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.message-item {
  display: flex;
  gap: 0.75rem;
  max-width: 70%;
}

.message-sent {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-received {
  align-self: flex-start;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
}

.message-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.message-sent .message-content {
  align-items: flex-end;
}

.message-received .message-content {
  align-items: flex-start;
}

.message-text {
  padding: 0.75rem 1rem;
  border-radius: 18px;
  font-size: 0.9rem;
  line-height: 1.4;
  word-break: break-word;
}

.message-sent .message-text {
  background-color: #4CAF50;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-received .message-text {
  background-color: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-meta {
  display: flex;
  gap: 0.5rem;
  font-size: 0.7rem;
  color: #999;
}

.message-sent .message-meta {
  flex-direction: row-reverse;
}

.chat-input {
  border-top: 1px solid #eee;
  padding: 1rem;
}

.chat-input textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
  resize: none;
  margin-bottom: 0.5rem;
}

.chat-input textarea:focus {
  outline: none;
  border-color: #4CAF50;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
}

.send-btn {
  padding: 0.5rem 1.5rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.send-btn:hover:not(:disabled) {
  background-color: #45a049;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>