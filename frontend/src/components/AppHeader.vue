<template>
  <header class="app-header">
    <div class="header-container">
      <div class="nav-tabs">
        <button
          v-for="tab in headerTabs"
          :key="tab.key"
          class="nav-tab"
          :class="{ active: activeTab === tab.key }"
          @click="switchTab(tab.key)"
        >
          {{ tab.label }}
        </button>
      </div>
      <div class="search-box">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16">
          <circle cx="11" cy="11" r="8"/>
          <path d="M21 21l-4.35-4.35"/>
        </svg>
        <input
          type="text"
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索..."
          @keyup.enter="handleSearch"
        />
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const headerTabs = [
  { key: 'following', label: '关注' },
  { key: 'discover', label: '发现' },
  { key: 'campus', label: '校区' }
]

const searchKeyword = ref('')

const activeTab = computed(() => {
  if (route.path === '/') {
    return route.query.tab || 'discover'
  }
  return 'discover'
})

function switchTab(key) {
  if (route.path === '/') {
    router.push({ path: '/', query: { tab: key } })
  } else {
    router.push({ path: '/', query: { tab: key } })
  }
}

function handleSearch() {
  const kw = searchKeyword.value.trim()
  if (!kw) return
  searchKeyword.value = ''
  router.push({ path: '/products', query: { keyword: kw } })
}
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 12px;
  max-width: 750px;
  margin: 0 auto;
  height: 48px;
  position: relative;
}

.nav-tabs {
  display: flex;
  max-width: 320px;
  width: 100%;
}

.nav-tab {
  flex: 1;
  padding: 12px 0;
  text-align: center;
  font-size: 15px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  border: none;
  background: none;
  position: relative;
  transition: color 0.25s ease;
}

.nav-tab.active {
  color: #FF6A00;
  font-weight: 700;
}

.nav-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 28px;
  height: 3px;
  background: #FF6A00;
  border-radius: 2px;
}

.search-box {
  position: absolute;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f5f5f5;
  border-radius: 16px;
  padding: 6px 12px;
  transition: width 0.3s ease, box-shadow 0.2s ease;
}

.search-box:focus-within {
  width: 160px;
  background: #fff;
  box-shadow: 0 0 0 2px rgba(255, 106, 0, 0.3);
}

.search-icon {
  color: #999;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: none;
  font-size: 13px;
  color: #333;
  outline: none;
  width: 0;
  min-width: 0;
}

.search-input::placeholder {
  color: #bbb;
}

@media (min-width: 769px) {
  .header-container {
    max-width: 750px;
    padding: 0 24px;
  }
}
</style>
