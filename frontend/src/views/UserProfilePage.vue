<template>
  <div class="user-profile-page">
    <div class="header">
      <button class="back-btn" @click="$router.back()">
        <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <span class="header-title">{{ user?.nickname || user?.username || '用户主页' }}</span>
    </div>
    <div class="content" v-if="user">
      <div class="profile-card">
        <img :src="user.avatar || defaultAvatar" class="avatar" @error="e => e.target.src = defaultAvatar" />
        <h3>{{ user.nickname || user.username }}</h3>
        <p v-if="user.bio">{{ user.bio }}</p>
        <p v-if="user.school">{{ user.school }} {{ user.major || '' }}</p>
      </div>
      <div class="stats">
        <div class="stat"><strong>0</strong><span>帖子</span></div>
        <div class="stat"><strong>0</strong><span>关注</span></div>
        <div class="stat"><strong>0</strong><span>粉丝</span></div>
      </div>
      <p class="placeholder-text">用户动态 - 建设中</p>
    </div>
    <div v-else class="content">
      <p class="placeholder-text">加载中...</p>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { userApi } from '../services/api'

const route = useRoute()
const user = ref(null)
const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#eee"/><circle cx="20" cy="15" r="8" fill="#ccc"/><ellipse cx="20" cy="35" rx="12" ry="8" fill="#ccc"/></svg>')

onMounted(async () => {
  try {
    const res = await userApi.getUserPublic(route.params.id)
    if (res.code === 200) user.value = res.data
  } catch (e) {
    try {
      const res = await userApi.getUserInfo()
      if (res.code === 200) user.value = res.data
    } catch (e2) {}
  }
})
</script>
<style scoped>
.user-profile-page { min-height: 100vh; background: #f5f5f5; }
.header { display: flex; align-items: center; padding: 12px 16px; background: #fff; border-bottom: 1px solid #f0f0f0; }
.back-btn { background: none; border: none; cursor: pointer; display: flex; color: #333; }
.header-title { flex: 1; text-align: center; font-size: 16px; font-weight: 600; margin-right: 32px; }
.content { padding: 20px; }
.profile-card {
  background: #fff; border-radius: 12px; padding: 24px 16px;
  text-align: center; margin-bottom: 12px;
}
.avatar { width: 72px; height: 72px; border-radius: 50%; object-fit: cover; background: #eee; margin-bottom: 12px; }
.profile-card h3 { font-size: 18px; color: #333; margin-bottom: 4px; }
.profile-card p { font-size: 13px; color: #999; }
.stats { display: flex; background: #fff; border-radius: 12px; padding: 16px; margin-bottom: 12px; }
.stat { flex: 1; text-align: center; }
.stat strong { display: block; font-size: 18px; color: #333; }
.stat span { font-size: 12px; color: #999; }
.placeholder-text { text-align: center; color: #ccc; padding: 40px 0; font-size: 14px; }
</style>
