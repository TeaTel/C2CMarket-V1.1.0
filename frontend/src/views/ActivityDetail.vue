<template>
  <div class="activity-detail-page">
    <header class="detail-nav">
      <button class="nav-back" @click="$router.back()">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="15,18 9,12 15,6"/></svg>
      </button>
      <span class="nav-title">活动详情</span>
      <button class="share-btn" @click="handleShare">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
          <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
        </svg>
      </button>
    </header>

    <div v-if="loading" class="loading-state">
      <div class="skeleton-banner"></div>
      <div class="skeleton-block">
        <div class="skeleton-line w-80"></div>
        <div class="skeleton-line w-60"></div>
        <div class="skeleton-line w-40"></div>
      </div>
    </div>

    <div v-else-if="error" class="error-state">
      <span class="error-icon">😕</span>
      <p>{{ error }}</p>
      <button @click="loadActivity" class="retry-btn">重试</button>
    </div>

    <main v-else-if="activity" class="detail-content">
      <section class="banner-section">
        <img v-if="activity.coverImage" :src="activity.coverImage" class="banner-img" loading="lazy" @error="onBannerError" />
        <div v-else class="banner-placeholder">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#ccc" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18"/><path d="M9 21V9"/></svg>
        </div>
        <div class="banner-overlay">
          <span class="activity-status" :class="statusClass">{{ statusText }}</span>
        </div>
      </section>

      <section class="info-card">
        <h1 class="activity-title">{{ activity.title }}</h1>

        <div class="meta-grid">
          <div class="meta-item">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#FF6A00" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            <div>
              <span class="meta-label">活动时间</span>
              <span class="meta-value">{{ formatDateTime(activity.startTime) }}</span>
            </div>
          </div>
          <div v-if="activity.endTime" class="meta-item">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#FF6A00" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            <div>
              <span class="meta-label">结束时间</span>
              <span class="meta-value">{{ formatDateTime(activity.endTime) }}</span>
            </div>
          </div>
          <div v-if="activity.location" class="meta-item">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#FF6A00" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
            <div>
              <span class="meta-label">活动地点</span>
              <span class="meta-value">{{ activity.location }}</span>
            </div>
          </div>
          <div v-if="activity.contact" class="meta-item">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#FF6A00" stroke-width="2"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z"/></svg>
            <div>
              <span class="meta-label">联系方式</span>
              <span class="meta-value">{{ activity.contact }}</span>
            </div>
          </div>

        </div>

        <div v-if="activityTags.length || activity.campusTag" class="activity-tags">
          <span v-for="tag in activityTags" :key="tag" class="tag-item circle-tag">{{ tag }}</span>
          <span v-if="activity.campusTag" class="tag-item campus-tag">
            <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
            {{ activity.campusTag }}
          </span>
        </div>
      </section>

      <section class="organizer-card" @click="goToOrganizer">
        <img :src="activity.userAvatar || defaultAvatar" class="organizer-avatar" @error="onAvatarError" loading="lazy" />
        <div class="organizer-info">
          <span class="organizer-name">{{ activity.userName || '活动组织者' }}</span>
          <span class="organizer-label">活动发起人</span>
        </div>
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#ccc" stroke-width="2"><polyline points="9,18 15,12 9,6"/></svg>
      </section>

      <section class="description-card">
        <h3 class="section-heading">活动介绍</h3>
        <div class="description-body" v-html="renderedContent"></div>
      </section>

      <section class="comment-section-wrapper">
        <CommentSection :target-id="activity.id" :target-type="'post'" :initial-comments="[]" />
      </section>
    </main>

    <footer v-if="activity" class="bottom-bar">
      <div class="bottom-actions">
        <LikeButton :is-liked="activity.isLiked" :count="activity.likeCount" target-type="POST" :target-id="activity.id" @toggled="onLikeToggled" />
        <button class="action-btn" @click="scrollToComments">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
          <span>{{ activity.commentCount || 0 }}</span>
        </button>
      </div>
      <button class="join-btn" :class="{ joined: hasJoined }" @click="toggleJoin" :disabled="joinLoading">
        {{ joinLoading ? '处理中...' : hasJoined ? '已报名' : '立即报名' }}
      </button>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { activityApi } from '../services/api'
