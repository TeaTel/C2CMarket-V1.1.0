<template>
  <div class="home-page">

    <nav class="circle-sub-nav">
      <button
        class="circle-tag"
        :class="{ active: activeTag === null && activeCampusTag === null }"
        @click="switchTag(null)"
      >
        全部
      </button>
      <template v-if="activeTab === 'campus'">
        <button
          v-for="campus in campusTags"
          :key="campus"
          class="circle-tag campus"
          :class="{ active: activeCampusTag === campus }"
          @click="switchCampusTag(campus)"
        >
          {{ campus }}
        </button>
      </template>
      <template v-else>
        <button
          v-for="tag in circleTags"
          :key="tag"
          class="circle-tag"
          :class="{ active: activeTag === tag }"
          @click="switchTag(tag)"
        >
          {{ tag }}
        </button>
      </template>
    </nav>

    <main class="feed-content">
      <div v-if="loading" class="loading-state">
        <div v-for="i in 6" :key="i" class="skeleton-card"></div>
      </div>

      <div v-else-if="error" class="error-state">
        <div class="error-icon">😔</div>
        <p class="error-text">{{ error }}</p>
        <button @click="loadFeed" class="retry-btn">点击重试</button>
      </div>

      <div v-else-if="showLoginGuide" class="login-guide">
        <div class="login-guide-card">
          <div class="login-guide-icon">🔒</div>
          <h3 class="login-guide-title">登录后可查看关注内容</h3>
          <p class="login-guide-desc">关注感兴趣的账号，获取他们的最新动态</p>
          <button @click="goToLogin" class="login-guide-btn">去登录</button>
        </div>
      </div>

      <div v-else-if="!showLoginGuide && feedItems.length === 0 && recommends.length === 0" class="empty-state">
        <div class="empty-icon">{{ emptyIcon }}</div>
        <p class="empty-text">{{ emptyText }}</p>
        <p class="empty-subtext">{{ emptySubText }}</p>
      </div>

      <template v-else>
        <section v-if="recommends.length > 0 && activeTab !== 'following'" class="recommend-section">
          <div class="section-header">
            <h3 class="section-title">✨ 猜你喜欢</h3>
            <span class="section-desc">根据你的浏览推荐</span>
          </div>
          <div class="recommend-scroll">
            <template v-for="item in recommends" :key="'rec-' + item.itemType + '-' + item.id">
              <PostCard v-if="item.itemType === 'POST'" :post="item" @click="goToPost(item)" />
              <ProductCard v-else-if="item.itemType === 'PRODUCT'" :product="item" @click="goToProduct(item.id)" />
            </template>
          </div>
        </section>

        <div class="section-divider" v-if="recommends.length > 0 && activeTab !== 'following' && feedItems.length > 0">
          <span>全部内容</span>
        </div>

        <div class="feed-list">
          <template v-for="item in feedItems" :key="item.itemType + '-' + item.id">
            <PostCard v-if="item.itemType === 'POST'" :post="item" @click="goToPost(item)" />
            <ProductCard v-else-if="item.itemType === 'PRODUCT'" :product="item" @click="goToProduct(item.id)" />
          </template>
        </div>
      </template>

      <!-- 加载更多状态 -->
      <div v-if="loadingMore" class="loading-more">
        <div class="loading-spinner"></div>
        <span class="loading-text">加载中...</span>
      </div>

      <div v-if="loadError && !loading && feedItems.length > 0" class="load-error">
        <span>加载失败</span>
        <button @click="retryLoadMore" class="retry-small-btn">点击重试</button>
      </div>

      <div v-if="!hasMore && !loadingMore && feedItems.length > 0" class="no-more">
        <span class="no-more-line"></span>
        <span class="no-more-text">已经到底啦~</span>
        <span class="no-more-line"></span>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { feedApi } from '../services/api'
import PostCard from '../components/PostCard.vue'
import ProductCard from '../components/ProductCard.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref(route.query.tab || 'discover')
const activeTag = ref(null)
const activeCampusTag = ref(null)

const circleTags = [
  '数码', '书籍', '运动', '美食', '音乐',
  '学习', '求职', '考研', '二手', '闲置',
  '分享', '经验', '求助', '摄影', '旅行'
]

const campusTags = ['南三区', '南二区', '南一区', '中区', '东区', '西区']

const feedItems = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const error = ref(null)
const loadError = ref(false)
const currentPage = ref(1)
const pageSize = 12

