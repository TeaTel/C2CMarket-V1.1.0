<template>
  <div class="orders">
    <NavBar />
    
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1 class="page-title">我的订单</h1>
          <div class="order-tabs">
            <button
              @click="activeTab = 'buyer'"
              :class="{ active: activeTab === 'buyer' }"
              class="tab-btn"
            >
              我购买的
            </button>
            <button
              @click="activeTab = 'seller'"
              :class="{ active: activeTab === 'seller' }"
              class="tab-btn"
            >
              我售出的
            </button>
          </div>
        </div>
        
        <div v-if="loading" class="loading">加载中...</div>
        
        <div v-else-if="orders.length === 0" class="empty-state">
          <div class="empty-icon">📦</div>
          <h3>暂无订单</h3>
          <p v-if="activeTab === 'buyer'">您还没有购买任何商品</p>
          <p v-else>您还没有售出任何商品</p>
          <router-link v-if="activeTab === 'buyer'" to="/products" class="empty-action-btn">
            去逛逛
          </router-link>
        </div>
        
        <div v-else class="orders-list">
          <div v-for="order in orders" :key="order.id" class="order-card">
            <div class="order-header">
              <div class="order-info">
                <span class="order-id">订单号: {{ order.orderNumber || order.id }}</span>
                <span class="order-time">{{ formatTime(order.createdAt) }}</span>
              </div>
              <div class="order-status" :class="getStatusClass(order.status)">
                {{ getStatusText(order.status) }}
              </div>
            </div>
            
            <div class="order-items">
              <div v-for="item in order.items" :key="item.id" class="order-item">
                <div class="item-image">
                  <img :src="item.productImage || '/placeholder-image.jpg'" :alt="item.productName" />
                </div>
                <div class="item-info">
                  <h4 class="item-name">{{ item.productName }}</h4>
                  <div class="item-meta">
                    <span class="item-price">¥{{ item.price }}</span>
                    <span class="item-quantity">×{{ item.quantity }}</span>
                  </div>
                </div>
                <div class="item-total">¥{{ item.price * item.quantity }}</div>
              </div>
            </div>
            
            <div class="order-footer">
              <div class="order-total">
                合计: <span class="total-amount">¥{{ order.totalAmount }}</span>
              </div>
              <div class="order-actions">
                <button
                  v-if="activeTab === 'buyer' && order.status === 'pending'"
                  @click="handleCancelOrder(order.id)"
                  class="action-btn cancel-btn"
                >
                  取消订单
                </button>
                <button
                  v-if="activeTab === 'buyer' && order.status === 'shipped'"
                  @click="handleConfirmOrder(order.id)"
                  class="action-btn confirm-btn"
                >
                  确认收货
                </button>
                <button
                  v-if="activeTab === 'seller' && order.status === 'pending'"
                  @click="handleAcceptOrder(order.id)"
                  class="action-btn accept-btn"
                >
                  接受订单
                </button>
                <button
                  v-if="activeTab === 'seller' && order.status === 'pending'"
                  @click="handleRejectOrder(order.id)"
                  class="action-btn reject-btn"
                >
                  拒绝订单
                </button>
                <button
                  v-if="activeTab === 'seller' && order.status === 'accepted'"
                  @click="handleShipOrder(order.id)"
                  class="action-btn ship-btn"
                >
                  发货
                </button>
                <button
                  @click="viewOrderDetail(order.id)"
                  class="action-btn detail-btn"
                >
                  查看详情
                </button>
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
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import NavBar from '../components/NavBar.vue'
import { orderApi } from '../services/api'

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('buyer')
const orders = ref([])
const loading = ref(true)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

watch(activeTab, () => {
  currentPage.value = 1
  loadOrders()
})

onMounted(async () => {
  await loadOrders()
})

