<template>
  <div class="chat-room">
    <header class="chat-header">
      <button @click="goBack" class="back-btn">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <polyline points="15,18 9,12 15,6"/>
        </svg>
      </button>
      <div class="header-info">
        <h3 class="contact-name">{{ contactInfo.nickname || contactInfo.username || '用户' }}</h3>
        <span v-if="isOnline" class="online-status">在线</span>
      </div>
      <button @click="showMoreOptions = !showMoreOptions" class="more-btn">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <circle cx="12" cy="5" r="1.5"/><circle cx="12" cy="12" r="1.5"/><circle cx="12" cy="19" r="1.5"/>
        </svg>
      </button>
      <div v-if="showMoreOptions" class="more-menu">
        <button @click="viewContactProfile" class="menu-item">查看资料</button>
        <button @click="clearMessages" class="menu-item danger">清空聊天记录</button>
      </div>
    </header>

    <div v-if="productInfo" class="product-card-inline">
      <img :src="productInfo.imageUrl" :alt="productInfo.name" class="product-thumb" />
      <div class="product-detail">
        <h4 class="product-name">{{ productInfo.name }}</h4>
        <p class="product-price">¥{{ productInfo.price }}</p>
      </div>
    </div>

    <main class="messages-container" ref="messagesContainer">
      <div v-if="loadingHistory" class="loading-more-messages">
        <div class="mini-spinner"></div><span>加载中...</span>
      </div>

      <div v-for="(timeGroup, date) in groupedMessages" :key="date" class="message-group">
        <div class="date-divider"><span class="date-text">{{ formatDate(date) }}</span></div>
        <div v-for="message in timeGroup" :key="message.id"
          :class="['message-item', message.senderId === currentUserId ? 'sent' : 'received']">
          <div v-if="message.senderId !== currentUserId" class="avatar-wrapper">
            <img :src="contactInfo.avatar || defaultAvatar" :alt="contactInfo.username" class="avatar" />
          </div>
          <div class="bubble-wrapper">
            <div class="message-bubble"><p class="message-text">{{ message.content }}</p></div>
            <div class="message-meta">
              <span class="message-time">{{ formatTime(message.createdAt) }}</span>
              <span v-if="message.senderId === currentUserId" class="read-status">{{ message.isRead ? '已读' : '未读' }}</span>
            </div>
          </div>
          <div v-if="message.senderId === currentUserId" class="avatar-wrapper self">
            <img :src="currentUserAvatar || defaultAvatar" alt="我" class="avatar" />
          </div>
        </div>
      </div>

      <div v-if="!loadingHistory && messages.length === 0" class="empty-chat">
        <div class="empty-icon">💬</div><p>发送消息开始聊天吧~</p>
      </div>
    </main>

    <footer class="input-area">
      <div class="toolbar">
        <button @click="toggleEmojiPicker" class="tool-btn" :class="{ active: showEmojiPicker }">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/><line x1="9" y1="9" x2="9.01" y2="9"/><line x1="15" y1="9" x2="15.01" y2="9"/></svg>
        </button>
      </div>
      <div class="input-wrapper">
        <textarea ref="inputRef" v-model="newMessage" class="message-input" placeholder="输入消息..." rows="1"
          @input="autoResize" @keydown.enter.exact.prevent="sendMessage"></textarea>
        <button @click="sendMessage" :disabled="!newMessage.trim() || sending" class="send-btn" :class="{ active: newMessage.trim() }">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
        </button>
      </div>
      <div v-if="showEmojiPicker" class="emoji-picker">
        <div class="emoji-grid">
          <span v-for="emoji in commonEmojis" :key="emoji" @click="insertEmoji(emoji)" class="emoji-item">{{ emoji }}</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { messageApi, wsManager, productApi, userApi } from '../services/api'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const messages = ref([])
const loadingHistory = ref(false)
const sending = ref(false)
const newMessage = ref('')
const showEmojiPicker = ref(false)
const showMoreOptions = ref(false)
const isOnline = ref(true)
const messagesContainer = ref(null)
const inputRef = ref(null)

