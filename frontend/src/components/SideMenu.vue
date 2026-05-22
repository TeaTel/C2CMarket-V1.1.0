<template>
  <teleport to="body">
    <transition name="drawer-fade">
      <div v-if="visible" class="side-overlay" @click="$emit('close')"></div>
    </transition>
    <transition name="drawer-slide">
      <aside v-if="visible" class="side-drawer">
        <div class="drawer-header">
          <div v-if="authStore.isAuthenticated" class="user-section" @click="goProfile">
            <img :src="authStore.currentUser.value?.avatar || defaultAvatar" class="user-avatar" @error="onAvatarError" />
            <div class="user-info">
              <span class="user-name">{{ authStore.currentUser.value?.nickname || authStore.currentUser.value?.username || '用户' }}</span>
              <span class="user-sub">{{ authStore.currentUser.value?.school || '校园集市' }}</span>
            </div>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div v-else class="user-section-guest">
            <div class="guest-avatar">{{ defaultAvatar }}</div>
            <div class="user-info">
              <span class="user-name">未登录</span>
              <span class="user-sub">登录享受更多功能</span>
            </div>
            <button class="guest-login-btn" @click="goLogin">登录</button>
          </div>
        </div>

        <div v-if="authStore.isAuthenticated" class="stats-row">
          <div class="stat-item" @click="goRoute('/my-products')">
            <span class="stat-num">{{ stats.published }}</span>
            <span class="stat-lbl">发布</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">{{ stats.sold }}</span>
            <span class="stat-lbl">已售</span>
          </div>
          <div class="stat-item" @click="goRoute('/favorites')">
            <span class="stat-num">{{ stats.favorites }}</span>
            <span class="stat-lbl">收藏</span>
          </div>
        </div>

        <nav class="menu-list">
          <div class="menu-item highlight" @click="goRoute('/products/create')">
            <span class="menu-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="#fff" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            </span>
            <span class="menu-text">发布新商品</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div class="menu-item" @click="goRoute('/my-products')">
            <span class="menu-icon">📦</span>
            <span class="menu-text">我的发布</span>
            <span v-if="stats.published > 0" class="menu-badge">{{ stats.published }}</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div class="menu-item" @click="goRoute('/orders')">
            <span class="menu-icon">🛒</span>
            <span class="menu-text">我的订单</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div class="menu-item" @click="goRoute('/favorites')">
            <span class="menu-icon">❤️</span>
            <span class="menu-text">我的收藏</span>
            <span v-if="stats.favorites > 0" class="menu-badge">{{ stats.favorites }}</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>

          <div class="menu-divider"><span>更多功能</span></div>

          <div class="menu-item new-feature" @click="goRoute('/orgs/my')">
            <span class="menu-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>
            </span>
            <span class="menu-text">我的组织</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div class="menu-item new-feature" @click="handleNewFeature('campus')">
            <span class="menu-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9,22 9,12 15,12 15,22"/></svg>
            </span>
            <span class="menu-text">我的校区</span>
            <span class="menu-tag new">NEW</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div class="menu-item new-feature" @click="handleNewFeature('stream')">
            <span class="menu-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><polygon points="5 3 19 12 5 21 5 3"/></svg>
            </span>
            <span class="menu-text">我要推流</span>
            <span class="menu-tag hot">HOT</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>

          <div class="menu-divider"></div>

          <div class="menu-item" @click="goRoute('/profile')">
            <span class="menu-icon">⚙️</span>
            <span class="menu-text">账号设置</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div class="menu-item" @click="goRoute('/address')">
            <span class="menu-icon">📍</span>
            <span class="menu-text">收货地址</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
          <div class="menu-item" @click="handleAbout">
            <span class="menu-icon">ℹ️</span>
            <span class="menu-text">关于我们</span>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
          </div>
        </nav>

        <div v-if="authStore.isAuthenticated" class="drawer-footer">
          <button class="logout-btn" @click="handleLogout">退出登录</button>
        </div>
      </aside>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

defineProps({ visible: { type: Boolean, default: false } })
const emit = defineEmits(['close'])

const router = useRouter()
const authStore = useAuthStore()

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#eee"/><circle cx="20" cy="15" r="8" fill="#ccc"/><ellipse cx="20" cy="35" rx="12" ry="8" fill="#ccc"/></svg>')

const stats = reactive({ published: 0, sold: 0, favorites: 0 })

function goProfile() { emit('close'); router.push('/profile') }
function goLogin() { emit('close'); router.push('/login') }
function goRoute(path) { emit('close'); router.push(path) }
function handleAbout() { emit('close'); alert('校园二手交易平台 v2.0 —— 让每一件闲置都有归宿') }
function handleLogout() { emit('close'); authStore.logout() }
function handleNewFeature(feature) {
  emit('close')
  const messages = {
    organization: '成立组织功能即将上线，敬请期待！',
    campus: '我的校区功能开发中，完成后可切换不同校区查看专属内容~',
    stream: '推流功能规划中，届时可直播展示商品详情！'
  }
  alert(messages[feature] || '功能开发中')
}
function onAvatarError(e) { e.target.src = defaultAvatar }
</script>

