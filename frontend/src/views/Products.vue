<template>
  <div class="products-page">
    <header class="search-header">
      <button @click="$router.back()" class="back-btn">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <polyline points="15,18 9,12 15,6"/>
        </svg>
      </button>
      <div class="search-bar">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <circle cx="11" cy="11" r="8"/>
          <path d="M21 21l-4.35-4.35"/>
        </svg>
        <input
          type="text"
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索商品..."
          @keyup.enter="handleSearch"
        />
      </div>
    </header>

    <section class="filter-section">
      <div class="filter-tabs">
        <button
          v-for="tab in filterTabs"
          :key="tab.key"
          @click="activeFilter = tab.key"
          :class="['filter-tab', { active: activeFilter === tab.key }]"
        >
          {{ tab.label }}
        </button>
      </div>
      <div v-if="showCategoryFilter" class="category-filter">
        <button
          v-for="cat in categories"
          :key="cat.id || 'all'"
          @click="selectCategory(cat.id)"
          :class="['category-chip', { active: selectedCategoryId === cat.id }]"
        >
          {{ cat.name }}
        </button>
      </div>
    </section>

    <main class="products-grid-container">
      <div v-if="loading && products.length === 0" class="loading-state">
        <div class="skeleton-grid">
          <div v-for="i in 4" :key="i" class="skeleton-card">
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

      <div v-else-if="!loading && products.length === 0" class="empty-state">
        <div class="empty-icon">🔍</div>
        <p class="empty-text">没有找到相关商品</p>
        <button @click="resetFilters" class="reset-btn">重置筛选条件</button>
      </div>

      <div v-else class="products-grid">
        <div
          v-for="product in products"
          :key="product.id"
          @click="$router.push(`/products/${product.id}`)"
          class="product-card"
        >
          <div class="card-image-wrapper">
            <img
              v-if="getProductImage(product)"
              :src="getProductImage(product)"
              :alt="product.name"
              class="card-image"
              loading="lazy"
              @error="onImageError"
            />
            <div v-else class="card-image-placeholder">
              <span class="placeholder-icon">{{ getCategoryIcon(product.categoryId) }}</span>
            </div>
            <span v-if="product.conditionText && product.conditionText !== '未设置'" class="condition-badge">
              {{ product.conditionText }}
            </span>
          </div>

          <div class="card-info">
            <h4 class="card-title">{{ product.name }}</h4>
            <div class="card-price-row">
              <span class="current-price">
                <small>¥</small>{{ formatPrice(product.price) }}
              </span>
              <span v-if="product.originalPrice && product.originalPrice > product.price" class="original-price">
                ¥{{ formatPrice(product.originalPrice) }}
              </span>
            </div>
            <div class="card-footer">
              <span class="seller-name">{{ product.sellerName || product.categoryName || '校园卖家' }}</span>
              <span class="publish-time">{{ getTimeAgo(product.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="loadingMore" class="load-more-indicator">
        <div class="spinner"></div>
        <span>加载中...</span>
      </div>

      <div v-if="!hasMore && products.length > 0" class="end-reached">
        - 已经到底啦 -
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '../services/api'

const route = useRoute()
const router = useRouter()

const products = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const error = ref(null)

const searchKeyword = ref('')
const activeFilter = ref('default')
const selectedCategoryId = ref(null)

const currentPage = ref(1)
const pageSize = 20

const showCategoryFilter = ref(false)

const filterTabs = [
  { key: 'default', label: '综合' },
  { key: 'price', label: '价格' },
  { key: 'time', label: '最新' },
  { key: 'category', label: '分类' }
]

const categories = [
  { id: null, name: '全部' },
  { id: 1, name: '数码电子' },
  { id: 2, name: '书籍教材' },
  { id: 3, name: '生活日用' },
  { id: 4, name: '服饰鞋包' },
  { id: 5, name: '美妆护肤' },
  { id: 6, name: '运动户外' }
]

const categoryIcons = { 1: '📱', 2: '📚', 3: '🏠', 4: '👕', 5: '💄', 6: '⚽', 7: '✏️', 8: '📦' }

onMounted(async () => {
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword
  }
  if (route.query.categoryId) {
    selectedCategoryId.value = parseInt(route.query.categoryId)
    activeFilter.value = 'category'
    showCategoryFilter.value = true
  }
  await loadProducts()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

watch(activeFilter, (newVal) => {
  showCategoryFilter.value = newVal === 'category'
  if (newVal !== 'category') {
    showCategoryFilter.value = false
  }
  currentPage.value = 1
  loadProducts()
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

    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    if (selectedCategoryId.value) {
      params.categoryId = selectedCategoryId.value
    }
    if (activeFilter.value === 'price') {
      params.orderBy = 'price'
      params.sortOrder = 'asc'
    } else if (activeFilter.value === 'time') {
      params.orderBy = 'createdAt'
      params.sortOrder = 'desc'
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

function handleSearch() {
  currentPage.value = 1
  loadProducts()
}

function selectCategory(categoryId) {
  selectedCategoryId.value = categoryId
  currentPage.value = 1
  loadProducts()
}

function resetFilters() {
  searchKeyword.value = ''
  activeFilter.value = 'default'
  selectedCategoryId.value = null
  currentPage.value = 1
  loadProducts()
}

function handleScroll() {
  if (loadingMore.value || !hasMore.value) return
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight
  if (scrollTop + clientHeight >= scrollHeight - 150) {
    currentPage.value++
    loadProducts(true)
  }
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

function getTimeAgo(timestamp) {
  if (!timestamp) return ''
  const diffMs = Date.now() - new Date(timestamp).getTime()
  const diffMin = Math.floor(diffMs / 60000)
  const diffHour = Math.floor(diffMs / 3600000)
  const diffDay = Math.floor(diffMs / 86400000)

  if (diffMin < 60) return diffMin <= 0 ? '刚刚' : `${diffMin}分钟前`
  if (diffHour < 24) return `${diffHour}小时前`
  if (diffDay < 30) return `${diffDay}天前`
  return new Date(timestamp).toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
}
</script>

<style scoped>
.products-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.search-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
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
  flex-shrink: 0;
  background: none;
  border: none;
  cursor: pointer;
}

.back-btn svg { width: 22px; height: 22px; }

.search-bar {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #f5f5f5;
  border-radius: 18px;
  padding: 8px 14px;
}

.search-icon {
  width: 18px;
  height: 18px;
  color: #999;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: none;
  font-size: 14px;
  color: #333;
  outline: none;
}

.filter-section {
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.filter-tabs {
  display: flex;
  gap: 4px;
  padding: 10px 16px;
  overflow-x: auto;
  scrollbar-width: none;
}

.filter-tabs::-webkit-scrollbar { display: none; }

.filter-tab {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 7px 14px;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  background-color: #f5f5f5;
  border-radius: 16px;
  white-space: nowrap;
  border: none;
  cursor: pointer;
}

.filter-tab.active {
  background: linear-gradient(135deg, #FF6A00 0%, #FF8533 100%);
  color: white;
}

.category-filter {
  padding: 12px 16px;
  border-top: 1px solid #f5f5f5;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-chip {
  padding: 6px 14px;
  font-size: 13px;
  color: #666;
  background-color: #f5f5f5;
  border-radius: 14px;
  border: none;
  cursor: pointer;
}

.category-chip.active {
  background-color: #FFF7E6;
  color: #FA8C16;
  font-weight: 600;
}

.products-grid-container {
  padding: 12px 16px;
}

.products-grid {
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

.card-image-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background-color: #f5f5f5;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.card-image-placeholder .placeholder-icon {
  font-size: 40px;
  opacity: 0.5;
}

.condition-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 3px 8px;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(4px);
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  color: #666;
}

.card-info {
  padding: 10px 12px;
}

.card-title {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
  margin: 0 0 8px 0;
  min-height: 36px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 6px;
}

.current-price {
  font-size: 17px;
  font-weight: 700;
  color: #FF4D4F;
}

.current-price small {
  font-size: 12px;
  font-weight: 600;
}

.original-price {
  font-size: 11px;
  color: #bbb;
  text-decoration: line-through;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.seller-name {
  font-size: 11px;
  color: #999;
  max-width: 55%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.publish-time {
  font-size: 11px;
  color: #ccc;
}

.loading-state { padding-top: 12px; }

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

.skeleton-info { padding: 10px 12px; }

.skeleton-text {
  height: 36px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 10px;
}

.skeleton-price {
  height: 20px;
  width: 70px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.error-state {
  text-align: center;
  padding: 80px 32px;
}

.error-icon {
  font-size: 56px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.error-text {
  font-size: 15px;
  color: #666;
  margin: 0 0 20px 0;
}

.retry-btn {
  background: linear-gradient(135deg, #FF6A00 0%, #FF8533 100%);
  color: white;
  padding: 10px 28px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
}

.empty-state {
  text-align: center;
  padding: 80px 32px;
}

.empty-icon {
  font-size: 56px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-text {
  font-size: 15px;
  color: #999;
  margin: 0 0 20px 0;
}

.reset-btn {
  background: linear-gradient(135deg, #FF6A00 0%, #FF8533 100%);
  color: white;
  padding: 10px 28px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
}

.load-more-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  color: #999;
  font-size: 13px;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid #e0e0e0;
  border-top-color: #FF6A00;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.end-reached {
  text-align: center;
  padding: 28px 0;
  color: #ccc;
  font-size: 13px;
}
</style>