const currentUserId = computed(() => authStore.currentUser.value?.id)
const currentUserAvatar = computed(() => authStore.currentUser.value?.avatar)
const contactInfo = ref({})
const productInfo = ref(null)
const conversationId = ref(null)
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48Y2lyY2xlIGN4PSIyMCIgY3k9IjIwIiByPSIyMCIgZmlsbD0iI0UwRTBFRCIvPjxjaXJjbGUgY3g9IjIwIiBjeT0iMTciIHI9IjgiIGZpbGw9IndoaXRlIi8+PC9zdmc+'

const commonEmojis = ['😀','😂','🥰','😎','🤔','😢','😡','👍','❤️','🎉','🔥','✨','👏','🙏','💪','🤝','💰','📚','🎁','☀️','🌙','⭐','🌈','🎈']

const groupedMessages = computed(() => {
  const groups = {}
  messages.value.forEach(msg => {
    const date = new Date(msg.createdAt).toDateString()
    if (!groups[date]) groups[date] = []
    groups[date].push(msg)
  })
  return groups
})

onMounted(async () => {
  await initChat()
  wsManager.on('chat_message', handleWsMessage)
  nextTick(() => inputRef.value?.focus())
})

onUnmounted(() => {
  wsManager.off('chat_message', handleWsMessage)
})

async function initChat() {
  const rawId = route.params.userId
  const otherUserId = Number(rawId)
  if (!rawId || isNaN(otherUserId) || otherUserId <= 0) {
    contactInfo.value = { username: '用户不存在' }
    return
  }
  await loadContactInfo(otherUserId)
  await findConversation(otherUserId)
  await loadProductInfo()
  if (conversationId.value) {
    await loadMessages()
    await markAsRead()
  }
}

async function loadContactInfo(userId) {
  try {
    const res = await userApi.getUserPublic(userId)
    if (res.code === 200) {
      contactInfo.value = res.data
      isOnline.value = true
    }
  } catch (e) {
    contactInfo.value = { id: userId, username: '未知用户' }
  }
}

async function findConversation(otherUserId) {
  try {
    const res = await messageApi.getContacts()
    if (res.code === 200) {
      const list = res.data || []
      const conv = list.find(c => c.otherUser && Number(c.otherUser.id) === otherUserId)
      if (conv) conversationId.value = conv.id
    }
  } catch (e) {}
}

async function loadProductInfo() {
  const productId = route.query.productId
  if (!productId) return
  try {
    const res = await productApi.getProductDetail(productId)
    if (res.code === 200) productInfo.value = res.data
  } catch (e) {}
}

async function loadMessages() {
  if (!conversationId.value) return
  try {
    loadingHistory.value = true
    const res = await messageApi.getConversation(conversationId.value, { page: 1, size: 50 })
    if (res.code === 200) {
      const list = res.data?.records || res.data || []
      messages.value = Array.isArray(list) ? list : []
      scrollToBottom()
    }
  } catch (e) {
    console.error('加载消息失败:', e)
  } finally {
    loadingHistory.value = false
  }
}

async function sendMessage() {
  const content = newMessage.value.trim()
  if (!content || sending.value) return
  const rawId = route.params.userId
  const otherUserId = Number(rawId)
  if (!rawId || isNaN(otherUserId) || otherUserId <= 0) return
  sending.value = true
  try {
    const payload = { receiverId: otherUserId, content }
    if (route.query.productId) payload.productId = Number(route.query.productId) || undefined

    const res = await messageApi.sendMessage(payload)
    if (res.code === 200) {
      messages.value.push({
        id: res.data?.id || Date.now(),
        senderId: currentUserId.value,
        content,
        createdAt: new Date().toISOString(),
        isRead: false
      })
      newMessage.value = ''
      autoResize()
      scrollToBottom()
      if (!conversationId.value) await findConversation(otherUserId)
    }
  } catch (e) {
    console.error('发送消息失败:', e)
  } finally {
    sending.value = false
  }
}