async function loadOrders() {
  try {
    loading.value = true
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    let response
    if (activeTab.value === 'buyer') {
      response = await orderApi.getBuyerOrders(params)
    } else {
      response = await orderApi.getSellerOrders(params)
    }
    
    if (response.code === 200) {
      orders.value = response.data.records || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    console.error('加载订单失败:', error)
  } finally {
    loading.value = false
  }
}

function getStatusText(status) {
  const statusMap = {
    pending: '待处理',
    accepted: '已接受',
    rejected: '已拒绝',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || status
}

function getStatusClass(status) {
  const classMap = {
    pending: 'status-pending',
    accepted: 'status-accepted',
    rejected: 'status-rejected',
    shipped: 'status-shipped',
    completed: 'status-completed',
    cancelled: 'status-cancelled'
  }
  return classMap[status] || ''
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN')
}

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  loadOrders()
}

function viewOrderDetail(orderId) {
  router.push(`/orders/${orderId}`)
}

async function handleCancelOrder(orderId) {
  if (!confirm('确定要取消这个订单吗？')) {
    return
  }
  
  try {
    const response = await orderApi.cancelOrder(orderId)
    if (response.code === 200) {
      alert('订单已取消')
      loadOrders()
    } else {
      alert(response.message || '取消订单失败')
    }
  } catch (error) {
    console.error('取消订单失败:', error)
    alert('取消订单失败，请稍后重试')
  }
}

async function handleConfirmOrder(orderId) {
  if (!confirm('确认收到商品了吗？')) {
    return
  }
  
  try {
    const response = await orderApi.confirmOrder(orderId)
    if (response.code === 200) {
      alert('订单已完成')
      loadOrders()
    } else {
      alert(response.message || '确认收货失败')
    }
  } catch (error) {
    console.error('确认收货失败:', error)
    alert('确认收货失败，请稍后重试')
  }
}

async function handleAcceptOrder(orderId) {
  try {
    const response = await orderApi.updateOrderStatus(orderId, 'accepted')
    if (response.code === 200) {
      alert('订单已接受')
      loadOrders()
    } else {
      alert(response.message || '接受订单失败')
    }
  } catch (error) {
    console.error('接受订单失败:', error)
    alert('接受订单失败，请稍后重试')
  }
}

async function handleRejectOrder(orderId) {
  if (!confirm('确定要拒绝这个订单吗？')) {
    return
  }
  
  try {
    const response = await orderApi.updateOrderStatus(orderId, 'rejected')
    if (response.code === 200) {
      alert('订单已拒绝')
      loadOrders()
    } else {
      alert(response.message || '拒绝订单失败')
    }
  } catch (error) {
    console.error('拒绝订单失败:', error)
    alert('拒绝订单失败，请稍后重试')
  }
}

async function handleShipOrder(orderId) {
  try {
    const response = await orderApi.updateOrderStatus(orderId, 'shipped')
    if (response.code === 200) {
      alert('订单已发货')
      loadOrders()
    } else {
      alert(response.message || '发货失败')
    }
  } catch (error) {
    console.error('发货失败:', error)
    alert('发货失败，请稍后重试')
  }
}
</script>

<style scoped>
.orders {
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
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  color: #333;
  margin-bottom: 1rem;
}

.order-tabs {
  display: flex;
  gap: 0.5rem;
  border-bottom: 1px solid #ddd;
}

.tab-btn {
  padding: 0.75rem 1.5rem;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  font-size: 1rem;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  color: #333;
}

.tab-btn.active {
  color: #4CAF50;
  border-bottom-color: #4CAF50;
  font-weight: 500;
}

.loading {
  text-align: center;
  padding: 3rem;
  font-size: 1.2rem;
  color: #666;
}

.empty-state {
  text-align: center;
  padding: 3rem;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: #666;
  margin-bottom: 1.5rem;
}

.empty-action-btn {
  display: inline-block;
  background-color: #4CAF50;
  color: white;
  text-decoration: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  font-weight: 500;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.order-card {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.order-id {
  font-size: 0.9rem;
  color: #666;
}

.order-time {
  font-size: 0.9rem;
  color: #999;
}

.order-status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-pending {
  background-color: #ff9800;
  color: white;
}

.status-accepted {
  background-color: #2196F3;
  color: white;
}

.status-rejected {
  background-color: #f44336;
  color: white;
}

.status-shipped {
  background-color: #9c27b0;
  color: white;
}

.status-completed {
  background-color: #4CAF50;
  color: white;
}

.status-cancelled {
  background-color: #9e9e9e;
  color: white;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1rem;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #f9f9f9;
  border-radius: 4px;
}

.item-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  background: #f5f5f5;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 1rem;
  color: #333;
  margin-bottom: 0.25rem;
}

.item-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.9rem;
  color: #666;
}

.item-total {
  font-size: 1.1rem;
  font-weight: bold;
  color: #f44336;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.order-total {
  font-size: 1.1rem;
  color: #333;
}

.total-amount {
  font-size: 1.3rem;
  font-weight: bold;
  color: #f44336;
}

.order-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn {
  background-color: #ff9800;
  color: white;
}

.cancel-btn:hover {
  background-color: #f57c00;
}

.confirm-btn {
  background-color: #4CAF50;
  color: white;
}

.confirm-btn:hover {
  background-color: #45a049;
}

.accept-btn {
  background-color: #2196F3;
  color: white;
}

.accept-btn:hover {
  background-color: #1976D2;
}

.reject-btn {
  background-color: #f44336;
  color: white;
}

.reject-btn:hover {
  background-color: #d32f2f;
}

.ship-btn {
  background-color: #9c27b0;
  color: white;
}

.ship-btn:hover {
  background-color: #7b1fa2;
}

.detail-btn {
  background-color: #9e9e9e;
  color: white;
}

.detail-btn:hover {
  background-color: #757575;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid #eee;
}

.pagination-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
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