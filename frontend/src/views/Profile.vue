<template>
  <div class="profile-page">
    <!-- 用户信息头部 -->
    <header class="profile-header">
      <div class="header-bg"></div>
      <div class="user-info-card">
        <div class="avatar-section" @click="handleAvatarClick">
          <img
            :src="userInfo.avatar || defaultAvatar"
            :alt="userInfo.nickname || '用户'"
            class="user-avatar"
          />
          <span class="edit-avatar-badge">编辑</span>
        </div>
        <h2 class="user-name">{{ userInfo.nickname || '未设置昵称' }}</h2>
        <p class="user-id">ID: {{ userInfo.id || '---' }}</p>
        <p v-if="userInfo.bio" class="user-bio">{{ userInfo.bio }}</p>

        <div class="stats-row">
          <div class="stat-item">
            <span class="stat-value">{{ stats.published }}</span>
            <span class="stat-label">发布</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">{{ stats.sold }}</span>
            <span class="stat-label">已售</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">{{ stats.favorites }}</span>
            <span class="stat-label">收藏</span>
          </div>
        </div>
      </div>
    </header>

    <!-- 功能菜单列表 -->
    <main class="menu-list">
      <!-- 我的商品 -->
      <section class="menu-group">
        <router-link to="/products/create" class="menu-item highlight">
          <span class="menu-icon publish-icon">+</span>
          <span class="menu-text">发布新商品</span>
          <svg class="arrow-right" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9,18 15,12 9,6"/>
          </svg>
        </router-link>

        <router-link to="/profile?tab=my-products" class="menu-item">
          <span class="menu-icon">📦</span>
          <span class="menu-text">我的发布</span>
          <span class="menu-badge" v-if="stats.published > 0">{{ stats.published }}</span>
          <svg class="arrow-right" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9,18 15,12 9,6"/>
          </svg>
        </router-link>

        <router-link to="/orders" class="menu-item">
          <span class="menu-icon">🛒</span>
          <span class="menu-text">我的订单</span>
          <svg class="arrow-right" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9,18 15,12 9,6"/>
          </svg>
        </router-link>

        <router-link to="/favorites" class="menu-item">
          <span class="menu-icon">❤️</span>
          <span class="menu-text">我的收藏</span>
          <svg class="arrow-right" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9,18 15,12 9,6"/>
          </svg>
        </router-link>
      </section>

      <!-- 账户设置 -->
      <section class="menu-group">
        <button @click="goToSettings" class="menu-item">
          <span class="menu-icon">⚙️</span>
          <span class="menu-text">账号设置</span>
          <svg class="arrow-right" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9,18 15,12 9,6"/>
          </svg>
        </button>

        <router-link to="/address" class="menu-item">
          <span class="menu-icon">📍</span>
          <span class="menu-text">收货地址</span>
          <svg class="arrow-right" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9,18 15,12 9,6"/>
          </svg>
        </router-link>

        <a href="#" @click.prevent="showAbout = true" class="menu-item">
          <span class="menu-icon">ℹ️</span>
          <span class="menu-text">关于我们</span>
          <svg class="arrow-right" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9,18 15,12 9,6"/>
          </svg>
        </a>
      </section>

      <!-- 退出登录 -->
      <section class="menu-group">
        <button @click="handleLogout" class="menu-item danger">
          <span class="menu-icon">🚪</span>
          <span class="menu-text">退出登录</span>
        </button>
      </section>

      <!-- 版本信息 -->
      <p class="version-info">校园二手 v1.0.0</p>
    </main>

    <!-- 退出登录确认弹窗 -->
    <Teleport to="body">
      <div v-if="showLogoutConfirm" class="logout-modal-overlay" @click="cancelLogout">
        <div class="logout-modal" @click.stop>
          <div class="modal-icon">⚠️</div>
          <h3 class="modal-title">确定要退出登录吗？</h3>
          <p class="modal-desc">退出后需要重新登录才能使用完整功能</p>

          <div class="modal-actions">
            <button @click="cancelLogout" class="modal-btn cancel-btn">取消</button>
            <button @click="confirmLogout" class="modal-btn confirm-btn">确定退出</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const authStore = useAuthStore()

// 默认头像（SVG base64）
const defaultAvatar = `data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48Y2lyY2xlIGN4PSIyMCIgY3k9IjIwIiByPSIyMCIgZmlsbD0iI0UwRTBFRCIvPjxjaXJjbGUgY3g9IjIwIiBjeT0iMTciIHI9IjgiIGZpbGw9IndoaXRlIi8+PC9zdmc+`

// 用户信息（从store计算）
const userInfo = computed(() => {
  // 优先使用store中的用户信息
  if (authStore.currentUser.value) {
    return authStore.currentUser.value
  }

  // 备用：从localStorage读取
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    try {
      return JSON.parse(storedUser)
    } catch (e) {
      console.error('解析用户信息失败:', e)
      return {}
    }
  }

  return {}
})

// 统计数据
const stats = reactive({
  published: 0,
  sold: 0,
  favorites: 0
})

const showAbout = ref(false)
const showLogoutConfirm = ref(false) // 控制退出登录确认弹窗

