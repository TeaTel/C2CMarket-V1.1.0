<template>
  <div class="products">
    <NavBar />
    
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1 class="page-title">商品列表</h1>
          <div class="page-actions">
            <router-link v-if="isAuthenticated" to="/products/create" class="create-btn">
              <span>+</span> 发布商品
            </router-link>
            <button
              v-else
              @click="handlePublishClick"
              class="create-btn create-btn-disabled"
              title="请先登录后再发布商品"
            >
              <span>🔒</span> 发布商品（需登录）
            </button>
          </div>
        </div>
        
        <div class="filters-section">
          <div class="search-box">
            <input
              type="text"
              v-model="searchQuery"
              placeholder="搜索商品名称或描述..."
              @keyup.enter="handleSearch"
            />
            <button @click="handleSearch" class="search-btn">搜索</button>
          </div>
          
          <div class="filter-options">
            <select v-model="selectedCategory" @change="handleFilterChange">
              <option value="">所有分类</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }}
              </option>
            </select>
            
            <select v-model="selectedStatus" @change="handleFilterChange">
              <option value="">所有状态</option>
              <option value="1">在售</option>
              <option value="2">已售出</option>
              <option value="0">待审核</option>
            </select>
            
            <select v-model="sortBy" @change="handleSortChange">
              <option value="created_at:desc">最新发布</option>
              <option value="price:asc">价格从低到高</option>
              <option value="price:desc">价格从高到低</option>
            </select>
          </div>
        </div>
        
        <div v-if="loading" class="loading">加载中...</div>
        
        <div v-else-if="products.length === 0" class="empty-state">
          <div class="empty-icon">📦</div>
          <h3>暂无商品</h3>
          <p>暂时没有找到符合条件的商品</p>
          <router-link v-if="isAuthenticated" to="/products/create" class="empty-action-btn">
            发布第一个商品
          </router-link>
          <button
            v-else
            @click="handlePublishClick"
            class="empty-action-btn empty-action-btn-disabled"
          >
            登录后发布第一个商品
          </button>
        </div>
        
        <div v-else class="products-grid">
          <div v-for="product in products" :key="product.id" class="product-card">
            <div class="product-image">
              <img :src="product.imageUrl || '/placeholder-image.jpg'" :alt="product.name" />
              <div class="product-status-badge" :class="getStatusClass(product.status)">
                {{ getStatusText(product.status) }}
              </div>
            </div>
            <div class="product-info">
              <div class="product-header">
                <h3 class="product-title">{{ product.name }}</h3>
                <span class="product-category">{{ getCategoryName(product.categoryId) }}</span>
              </div>
              <p class="product-description">{{ product.description }}</p>
              <div class="product-meta">
                <div class="product-price">¥{{ product.price }}</div>
                <div class="product-seller">
                  <span class="seller-label">卖家:</span>
                  <span class="seller-name">{{ product.sellerName || '匿名' }}</span>
                </div>
              </div>
              <div class="product-footer">
                <span class="product-time">{{ formatTime(product.createdAt) }}</span>
                <div class="product-actions">
                  <button @click="viewProduct(product.id)" class="view-btn">查看详情</button>
                  <button
                    v-if="isAuthenticated && product.status === 1"
                    @click="addToCart(product)"
                    class="cart-btn"
                  >
                    加入购物车
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="total > pageSize" class="pagination">
          <button
            @click="goToPage(currentPage - 1)"
            :disabled="currentPage === 1"
            class="pagination-btn prev"
          >
            上一页
          </button>
          <span class="pagination-info">
            第 {{ currentPage }} 页 / 共 {{ totalPages }} 页
          </span>
          <button
            @click="goToPage(currentPage + 1)"
            :disabled="currentPage === totalPages"
            class="pagination-btn next"
          >
            下一页
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import NavBar from '../components/NavBar.vue'
import { productApi, categoryApi } from '../services/api'

const router = useRouter()
const authStore = useAuthStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)

// 数据状态
const products = ref([])
const categories = ref([])
const loading = ref(true)
const total = ref(0)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(12)

// 筛选参数
const searchQuery = ref('')
const selectedCategory = ref('')
const selectedStatus = ref('')
const sortBy = ref('created_at:desc')

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

onMounted(async () => {
  await Promise.all([loadProducts(), loadCategories()])
})

