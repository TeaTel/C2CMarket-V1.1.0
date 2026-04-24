<template>
  <div id="app" class="app-container">
    <!-- 全局Header（登录/注册/用户信息） -->
    <AppHeader />

    <!-- 主内容区域（带底部TabBar间距） -->
    <main class="main-content" :class="{ 'with-tabbar': showTabBar }">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底部TabBar导航 -->
    <TabBar />
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from './store/auth'
import { wsManager } from './services/api'
import TabBar from './components/NavBar.vue'
import AppHeader from './components/AppHeader.vue'

const route = useRoute()
const authStore = useAuthStore()

// 根据路由meta判断是否显示TabBar
const showTabBar = computed(() => {
  return route.meta.showTabBar === true
})

// 初始化WebSocket连接（用于实时聊天）
function initWebSocket() {
  if (authStore.isAuthenticated) {
    const token = localStorage.getItem('token')
    if (token && !wsManager.isConnected) {
      wsManager.connect(token)

      // 监听新消息事件
      wsManager.on('chat_message', (data) => {
        console.log('收到新消息:', data)
        // 可以在这里触发全局通知或刷新消息列表
      })
    }
  }
}

// 监听登录状态变化，自动连接/断开WebSocket
watch(
  () => authStore.isAuthenticated,
  (isAuth) => {
    if (isAuth) {
      initWebSocket()
    } else {
      wsManager.disconnect()
    }
  },
  { immediate: true }
)

// 页面加载时初始化
onMounted(() => {
  // 设置viewport（移动端优化）
  const viewport = document.querySelector('meta[name=viewport]')
  if (!viewport) {
    const meta = document.createElement('meta')
    meta.name = 'viewport'
    meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover'
    document.head.appendChild(meta)
  }

  // 初始化WebSocket
  initWebSocket()
})
</script>

<style>
@import './assets/css/design-system.css';

/* 全局App样式 */
.app-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  position: relative;
}

/* 主内容区域 */
.main-content {
  min-height: 100vh;
  padding-top: 48px; /* Header高度 */
  padding-bottom: 0; /* 默认无底部间距 */
  transition: padding-bottom 0.3s ease;
}

/* 当显示TabBar时，添加底部间距 */
.main-content.with-tabbar {
  padding-bottom: 56px; /* TabBar高度 + 安全区域 */
  padding-bottom: calc(56px + env(safe-area-inset-bottom, 0px));
}

/* 路由切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ============================================
   全局样式重置和优化
   ============================================ */

/* 移动端触摸优化 */
* {
  -webkit-tap-highlight-color: transparent;
  -webkit-touch-callout: none;
}

/* 禁止文本选择（提升移动端体验） */
body {
  -webkit-user-select: none;
  user-select: none;
  overscroll-behavior-y: contain; /* 防止下拉刷新 */
}

/* 允许输入框选择文本 */
input,
textarea {
  -webkit-user-select: auto;
  user-select: auto;
}

/* 滚动条样式（Webkit） */
::-webkit-scrollbar {
  width: 4px;
  height: 4px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.15);
  border-radius: 20px;
}

::-webkit-scrollbar-thumb:hover {
  background-color: rgba(0, 0, 0, 0.25);
}

/* 图片默认样式 */
img {
  max-width: 100%;
  height: auto;
  display: block;
}

/* 链接默认样式重置 */
a {
  color: inherit;
  text-decoration: none;
}

/* 按钮重置 */
button {
  font-family: inherit;
  cursor: pointer;
  border: none;
  background: none;
  padding: 0;
  margin: 0;
}

/* 输入框重置 */
input,
textarea,
select {
  font-family: inherit;
  font-size: inherit;
}

/* ============================================
   响应式断点工具类
   ============================================ */

/* 平板及以下 */
@media (max-width: 768px) {
  .hide-on-mobile {
    display: none !important;
  }
}

/* 桌面端及以上 */
@media (min-width: 769px) {
  .hide-on-desktop {
    display: none !important;
  }
  
  /* 桌面端内容居中限制宽度 */
  .desktop-container {
    max-width: 750px;
    margin: 0 auto;
    background-color: #fff;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.08);
  }
}
</style>