// 已加载的ID集合，用于前端去重
const loadedIds = ref(new Set())

const recommends = ref([])
const recLoading = ref(false)

const isAuthenticated = computed(() => authStore.isAuthenticated.value)

const showLoginGuide = computed(() => {
  return activeTab.value === 'following' && !isAuthenticated.value && !loading.value
})

const emptyIcon = computed(() => {
  const icons = { discover: '📭', following: '👥', campus: '🏫' }
  return icons[activeTab.value] || '📭'
})

const emptyText = computed(() => {
  const texts = { discover: '暂无内容', following: '暂无关注内容', campus: '暂无校区内容' }
  return texts[activeTab.value] || '暂无内容'
})

const emptySubText = computed(() => {
  const texts = { discover: '快去发布第一条内容吧！', following: '关注更多用户，获取他们的最新动态', campus: '设置你的校区，发现身边的同学' }
  return texts[activeTab.value] || ''
})

// 滚动事件处理函数引用，用于卸载时移除
let scrollHandler = null

onMounted(() => {
  loadFeed()
  loadRecommendations()
  setupScrollObserver()
})

onUnmounted(() => {
  if (scrollHandler) {
    window.removeEventListener('scroll', scrollHandler, { passive: true })
    scrollHandler = null
  }
})

function setupScrollObserver() {
  let ticking = false
  scrollHandler = () => {
    if (ticking) return
    ticking = true
    requestAnimationFrame(() => {
      ticking = false
      if (loadingMore.value || !hasMore.value || loading.value || loadError.value) return
      const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
      const scrollHeight = document.documentElement.scrollHeight
      const clientHeight = document.documentElement.clientHeight
      if (scrollTop + clientHeight >= scrollHeight - 200) {
        loadMoreItems()
      }
    })
  }
  window.addEventListener('scroll', scrollHandler, { passive: true })
}

// 前端去重：基于 itemType + id
function dedupItems(newItems) {
  const result = []
  for (const item of newItems) {
    const key = `${item.itemType}-${item.id}`
    if (!loadedIds.value.has(key)) {
      loadedIds.value.add(key)
      result.push(item)
    }
  }
  return result
}

