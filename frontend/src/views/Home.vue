<template>
  <div class="home">
    <NavBar />
    
    <main class="main-content">
      <div class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">校园二手交易平台</h1>
          <p class="hero-description">买卖闲置物品，让校园生活更环保、更经济</p>
          <div class="hero-actions">
            <router-link v-if="!isAuthenticated" to="/register" class="primary-btn">立即注册</router-link>
            <router-link to="/products" class="secondary-btn">浏览商品</router-link>
          </div>
        </div>
      </div>
      
      <div class="container">
        <section class="features-section">
          <h2 class="section-title">平台特色</h2>
          <div class="features-grid">
            <div class="feature-card">
              <div class="feature-icon">💰</div>
              <h3 class="feature-title">经济实惠</h3>
              <p class="feature-description">二手物品价格实惠，帮助学生节省开支</p>
            </div>
            <div class="feature-card">
              <div class="feature-icon">🌱</div>
              <h3 class="feature-title">环保循环</h3>
              <p class="feature-description">促进物品循环利用，减少资源浪费</p>
            </div>
            <div class="feature-card">
              <div class="feature-icon">🔒</div>
              <h3 class="feature-title">安全可靠</h3>
              <p class="feature-description">实名认证，交易安全有保障</p>
            </div>
            <div class="feature-card">
              <div class="feature-icon">🚚</div>
              <h3 class="feature-title">便捷交易</h3>
              <p class="feature-description">校内交易，方便快捷</p>
            </div>
          </div>
        </section>
        
        <section class="products-section">
          <div class="section-header">
            <h2 class="section-title">热门商品</h2>
            <router-link to="/products" class="view-all-link">查看全部 →</router-link>
          </div>
          
          <div v-if="loading" class="loading">加载中...</div>
          <div v-else-if="products.length === 0" class="empty-state">
            <p>暂无商品，快去发布你的闲置物品吧！</p>
          </div>
          <div v-else class="products-grid">
            <div v-for="product in products" :key="product.id" class="product-card">
              <div class="product-image">
                <img :src="product.imageUrl || '/placeholder-image.jpg'" :alt="product.name" />
              </div>
              <div class="product-info">
                <h3 class="product-title">{{ product.name }}</h3>
                <p class="product-description">{{ product.description }}</p>
                <div class="product-meta">
                  <span class="product-price">¥{{ product.price }}</span>
                  <span class="product-status">{{ getStatusText(product.status) }}</span>
                </div>
                <div class="product-actions">
                  <button @click="viewProduct(product.id)" class="view-btn">查看详情</button>
                  <button v-if="isAuthenticated" @click="addToCart(product)" class="cart-btn">加入购物车</button>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>
    
    <footer class="footer">
      <div class="container">
        <p>© 2024 校园二手交易平台. 版权所有.</p>
        <p>联系我们: campus-market@example.com | 电话: 123-456-7890</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import NavBar from '../components/NavBar.vue'
import { productApi } from '../services/api'

const router = useRouter()
const authStore = useAuthStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)
const products = ref([])
const loading = ref(true)

onMounted(async () => {
  await loadProducts()
})

async function loadProducts() {
  try {
    loading.value = true
    const response = await productApi.getProducts({ page: 1, size: 8 })
    if (response.code === 200) {
      products.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载商品失败:', error)
  } finally {
    loading.value = false
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

function viewProduct(productId) {
  router.push(`/products/${productId}`)
}

function addToCart(product) {
  // 这里可以添加购物车逻辑
  alert(`已将 ${product.name} 加入购物车`)
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}

.hero-section {
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  padding: 4rem 1rem;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.hero-description {
  font-size: 1.25rem;
  margin-bottom: 2rem;
  opacity: 0.9;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.primary-btn, .secondary-btn {
  padding: 0.75rem 2rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s;
}

.primary-btn {
  background-color: white;
  color: #4CAF50;
}

.primary-btn:hover {
  background-color: #f5f5f5;
  transform: translateY(-2px);
}

.secondary-btn {
  background-color: transparent;
  color: white;
  border: 2px solid white;
}

.secondary-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.features-section {
  padding: 4rem 0;
}

.section-title {
  font-size: 2rem;
  color: #333;
  text-align: center;
  margin-bottom: 3rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.feature-card:hover {
  transform: translateY(-4px);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-title {
  font-size: 1.25rem;
  color: #333;
  margin-bottom: 0.5rem;
}

.feature-description {
  color: #666;
  line-height: 1.6;
}

.products-section {
  padding: 4rem 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.view-all-link {
  color: #4CAF50;
  text-decoration: none;
  font-weight: 500;
}

.view-all-link:hover {
  text-decoration: underline;
}

.loading, .empty-state {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
}

.product-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.product-card:hover {
  transform: translateY(-4px);
}

.product-image {
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 1.5rem;
}

.product-title {
  font-size: 1.125rem;
  color: #333;
  margin-bottom: 0.5rem;
}

.product-description {
  color: #666;
  font-size: 0.875rem;
  margin-bottom: 1rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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

.product-status {
  font-size: 0.875rem;
  color: #666;
  padding: 0.25rem 0.5rem;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.product-actions {
  display: flex;
  gap: 0.5rem;
}

.view-btn, .cart-btn {
  flex: 1;
  padding: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
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

.footer {
  background-color: #333;
  color: white;
  padding: 2rem 0;
  text-align: center;
}

.footer p {
  margin: 0.5rem 0;
  color: #ccc;
}
</style>