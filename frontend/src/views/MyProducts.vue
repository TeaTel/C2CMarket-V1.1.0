<template>
  <div class="my-products-page">
    <header class="page-header">
      <button @click="$router.back()" class="back-btn">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <polyline points="15,18 9,12 15,6"/>
        </svg>
      </button>
      <h1 class="header-title">我的发布</h1>
    </header>

    <main class="main-content">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="error" class="error-state">
        <div class="error-icon">⚠️</div>
        <p>{{ error }}</p>
        <button @click="loadMyProducts" class="retry-btn">重试</button>
      </div>

      <div v-else-if="products.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <h3>还没有发布商品</h3>
        <p>快去发布你的闲置物品吧</p>
        <router-link to="/products/create" class="publish-btn">发布商品</router-link>
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
              v-if="getProductImage(product)"
              :src="getProductImage(product)"
              alt=""
              loading="lazy"
            />
            <div v-else class="image-placeholder">
              <span>{{ getCategoryEmoji(product.categoryId) }}</span>
            </div>
            <div class="status-badge" :class="getStatusClass(product.status)">
              {{ getStatusText(product.status) }}
            </div>
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <div class="product-meta">
              <span class="product-price">¥{{ product.price }}</span>
              <span class="product-condition">{{ product.conditionText }}</span>
            </div>
            <div class="product-stats">
              <span>👁 {{ product.viewCount || 0 }}</span>
              <span>❤️ {{ product.likeCount || 0 }}</span>
              <span class="product-time">{{ formatTime(product.createdAt) }}</span>
            </div>
          </div>
          <div class="product-actions" @click.stop>
            <button
              v-if="product.status === 1"
              @click="toggleStatus(product.id, 0)"
              class="action-btn off-btn"
            >下架</button>
            <button
              v-if="product.status === 0"
              @click="toggleStatus(product.id, 1)"
              class="action-btn on-btn"
            >上架</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { productApi } from '../services/api'

const router = useRouter()
const products = ref([])
const loading = ref(true)
const error = ref('')

function getProductImage(product) {
  if (product.coverImage) return product.coverImage
  if (product.imageUrls) {
    try {
      const urls = typeof product.imageUrls === 'string' ? JSON.parse(product.imageUrls) : product.imageUrls
      if (Array.isArray(urls) && urls.length > 0) return urls[0]
    } catch (e) {}
  }
  return null
}

function getCategoryEmoji(categoryId) {
  const map = { 1: '📱', 2: '📚', 3: '🏠', 4: '👕', 5: '💄', 6: '⚽' }
  return map[categoryId] || '📦'
}

function getStatusText(status) {
  const map = { 0: '已下架', 1: '在售', 2: '已售出', 3: '预约中' }
  return map[status] || '未知'
}

function getStatusClass(status) {
  const map = { 0: 'status-off', 1: 'status-on', 2: 'status-sold', 3: 'status-reserved' }
  return map[status] || ''
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return date.toLocaleDateString('zh-CN')
}

function goToDetail(id) {
  router.push(`/products/${id}`)
}

async function toggleStatus(productId, status) {
  try {
    const res = await productApi.updateProduct(productId, { status })
    if (res.code === 200) {
      const product = products.value.find(p => p.id === productId)
      if (product) product.status = status
    }
  } catch (e) {
    console.error('操作失败:', e)
  }
}

async function loadMyProducts() {
  loading.value = true
  error.value = ''
  try {
    const res = await productApi.getMyProducts()
    if (res.code === 200) {
      products.value = res.data || []
    } else {
      error.value = res.message || '加载失败'
    }
  } catch (e) {
    console.error('加载我的商品失败:', e)
    error.value = '加载失败，请检查网络后重试'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMyProducts()
})
</script>

<style scoped>
.my-products-page {
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

.publish-btn {
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
  position: relative;
  width: 100px;
  height: 100px;
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

.status-badge {
  position: absolute;
  top: 6px;
  left: 6px;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 600;
  color: white;
}

.status-on { background-color: #52c41a; }
.status-off { background-color: #999; }
.status-sold { background-color: #1890ff; }
.status-reserved { background-color: #faad14; }

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
  align-items: center;
  gap: 8px;
}

.product-price {
  font-size: 17px;
  font-weight: 700;
  color: #FF4D4F;
}

.product-condition {
  font-size: 12px;
  color: #999;
  padding: 2px 6px;
  background: #f5f5f5;
  border-radius: 4px;
}

.product-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #bbb;
}

.product-time {
  margin-left: auto;
}

.product-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  flex-shrink: 0;
}

.action-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
}

.off-btn {
  background-color: #f5f5f5;
  color: #666;
}

.on-btn {
  background-color: #FFF7E6;
  color: #FA8C16;
}
</style>