function handleWsMessage(data) {
  const rawId = route.params.userId
  const otherUserId = Number(rawId)
  if (!rawId || isNaN(otherUserId)) return
  if (Number(data.senderId) === currentUserId.value) return
  if (Number(data.senderId) !== otherUserId && Number(data.receiverId) !== currentUserId.value) return
  messages.value.push({
    id: data.id || Date.now(),
    senderId: Number(data.senderId),
    content: data.content,
    createdAt: data.timestamp || new Date().toISOString(),
    isRead: true
  })
  scrollToBottom()
}

async function markAsRead() {
  if (!conversationId.value) return
  try {
    await messageApi.markConversationAsRead(conversationId.value)
  } catch (e) {}
}

function goBack() { router.back() }
function viewContactProfile() { showMoreOptions.value = false; router.push(`/users/${route.params.userId}`) }
function clearMessages() { if (confirm('确定要清空聊天记录吗？')) { messages.value = []; showMoreOptions.value = false } }
function toggleEmojiPicker() { showEmojiPicker.value = !showEmojiPicker.value }
function insertEmoji(emoji) { newMessage.value += emoji; inputRef.value?.focus(); showEmojiPicker.value = false }
function autoResize() { const tx = inputRef.value; if (!tx) return; tx.style.height = 'auto'; tx.style.height = Math.min(tx.scrollHeight, 120) + 'px' }
function scrollToBottom() { nextTick(() => { if (messagesContainer.value) messagesContainer.value.scrollTo({ top: messagesContainer.value.scrollHeight, behavior: 'smooth' }) }) }
function formatDate(d) { const dt = new Date(d); const today = new Date(); const yest = new Date(today); yest.setDate(yest.getDate()-1); if (dt.toDateString()===today.toDateString()) return '今天'; if (dt.toDateString()===yest.toDateString()) return '昨天'; return `${dt.getMonth()+1}月${dt.getDate()}日` }
function formatTime(ts) { if (!ts) return ''; return new Date(ts).toLocaleTimeString('zh-CN',{hour:'2-digit',minute:'2-digit'}) }
</script>

