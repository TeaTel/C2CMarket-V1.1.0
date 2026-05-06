<template>
  <div class="post-detail-page">
    <div class="header">
      <button class="back-btn" @click="$router.back()">
        <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <span class="header-title">帖子详情</span>
      <button class="share-btn">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><path d="M8.59 13.51l6.83 3.98M15.41 6.51l-6.82 3.98"/></svg>
      </button>
    </div>

    <div v-if="loading" class="loading">
      <div class="skeleton" style="height:200px;margin-bottom:16px;border-radius:12px;"></div>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="loadPost">重试</button>
    </div>

    <div v-else-if="post" class="post-content-area">
      <div class="author-section">
        <img :src="post.userAvatar || defaultAvatar" class="author-avatar" @error="onAvatarError" />
        <div class="author-info">
          <span class="author-name" @click="goToUser(post.userId)">{{ post.userName || '匿名用户' }}</span>
          <span class="post-meta">{{ formatTime(post.createdAt) }} · {{ post.viewCount || 0 }} 阅读</span>
        </div>
        <button v-if="auth.isAuthenticated && auth.currentUser?.id !== post.userId" class="follow-btn" :class="{ following: isFollowing }" @click="toggleFollow">
          {{ isFollowing ? '已关注' : '+ 关注' }}
        </button>
      </div>

      <h1 class="post-title">{{ post.title }}</h1>
      <div class="post-body" v-html="renderedContent"></div>

      <div class="post-actions-bar">
        <LikeButton :is-liked="post.isLiked" :count="post.likeCount" target-type="POST" :target-id="post.id" @toggled="onLikeToggled" />
        <div class="action-item">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
          <span>评论</span>
        </div>
      </div>

      <div class="divider"></div>

      <CommentSection :post-id="post.id" :initial-comments="[]" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { postApi, followApi } from '../services/api'
import { useAuthStore } from '../store/auth'
import LikeButton from '../components/LikeButton.vue'
import CommentSection from '../components/CommentSection.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const post = ref(null)
const loading = ref(true)
const error = ref(null)
const isFollowing = ref(false)

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#eee"/><circle cx="20" cy="15" r="8" fill="#ccc"/><ellipse cx="20" cy="35" rx="12" ry="8" fill="#ccc"/></svg>')

const renderedContent = computed(() => {
  if (!post.value?.content) return ''
  return post.value.content.replace(/\n/g, '<br>')
})

onMounted(() => { loadPost() })

async function loadPost() {
  loading.value = true
  error.value = null
  try {
    const id = route.params.id
    const res = await postApi.getPostDetail(id)
    if (res.code === 200) {
      post.value = res.data
      if (post.value.userId && auth.isAuthenticated) {
        checkFollowStatus(post.value.userId)
      }
    } else {
      error.value = res.message || '加载失败'
    }
  } catch (e) {
    error.value = '加载失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

async function checkFollowStatus(userId) {
  try {
    const res = await followApi.checkFollowing(userId)
    if (res.code === 200) isFollowing.value = res.data.isFollowing
  } catch (e) {}
}

async function toggleFollow() {
  if (!post.value) return
  try {
    const res = await followApi.toggleFollow(post.value.userId)
    if (res.code === 200) isFollowing.value = res.data.isFollowing
  } catch (e) {}
}

function onLikeToggled(isLiked, count) {
  if (post.value) {
    post.value.isLiked = isLiked
    post.value.likeCount = count
  }
}

function goToUser(userId) { router.push(`/users/${userId}`) }
function onAvatarError(e) { e.target.src = defaultAvatar }

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return date.toLocaleDateString()
}
</script>

<style scoped>
.post-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
}
.header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 10;
}
.back-btn, .share-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  display: flex;
  color: #333;
}
.header-title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
}
.post-content-area {
  padding: 16px;
  max-width: 750px;
  margin: 0 auto;
}
.author-section {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 16px;
}
.author-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  background: #eee;
}
.author-info { flex: 1; }
.author-name { font-size: 15px; font-weight: 600; color: #333; cursor: pointer; }
.post-meta { font-size: 12px; color: #999; display: block; }
.follow-btn {
  padding: 6px 16px;
  border-radius: 16px;
  border: 1px solid #ff6a00;
  background: #fff;
  color: #ff6a00;
  font-size: 13px;
  cursor: pointer;
}
.follow-btn.following { background: #f0f0f0; border-color: #ccc; color: #999; }
.post-title {
  font-size: 20px;
  font-weight: 700;
  color: #222;
  margin-bottom: 12px;
  line-height: 1.4;
}
.post-body {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  font-size: 15px;
  line-height: 1.8;
  color: #444;
  margin-bottom: 16px;
}
.post-actions-bar {
  display: flex;
  gap: 24px;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 12px 16px;
  margin-bottom: 16px;
}
.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
}
.divider { height: 1px; background: #f0f0f0; margin: 8px 0 16px; }
.loading, .error { padding: 60px 20px; text-align: center; }
.error p { color: #999; margin-bottom: 12px; }
.error button {
  padding: 8px 24px; border: none; background: #ff6a00;
  color: #fff; border-radius: 20px; cursor: pointer;
}
.skeleton { background: #f0f0f0; animation: shimmer 1.5s infinite; }
@keyframes shimmer {
  0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; }
}
</style>
