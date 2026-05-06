<template>
  <div class="community-page">
    <div class="top-tabs">
      <button :class="{ active: tab === 'feed' }" @click="tab = 'feed'">推荐</button>
      <button :class="{ active: tab === 'posts' }" @click="tab = 'posts'">帖子</button>
      <button :class="{ active: tab === 'hot' }" @click="tab = 'hot'">热门</button>
    </div>

    <div class="content-area">
      <div v-if="loading" class="loading-skeletons">
        <div class="skeleton" v-for="i in 5" :key="i" style="height:120px;margin-bottom:12px;border-radius:12px;background:#f0f0f0;"></div>
      </div>

      <div v-else-if="error" class="error-state">
        <p>{{ error }}</p>
        <button @click="loadFeed">重试</button>
      </div>

      <div v-else-if="items.length === 0" class="empty-state">
        <p>暂无内容，快来发布第一帖吧！</p>
      </div>

      <div v-else class="feed-list">
        <PostCard
          v-for="item in items.filter(i => i.itemType === 'POST')"
          :key="'post-' + item.id"
          :post="item"
          @click="goToPost(item.id)"
        />
        <div
          v-for="item in items.filter(i => i.itemType === 'PRODUCT')"
          :key="'product-' + item.id"
          class="product-item"
          @click="goToProduct(item.id)"
        >
          <img v-if="item.coverImage" :src="item.coverImage" class="product-image" />
          <div class="product-info">
            <h4>{{ item.title }}</h4>
            <p class="product-desc">{{ truncate(item.content, 60) }}</p>
            <div class="product-meta">
              <span class="price">¥{{ item.price }}</span>
              <span class="location">{{ item.location }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="hasMore && !loading" class="load-more">
        <button @click="loadMore" :disabled="loadingMore">
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </div>

    <button class="fab" @click="goToCreatePost">+</button>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { feedApi, postApi } from '../services/api'
import PostCard from '../components/PostCard.vue'

const router = useRouter()
const tab = ref('feed')
const items = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const error = ref(null)
const page = ref(1)
const hasMore = ref(true)

onMounted(() => loadFeed())

watch(tab, () => {
  page.value = 1
  items.value = []
  loadFeed()
})

async function loadFeed() {
  loading.value = true
  error.value = null
  try {
    const sortMap = { feed: 'time_desc', posts: 'time_desc', hot: 'hot' }
    if (tab.value === 'posts') {
      const res = await postApi.getPostList({ page: page.value, size: 10, sortBy: sortMap[tab.value] })
      if (res.code === 200) {
        const list = res.data.list || []
        const postItems = list.map(p => ({ ...p, itemType: 'POST', postTypeText: p.postTypeText }))
        items.value = postItems
        hasMore.value = list.length >= 10
      }
    } else {
      const res = await feedApi.getFeed({ page: page.value, size: 12 })
      if (res.code === 200) {
        const list = res.data.list || []
        if (tab.value === 'hot') {
          list.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
        }
        items.value = list
        hasMore.value = list.length >= 12
      }
    }
  } catch (e) {
    error.value = '加载失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  loadingMore.value = true
  page.value++
  try {
    const sortMap = { feed: 'time_desc', posts: 'time_desc', hot: 'hot' }
    if (tab.value === 'posts') {
      const res = await postApi.getPostList({ page: page.value, size: 10, sortBy: sortMap[tab.value] })
      if (res.code === 200) {
        const list = res.data.list || []
        items.value = [...items.value, ...list.map(p => ({ ...p, itemType: 'POST' }))]
        hasMore.value = list.length >= 10
      }
    } else {
      const res = await feedApi.getFeed({ page: page.value, size: 12 })
      if (res.code === 200) {
        const list = res.data.list || []
        items.value = [...items.value, ...list]
        hasMore.value = list.length >= 12
      }
    }
  } catch (e) {
    page.value--
  } finally {
    loadingMore.value = false
  }
}

function goToPost(id) { router.push(`/community/posts/${id}`) }
function goToProduct(id) { router.push(`/products/${id}`) }
function goToCreatePost() { router.push('/community/posts/create') }

function truncate(str, len) {
  if (!str) return ''
  return str.length > len ? str.substring(0, len) + '...' : str
}
</script>

<style scoped>
.community-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 70px;
}
.top-tabs {
  display: flex;
  background: #fff;
  padding: 0 16px;
  gap: 24px;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 10;
}
.top-tabs button {
  padding: 12px 4px;
  border: none;
  background: none;
  font-size: 15px;
  color: #999;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.top-tabs button.active {
  color: #ff6a00;
  border-bottom-color: #ff6a00;
  font-weight: 600;
}
.content-area {
  padding: 12px 16px;
}
.loading-skeletons, .error-state, .empty-state {
  padding: 40px 0;
  text-align: center;
}
.error-state p { color: #999; margin-bottom: 12px; }
.error-state button {
  padding: 8px 24px;
  border: none;
  background: #ff6a00;
  color: #fff;
  border-radius: 20px;
  cursor: pointer;
}
.empty-state p { color: #999; font-size: 14px; }

.product-item {
  display: flex;
  gap: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  background: #eee;
  flex-shrink: 0;
}
.product-info { flex: 1; display: flex; flex-direction: column; }
.product-info h4 { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 4px; }
.product-desc { font-size: 13px; color: #999; margin-bottom: 8px; }
.product-meta { display: flex; gap: 12px; align-items: center; margin-top: auto; }
.price { font-size: 16px; font-weight: 700; color: #ff6a00; }
.location { font-size: 12px; color: #999; }

.fab {
  position: fixed;
  bottom: 100px;
  right: 20px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #ff6a00, #ff9500);
  color: #fff;
  font-size: 28px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(255,106,0,0.4);
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: center;
}
.load-more { text-align: center; padding: 16px 0; }
.load-more button {
  padding: 10px 32px;
  border: 1px solid #ff6a00;
  background: #fff;
  color: #ff6a00;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
}
.load-more button:disabled { opacity: 0.5; }
</style>
