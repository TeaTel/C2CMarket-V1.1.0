<template>
  <div class="detail-page">
    <header class="detail-nav">
      <div class="nav-left">
        <button class="nav-back" @click="$router.back()">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="15,18 9,12 15,6"/>
          </svg>
        </button>
        <img :src="product?.sellerAvatar || defaultAvatar" class="nav-avatar" @click="goToSeller" @error="onAvatarError" />
        <span class="nav-username" @click="goToSeller">{{ product?.sellerName || '匿名卖家' }}</span>
      </div>
      <div class="nav-right">
        <button v-if="auth.isAuthenticated && auth.currentUser?.id !== product?.sellerId" class="follow-btn" :class="{ followed: isFollowing }" @click="toggleFollow">
          <svg v-if="!isFollowing" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg>
          {{ isFollowing ? '已关注' : '关注' }}
        </button>
        <button class="share-btn" @click="handleShare">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
            <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
          </svg>
        </button>
      </div>
    </header>

    <div v-if="loading" class="loading-state">
      <div class="skeleton-image"></div>
      <div class="skeleton-block">
        <div class="skeleton-line w-80"></div>
        <div class="skeleton-line w-40"></div>
        <div class="skeleton-line w-60"></div>
      </div>
    </div>

    <div v-else-if="error" class="error-state">
      <span class="error-icon">😕</span>
      <p>{{ error }}</p>
      <button @click="loadProduct">重试</button>
    </div>

    <main v-else-if="product" class="detail-content">
      <section v-if="productImages.length > 0" class="image-gallery">
        <div class="gallery-grid" :class="{ single: productImages.length === 1, multi: productImages.length > 1 }">
          <div v-for="(img, idx) in productImages" :key="idx" class="gallery-item" @click="previewImage(img)">
            <img :src="img" loading="lazy" class="gallery-img" @error="onImageError" />
          </div>
        </div>
        <span v-if="productImages.length > 1" class="gallery-counter">{{ currentImage + 1 }} / {{ productImages.length }}</span>
      </section>

      <section class="text-section">
        <div class="price-row">
          <span class="price-current">¥{{ formatPrice(product.price) }}</span>
          <span v-if="product.originalPrice && product.originalPrice > product.price" class="price-original">¥{{ formatPrice(product.originalPrice) }}</span>
        </div>
        <h1 class="detail-title">{{ product.name }}</h1>
        <div v-if="productTags.length || product.campusTag" class="detail-tags">
          <span v-for="tag in productTags" :key="tag" class="tag-item circle-tag">{{ tag }}</span>
          <span v-if="product.campusTag" class="tag-item campus-tag">
            <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
            {{ product.campusTag }}
          </span>
        </div>
        <div v-if="product.sellerCampus" class="seller-campus-info">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#999" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9,22 9,12 15,12 15,22"/></svg>
          <span>卖家校区：{{ product.sellerCampus }}</span>
        </div>
        <p class="detail-body">{{ product.description || '暂无详细描述' }}</p>
      </section>

      <section v-if="product.detailImages?.length" class="extra-images">
        <img v-for="(img, idx) in product.detailImages" :key="idx" :src="img" class="extra-img" loading="lazy" @click="previewImage(img)" />
      </section>

      <section class="comment-section-wrapper">
        <CommentSection :target-id="product.id" :target-type="'product'" :initial-comments="[]" />
      </section>
    </main>

    <footer v-if="product" class="bottom-bar">
      <div class="bottom-comment-input" @click="focusComment">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#999" stroke-width="2">
          <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
        </svg>
        <span>添加评论...</span>
      </div>
      <div class="bottom-actions">
        <LikeButton :is-liked="product.isLiked" :count="product.likeCount || 0" target-type="PRODUCT" :target-id="product.id" @toggled="onLikeToggled" />
        <button class="action-btn" :class="{ active: isFavorited }" @click="toggleFavorite">
          <svg v-if="!isFavorited" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" width="20" height="20" fill="#FF6A00" stroke="#FF6A00" stroke-width="2">
            <path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z"/>
          </svg>
        </button>
        <button class="action-btn" @click="focusComment">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
          </svg>
          <span class="action-count">{{ product.commentCount || 0 }}</span>
        </button>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi, followApi, favoriteApi } from '../services/api'
import { useAuthStore } from '../store/auth'
import LikeButton from '../components/LikeButton.vue'
import CommentSection from '../components/CommentSection.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const product = ref(null)
const loading = ref(true)
const error = ref(null)
const currentImage = ref(0)
const isFollowing = ref(false)
const isFavorited = ref(false)

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#eee"/><circle cx="20" cy="15" r="8" fill="#ccc"/><ellipse cx="20" cy="35" rx="12" ry="8" fill="#ccc"/></svg>')

const productImages = computed(() => {
  if (!product.value) return []
  // imageUrls 可能是 JSON 字符串或数组
  let urls = product.value.imageUrls
  if (typeof urls === 'string') {
    try { urls = JSON.parse(urls) } catch (e) { urls = null }
  }
  if (Array.isArray(urls) && urls.length) return urls
  if (product.value.coverImage) return [product.value.coverImage]
  return []
})

const productTags = computed(() => {
  if (!product.value?.tags) return []
  return product.value.tags.split(',').filter(t => t.trim())
})

onMounted(() => { loadProduct() })