async function loadProducts() {
  try {
    loading.value = true
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (searchQuery.value) {
      params.keyword = searchQuery.value
    }
    
    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }
    
    if (selectedStatus.value) {
      params.status = selectedStatus.value
    }
    
    const [sortField, sortOrder] = sortBy.value.split(':')
    params.sortField = sortField
    params.sortOrder = sortOrder
    
    const response = await productApi.getProducts(params)
    if (response.code === 200) {
      products.value = response.data.records || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    console.error('加载商品失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    loading.value = true
    const response = await categoryApi.getCategories()
    if (response.code === 200) {
      // 兼容多种后端返回格式
      const data = response.data
      categories.value = Array.isArray(data)
        ? data
        : (data?.records || data?.list || [])
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    // 使用默认分类作为fallback
    categories.value = [
      { id: 1, name: '电子产品' },
      { id: 2, name: '书籍教材' },
      { id: 3, name: '生活用品' },
      { id: 4, name: '服装鞋帽' },
      { id: 5, name: '运动器材' },
      { id: 6, name: '其他' }
    ]
  }
}

function handleSearch() {
  currentPage.value = 1
  loadProducts()
}

function handleFilterChange() {
  currentPage.value = 1
  loadProducts()
}

function handleSortChange() {
  loadProducts()
}

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  loadProducts()
}

function getStatusText(status) {
  const statusMap = {
    0: '待审核',
    1: '在售',
    2: '已售出',
    3: '已下架'
  }
  return statusMap[status] || '未知状态'
}

function getStatusClass(status) {
  const classMap = {
    0: 'status-pending',
    1: 'status-available',
    2: 'status-sold',
    3: 'status-offline'
  }
  return classMap[status] || ''
}

function getCategoryName(categoryId) {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : '未分类'
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleDateString('zh-CN')
}

function viewProduct(productId) {
  router.push(`/products/${productId}`)
}

function addToCart(product) {
  // 这里可以添加购物车逻辑
  alert(`已将 ${product.name} 加入购物车`)
}

function handlePublishClick() {
  router.push({
    path: '/login',
    query: { redirect: '/products/create' }
  })
}
</script>

<style scoped>
.products {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  color: #333;
  margin: 0;
}

.create-btn {
  background-color: #4CAF50;
  color: white;
  text-decoration: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  transition: background-color 0.2s;
}

.create-btn:hover {
  background-color: #45a049;
}

.create-btn-disabled {
  background-color: #9e9e9e;
  cursor: pointer;
}

.create-btn-disabled:hover {
  background-color: #757575;
}

.filters-section {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.search-box {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.search-box input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.search-box input:focus {
  outline: none;
  border-color: #4CAF50;
}

.search-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.search-btn:hover {
  background-color: #45a049;
}

.filter-options {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.filter-options select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: white;
  min-width: 150px;
}

.filter-options select:focus {
  outline: none;
  border-color: #4CAF50;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  color: #333;
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: #666;
  margin-bottom: 1.5rem;
}

.empty-action-btn {
  background-color: #4CAF50;
  color: white;
  text-decoration: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  font-weight: 500;
  display: inline-block;
}

.empty-action-btn:hover {
  background-color: #45a049;
}

.empty-action-btn-disabled {
  background-color: #9e9e9e;
  cursor: pointer;
  border: none;
}

.empty-action-btn-disabled:hover {
  background-color: #757575;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.product-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-status-badge {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
  color: white;
}

.status-pending {
  background-color: #ff9800;
}

.status-available {
  background-color: #4CAF50;
}

.status-sold {
  background-color: #f44336;
}

.status-offline {
  background-color: #9e9e9e;
}

.product-info {
  padding: 1.5rem;
}

.product-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.5rem;
}

.product-title {
  font-size: 1.125rem;
  color: #333;
  margin: 0;
  flex: 1;
}

.product-category {
  font-size: 0.75rem;
  color: #666;
  background-color: #f5f5f5;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  margin-left: 0.5rem;
}

.product-description {
  color: #666;
  font-size: 0.875rem;
  margin-bottom: 1rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.5rem;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.product-price {
  font-size: 1.25rem;
  color: #f44336;
  font-weight: 600;
}

.product-seller {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.seller-label {
  font-size: 0.875rem;
  color: #666;
}

.seller-name {
  font-size: 0.875rem;
  color: #333;
  font-weight: 500;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-time {
  font-size: 0.75rem;
  color: #999;
}

.product-actions {
  display: flex;
  gap: 0.5rem;
}

.view-btn, .cart-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: background-color 0.2s;
}

.view-btn {
  background-color: #4CAF50;
  color: white;
}

.view-btn:hover {
  background-color: #45a049;
}

.cart-btn {
  background-color: #2196F3;
  color: white;
}

.cart-btn:hover {
  background-color: #1976D2;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
}

.pagination-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  background-color: #f5f5f5;
  border-color: #4CAF50;
  color: #4CAF50;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  color: #666;
}
</style>