<style scoped>
.chat-room { display:flex; flex-direction:column; height:100vh; background:#f5f5f5; position:fixed; top:0; left:0; right:0; bottom:0; z-index:1001; }
.chat-header { display:flex; align-items:center; gap:12px; padding:12px 16px; background:#fff; border-bottom:1px solid #f0f0f0; position:relative; }
.back-btn { width:36px; height:36px; display:flex; align-items:center; justify-content:center; color:#333; border-radius:50%; transition:background 0.2s; border:none; background:none; cursor:pointer; }
.back-btn:active { background:#f5f5f5; }
.back-btn svg { width:22px; height:22px; }
.header-info { flex:1; display:flex; align-items:baseline; gap:8px; }
.contact-name { font-size:17px; font-weight:600; color:#333; margin:0; }
.online-status { font-size:12px; color:#52c41a; }
.more-btn { width:36px; height:36px; display:flex; align-items:center; justify-content:center; color:#666; border-radius:50%; border:none; background:none; cursor:pointer; }
.more-btn:active { background:#f5f5f5; }
.more-menu { position:absolute; top:56px; right:16px; background:#fff; border-radius:12px; box-shadow:0 4px 20px rgba(0,0,0,0.12); overflow:hidden; min-width:140px; z-index:10; }
.menu-item { width:100%; padding:14px 20px; font-size:15px; color:#333; text-align:left; background:none; border:none; cursor:pointer; }
.menu-item:active { background:#f5f5f5; }
.menu-item.danger { color:#ff4d4f; }
.product-card-inline { display:flex; gap:12px; padding:12px 16px; background:#fff; border-bottom:1px solid #f0f0f0; align-items:center; }
.product-thumb { width:60px; height:60px; border-radius:8px; object-fit:cover; background:#f5f5f5; }
.product-detail { flex:1; }
.product-name { font-size:14px; font-weight:500; color:#333; margin:0 0 4px; }
.product-price { font-size:16px; font-weight:700; color:#FF4D4F; margin:0; }
.messages-container { flex:1; overflow-y:auto; padding:16px; }
.loading-more-messages { display:flex; align-items:center; justify-content:center; gap:8px; padding:16px; color:#999; font-size:13px; }
.mini-spinner { width:16px; height:16px; border:2px solid #e0e0e0; border-top-color:#FF6A00; border-radius:50%; animation:spin 0.8s linear infinite; }
@keyframes spin { to { transform:rotate(360deg); } }
.date-divider { text-align:center; margin:20px 0; }
.date-text { display:inline-block; padding:4px 12px; background:rgba(0,0,0,0.05); border-radius:12px; font-size:12px; color:#999; }
.message-item { display:flex; gap:8px; margin-bottom:16px; max-width:85%; }
.message-item.sent { align-self:flex-end; flex-direction:row-reverse; }
.message-item.received { align-self:flex-start; }
.avatar-wrapper { flex-shrink:0; }
.avatar-wrapper.self { visibility:hidden; }
.avatar { width:38px; height:38px; border-radius:50%; object-fit:cover; background:#e0e0e0; }
.bubble-wrapper { display:flex; flex-direction:column; gap:4px; }
.message-item.sent .bubble-wrapper { align-items:flex-end; }
.message-item.received .bubble-wrapper { align-items:flex-start; }
.message-bubble { padding:10px 14px; border-radius:18px; max-width:280px; word-break:break-word; }
.message-item.sent .message-bubble { background:linear-gradient(135deg,#FF6A00,#FF8533); color:#fff; border-bottom-right-radius:4px; }
.message-item.received .message-bubble { background:#fff; color:#333; border-bottom-left-radius:4px; box-shadow:0 1px 2px rgba(0,0,0,0.05); }
.message-text { margin:0; font-size:15px; line-height:1.5; }
.message-meta { display:flex; gap:8px; font-size:11px; color:#bbb; padding:0 4px; }
.read-status { color:#52c41a; }
.empty-chat { display:flex; flex-direction:column; align-items:center; justify-content:center; height:60vh; color:#ccc; }
.empty-icon { font-size:48px; margin-bottom:12px; opacity:0.6; }
.empty-chat p { font-size:14px; margin:0; }
.input-area { background:#fff; border-top:1px solid #f0f0f0; padding:10px 16px; padding-bottom:calc(10px + env(safe-area-inset-bottom,0px)); }
.toolbar { display:flex; gap:8px; margin-bottom:8px; }
.tool-btn { width:32px; height:32px; display:flex; align-items:center; justify-content:center; color:#666; border-radius:6px; border:none; background:none; cursor:pointer; }
.tool-btn:active,.tool-btn.active { background:#f5f5f5; color:#FF6A00; }
.tool-btn svg { width:22px; height:22px; }
.input-wrapper { display:flex; align-items:flex-end; gap:10px; background:#f5f5f5; border-radius:20px; padding:8px 12px; }
.message-input { flex:1; border:none; background:none; font-size:15px; color:#333; resize:none; outline:none; max-height:120px; line-height:1.5; font-family:inherit; }
.message-input::placeholder { color:#bbb; }
.send-btn { width:36px; height:36px; display:flex; align-items:center; justify-content:center; background:#e0e0e0; border-radius:50%; color:#fff; border:none; cursor:pointer; flex-shrink:0; }
.send-btn svg { width:18px; height:18px; }
.send-btn.active { background:linear-gradient(135deg,#FF6A00,#FF8533); box-shadow:0 4px 12px rgba(255,106,0,0.35); }
.send-btn:disabled { opacity:0.5; cursor:not-allowed; }
.emoji-picker { position:absolute; bottom:80px; left:16px; right:16px; background:#fff; border-radius:16px; padding:16px; box-shadow:0 -4px 20px rgba(0,0,0,0.1); z-index:10; }
.emoji-grid { display:grid; grid-template-columns:repeat(8,1fr); gap:8px; }
.emoji-item { width:40px; height:40px; display:flex; align-items:center; justify-content:center; font-size:24px; border-radius:8px; cursor:pointer; }
.emoji-item:active { background:#f5f5f5; transform:scale(0.9); }
</style>
