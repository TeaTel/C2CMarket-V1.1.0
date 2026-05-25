<template>
  <div class="product-card" @click="$emit('click')">
    <div class="card-image">
      <img
        v-if="product.coverImage"
        :src="product.coverImage"
        :alt="product.title"
        class="cover-image"
        loading="lazy"
        @error="onImageError"
      />
      <div v-else class="cover-placeholder">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="36" height="36">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
          <circle cx="8.5" cy="8.5" r="1.5"/>
          <polyline points="21 15 16 10 5 21"/>
        </svg>
      </div>
      <span class="price-tag">¥{{ formatPrice(product.price) }}</span>
    </div>

    <h3 class="card-title">{{ product.title }}</h3>

    <div v-if="productTags.length || product.campusTag" class="card-tags">
      <span v-for="tag in productTags" :key="tag" class="tag-item circle-tag">{{ tag }}</span>
      <span v-if="product.campusTag" class="tag-item campus-tag">{{ product.campusTag }}</span>
    </div>

    <div class="card-user-bar">
      <div class="user-left" @click.stop="goToUser">
        <img :src="product.userAvatar || defaultAvatar" class="user-avatar" loading="lazy" @error="onAvatarError" />
        <span class="user-name">{{ product.userName || product.sellerName || '校园卖家' }}</span>
      </div>
      <div class="like-count" @click.stop>
        <LikeButton
          :is-liked="product.isLiked"
          :count="product.likeCount"
          target-type="PRODUCT"
          :target-id="product.id"
          @toggled="onLikeToggled"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import LikeButton from './LikeButton.vue'

const props = defineProps({
  product: { type: Object, required: true }
})

const emit = defineEmits(['click', 'like-toggled'])
const router = useRouter()

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#eee"/><circle cx="20" cy="15" r="8" fill="#ccc"/><ellipse cx="20" cy="35" rx="12" ry="8" fill="#ccc"/></svg>')

const productTags = computed(() => {
  if (!props.product.tags) return []
  return props.product.tags.split(',').filter(t => t.trim())
})

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

function goToUser() {
  if (props.product.userId) router.push(`/users/${props.product.userId}`)
}
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: transform 0.15s ease;
}

.product-card:active {
  transform: scale(0.98);
}

.card-image {
  position: relative;
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

.price-tag {
  position: absolute;
  bottom: 8px;
  left: 8px;
  padding: 4px 10px;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(6px);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  border-radius: 8px;
  line-height: 1;
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

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 6px 12px 0;
}

.tag-item {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 3px;
  white-space: nowrap;
}

.circle-tag {
  background: #FFF2E6;
  color: #FF6A00;
}

.campus-tag {
  background: #E8F5E9;
  color: #4CAF50;
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
