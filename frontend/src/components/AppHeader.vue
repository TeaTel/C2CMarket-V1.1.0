<template>
  <header class="app-header">
    <div class="header-container">
      <!-- 左侧：Logo和品牌名 -->
      <div class="brand-section">
        <router-link to="/" class="brand-link">
          <span class="brand-icon">🎓</span>
          <span class="brand-name">校园二手</span>
        </router-link>
      </div>

      <!-- 右侧：用户操作区 -->
      <div class="user-actions">
        <!-- 未登录状态：显示登录和注册按钮 -->
        <template v-if="!isAuthenticated">
          <router-link to="/login" class="auth-btn login-btn">登录</router-link>
          <router-link to="/register" class="auth-btn register-btn">注册</router-link>
        </template>

        <!-- 已登录状态：显示用户信息 -->
        <template v-else>
          <router-link to="/profile" class="user-info">
            <img
              :src="currentUser?.avatar || defaultAvatar"
              :alt="currentUser?.nickname || '用户'"
              class="user-avatar"
            />
            <span class="user-nickname">{{ currentUser?.nickname || currentUser?.username || '用户' }}</span>
          </router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../store/auth'

const authStore = useAuthStore()

// 计算属性
const isAuthenticated = computed(() => authStore.isAuthenticated.value)
const currentUser = computed(() => authStore.currentUser.value)

// 默认头像（SVG base64）
const defaultAvatar = `data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48Y2lyY2xlIGN4PSIyMCIgY3k9IjIwIiByPSIyMCIgZmlsbD0iI0UwRTBFRCIvPjxjaXJjbGUgY3g9IjIwIiBjeT0iMTciIHI9IjgiIGZpbGw9IndoaXRlIi8+PC9zdmc+`
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 999;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  max-width: 750px;
  margin: 0 auto;
  height: 48px;
}

/* 品牌区域 */
.brand-section {
  flex-shrink: 0;
}

.brand-link {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #333;
}

.brand-icon {
  font-size: 24px;
  line-height: 1;
}

.brand-name {
  font-size: 18px;
  font-weight: 700;
  color: #FF6A00;
  letter-spacing: 1px;
}

/* 用户操作区域 */
.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 认证按钮（未登录状态） */
.auth-btn {
  padding: 8px 20px;
  border-radius: 18px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.25s ease;
  cursor: pointer;
}

.login-btn {
  color: #FF6A00;
  background-color: transparent;
  border: 1.5px solid #FF6A00;
}

.login-btn:active {
  background-color: #FFF7E6;
  transform: scale(0.96);
}

.register-btn {
  color: white;
  background: linear-gradient(135deg, #FF6A00 0%, #FF8533 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(255, 106, 0, 0.25);
}

.register-btn:active {
  transform: scale(0.96);
  box-shadow: 0 2px 8px rgba(255, 106, 0, 0.35);
}

/* 用户信息（已登录状态） */
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  padding: 4px 12px 4px 4px;
  border-radius: 20px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.user-info:active {
  background-color: #f5f5f5;
  transform: scale(0.98);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #FF6A00;
  background-color: #e0e0e0;
}

.user-nickname {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 响应式适配 */
@media (max-width: 360px) {
  .brand-name {
    font-size: 16px;
  }

  .auth-btn {
    padding: 6px 14px;
    font-size: 13px;
  }

  .user-nickname {
    max-width: 60px;
    font-size: 13px;
  }
}

@media (min-width: 769px) {
  .header-container {
    max-width: 750px;
    padding: 12px 24px;
  }
}
</style>