import LikeButton from '../components/LikeButton.vue'
import CommentSection from '../components/CommentSection.vue'

const route = useRoute()
const router = useRouter()

const activity = ref(null)
const loading = ref(true)
const error = ref(null)
const hasJoined = ref(false)
const joinLoading = ref(false)

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#eee"/><circle cx="20" cy="15" r="8" fill="#ccc"/><ellipse cx="20" cy="35" rx="12" ry="8" fill="#ccc"/></svg>')

const renderedContent = computed(() => {
  if (!activity.value?.content) return '暂无详细介绍'
  return activity.value.content.replace(/\n/g, '<br>')
})

const activityTags = computed(() => {
  if (!activity.value?.tags) return []
  return activity.value.tags.split(',').filter(t => t.trim())
})

const statusClass = computed(() => {
  if (!activity.value) return ''
  const now = new Date()
  const start = activity.value.startTime ? new Date(activity.value.startTime) : null
  const end = activity.value.endTime ? new Date(activity.value.endTime) : null
  if (!start) return 'upcoming'
  if (now < start) return 'upcoming'
  if (end && now > end) return 'past'
  return 'ongoing'
})

const statusText = computed(() => {
  const map = { upcoming: '即将开始', ongoing: '进行中', past: '已结束' }
  return map[statusClass.value] || '活动'
})

onMounted(() => { loadActivity() })

async function loadActivity() {
  loading.value = true
  error.value = null
  try {
    const res = await activityApi.getActivityDetail(route.params.id)
    if (res.code === 200) {
      activity.value = res.data
    } else {
      error.value = res.message || '加载失败'
    }
  } catch (e) {
    error.value = '网络错误，请检查网络连接'
  } finally {
    loading.value = false
  }
}

async function toggleJoin() {
  if (joinLoading.value) return
  joinLoading.value = true
  try {
    if (hasJoined.value) {
      const res = await activityApi.cancelJoin(route.params.id)
      if (res.code === 200) hasJoined.value = false
    } else {
      const res = await activityApi.joinActivity(route.params.id)
      if (res.code === 200) hasJoined.value = true
    }
  } catch (e) {
    // silent
  } finally {
    joinLoading.value = false
  }
}

function onLikeToggled(isLiked, count) {
  if (activity.value) { activity.value.isLiked = isLiked; activity.value.likeCount = count }
}

function scrollToComments() {
  window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' })
}

function goToOrganizer() {
  if (activity.value?.userId) router.push(`/users/${activity.value.userId}`)
}

function handleShare() { navigator.clipboard?.writeText(window.location.href) }

function onAvatarError(e) { e.target.src = defaultAvatar }
function onBannerError(e) { e.target.style.display = 'none' }

function formatDateTime(d) {
  if (!d) return '待定'
  const dt = new Date(d)
  const y = dt.getFullYear()
  const M = String(dt.getMonth() + 1).padStart(2, '0')
  const day = String(dt.getDate()).padStart(2, '0')
  const h = String(dt.getHours()).padStart(2, '0')
  const m = String(dt.getMinutes()).padStart(2, '0')
  return `${y}年${M}月${day}日 ${h}:${m}`
}
</script>

<style scoped>
.activity-detail-page {
  min-height: 100vh;
  background: #F5F7FA;
  padding-bottom: 66px;
}

