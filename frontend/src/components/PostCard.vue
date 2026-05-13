<template>
  <div class="post-card" @click="$emit('click')">
    <div class="card-image">
      <img
        v-if="post.coverImage"
        :src="post.coverImage"
        :alt="post.title"
        class="cover-image"
        loading="lazy"
        @error="onImageError"
      />
      <div v-else class="cover-placeholder">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="36" height="36">
          <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="16" y1="13" x2="8" y2="13"/>
          <line x1="16" y1="17" x2="8" y2="17"/>
          <polyline points="10 9 9 9 8 9"/>
        </svg>
      </div>
    </div>

    <h3 class="card-title">{{ post.title }}</h3>

    <div class="card-user-bar">
      <div class="user-left" @click.stop="goToUser">
        <img :src="post.userAvatar || defaultAvatar" class="user-avatar" loading="lazy" @error="onAvatarError" />
        <span class="user-name">{{ post.userName || '匿名用户' }}</span>
      </div>
      <div class="like-count" @click.stop>
        <LikeButton
          :is-liked="post.isLiked"
          :count="post.likeCount"
          target-type="POST"
          :target-id="post.id"
          @toggled="onLikeToggled"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import LikeButton from './LikeButton.vue'

const props = defineProps({
  post: { type: Object, required: true }
})

const emit = defineEmits(['click', 'like-toggled'])
const router = useRouter()
const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#eee"/><circle cx="20" cy="15" r="8" fill="#ccc"/><ellipse cx="20" cy="35" rx="12" ry="8" fill="#ccc"/></svg>')

function onAvatarError(e) {
  e.target.src = defaultAvatar
}

function onImageError(e) {
  e.target.style.display = 'none'
}

function onLikeToggled(isLiked, count) {
  props.post.isLiked = isLiked
  props.post.likeCount = count
  emit('like-toggled', { isLiked, count })
}

function goToUser() {
  if (props.post.userId) router.push(`/users/${props.post.userId}`)
}
</script>

<style scoped>
.post-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: transform 0.15s ease;
}

.post-card:active {
  transform: scale(0.98);
}

.card-image {
  width: 100%;
  aspect-ratio: 1 / 1;
  background: linear-gradient(135deg, #f0f2f5, #e4e7eb);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  color: #ccc;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #222;
  line-height: 1.4;
  padding: 10px 12px 0;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-user-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px 12px;
}

.user-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1;
}

.user-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  background: #eee;
  flex-shrink: 0;
}

.user-name {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.like-count {
  flex-shrink: 0;
  margin-left: 8px;
}
</style>
