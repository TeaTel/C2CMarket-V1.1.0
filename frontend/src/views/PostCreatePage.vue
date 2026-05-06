<template>
  <div class="post-create-page">
    <div class="header">
      <button class="back-btn" @click="$router.back()">取消</button>
      <span class="header-title">发布帖子</span>
      <button class="submit-btn" :disabled="!canSubmit" @click="submitPost">发布</button>
    </div>

    <div class="form-area">
      <input v-model="title" class="title-input" placeholder="请输入帖子标题（2-200字）" maxlength="200" />
      <div class="type-selector">
        <button v-for="t in postTypes" :key="t.value" :class="{ active: postType === t.value }" @click="postType = t.value">
          {{ t.label }}
        </button>
      </div>
      <textarea v-model="content" class="content-input" placeholder="分享你的想法..." maxlength="10000"></textarea>
      <div class="char-count">{{ content.length }}/10000</div>
      <div v-if="error" class="error-msg">{{ error }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { postApi } from '../services/api'

const router = useRouter()
const title = ref('')
const content = ref('')
const postType = ref('DISCUSSION')
const error = ref(null)

const postTypes = [
  { value: 'DISCUSSION', label: '讨论' },
  { value: 'SHOWCASE', label: '展示' },
  { value: 'HELP', label: '求助' },
  { value: 'ACTIVITY', label: '活动' }
]

const canSubmit = computed(() => title.value.trim().length >= 2 && content.value.trim().length > 0)

async function submitPost() {
  if (!canSubmit.value) return
  error.value = null
  try {
    const res = await postApi.createPost({
      title: title.value.trim(),
      content: content.value.trim(),
      postType: postType.value
    })
    if (res.code === 200) {
      router.push(`/community/posts/${res.data.id}`)
    } else {
      error.value = res.message || '发布失败'
    }
  } catch (e) {
    error.value = '发布失败，请检查网络连接'
  }
}
</script>

<style scoped>
.post-create-page {
  min-height: 100vh;
  background: #f5f5f5;
}
.header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.back-btn {
  background: none; border: none; font-size: 15px; color: #666; cursor: pointer;
}
.header-title { flex: 1; text-align: center; font-size: 16px; font-weight: 600; }
.submit-btn {
  padding: 6px 20px; border-radius: 16px; border: none;
  background: linear-gradient(135deg, #ff6a00, #ff9500);
  color: #fff; font-size: 14px; cursor: pointer;
}
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.form-area { padding: 16px; }
.title-input {
  width: 100%; padding: 12px; border: none; border-radius: 8px;
  font-size: 18px; font-weight: 600; outline: none; background: #fff;
  margin-bottom: 12px; box-sizing: border-box;
}
.type-selector {
  display: flex; gap: 8px; margin-bottom: 12px;
}
.type-selector button {
  padding: 6px 14px; border-radius: 16px; border: 1px solid #e0e0e0;
  background: #fff; font-size: 13px; cursor: pointer; color: #666;
}
.type-selector button.active {
  border-color: #ff6a00; color: #ff6a00; background: #fff8f2;
}
.content-input {
  width: 100%; min-height: 200px; padding: 12px; border: none;
  border-radius: 8px; font-size: 15px; line-height: 1.8;
  outline: none; background: #fff; resize: vertical;
  box-sizing: border-box;
}
.char-count { text-align: right; font-size: 12px; color: #999; padding: 4px 0; }
.error-msg { color: #ff4757; font-size: 14px; padding: 8px 0; }
</style>
