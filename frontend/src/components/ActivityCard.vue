<template>
  <div class="activity-card" @click="$emit('click', activity.id)">
    <div class="activity-banner">
      <img
        v-if="activity.coverImage"
        :src="activity.coverImage"
        :alt="activity.title"
        class="activity-image"
        loading="lazy"
      />
      <div v-else class="activity-image-placeholder">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="40" height="40">
          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
          <line x1="16" y1="2" x2="16" y2="6"/>
          <line x1="8" y1="2" x2="8" y2="6"/>
          <line x1="3" y1="10" x2="21" y2="10"/>
        </svg>
      </div>
      <div class="activity-status-badge" :class="statusClass">
        {{ statusText }}
      </div>
    </div>
    <div class="activity-body">
      <h3 class="activity-title">{{ activity.title }}</h3>
      <p class="activity-org" v-if="activity.organizerName">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
          <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <path d="M23 21v-2a4 4 0 00-3-3.87"/>
          <path d="M16 3.13a4 4 0 010 7.75"/>
        </svg>
        {{ activity.organizerName }}
      </p>
      <div class="activity-meta">
        <span class="activity-time">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
          {{ formatDate(activity.startTime) }}
        </span>
        <span class="activity-location" v-if="activity.location">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
          {{ activity.location }}
        </span>
      </div>
      <div class="activity-footer">
        <span class="participant-count">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
            <path d="M16 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
            <circle cx="8.5" cy="7" r="4"/>
            <polyline points="17 11 19 13 23 9"/>
          </svg>
          {{ formatCount(activity.participantCount) }} 人已报名
        </span>
        <span class="activity-tag" v-if="activity.categoryName">{{ activity.categoryName }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  activity: { type: Object, required: true }
})

defineEmits(['click'])

const statusText = computed(() => {
  const map = { upcoming: '即将开始', ongoing: '进行中', past: '已结束' }
  return map[props.activity.status] || '即将开始'
})

const statusClass = computed(() => {
  const map = { upcoming: 'status-upcoming', ongoing: 'status-ongoing', past: 'status-past' }
  return map[props.activity.status] || 'status-upcoming'
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const isSameYear = d.getFullYear() === now.getFullYear()
  const month = d.getMonth() + 1
  const day = d.getDate()
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  if (isSameYear) {
    return `${month}月${day}日 ${hours}:${minutes}`
  }
  return `${d.getFullYear()}/${month}/${day} ${hours}:${minutes}`
}

function formatCount(count) {
  if (!count) return '0'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
  return count.toString()
}
</script>

<style scoped>
.activity-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.activity-card:active {
  transform: scale(0.98);
}

.activity-banner {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.activity-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.6);
}

.activity-status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  backdrop-filter: blur(8px);
}

.status-upcoming {
  background: rgba(76, 175, 80, 0.9);
  color: #fff;
}

.status-ongoing {
  background: rgba(255, 106, 0, 0.9);
  color: #fff;
  animation: pulse-status 2s infinite;
}

.status-past {
  background: rgba(158, 158, 158, 0.8);
  color: #fff;
}

@keyframes pulse-status {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.75; }
}

.activity-body {
  padding: 12px 14px;
}

.activity-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-org {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #FF6A00;
  margin-bottom: 8px;
}

.activity-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 10px;
}

.activity-time,
.activity-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.activity-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid #f5f5f5;
}

.participant-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #666;
}

.activity-tag {
  padding: 3px 8px;
  border-radius: 8px;
  font-size: 11px;
  background: #f0f0f0;
  color: #888;
}
</style>