async function loadFeed(isLoadMore = false) {
  try {
    error.value = null
    loadError.value = false
    if (isLoadMore) {
      loadingMore.value = true
    } else {
      loading.value = true
    }

    const params = {
      page: currentPage.value,
      size: pageSize,
      type: activeTab.value
    }

    if (activeTag.value) {
      params.tag = activeTag.value
    }

    if (activeCampusTag.value) {
      params.campusTag = activeCampusTag.value
    }

    const res = await feedApi.getFeed(params)

    if (res.code === 200) {
      const data = res.data || {}
      const rawItems = data.list || data.records || data.items || []
      const newItems = dedupItems(rawItems)

      if (isLoadMore) {
        feedItems.value = [...feedItems.value, ...newItems]
      } else {
        feedItems.value = newItems
      }

      // 判断是否还有更多数据
      if (rawItems.length === 0 || newItems.length === 0) {
        // 返回空列表或全部去重后无新数据，说明没有更多了
        hasMore.value = false
      } else if (rawItems.length < pageSize) {
        // 返回数量不足一页，说明是最后一页
        hasMore.value = false
      } else {
        const total = data.total || 0
        hasMore.value = feedItems.value.length < total
      }
    } else {
      throw new Error(res.message || '加载内容失败')
    }
  } catch (err) {
    console.error('加载Feed失败:', err)
    if (!isLoadMore) {
      error.value = err.message || '加载失败，请稍后重试'
    } else {
      loadError.value = true
      currentPage.value-- // 回退页码，允许重试
    }
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMoreItems() {
  if (!hasMore.value || loadingMore.value || loadError.value) return
  currentPage.value++
  loadFeed(true)
}

function retryLoadMore() {
  loadError.value = false
  loadFeed(true)
}

watch(() => route.query.tab, (newTab) => {
  if (newTab && newTab !== activeTab.value) {
    activeTab.value = newTab
    activeTag.value = null
    activeCampusTag.value = null
    currentPage.value = 1
    hasMore.value = true
    feedItems.value = []
    loadedIds.value = new Set()
    error.value = null
    loadError.value = false
    window.scrollTo({ top: 0, behavior: 'smooth' })
    loadFeed()
  }
})

function switchTag(tag) {
  if (activeTag.value === tag) return
  activeTag.value = tag
  activeCampusTag.value = null
  currentPage.value = 1
  hasMore.value = true
  feedItems.value = []
  loadedIds.value = new Set()
  error.value = null
  loadError.value = false
  window.scrollTo({ top: 0, behavior: 'smooth' })
  loadFeed()
}

function switchCampusTag(campus) {
  if (activeCampusTag.value === campus) return
  activeCampusTag.value = campus
  activeTag.value = null
  currentPage.value = 1
  hasMore.value = true
  feedItems.value = []
  loadedIds.value = new Set()
  error.value = null
  loadError.value = false
  window.scrollTo({ top: 0, behavior: 'smooth' })
  loadFeed()
}

function goToPost(item) {
  trackBehavior('POST', item.id)
  if (item.postType === 'ACTIVITY') {
    router.push(`/activities/${item.id}`)
  } else {
    router.push(`/community/posts/${item.id}`)
  }
}

function goToProduct(productId) {
  trackBehavior('PRODUCT', productId)
  router.push(`/products/${productId}`)
}

function goToLogin() {
  router.push({ path: '/login', query: { redirect: '/' } })
}

async function loadRecommendations() {
  if (!isAuthenticated.value) return
  try {
    recLoading.value = true
    const res = await feedApi.getRecommendations(6)
    if (res.code === 200) {
      recommends.value = res.data?.list || []
    }
  } catch (e) {
    // silent
  } finally {
    recLoading.value = false
  }
}

function trackBehavior(targetType, targetId) {
  if (!isAuthenticated.value) return
  feedApi.recordBehavior(targetType, targetId).catch(() => {})
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.circle-sub-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fff;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  border-bottom: 1px solid #f5f5f5;
}

.circle-sub-nav::-webkit-scrollbar {
  display: none;
}

.circle-tag {
  flex-shrink: 0;
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  background: #f5f5f5;
  color: #666;
  border: none;
  cursor: pointer;
  white-space: nowrap;
  scroll-snap-align: start;
  transition: all 0.2s ease;
}

.circle-tag.active {
  background: linear-gradient(135deg, #FF6A00, #FF8533);
  color: #fff;
  box-shadow: 0 2px 8px rgba(255, 106, 0, 0.25);
}

.circle-tag.campus.active {
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.25);
}

.circle-tag:active {
  transform: scale(0.95);
}

.feed-content {
  padding: 12px 16px;
}

.recommend-section {
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 17px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.section-desc {
  font-size: 12px;
  color: #999;
}

.recommend-scroll {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 20px 0 12px;
  color: #ccc;
  font-size: 13px;
  font-weight: 500;
}

.section-divider::before,
.section-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #E8ECF0;
}

.loading-state {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-card {
  width: 100%;
  height: 120px;
  background: #fff;
  border-radius: 12px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
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
  color: #666;
  margin-bottom: 8px;
}

.empty-subtext {
  font-size: 13px;
  color: #999;
}

.login-guide {
  display: flex;
  justify-content: center;
  padding: 40px 16px;
}

.login-guide-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  width: 100%;
  max-width: 320px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.login-guide-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.login-guide-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.login-guide-desc {
  font-size: 14px;
  color: #999;
  margin-bottom: 20px;
  line-height: 1.5;
}

.login-guide-btn {
  background: linear-gradient(135deg, #FF6A00, #FF8533);
  color: #fff;
  padding: 12px 48px;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
  border: none;
  cursor: pointer;
}

.login-guide-btn:active {
  transform: scale(0.96);
}

.feed-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 加载更多动画 */
.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 24px 0 16px;
  min-height: 60px;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2.5px solid #f0f0f0;
  border-top-color: #FF6A00;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 14px;
  color: #999;
}

/* 加载失败 */
.load-error {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px 0;
  min-height: 60px;
  color: #999;
  font-size: 14px;
}

.retry-small-btn {
  padding: 6px 16px;
  border-radius: 14px;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s;
}

.retry-small-btn:active {
  background: #f5f5f5;
}

.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px 0 32px;
  min-height: 60px;
  color: #ccc;
  font-size: 13px;
}

.no-more-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(to right, transparent, #e0e0e0, transparent);
}

@media (min-width: 769px) {
  .home-page {
    max-width: 750px;
    margin: 0 auto;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.08);
    background: #fff;
  }

  .feed-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>
