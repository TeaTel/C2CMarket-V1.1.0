<template>
  <!-- 底部TabBar导航（5按钮布局） -->
  <nav class="tab-bar" v-if="showTabBar">
    <div class="tab-bar-container">
      <router-link v-for="tab in allTabs" :key="tab.path || tab.key"
        v-if="tab.key === 'publish'"
      >
        <button class="tab-item publish-tab-item" @click="showPublishSheet = true">
          <div class="tab-icon-wrapper">
            <svg class="tab-icon publish-icon" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" fill="currentColor"/>
              <path d="M12 8v8M8 12h8" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
            </svg>
          </div>
          <span class="tab-label">发布</span>
        </button>
      </router-link>
      <router-link
        v-else
        :to="tab.path"
        class="tab-item"
        :class="{ active: isActive(tab.path) }"
      >
        <div class="tab-icon-wrapper">
          <span v-if="tab.badge && unreadCount > 0" class="tab-badge">
            {{ unreadCount > 99 ? '99+' : unreadCount }}
          </span>
          <svg v-if="tab.icon === 'home'" class="tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9,22 9,12 15,12 15,22"/>
          </svg>
          <svg v-else-if="tab.icon === 'message'" class="tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
          </svg>
          <svg v-else-if="tab.icon === 'activity'" class="tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
        </div>
        <span class="tab-label">{{ tab.label }}</span>
      </router-link>
    </div>
  </nav>

  <PublishActionSheet :visible="showPublishSheet" @close="showPublishSheet = false" />
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { messageApi } from '../services/api'
import PublishActionSheet from './PublishActionSheet.vue'

const route = useRoute()
const authStore = useAuthStore()

const showPublishSheet = ref(false)

const allTabs = [
  {
    path: '/',
    label: '首页',
    icon: 'home',
    badge: false
  },
  {
    key: 'publish'
  },
  {
    path: '/messages',
    label: '消息',
    icon: 'message',
    badge: true
  },
  {
    path: '/activities',
    label: '活动',
    icon: 'activity',
    badge: false
  }
]

const unreadCount = ref(0)

// 根据路由meta判断是否显示TabBar
const showTabBar = computed(() => {
  return route.meta.showTabBar !== false
})

// 判断当前激活的tab
function isActive(path) {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}

// 获取未读消息数量
async function fetchUnreadCount() {
  if (!authStore.isAuthenticated) return

  try {
    const response = await messageApi.getUnreadCount()
    if (response.code === 200) {
      unreadCount.value = response.data || 0
    }
  } catch (error) {
    console.error('获取未读消息数失败:', error)
  }
}

// 监听登录状态变化
watch(
  () => authStore.isAuthenticated,
  (isAuth) => {
    if (isAuth) {
      fetchUnreadCount()
    } else {
      unreadCount.value = 0
    }
  },
  { immediate: true }
)

// 定时刷新未读数（每60秒）
onMounted(() => {
  setInterval(() => {
    if (authStore.isAuthenticated) {
      fetchUnreadCount()
    }
  }, 60000)
})
</script>

<style scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background-color: #ffffff;
  border-top: 1px solid #f0f0f0;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  padding-bottom: env(safe-area-inset-bottom); /* iPhone X+ 安全区域 */
}

.tab-bar-container {
  display: flex;
  align-items: center;
  justify-content: space-around;
  max-width: 750px; /* iPad最大宽度 */
  margin: 0 auto;
  height: 56px;
  padding: 0 8px;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  flex: 1;
  height: 100%;
  text-decoration: none;
  color: #999999;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  padding: 6px 0;
  border: none;
  background: none;
  cursor: pointer;
}

.publish-tab-item {
  position: relative;
  z-index: 1;
}

.tab-item:active {
  transform: scale(0.92);
}

/* 激活状态 */
.tab-item.active {
  color: #FF6A00; /* 闲鱼橙色 */
}

.tab-item.active .tab-icon {
  transform: scale(1.05);
}

/* 图标容器 */
.tab-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
}

/* SVG图标 */
.tab-icon {
  width: 24px;
  height: 24px;
  transition: all 0.25s ease;
}

/* 发布按钮特殊样式 */
.publish-icon {
  width: 44px !important;
  height: 44px !important;
  background: linear-gradient(135deg, #FF6A00 0%, #FF8533 100%);
  border-radius: 50%;
  padding: 10px;
  box-shadow: 0 4px 12px rgba(255, 106, 0, 0.35);
  color: white !important;
  margin-top: -16px; /* 向上突出 */
  transition: all 0.3s ease;
}

.tab-item:active .publish-icon {
  transform: scale(0.9);
  box-shadow: 0 2px 8px rgba(255, 106, 0, 0.45);
}

/* 徽章 */
.tab-badge {
  position: absolute;
  top: -4px;
  right: -8px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background-color: #FF4D4F;
  color: white;
  font-size: 10px;
  font-weight: 600;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
  line-height: 1;
  animation: badgePulse 2s infinite;
}

@keyframes badgePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.08); }
}

/* 文字标签 */
.tab-label {
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.02em;
  transition: all 0.25s ease;
}

.tab-item.active .tab-label {
  font-weight: 600;
  color: #FF6A00;
}

/* 响应式适配 */
@media (min-width: 768px) {
  .tab-bar-container {
    max-width: 500px;
  }

  .tab-icon {
    width: 26px;
    height: 26px;
  }

  .tab-label {
    font-size: 11px;
  }
}

/* 深色模式支持（可选） */
@media (prefers-color-scheme: dark) {
  .tab-bar {
    background-color: #1a1a1a;
    border-top-color: #333;
  }

  .tab-item {
    color: #888;
  }

  .tab-item.active {
    color: #FF8533;
  }

  .tab-badge {
    border-color: #1a1a1a;
  }
}
</style>
