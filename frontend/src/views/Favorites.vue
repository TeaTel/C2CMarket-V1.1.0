<template>
  <div class="favorites-page">
    <header class="page-header">
      <button @click="$router.back()" class="back-btn">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <polyline points="15,18 9,12 15,6"/>
        </svg>
      </button>
      <h1 class="header-title">我的收藏</h1>
      <span class="count-badge" v-if="total > 0">{{ total }}</span>
    </header>

    <main class="main-content">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="error" class="error-state">
        <div class="error-icon">⚠️</div>
        <p>{{ error }}</p>
        <button @click="loadFavorites" class="retry-btn">重试</button>
      </div>

      <div v-else-if="products.length === 0" class="empty-state">
        <div class="empty-icon">❤️</div>
        <h3>还没有收藏商品</h3>
        <p>去逛逛，收藏感兴趣的商品吧</p>
        <router-link to="/products" class="browse-btn">逛一逛</router-link>
      </div>

      <div v-else class="product-list">
        <div
          v-for="product in products"
          :key="product.id"
          class="product-card"
          @click="goToDetail(product.id)"
        >
          <div class="product-image">
            <img
              v-if="product.coverImage"
              :src="product.coverImage"
              alt=""
              loading="lazy"
            />
            <div v-else class="image-placeholder">
              <span>📦</span>
            </div>
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <div class="product-meta">
              <span class="product-price">¥{{ product.price }}</span>
              <span v-if="product.originalPrice" class="product-original">¥{{ product.originalPrice }}</span>
            </div>
            <div class="product-footer">
              <span class="product-status" :class="{ 'on-sale': product.status === 1 }">
                {{ getStatusText(product.status) }}
              </span>
              <span class="fav-time">{{ formatTime(product.favoritedAt) }}</span>
            </div>
          </div>
          <button @click.stop="removeFavorite(product.id)" class="unfav-btn" title="取消收藏">
            ❤️
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { favoriteApi } from '../services/api'

const router = useRouter()
const products = ref([])
const total = ref(0)
const loading = ref(true)
const error = ref('')

function getStatusText(status) {
  const map = { 0: '已下架', 1: '在售', 2: '已售出', 3: '预约中' }
  return map[status] || '未知'
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚收藏'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前收藏'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前收藏'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前收藏'
  return date.toLocaleDateString('zh-CN')
}

function goToDetail(id) {
  router.push(`/products/${id}`)
}

async function removeFavorite(productId) {
  try {
    await favoriteApi.removeFavorite(productId)
    products.value = products.value.filter(p => p.id !== productId)
    total.value = Math.max(0, total.value - 1)
  } catch (e) {
    console.error('取消收藏失败:', e)
  }
}

async function loadFavorites() {
  loading.value = true
  error.value = ''
  try {
    const res = await favoriteApi.getMyFavorites()
    if (res.code === 200) {
      const data = res.data || {}
      products.value = data.list || []
      total.value = data.total || 0
    } else {
      error.value = res.message || '加载失败'
    }
  } catch (e) {
    console.error('加载收藏失败:', e)
    error.value = '加载失败，请检查网络后重试'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
  border-radius: 50%;
}

.back-btn svg { width: 22px; height: 22px; }

.header-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex: 1;
}

.count-badge {
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

.main-content {
  padding: 12px 16px 80px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  color: #999;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #f0f0f0;
  border-top-color: #FF6A00;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin { to { transform: rotate(360deg); } }

.error-state {
  text-align: center;
  padding: 60px 0;
}

.error-icon { font-size: 48px; margin-bottom: 12px; }
.error-state p { color: #666; margin-bottom: 16px; }

.retry-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #FF6A00, #FF8533);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-state h3 { font-size: 18px; color: #333; margin: 0 0 8px; }
.empty-state p { color: #999; margin-bottom: 20px; }

.browse-btn {
  display: inline-block;
  padding: 12px 32px;
  background: linear-gradient(135deg, #FF6A00, #FF8533);
  color: white;
  text-decoration: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.product-card {
  display: flex;
  gap: 14px;
  padding: 14px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.2s ease;
}

.product-card:active {
  transform: scale(0.98);
}

.product-image {
  width: 90px;
  height: 90px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  background: #f8f8f8;
}

.product-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-meta {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.product-price {
  font-size: 17px;
  font-weight: 700;
  color: #FF4D4F;
}

.product-original {
  font-size: 13px;
  color: #ccc;
  text-decoration: line-through;
}

.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.product-status {
  font-size: 12px;
  color: #999;
  padding: 2px 6px;
  background: #f5f5f5;
  border-radius: 4px;
}

.product-status.on-sale {
  color: #52c41a;
  background: #f6ffed;
}

.fav-time {
  font-size: 12px;
  color: #ccc;
}

.unfav-btn {
  align-self: center;
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  padding: 8px;
  flex-shrink: 0;
  transition: transform 0.2s;
}

.unfav-btn:active {
  transform: scale(1.3);
}
</style>