<style scoped>
.side-overlay {
  position: fixed; inset: 0; z-index: 2000;
  background: rgba(0,0,0,0.45);
}
.side-drawer {
  position: fixed; top: 0; left: 0; bottom: 0; z-index: 2001;
  width: 300px; max-width: 85vw;
  background: #FFFFFF;
  display: flex; flex-direction: column;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}
.drawer-header {
  padding: 20px 16px 12px;
  background: linear-gradient(135deg, #FFF7E6, #FFE7BA);
}
.user-section {
  display: flex; align-items: center; gap: 12px;
  cursor: pointer; border-radius: 8px; padding: 4px;
  transition: background 0.15s;
}
.user-section:active { background: rgba(255,106,0,0.08); }
.user-avatar {
  width: 52px; height: 52px; border-radius: 50%;
  object-fit: cover; background: #eee; flex-shrink: 0;
}
.user-info { flex: 1; min-width: 0; }
.user-name { display: block; font-size: 17px; font-weight: 700; color: #333; }
.user-sub { display: block; font-size: 12px; color: #999; margin-top: 2px; }
.user-section-guest {
  display: flex; align-items: center; gap: 12px;
}
.guest-avatar { width: 52px; height: 52px; border-radius: 50%; background: #eee; flex-shrink: 0; }
.guest-login-btn {
  padding: 8px 20px; border-radius: 16px; border: none;
  background: linear-gradient(135deg, #FF6A00, #FF8533);
  color: #fff; font-size: 13px; font-weight: 600; cursor: pointer;
}
.guest-login-btn:active { transform: scale(0.96); }
.stats-row {
  display: flex; align-items: center; padding: 12px 16px;
  background: #fff; border-bottom: 1px solid #F5F7FA;
}
.stat-item {
  flex: 1; text-align: center; cursor: pointer;
  padding: 4px 0; border-radius: 8px; transition: background 0.15s;
}
.stat-item:active { background: #f5f5f5; }
.stat-num { display: block; font-size: 18px; font-weight: 700; color: #333; }
.stat-lbl { display: block; font-size: 11px; color: #999; margin-top: 2px; }
.menu-list { flex: 1; padding: 8px 0; }
.menu-item {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 20px; cursor: pointer;
  transition: background 0.15s; border: none; background: none; width: 100%; text-align: left;
  font-size: 15px; color: #333; text-decoration: none;
}
.menu-item:active { background: #f5f5f5; }
.menu-item.highlight { background: linear-gradient(135deg, #FFF7E6, #FFF0D9); margin: 0 12px 4px; border-radius: 10px; padding: 12px 16px; }
.menu-item.highlight .menu-text { color: #FF6A00; font-weight: 600; }
.menu-item.highlight .menu-icon {
  width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #FF6A00, #FF8533); border-radius: 50%;
}
.menu-icon { font-size: 20px; flex-shrink: 0; width: 24px; text-align: center; }
.menu-text { flex: 1; font-size: 15px; color: #333; }
.menu-badge {
  background: #FF4D4F; color: #fff; font-size: 11px; font-weight: 600;
  padding: 2px 7px; border-radius: 8px; margin-right: -4px;
}
.menu-tag {
  font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 4px; margin-right: -4px;
}
.menu-tag.new { background: #E8F4FD; color: #1890FF; }
.menu-tag.hot { background: #FFF1F0; color: #FF4D4F; }
.menu-divider {
  display: flex; align-items: center; padding: 8px 20px;
  color: #ccc; font-size: 12px;
}
.menu-divider::before, .menu-divider::after { content: ''; flex: 1; height: 1px; background: #E8ECF0; margin: 0 10px; }
.new-feature .menu-text { color: #555; }
.drawer-footer {
  padding: 16px 20px; border-top: 1px solid #F5F7FA;
}
.logout-btn {
  width: 100%; padding: 12px; border: 1px solid #E8ECF0; border-radius: 8px;
  background: #fff; color: #999; font-size: 14px; font-weight: 500; cursor: pointer;
  transition: all 0.15s;
}
.logout-btn:active { background: #FFF1F0; color: #FF4D4F; border-color: #FFCCC7; }

.drawer-fade-enter-active, .drawer-fade-leave-active { transition: opacity 0.25s ease; }
.drawer-fade-enter-from, .drawer-fade-leave-to { opacity: 0; }
.drawer-slide-enter-active, .drawer-slide-leave-active { transition: transform 0.25s cubic-bezier(0.4,0,0.2,1); }
.drawer-slide-enter-from, .drawer-slide-leave-to { transform: translateX(-100%); }

@media (min-width: 768px) {
  .side-drawer { width: 360px; }
}
</style>
