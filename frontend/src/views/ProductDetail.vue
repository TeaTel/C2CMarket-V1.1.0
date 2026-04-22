<template>
  <div class="product-detail">
    <NavBar />
    
    <main class="main-content">
      <div class="container">
        <div v-if="loading" class="loading">加载中...</div>
        
        <div v-else-if="!product" class="error-state">
          <div class="error-icon">❌</div>
          <h3>商品不存在</h3>
          <p>抱歉，您要查看的商品不存在或已被删除</p>
          <router-link to="/products" class="back-btn">返回商品列表</router-link>
        </div>
        
        <div v-else class="product-detail-content">
          <!-- 面包屑导航 -->
          <div class="breadcrumb">
            <router-link to="/">首页</router-link>
            <span class="separator">/</span>
            <router-link to="/products">商品列表</router-link>
            <span class="separator">/</span>
            <span class="current">{{ product.name }}</span>
          </div>
          
          <div class="product-main">
            <!-- 商品图片 -->
            <div class="product-gallery">
              <div class="main-image">
                <img :src="product.imageUrl || '/placeholder-image.jpg'" :alt="product.name" />
              </div>
            </div>
            
            <!-- 商品信息 -->
            <div class="product-info">
              <div class="product-header">
                <div class="product-status-badge" :class="getStatusClass(product.status)">
                  {{ getStatusText(product.status) }}
                </div>
                <h1 class="product-title">{{ product.name }}</h1>
                <div class="product-price">¥{{ product.price }}</div>
              </div>
              
              <div class="product-meta">
                <div class="meta-item">
                  <span class="meta-label">分类:</span>
                  <span class="meta-value">{{ getCategoryName(product.categoryId) }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">卖家:</span>
                  <span class="meta-value">{{ product.sellerName || '匿名' }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">发布时间:</span>
                  <span class="meta-value">{{ formatTime(product.createdAt) }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">浏览量:</span>
                  <span class="meta-value">{{ product.viewCount || 0 }}</span>
                </div>
              </div>
              
              <div class="product-description">
                <h3>商品描述</h3>
                <p>{{ product.description }}</p>
              </div>
              
              <div class="product-actions">
                <button
                  v-if="isAuthenticated && product.status === 1 && !isOwner"
                  @click="handleContactSeller"
                  class="contact-btn"
                >
                  联系卖家
                </button>
                <button
                  v-if="isAuthenticated && product.status === 1 && !isOwner"
                  @click="handleBuyNow"
                  class="buy-btn"
                >
                  立即购买
                </button>
                <button
                  v-if="isOwner"
                  @click="handleEditProduct"
                  class="edit-btn"
                >
                  编辑商品
                </button>
                <button
                  v-if="isOwner"
                  @click="handleDeleteProduct"
                  class="delete-btn"
                >
                  删除商品
                </button>
              </div>
            </div>
          </div>
          
          <!-- 卖家信息 -->
          <div class="seller-section">
            <h3>卖家信息</h3>
            <div class="seller-card">
              <div class="seller-avatar">
                <img :src="sellerInfo.avatar || '/default-avatar.png'" :alt="sellerInfo.username" />
              </div>
              <div class="seller-details">
                <h4>{{ sellerInfo.username }}</h4>
                <div class="seller-meta">
                  <span class="meta-item">
                    <span class="meta-label">学校:</span>
                    <span class="meta-value">{{ sellerInfo.school || '未填写' }}</span>
                  </span>
                  <span class="meta-item">
                    <span class="meta-label">专业:</span>
                    <span class="meta-value">{{ sellerInfo.major || '未填写' }}</span>
                  </span>
                  <span class="meta-item">
                    <span class="meta-label">身份:</span>
                    <span class="meta-value">{{ sellerInfo.isStudent ? '学生' : '教师' }}</span>
                  </span>
                </div>
                <div class="seller-stats">
                  <div class="stat-item">
                    <span class="stat-value">{{ sellerStats.totalProducts || 0 }}</span>
                    <span class="stat-label">在售商品</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ sellerStats.totalSales || 0 }}</span>
                    <span class="stat-label">成交记录</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ sellerStats.rating || '暂无' }}</span>
                    <span class="stat-label">卖家评分</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import NavBar from '../components/NavBar.vue'
import { productApi } from '../services/api'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const product = ref(null)
const loading = ref(true)
const sellerInfo = ref({})
const sellerStats = ref({})

const isAuthenticated = computed(() => authStore.isAuthenticated)
const currentUser = computed(() => authStore.currentUser)
const isOwner = computed(() => {
  return product.value && currentUser.value && product.value.sellerId === currentUser.value.id
})

onMounted(async () => {
  await loadProductDetail()
})