onMounted(async () => {
  // 尝试从API获取最新用户信息
  if (authStore.isAuthenticated.value) {
    await fetchUserInfo()
    await fetchStats()
  }
})

// 获取用户信息
async function fetchUserInfo() {
  try {
    const result = await authStore.fetchUserInfo()
    if (result.success) {
      console.log('用户信息已更新')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // 静默失败，使用缓存数据
  }
}

// 获取统计数据（模拟，实际应从API获取）
async function fetchStats() {
  // TODO: 替换为实际的API调用
  // 这里使用模拟数据，实际项目中应该调用统计接口
  stats.published = Math.floor(Math.random() * 10)
  stats.sold = Math.floor(Math.random() * 5)
  stats.favorites = Math.floor(Math.random() * 20)
}

function handleAvatarClick() {
  alert('头像上传功能开发中...')
}

function goToSettings() {
  router.push('/settings')
}

// 显示退出确认弹窗
function handleLogout() {
  showLogoutConfirm.value = true
}

// 确认退出登录
function confirmLogout() {
  showLogoutConfirm.value = false

  // 调用authStore的logout方法（自动清除所有状态并跳转）
  authStore.logout()
}

// 取消退出
function cancelLogout() {
  showLogoutConfirm.value = false
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* ============================================
   用户信息头部
   ============================================ */
.profile-header {
  position: relative;
  padding-bottom: 20px;
}

.header-bg {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 180px;
  background: linear-gradient(135deg, #FF6A00 0%, #FF8533 50%, #FFB347 100%);
}

.user-info-card {
  position: relative;
  z-index: 1;
  padding-top: 50px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-section {
  position: relative;
  margin-bottom: 14px;
  cursor: pointer;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid white;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  background-color: #e0e0e0;
}

.edit-avatar-badge {
  position: absolute;
  bottom: 4px;
  right: 0;
  padding: 4px 10px;
  background: linear-gradient(135deg, #FF6A00 0%, #FF8533 100%);
  color: white;
  font-size: 11px;
  font-weight: 600;
  border-radius: 10px;
  border: 2px solid white;
}

.user-name {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin: 0 0 4px 0;
}

.user-id {
  font-size: 13px;
  color: #999;
  margin: 0 0 8px 0;
}

.user-bio {
  font-size: 14px;
  color: #666;
  margin: 0 0 16px 0;
  max-width: 260px;
  text-align: center;
  line-height: 1.5;
}

/* 统计数据 */
.stats-row {
  display: flex;
  align-items: center;
  gap: 28px;
  background-color: #fff;
  padding: 18px 40px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 22px;
  font-weight: 800;
  color: #FF6A00;
}

.stat-label {
  font-size: 12px;
  color: #999;
  font-weight: 500;
}

.stat-divider {
  width: 1px;
  height: 32px;
  background-color: #f0f0f0;
}

/* ============================================
   菜单列表
   ============================================ */
.menu-list {
  padding: 12px 16px;
}

.menu-group {
  background-color: #fff;
  border-radius: 14px;
  overflow: hidden;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  text-decoration: none;
  color: #333;
  transition: background-color 0.15s ease;
  border-bottom: 1px solid #fafafa;
  cursor: pointer;
  width: 100%;
  background: none;
  border-left: none;
  border-right: none;
  border-top: none;
  text-align: left;
  font-size: 15px;
  font-family: inherit;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background-color: #fafafa;
}

.menu-item.highlight {
  background: linear-gradient(135deg, #FFF7E6 0%, #FFFFFF 100%);
  font-weight: 600;
  color: #FA8C16;
}

.publish-icon {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF6A00 0%, #FF8533 100%);
  color: white;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 300;
}

.menu-icon {
  font-size: 20px;
  width: 24px;
  text-align: center;
  flex-shrink: 0;
}

.menu-text {
  flex: 1;
  font-size: 15px;
}

.menu-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 7px;
  background-color: #FF4D4F;
  color: white;
  font-size: 11px;
  font-weight: 600;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow-right {
  width: 16px;
  height: 16px;
  color: #ccc;
  flex-shrink: 0;
}

.menu-item.danger .menu-text {
  color: #FF4D4F;
}

.version-info {
  text-align: center;
  font-size: 12px;
  color: #ccc;
  margin-top: 24px;
}

/* ============================================
   退出登录确认弹窗
   ============================================ */
.logout-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.logout-modal {
  background-color: #fff;
  border-radius: 16px;
  padding: 32px 24px;
  max-width: 320px;
  width: 100%;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.25s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.modal-desc {
  font-size: 14px;
  color: #999;
  margin: 0 0 28px 0;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  gap: 12px;
}

.modal-btn {
  flex: 1;
  padding: 12px 20px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  border: none;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
}

.cancel-btn:active {
  background-color: #e8e8e8;
  transform: scale(0.98);
}

.confirm-btn {
  background-color: #FF4D4F;
  color: white;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.25);
}

.confirm-btn:active {
  background-color: #cf1322;
  transform: scale(0.98);
  box-shadow: 0 2px 6px rgba(255, 77, 79, 0.35);
}
</style>
