<template>
  <div class="activities-page">
    <header class="page-header">
      <h1 class="page-title">校园活动</h1>
    </header>

    <div class="activity-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-btn"
        :class="{ active: activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <main class="activity-list">
      <div v-if="loading" class="loading-state">
        <div v-for="i in 4" :key="i" class="skeleton-card">
          <div class="skeleton-banner"></div>
          <div class="skeleton-body">
            <div class="skeleton-line short"></div>
            <div class="skeleton-line medium"></div>
            <div class="skeleton-line long"></div>
          </div>
        </div>
      </div>

      <div v-else-if="error" class="error-state">
        <div class="error-icon">😔</div>
        <p class="error-text">{{ error }}</p>
        <button @click="loadActivities" class="retry-btn">点击重试</button>
      </div>

      <div v-else-if="activities.length === 0" class="empty-state">
        <div class="empty-icon">📅</div>
        <h3 class="empty-title">{{ emptyTitle }}</h3>
        <p class="empty-desc">{{ emptyDesc }}</p>
      </div>

      <div v-else class="activity-grid">
        <ActivityCard
          v-for="activity in activities"
          :key="activity.id"
          :activity="activity"
          @click="goToDetail"
        />
      </div>

      <div v-if="!loading && hasMore && activities.length > 0" class="load-more">
        <button @click="loadMore" :disabled="loadingMore" class="load-more-btn">
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </button>
      </div>

      <div v-if="!hasMore && activities.length > 0" class="no-more">
        <span class="no-more-line"></span>
        <span class="no-more-text">已经到底啦~</span>
        <span class="no-more-line"></span>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '../services/api'
import ActivityCard from '../components/ActivityCard.vue'

const router = useRouter()

const tabs = [
  { label: '即将开始', value: 'upcoming' },
  { label: '进行中', value: 'ongoing' },
  { label: '已结束', value: 'past' }
]

const activeTab = ref('upcoming')
const activities = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const error = ref(null)
const page = ref(1)
const hasMore = ref(true)
const pageSize = 10

const emptyTitle = computed(() => {
  const map = { upcoming: '暂无即将开始的活动', ongoing: '当前没有进行中的活动', past: '暂无已结束的活动' }
  return map[activeTab.value] || '暂无活动'
})

const emptyDesc = computed(() => {
  const map = { upcoming: '敬请期待更多精彩活动', ongoing: '活动结束后会出现在这里', past: '' }
  return map[activeTab.value] || ''
})

onMounted(() => {
  loadActivities()
})

watch(activeTab, () => {
  page.value = 1
  hasMore.value = true
  activities.value = []
  loadActivities()
})

async function loadActivities(isLoadMore = false) {
  try {
    error.value = null
    if (isLoadMore) {
      loadingMore.value = true
    } else {
      loading.value = true
    }

    const params = {
      page: page.value,
      size: pageSize,
      status: activeTab.value
    }

    const response = await activityApi.getActivities(params)

    if (response.code === 200) {
      const data = response.data || {}
      const newItems = data.list || data.records || data.items || []

      if (isLoadMore) {
        activities.value = [...activities.value, ...newItems]
      } else {
        activities.value = newItems
      }

      const total = data.total || 0
      hasMore.value = activities.value.length < total
    } else {
      throw new Error(response.message || '加载活动失败')
    }
  } catch (err) {
    console.error('加载活动失败:', err)
    activities.value = generateMockActivities()
    hasMore.value = false
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  if (!hasMore.value || loadingMore.value) return
  page.value++
  loadActivities(true)
}

function switchTab(tab) {
  activeTab.value = tab
}

function goToDetail(activityId) {
  router.push(`/activities/${activityId}`)
}

function generateMockActivities() {
  const mockData = [
    {
      id: 1,
      title: '校园春季运动会开幕式',
      coverImage: '',
      organizerName: '校学生会体育部',
      startTime: new Date(Date.now() + 86400000 * 3).toISOString(),
      location: '北区体育场',
      participantCount: 256,
      categoryName: '体育竞技',
      status: 'upcoming'
    },
    {
      id: 2,
      title: 'ACM编程竞赛校内选拔赛',
      coverImage: '',
      organizerName: '计算机科学与技术协会',
      startTime: new Date(Date.now() + 86400000 * 7).toISOString(),
      location: '工科楼A201',
      participantCount: 89,
      categoryName: '学术竞赛',
      status: 'upcoming'
    },
    {
      id: 3,
      title: '校园音乐节 - 青春之声',
      coverImage: '',
      organizerName: '音乐社',
      startTime: new Date(Date.now() + 86400000 * 14).toISOString(),
      location: '大学生活动中心',
      participantCount: 420,
      categoryName: '文化艺术',
      status: 'upcoming'
    },
    {
      id: 4,
      title: '毕业生求职经验分享会',
      coverImage: '',
      organizerName: '就业指导中心',
      startTime: new Date(Date.now() - 86400000 * 1).toISOString(),
      location: '图书馆报告厅',
      participantCount: 156,
      categoryName: '职业发展',
      status: 'ongoing'
    },
    {
      id: 5,
      title: '周末读书分享会',
      coverImage: '',
      organizerName: '读书社',
      startTime: new Date(Date.now() + 86400000 * 2).toISOString(),
      location: '文学院咖啡厅',
      participantCount: 45,
      categoryName: '文化艺术',
      status: 'upcoming'
    },
    {
      id: 6,
      title: '荧光夜跑公益挑战赛',
      coverImage: '',
      organizerName: '青年志愿者协会',
      startTime: new Date(Date.now() - 86400000 * 10).toISOString(),
      location: '田径场',
      participantCount: 512,
      categoryName: '公益志愿',
      status: 'past'
    }
  ]

  return mockData.filter(item => item.status === activeTab.value)
}
</script>

<style scoped>
.activities-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  padding: 0 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  height: 48px;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.activity-tabs {
  display: flex;
  gap: 0;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.tab-btn {
  flex: 1;
  padding: 12px 0;
  border: none;
  background: none;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: color 0.25s ease;
}

.tab-btn.active {
  color: #FF6A00;
  font-weight: 700;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: #FF6A00;
  border-radius: 2px;
}

.activity-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
}

.loading-state {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.skeleton-banner {
  width: 100%;
  aspect-ratio: 16 / 9;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-body {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-line {
  height: 16px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

.skeleton-line.short { width: 50%; }
.skeleton-line.medium { width: 75%; }
.skeleton-line.long { width: 90%; }

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
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-title {
  font-size: 17px;
  font-weight: 600;
  color: #666;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #999;
}

.load-more {
  text-align: center;
  padding: 8px 0 24px;
}

.load-more-btn {
  background: #f5f5f5;
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
  padding: 8px 0 32px;
  color: #ccc;
  font-size: 13px;
}

.no-more-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(to right, transparent, #e0e0e0, transparent);
}
</style>
