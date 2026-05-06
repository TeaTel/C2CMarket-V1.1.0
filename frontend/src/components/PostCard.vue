<template>
  <div class="post-card" @click="$emit('click')">
    <div class="post-header">
      <img :src="post.userAvatar || defaultAvatar" class="avatar" @error="onAvatarError" />
      <div class="user-info">
        <span class="username">{{ post.userName || '匿名用户' }}</span>
        <span class="time">{{ formatTime(post.createdAt) }}</span>
      </div>
      <span class="post-type-tag" :class="postTypeClass">{{ post.postTypeText || post.postType }}</span>
    </div>
    <h3 class="post-title">{{ post.title }}</h3>
    <p class="post-content">{{ truncateContent(post.content) }}</p>
    <div class="post-footer">
      <div class="stat-item">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
        <span>{{ formatCount(post.viewCount) }}</span>
      </div>
      <div class="stat-item" @click.stop="handleLike">
        <LikeButton :is-liked="post.isLiked" :count="post.likeCount" target-type="POST" :target-id="post.id" @toggled="onLikeToggled" />
      </div>
      <div class="stat-item">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
        <span>{{ formatCount(post.commentCount) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import LikeButton from './LikeButton.vue'

const props = defineProps({
  post: { type: Object, required: true }
})

const emit = defineEmits(['click', 'like-toggled'])
const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#eee"/><circle cx="20" cy="15" r="8" fill="#ccc"/><ellipse cx="20" cy="35" rx="12" ry="8" fill="#ccc"/></svg>')

const postTypeClass = {
  DISCUSSION: 'type-discussion',
  SHOWCASE: 'type-showcase',
  HELP: 'type-help',
  ACTIVITY: 'type-activity'
}[props.post.postType] || 'type-discussion'

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

function formatCount(count) {
  if (!count) return '0'
  if (count >= 10000) return (count / 10000).toFixed(1) + 'w'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
  return count.toString()
}

function truncateContent(content) {
  if (!content) return ''
  return content.length > 120 ? content.substring(0, 120) + '...' : content
}

function onAvatarError(e) {
  e.target.src = defaultAvatar
}

function handleLike() {
  // handled by LikeButton
}

function onLikeToggled(isLiked, count) {
  props.post.isLiked = isLiked
  props.post.likeCount = count
  emit('like-toggled', { isLiked, count })
}
</script>

<style scoped>
.post-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  cursor: pointer;
}
.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  background: #eee;
}
.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.username {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}
.time {
  font-size: 12px;
  color: #999;
}
.post-type-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}
.type-discussion { background: #e3f2fd; color: #1565c0; }
.type-showcase { background: #fce4ec; color: #c62828; }
.type-help { background: #fff3e0; color: #e65100; }
.type-activity { background: #e8f5e9; color: #2e7d32; }
.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #222;
  margin-bottom: 6px;
  line-height: 1.4;
}
.post-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.post-footer {
  display: flex;
  gap: 20px;
  align-items: center;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
}
</style>
