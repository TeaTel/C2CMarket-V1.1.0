<template>
  <div class="product-card" @click="$emit('click')">
    <div class="card-header">
      <img :src="product.userAvatar || defaultAvatar" class="avatar" loading="lazy" @error="onAvatarError" />
      <div class="user-info">
        <span class="username">{{ product.userName || product.sellerName || '校园卖家' }}</span>
        <span class="time">{{ formatTime(product.createdAt) }}</span>
      </div>
      <div class="like-area" @click.stop>
        <LikeButton
          :is-liked="product.isLiked"
          :count="product.likeCount"
          target-type="PRODUCT"
          :target-id="product.id"
          @toggled="onLikeToggled"
        />
      </div>
    </div>

    <div class="product-body">
      <img
        v-if="product.coverImage"
        :src="product.coverImage"
        :alt="product.title"
        class="product-image"
        loading="lazy"
        @error="onImageError"
      />
      <h3 class="product-title">{{ product.title }}</h3>
      <div class="product-meta">
        <span class="product-price">¥{{ formatPrice(product.price) }}</span>
        <span v-if="product.originalPrice && product.originalPrice > product.price" class="original-price">¥{{ formatPrice(product.originalPrice) }}</span>
        <span class="product-circle" v-if="product.circleName">{{ product.circleName }}</span>
      </div>
    </div>

    <div class="card-footer">
      <span class="stat-item">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
          <circle cx="12" cy="12" r="3"/>
        </svg>
        <span>{{ formatCount(product.viewCount) }}</span>
      </span>
      <span class="stat-item">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
        </svg>
        <span>{{ formatCount(product.commentCount) }}</span>
      </span>
    </div>
  </div>
</template>

<script setup>
import LikeButton from './LikeButton.vue'

const props = defineProps({
  product: { type: Object, required: true }
})

const emit = defineEmits(['click', 'like-toggled'])

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#eee"/><circle cx="20" cy="15" r="8" fill="#ccc"/><ellipse cx="20" cy="35" rx="12" ry="8" fill="#ccc"/></svg>')

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

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

function onAvatarError(e) {
  e.target.src = defaultAvatar
}

function onImageError(e) {
  e.target.style.display = 'none'
}

function onLikeToggled(isLiked, count) {
  props.product.isLiked = isLiked
  props.product.likeCount = count
  emit('like-toggled', { isLiked, count })
}
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: transform 0.15s ease;
}

.product-card:active {
  transform: scale(0.98);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  background: #eee;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time {
  font-size: 12px;
  color: #999;
}

.like-area {
  flex-shrink: 0;
}

.product-body {
  display: flex;
  flex-direction: column;
}

.product-image {
  width: 100%;
  aspect-ratio: 16 / 10;
  object-fit: cover;
  border-radius: 8px;
  background: #f5f5f5;
  margin-bottom: 10px;
}

.product-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.product-price {
  font-size: 17px;
  font-weight: 700;
  color: #FF6A00;
}

.original-price {
  font-size: 12px;
  color: #bbb;
  text-decoration: line-through;
}

.product-circle {
  font-size: 11px;
  color: #FF6A00;
  background: #FFF7E6;
  padding: 2px 8px;
  border-radius: 8px;
  margin-left: auto;
}

.card-footer {
  display: flex;
  gap: 20px;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #f5f5f5;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
}
</style>