.detail-nav {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  display: flex; align-items: center; height: 56px; padding: 0 16px;
  background: #FFFFFF; border-bottom: 1px solid #E8ECF0;
}
.nav-back { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border: none; background: none; color: #333; cursor: pointer; flex-shrink: 0; }
.nav-title { flex: 1; text-align: center; font-size: 16px; font-weight: 600; color: #333; }
.share-btn { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border: none; background: none; color: #666; cursor: pointer; border-radius: 4px; }
.share-btn:active { background: #F0F2F5; }

.loading-state { padding-top: 56px; }
.skeleton-banner { width: 100%; aspect-ratio: 16/9; background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; }
.skeleton-block { padding: 16px; background: #fff; }
.skeleton-line { height: 18px; border-radius: 4px; background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; margin-bottom: 12px; }
.w-80 { width: 80%; } .w-60 { width: 60%; } .w-40 { width: 40%; }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

.error-state { padding: 80px 32px; text-align: center; color: #999; }
.error-icon { font-size: 64px; display: block; margin-bottom: 16px; }
.retry-btn { margin-top: 16px; padding: 10px 32px; background: #FF6A00; color: #fff; border-radius: 4px; font-size: 14px; border: none; cursor: pointer; }

.detail-content { padding-top: 56px; }

.banner-section { position: relative; background: #FFFFFF; }
.banner-img { width: 100%; aspect-ratio: 16/9; object-fit: cover; }
.banner-placeholder { width: 100%; aspect-ratio: 16/9; background: linear-gradient(135deg, #FFF7E6, #FFE7BA); display: flex; align-items: center; justify-content: center; }
.banner-overlay { position: absolute; top: 12px; right: 12px; }
.activity-status { padding: 4px 12px; border-radius: 10px; font-size: 12px; font-weight: 600; color: #fff; }
.activity-status.upcoming { background: linear-gradient(135deg, #FF6A00, #FF8533); }
.activity-status.ongoing { background: linear-gradient(135deg, #52c41a, #73d13d); }
.activity-status.past { background: rgba(0,0,0,0.35); }

.info-card { background: #FFFFFF; padding: 20px 16px; }
.activity-title { font-size: 20px; font-weight: 700; color: #333; margin: 0 0 16px; line-height: 1.4; }

.meta-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px 8px; }
.meta-item { display: flex; gap: 8px; align-items: flex-start; }
.meta-item svg { margin-top: 2px; flex-shrink: 0; }
.meta-label { display: block; font-size: 12px; color: #999; margin-bottom: 2px; }
.meta-value { font-size: 14px; font-weight: 600; color: #333; }

.organizer-card { display: flex; align-items: center; gap: 12px; padding: 16px; background: #FFFFFF; margin-top: 10px; cursor: pointer; }
.organizer-card:active { background: #fafafa; }
.organizer-avatar { width: 44px; height: 44px; border-radius: 50%; object-fit: cover; background: #eee; flex-shrink: 0; }
.organizer-info { flex: 1; display: flex; flex-direction: column; }
.organizer-name { font-size: 15px; font-weight: 600; color: #333; }
.organizer-label { font-size: 12px; color: #999; }

.activity-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
}

.tag-item {
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 4px;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.circle-tag {
  background: #FFF2E6;
  color: #FF6A00;
}

.campus-tag {
  background: #E8F5E9;
  color: #4CAF50;
}

.description-card { background: #FFFFFF; padding: 20px 16px; margin-top: 10px; }
.section-heading { font-size: 17px; font-weight: 700; color: #333; margin: 0 0 12px; }
.description-body { font-size: 15px; line-height: 1.7; color: #444; white-space: pre-wrap; word-break: break-word; }

.comment-section-wrapper { margin-top: 16px; }

.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; z-index: 100; display: flex; align-items: center; height: 50px; padding: 0 16px; padding-bottom: env(safe-area-inset-bottom, 0px); background: #FFFFFF; border-top: 1px solid #E8ECF0; }
.bottom-actions { display: flex; align-items: center; gap: 4px; }
.action-btn { display: flex; align-items: center; gap: 4px; padding: 8px 10px; border: none; background: none; color: #666; cursor: pointer; border-radius: 4px; font-size: 14px; }
.action-btn:active { background: #F0F2F5; }
.join-btn { margin-left: auto; padding: 10px 28px; border-radius: 20px; border: none; background: linear-gradient(135deg, #FF6A00, #FF8533); color: #fff; font-size: 15px; font-weight: 700; cursor: pointer; transition: all 0.2s ease; }
.join-btn:active { transform: scale(0.96); }
.join-btn.joined { background: #E8F4FD; color: #1890FF; }
.join-btn:disabled { opacity: 0.6; }
</style>