async function loadProduct() {
  loading.value = true
  error.value = null
  try {
    const res = await productApi.getProductDetail(route.params.id)
    if (res.code === 200) {
      product.value = res.data
      if (product.value.sellerId && auth.isAuthenticated) checkFollowStatus(product.value.sellerId)
    } else {
      error.value = res.message || '加载失败'
    }
  } catch (e) {
    error.value = '网络错误，请检查网络连接'
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
  if (!product.value) return
  try {
    const res = await followApi.toggleFollow(product.value.sellerId)
    if (res.code === 200) isFollowing.value = res.data.isFollowing
  } catch (e) {}
}

function onLikeToggled(isLiked, count) {
  if (product.value) { product.value.isLiked = isLiked; product.value.likeCount = count }
}

async function toggleFavorite() {
  if (!product.value) return
  try {
    if (isFavorited.value) {
      await favoriteApi.removeFavorite(product.value.id)
      isFavorited.value = false
    } else {
      await favoriteApi.addFavorite(product.value.id)
      isFavorited.value = true
    }
  } catch (e) {
    isFavorited.value = !isFavorited.value
  }
}
function focusComment() { window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' }) }
function previewImage(url) { window.open(url, '_blank') }
function handleShare() { navigator.clipboard?.writeText(window.location.href) }

function focusChat() {
  if (!auth.isAuthenticated) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  router.push({ path: `/chat/${product.value.sellerId}`, query: { productId: product.value.id } })
}

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

function onAvatarError(e) { e.target.src = defaultAvatar }
function onImageError(e) { e.target.style.display = 'none' }
function goToSeller() { if (product.value?.sellerId) router.push(`/users/${product.value.sellerId}`) }
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background-color: #F5F7FA;
  padding-bottom: 66px;
}

.detail-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 16px;
  background: #FFFFFF;
  border-bottom: 1px solid #E8ECF0;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.nav-back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  color: #333333;
  cursor: pointer;
  flex-shrink: 0;
}

.nav-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  background: #eee;
  flex-shrink: 0;
  cursor: pointer;
}

.nav-username {
  font-size: 14px;
  font-weight: 500;
  color: #333333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.follow-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 4px;
  border: 1px solid #DDE1E6;
  background: #F0F2F5;
  color: #666666;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.follow-btn.followed {
  background: #E8F4FD;
  border-color: #B3D8F5;
  color: #1890FF;
}

.share-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  color: #666666;
  cursor: pointer;
  border-radius: 4px;
}

.share-btn:active { background: #F0F2F5; }

.loading-state { padding-top: 56px; }
.skeleton-image {
  width: 100%;
  aspect-ratio: 1/1;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
.skeleton-block { padding: 16px; background: #fff; }
.skeleton-line {
  height: 18px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  margin-bottom: 12px;
}
.w-80 { width: 80%; } .w-60 { width: 60%; } .w-40 { width: 40%; }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

.error-state {
  padding: 120px 32px;
  text-align: center;
  color: #999999;
}
.error-icon { font-size: 64px; margin-bottom: 16px; display: block; }
.error-state button {
  padding: 10px 32px; border: none; background: #FF6A00;
  color: #fff; border-radius: 4px; font-size: 14px; cursor: pointer; margin-top: 16px;
}

.detail-content {
  padding-top: 56px;
}

.image-gallery {
  background: #FFFFFF;
  position: relative;
}

.gallery-grid {
  display: grid;
  gap: 2px;
}

.gallery-grid.single { grid-template-columns: 1fr; }
.gallery-grid.multi { grid-template-columns: repeat(2, 1fr); }

.gallery-item {
  overflow: hidden;
  background: #F5F7FA;
  cursor: pointer;
}

.gallery-img {
  width: 100%;
  object-fit: cover;
  display: block;
}

.gallery-grid.single .gallery-item { aspect-ratio: 1/1; }
.gallery-grid.single .gallery-img { height: 100%; }
.gallery-grid.multi .gallery-item { aspect-ratio: 1/1; }
.gallery-grid.multi .gallery-img { height: 100%; }

.gallery-counter {
  position: absolute;
  bottom: 8px;
  right: 8px;
  padding: 4px 10px;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(6px);
  color: #fff;
  font-size: 12px;
  border-radius: 10px;
}

.text-section {
  background: #FFFFFF;
  padding: 16px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 10px;
}

.price-current {
  font-size: 24px;
  font-weight: 700;
  color: #FF4D4F;
}

.price-original {
  font-size: 14px;
  color: #999999;
  text-decoration: line-through;
}

.detail-title {
  font-size: 18px;
  font-weight: 700;
  color: #333333;
  line-height: 1.4;
  margin: 0 0 12px;
}

.detail-body {
  font-size: 16px;
  font-weight: 400;
  color: #333333;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
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

.seller-campus-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
}

.extra-images {
  background: #FFFFFF;
  margin-top: 16px;
}

.extra-img {
  width: 100%;
  display: block;
  cursor: pointer;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  height: 50px;
  padding: 0 16px;
  padding-bottom: env(safe-area-inset-bottom, 0px);
  background: #FFFFFF;
  border-top: 1px solid #E8ECF0;
}

.bottom-comment-input {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  width: 60%;
  padding: 8px 12px;
  background: #F5F7FA;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #999999;
  margin-right: 12px;
}

.bottom-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 10px;
  border: none;
  background: none;
  color: #666666;
  cursor: pointer;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.2s ease;
}

.action-btn:active { background: #F0F2F5; }

.action-count {
  font-size: 13px;
  color: #666666;
}

.comment-section-wrapper {
  margin-top: 16px;
}
</style>
