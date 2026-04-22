<template>
  <nav class="navbar">
    <div class="navbar-container container">
      <!-- 品牌标识 -->
      <div class="navbar-brand">
        <router-link to="/" class="brand-link">
          <div class="brand-icon-wrapper">
            <span class="brand-icon">🛒</span>
          </div>
          <div class="brand-text-wrapper">
            <span class="brand-text">校园二手交易平台</span>
            <span class="brand-tagline">让闲置物品流动起来</span>
          </div>
        </router-link>
      </div>
      
      <!-- 主导航菜单 -->
      <div class="navbar-menu">
        <router-link to="/" class="nav-link">
          <span class="nav-icon">🏠</span>
          <span class="nav-text">首页</span>
        </router-link>
        <router-link to="/products" class="nav-link">
          <span class="nav-icon">📦</span>
          <span class="nav-text">商品</span>
        </router-link>
        <router-link to="/orders" class="nav-link">
          <span class="nav-icon">📋</span>
          <span class="nav-text">订单</span>
        </router-link>
        <router-link to="/messages" class="nav-link">
          <span class="nav-icon">💬</span>
          <span class="nav-text">消息</span>
          <span v-if="unreadCount > 0" class="badge badge-primary">
            {{ unreadCount > 99 ? '99+' : unreadCount }}
          </span>
        </router-link>
        <router-link to="/categories" class="nav-link">
          <span class="nav-icon">🏷️</span>
          <span class="nav-text">分类</span>
        </router-link>
      </div>
      
      <!-- 用户操作区域 -->
      <div class="navbar-actions">
        <template v-if="isAuthenticated">
          <div class="user-dropdown">
            <button class="user-profile-btn" @click="toggleDropdown">
              <div class="user-avatar">
                <span class="avatar-text">{{ userInitial }}</span>
              </div>
              <div class="user-info">
                <span class="username">{{ user?.username }}</span>
                <span class="user-role">学生用户</span>
              </div>
              <span class="dropdown-arrow">▼</span>
            </button>
            
            <div v-if="dropdownOpen" class="dropdown-menu">
              <router-link to="/profile" class="dropdown-item">
                <span class="dropdown-icon">👤</span>
                <span>个人资料</span>
              </router-link>
              <router-link to="/settings" class="dropdown-item">
                <span class="dropdown-icon">⚙️</span>
                <span>设置</span>
              </router-link>
              <div class="dropdown-divider"></div>
              <button @click="handleLogout" class="dropdown-item logout-item">
                <span class="dropdown-icon">🚪</span>
                <span>退出登录</span>
              </button>
            </div>
          </div>
        </template>
        <template v-else>
          <div class="auth-buttons">
            <router-link to="/login" class="btn btn-outline btn-sm">登录</router-link>
            <router-link to="/register" class="btn btn-primary btn-sm">注册</router-link>
          </div>
        </template>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const authStore = useAuthStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)
const user = computed(() => authStore.user)
const dropdownOpen = ref(false)
const unreadCount = ref(0)
const hasFetchedUnreadCount = ref(false) // 防止重复获取的标志

// 获取用户首字母（用于头像）
const userInitial = computed(() => {
  if (!user.value?.username) return 'U'
  return user.value.username.charAt(0).toUpperCase()
})

// 切换下拉菜单
function toggleDropdown() {
  dropdownOpen.value = !dropdownOpen.value
}

// 关闭下拉菜单（点击外部时）
function closeDropdown(event) {
  const dropdown = document.querySelector('.user-dropdown')
  if (dropdown && !dropdown.contains(event.target)) {
    dropdownOpen.value = false
  }
}

// 处理退出登录
function handleLogout() {
  authStore.logout()
  dropdownOpen.value = false
  router.push('/login')
}

// 获取未读消息数量
function fetchUnreadCount() {
  // 如果已经获取过，不再重复获取
  if (hasFetchedUnreadCount.value) {
    return
  }
  
  // 这里可以调用API获取真实的未读消息数量
  // 暂时使用固定的未读消息数量，避免随机变化
  // 实际项目中应该调用API: const response = await messageApi.getUnreadCount()
  unreadCount.value = 3 // 固定3条未读消息，用于演示
  hasFetchedUnreadCount.value = true
}

// 监听登录状态变化
watch(isAuthenticated, (newValue) => {
  if (newValue) {
    // 用户登录时获取未读消息数量
    fetchUnreadCount()
  } else {
    // 用户登出时重置未读消息数量
    unreadCount.value = 0
    hasFetchedUnreadCount.value = false
  }
}, { immediate: true })

// 生命周期钩子
onMounted(() => {
  document.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  document.removeEventListener('click', closeDropdown)
})
</script>

<style scoped>
.navbar {
  background-color: var(--color-bg-primary);
  border-bottom: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 50;
  backdrop-filter: blur(8px);
  background-color: rgba(255, 255, 255, 0.95);
}

.navbar-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  gap: var(--space-6);
}

/* 品牌标识样式 */
.navbar-brand {
  flex-shrink: 0;
}

.brand-link {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  text-decoration: none;
  color: inherit;
  transition: var(--transition-normal);
}

