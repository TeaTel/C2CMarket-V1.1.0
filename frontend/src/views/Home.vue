<template>
  <div class="home-page">
    <header class="search-header">
      <div class="search-bar">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <circle cx="11" cy="11" r="8"/>
          <path d="M21 21l-4.35-4.35"/>
        </svg>
        <input
          type="text"
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜搜你想要的宝贝..."
          @keyup.enter="handleSearch"
        />
        <button v-if="searchKeyword" @click="clearSearch" class="clear-btn">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <path d="M15 9l-6 6M9 9l6 6"/>
          </svg>
        </button>
      </div>
    </header>

    <main class="home-content">
      <section class="banner-section">
        <div class="banner-slider">
          <div class="banner-slide">
            <div class="banner-content">
              <h2 class="banner-title">校园闲置好物</h2>
              <p class="banner-subtitle">让闲置流动起来，让生活更美好</p>
              <button @click="$router.push('/products/create')" class="banner-btn">立即发布</button>
            </div>
          </div>
        </div>
      </section>

      <section class="quick-categories">
        <div
          v-for="category in quickCategories"
          :key="category.id"
          @click="goToCategory(category)"
          class="category-item"
        >
          <div class="category-icon" :style="{ background: category.color }">
            {{ category.icon }}
          </div>
          <span class="category-name">{{ category.name }}</span>
        </div>
      </section>

      <section class="products-section">
        <div class="section-header">
          <h3 class="section-title">为你推荐</h3>
          <router-link to="/products" class="view-more">
            查看更多
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="9,18 15,12 9,6"/>
            </svg>
          </router-link>
        </div>

        <div v-if="loading" class="loading-state">
          <div class="skeleton-grid">
            <div v-for="i in 6" :key="i" class="skeleton-card">
              <div class="skeleton-image"></div>
              <div class="skeleton-info">
                <div class="skeleton-text"></div>
                <div class="skeleton-price"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="error && products.length === 0" class="error-state">
          <div class="error-icon">😔</div>
          <p class="error-text">{{ error }}</p>
          <button @click="retryLoad" class="retry-btn">点击重试</button>
        </div>

        <div v-else-if="products.length === 0" class="empty-state">
          <div class="empty-icon">📦</div>
          <p class="empty-text">暂无商品，快去发布你的闲置物品吧！</p>
        </div>

        <div v-else class="waterfall-grid">
          <div
            v-for="product in products"
            :key="product.id"
            @click="goToDetail(product.id)"
            class="product-card"
          >
            <div class="product-image-wrapper">
              <img
                v-if="getProductImage(product)"
                :src="getProductImage(product)"
                :alt="product.name"
                class="product-image"
                loading="lazy"
                @error="onImageError"
              />
              <div v-else class="product-image-placeholder">
                <span class="placeholder-icon">{{ getCategoryIcon(product.categoryId) }}</span>
              </div>
              <span v-if="product.conditionText && product.conditionText !== '未设置'" class="condition-tag">
                {{ product.conditionText }}
              </span>
            </div>

            <div class="product-info">
              <h4 class="product-name line-clamp-2">{{ product.name }}</h4>
              <div class="product-meta">
                <span class="product-price">
                  <small>¥</small>{{ formatPrice(product.price) }}
                </span>
                <span v-if="product.originalPrice && product.originalPrice > product.price" class="original-price">
                  ¥{{ formatPrice(product.originalPrice) }}
                </span>
              </div>
              <div class="product-footer">
                <span class="seller-info">{{ product.sellerName || product.categoryName || '校园卖家' }}</span>
                <span class="like-count" v-if="product.likeCount">
                  <svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
                    <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/>
                  </svg>
                  {{ product.likeCount }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="!loading && hasMore && products.length > 0" class="load-more">
          <button @click="loadMore" :disabled="loadingMore" class="load-more-btn">
            {{ loadingMore ? '加载中...' : '上滑加载更多' }}
          </button>
        </div>

        <div v-if="!hasMore && products.length > 0" class="no-more">
          <span class="no-more-line"></span>
          <span class="no-more-text">已经到底啦~</span>
          <span class="no-more-line"></span>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { productApi } from '../services/api'

const router = useRouter()

const products = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const error = ref(null)

const currentPage = ref(1)
const pageSize = 10

const searchKeyword = ref('')

const quickCategories = ref([
  { id: 1, name: '数码电子', icon: '📱', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { id: 2, name: '书籍教材', icon: '📚', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { id: 3, name: '生活日用', icon: '🏠', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { id: 4, name: '服饰鞋包', icon: '👕', color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' },
  { id: 5, name: '美妆护肤', icon: '💄', color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' },
  { id: 6, name: '运动户外', icon: '⚽', color: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)' },
  { id: 7, name: '文具手办', icon: '✏️', color: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)' },
  { id: 8, name: '其他', icon: '📦', color: 'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)' }
])

const categoryIcons = { 1: '📱', 2: '📚', 3: '🏠', 4: '👕', 5: '💄', 6: '⚽', 7: '✏️', 8: '📦' }

onMounted(async () => {
  await loadProducts()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

async function loadProducts(isLoadMore = false) {
  try {
    error.value = null
    if (isLoadMore) {
      loadingMore.value = true
    } else {
      loading.value = true
    }

    const params = {
      page: currentPage.value,
      size: pageSize,
      status: 1
    }

    const response = await productApi.getProducts(params)

    if (response.code === 200) {
      const data = response.data || {}
      const newProducts = data.list || data.records || data.items || []

      if (isLoadMore) {
        products.value = [...products.value, ...newProducts]
      } else {
        products.value = newProducts
      }

      const total = data.total || 0
      hasMore.value = products.value.length < total
    } else {
      throw new Error(response.message || '加载商品失败')
    }
  } catch (err) {
    console.error('加载商品失败:', err)
    error.value = err.message || '加载失败，请稍后重试'
    if (isLoadMore) {
      currentPage.value--
    }
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function retryLoad() {
  currentPage.value = 1
  loadProducts()
}

function loadMore() {
  if (!hasMore.value || loadingMore.value) return
  currentPage.value++
  loadProducts(true)
}

function handleScroll() {
  if (loadingMore.value || !hasMore.value) return
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    loadMore()
  }
}

function handleSearch() {
  if (!searchKeyword.value.trim()) return
  router.push({ path: '/products', query: { keyword: searchKeyword.value.trim() } })
}

function clearSearch() {
  searchKeyword.value = ''
}

function goToDetail(productId) {
  router.push(`/products/${productId}`)
}

function goToCategory(category) {
  router.push({ path: '/products', query: { categoryId: category.id } })
}

function getProductImage(product) {
  if (product.coverImage) return product.coverImage
  if (product.imageUrls) {
    try {
      const urls = typeof product.imageUrls === 'string' ? JSON.parse(product.imageUrls) : product.imageUrls
      if (Array.isArray(urls) && urls.length > 0) return urls[0]
    } catch (e) { /* ignore */ }
  }
  if (product.imageUrl) return product.imageUrl
  return null
}

function onImageError(e) {
  e.target.style.display = 'none'
  const placeholder = e.target.nextElementSibling
  if (placeholder) placeholder.style.display = 'flex'
}

function getCategoryIcon(categoryId) {
  return categoryIcons[categoryId] || '📦'
}

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2
  })
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.search-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: #fff;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  background-color: #f5f5f5;
  border-radius: 20px;
  padding: 10px 16px;
  transition: all 0.25s ease;
}

.search-bar:focus-within {
  background-color: #fff;
  box-shadow: 0 0 0 2px #FF6A00;
}

.search-icon {
  width: 20px;
  height: 20px;
  color: #999;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: none;
  font-size: 15px;
  color: #333;
  outline: none;
}

.search-input::placeholder {
  color: #bbb;
}

.clear-btn {
  width: 20px;
  height: 20px;
  color: #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: none;
  border: none;
  cursor: pointer;
}

.clear-btn svg {
  width: 16px;
  height: 16px;
}

.banner-section {
  margin: 12px 16px 0;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #FF6A00 0%, #FF8533 50%, #FFB347 100%);
  aspect-ratio: 2 / 1;
}

.banner-slide {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.banner-content {
  text-align: left;
  color: white;
  max-width: 280px;
}

.banner-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
  line-height: 1.3;
}

.banner-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 16px;
}

.banner-btn {
  background-color: white;
  color: #FF6A00;
  padding: 10px 24px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
}

.banner-btn:active {
  transform: scale(0.95);
}

.quick-categories {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px 8px;
  padding: 20px 16px;
  background-color: #fff;
  margin-top: 12px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.category-item:active {
  transform: scale(0.92);
}

.category-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.category-name {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.products-section {
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.view-more {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #999;
  text-decoration: none;
}

.view-more svg {
  width: 16px;
  height: 16px;
}

.waterfall-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.product-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
}

.product-card:active {
  transform: scale(0.97);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background-color: #f5f5f5;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.placeholder-icon {
  font-size: 40px;
  opacity: 0.5;
}

.condition-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 4px 8px;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(4px);
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  color: #666;
}

.product-info {
  padding: 12px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
  margin: 0 0 8px;
  min-height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-meta {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 8px;
}

.product-price {
  font-size: 18px;
  font-weight: 700;
  color: #FF4D4F;
}

.product-price small {
  font-size: 12px;
  font-weight: 600;
}

.original-price {
  font-size: 12px;
  color: #bbb;
  text-decoration: line-through;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.seller-info {
  font-size: 12px;
  color: #999;
  max-width: 60%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.like-count {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: #999;
}

.loading-state {
  padding: 16px 0;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.skeleton-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.skeleton-image {
  width: 100%;
  aspect-ratio: 1 / 1;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info {
  padding: 12px;
}

.skeleton-text {
  height: 40px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 10px;
}

.skeleton-price {
  height: 22px;
  width: 80px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-text {
  font-size: 15px;
  color: #999;
  margin-bottom: 24px;
}

.error-state {
  text-align: center;
  padding: 60px 20px;
}

.error-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.8;
}

.error-text {
  font-size: 15px;
  color: #666;
  margin-bottom: 24px;
  line-height: 1.5;
  max-width: 280px;
  margin-left: auto;
  margin-right: auto;
}

.retry-btn {
  background: linear-gradient(135deg, #FF6A00 0%, #FF8533 100%);
  color: white;
  padding: 12px 32px;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 600;
  border: none;
  cursor: pointer;
}

.load-more {
  text-align: center;
  padding: 24px 0 16px;
}

.load-more-btn {
  background-color: #f5f5f5;
  color: #999;
  padding: 10px 32px;
  border-radius: 20px;
  font-size: 14px;
  border: none;
  cursor: pointer;
}

.load-more-btn:disabled {
  opacity: 0.6;
}

.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px 0 32px;
  color: #ccc;
  font-size: 13px;
}

.no-more-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(to right, transparent, #e0e0e0, transparent);
}
</style>