async function loadProductDetail() {
  try {
    loading.value = true
    const productId = route.params.id
    
    // 验证商品ID是否为有效的数字
    if (!productId || isNaN(parseInt(productId))) {
      console.error('无效的商品ID:', productId)
      product.value = null
      return
    }
    
    const response = await productApi.getProductDetail(productId)
    if (response.code === 200 && response.data) {
      product.value = response.data
      await loadSellerInfo(response.data.sellerId)
    }
  } catch (error) {
    console.error('加载商品详情失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadSellerInfo(sellerId) {
  try {
    // 这里可以调用用户API获取卖家信息
    // 暂时使用模拟数据
    sellerInfo.value = {
      id: sellerId,
      username: '卖家用户',
      avatar: '/default-avatar.png',
      school: '清华大学',
      major: '计算机科学',
      isStudent: true
    }
    
    sellerStats.value = {
      totalProducts: 5,
      totalSales: 12,
      rating: '4.8'
    }
  } catch (error) {
    console.error('加载卖家信息失败:', error)
  }
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
  // 这里可以调用分类API获取分类名称
  const categoryMap = {
    1: '书籍教材',
    2: '电子产品',
    3: '生活用品',
    4: '服装鞋帽',
    5: '运动器材',
    6: '其他'
  }
  return categoryMap[categoryId] || '未分类'
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN')
}

function handleContactSeller() {
  if (!isAuthenticated.value) {
    router.push('/login')
    return
  }
  
  // 跳转到消息页面
  router.push(`/messages?to=${product.value.sellerId}&product=${product.value.id}`)
}

function handleBuyNow() {
  if (!isAuthenticated.value) {
    router.push('/login')
    return
  }
  
  // 创建订单
  router.push(`/orders/create?product=${product.value.id}`)
}

function handleEditProduct() {
  router.push(`/products/${product.value.id}/edit`)
}

async function handleDeleteProduct() {
  if (!confirm('确定要删除这个商品吗？此操作不可撤销。')) {
    return
  }
  
  try {
    const response = await productApi.deleteProduct(product.value.id)
    if (response.code === 200) {
      alert('商品删除成功')
      router.push('/products')
    } else {
      alert(response.message || '删除失败')
    }
  } catch (error) {
    console.error('删除商品失败:', error)
    alert('删除失败，请稍后重试')
  }
}
</script>

<style scoped>
.product-detail {
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

.loading {
  text-align: center;
  padding: 3rem;
  font-size: 1.2rem;
  color: #666;
}

.error-state {
  text-align: center;
  padding: 3rem;
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.error-state h3 {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 0.5rem;
}

.error-state p {
  color: #666;
  margin-bottom: 1.5rem;
}

.back-btn {
  display: inline-block;
  background-color: #4CAF50;
  color: white;
  text-decoration: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  font-weight: 500;
}

.breadcrumb {
  margin-bottom: 2rem;
  font-size: 0.9rem;
  color: #666;
}

.breadcrumb a {
  color: #4CAF50;
  text-decoration: none;
}

.breadcrumb a:hover {
  text-decoration: underline;
}

.separator {
  margin: 0 0.5rem;
}

.current {
  color: #333;
  font-weight: 500;
}

.product-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  margin-bottom: 3rem;
}

.product-gallery {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.main-image {
  aspect-ratio: 1/1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.main-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.product-info {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.product-header {
  margin-bottom: 1.5rem;
}

.product-status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.status-pending {
  background-color: #ff9800;
  color: white;
}

.status-available {
  background-color: #4CAF50;
  color: white;
}

.status-sold {
  background-color: #f44336;
  color: white;
}

.status-offline {
  background-color: #9e9e9e;
  color: white;
}

.product-title {
  font-size: 1.8rem;
  color: #333;
  margin: 0.5rem 0;
}

.product-price {
  font-size: 2rem;
  color: #f44336;
  font-weight: bold;
  margin: 1rem 0;
}

.product-meta {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid #eee;
}

.meta-item {
  display: flex;
  flex-direction: column;
}

.meta-label {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 0.25rem;
}

.meta-value {
  font-size: 1rem;
  color: #333;
  font-weight: 500;
}

.product-description h3 {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 1rem;
}

.product-description p {
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
}

.product-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid #eee;
}

.product-actions button {
  flex: 1;
  padding: 1rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.contact-btn {
  background-color: #2196F3;
  color: white;
}

.contact-btn:hover {
  background-color: #1976D2;
}

.buy-btn {
  background-color: #f44336;
  color: white;
}

.buy-btn:hover {
  background-color: #d32f2f;
}

.edit-btn {
  background-color: #4CAF50;
  color: white;
}

.edit-btn:hover {
  background-color: #45a049;
}

.delete-btn {
  background-color: #9e9e9e;
  color: white;
}

.delete-btn:hover {
  background-color: #757575;
}

.seller-section {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.seller-section h3 {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 1.5rem;
}

.seller-card {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.seller-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  background: #f5f5f5;
}

.seller-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.seller-details {
  flex: 1;
}

.seller-details h4 {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 0.5rem;
}

.seller-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1rem;
}

.seller-meta .meta-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 0.5rem;
}

.seller-stats {
  display: flex;
  gap: 2rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 1.2rem;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
}
</style>