.brand-link:hover {
  opacity: 0.8;
}

.brand-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--color-primary-500), var(--color-primary-700));
  border-radius: var(--radius-lg);
  color: white;
  font-size: 1.5rem;
}

.brand-text-wrapper {
  display: flex;
  flex-direction: column;
}

.brand-text {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
  line-height: 1.2;
}

.brand-tagline {
  font-size: var(--font-size-xs);
  color: var(--color-gray-600);
  margin-top: 2px;
}

/* 主导航菜单样式 */
.navbar-menu {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex: 1;
  justify-content: center;
  margin: 0 var(--space-6);
}

.nav-link {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  text-decoration: none;
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
  border-radius: var(--radius-md);
  transition: var(--transition-normal);
  position: relative;
}

.nav-link:hover {
  background-color: var(--color-gray-50);
  color: var(--color-primary-600);
}

.nav-link.router-link-active {
  background-color: var(--color-primary-50);
  color: var(--color-primary-700);
}

.nav-link.router-link-active::before {
  content: '';
  position: absolute;
  bottom: -1px;
  left: var(--space-3);
  right: var(--space-3);
  height: 2px;
  background-color: var(--color-primary-500);
  border-radius: var(--radius-full);
}

.nav-icon {
  font-size: 1.125rem;
}

.nav-text {
  white-space: nowrap;
}

/* 徽章样式 */
.nav-link .badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 用户操作区域样式 */
.navbar-actions {
  flex-shrink: 0;
}

.auth-buttons {
  display: flex;
  gap: var(--space-2);
}

/* 用户下拉菜单样式 */
.user-dropdown {
  position: relative;
}

.user-profile-btn {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-2);
  background: none;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: var(--transition-normal);
  min-width: 160px;
}

.user-profile-btn:hover {
  background-color: var(--color-gray-50);
  border-color: var(--color-border-medium);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-primary-500), var(--color-secondary-500));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: var(--font-weight-bold);
  font-size: var(--font-size-sm);
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  flex: 1;
  min-width: 0;
}

.username {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100px;
}

.user-role {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin-top: 1px;
}

.dropdown-arrow {
  font-size: 10px;
  color: var(--color-gray-500);
  transition: transform var(--transition-fast);
}

.user-dropdown:hover .dropdown-arrow {
  transform: rotate(180deg);
}

/* 下拉菜单样式 */
.dropdown-menu {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  width: 200px;
  background-color: var(--color-bg-primary);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  z-index: 100;
  overflow: hidden;
  animation: slideDown 0.2s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  text-decoration: none;
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  background: none;
  border: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
  transition: var(--transition-fast);
}

.dropdown-item:hover {
  background-color: var(--color-gray-50);
}

.dropdown-item.router-link-active {
  background-color: var(--color-primary-50);
  color: var(--color-primary-700);
}

.dropdown-icon {
  font-size: 1rem;
  width: 20px;
  text-align: center;
}

.dropdown-divider {
  height: 1px;
  background-color: var(--color-border-light);
  margin: var(--space-1) 0;
}

.logout-item {
  color: var(--color-error);
}

.logout-item:hover {
  background-color: rgba(244, 67, 54, 0.1);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .navbar-container {
    gap: var(--space-4);
  }
  
  .navbar-menu {
    margin: 0 var(--space-4);
    gap: 0;
  }
  
  .nav-link {
    padding: var(--space-2);
  }
  
  .nav-text {
    display: none;
  }
  
  .user-profile-btn {
    min-width: auto;
    padding: var(--space-1);
  }
  
  .user-info {
    display: none;
  }
  
  .dropdown-arrow {
    display: none;
  }
}

@media (max-width: 768px) {
  .navbar-container {
    padding: 0 var(--space-3);
  }
  
  .brand-tagline {
    display: none;
  }
  
  .navbar-menu {
    margin: 0 var(--space-2);
  }
  
  .auth-buttons .btn-sm {
    padding: var(--space-1) var(--space-2);
    font-size: var(--font-size-xs);
  }
}

@media (max-width: 640px) {
  .navbar {
    height: 56px;
  }
  
  .navbar-container {
    height: 56px;
  }
  
  .brand-icon-wrapper {
    width: 32px;
    height: 32px;
    font-size: 1.25rem;
  }
  
  .brand-text {
    font-size: var(--font-size-base);
  }
  
  .navbar-menu {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background-color: var(--color-bg-primary);
    border-top: 1px solid var(--color-border-light);
    padding: var(--space-2);
    margin: 0;
    justify-content: space-around;
    z-index: 40;
  }
  
  .nav-link {
    flex-direction: column;
    gap: var(--space-1);
    padding: var(--space-1);
    border-radius: var(--radius-md);
  }
  
  .nav-link.router-link-active::before {
    top: -1px;
    bottom: auto;
    left: 50%;
    right: auto;
    width: 4px;
    height: 4px;
    transform: translateX(-50%);
  }
  
  .nav-icon {
    font-size: 1.25rem;
  }
  
  .nav-text {
    display: block;
    font-size: var(--font-size-xs);
  }
  
  .navbar-actions {
    margin-left: auto;
  }
}
</style>