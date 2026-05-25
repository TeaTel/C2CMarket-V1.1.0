<template>
  <div class="post-create-page">
    <div class="header">
      <button class="back-btn" @click="$router.back()">取消</button>
      <span class="header-title">发布帖子</span>
      <button class="submit-btn" :disabled="!canSubmit || submitting" @click="submitPost">{{ submitting ? '发布中...' : '发布' }}</button>
    </div>

    <div class="form-area">
      <section class="upload-section">
        <div class="section-label">帖子图片（选填）</div>
        <ImageUploader v-model="imageUrls" :max-count="9" :max-size="10" />
      </section>

      <input v-model="title" class="title-input" placeholder="请输入帖子标题（2-200字）" maxlength="200" />
      <div class="type-selector">
        <button v-for="t in postTypes" :key="t.value" :class="{ active: postType === t.value }" @click="postType = t.value">{{ t.label }}</button>
      </div>
      <textarea v-model="content" class="content-input" placeholder="分享你的想法..." maxlength="10000"></textarea>
      <div class="char-count">{{ content.length }}/10000</div>

      <div class="tag-section">
        <div class="section-label">圈子标签（至少选 1 个，最多 5 个）</div>
        <TagInput v-model="tags" :preset-tags="presetTags" :max-tags="5" placeholder="输入标签后按回车或逗号分隔..." />
      </div>

      <div class="campus-tag-section">
        <div class="section-label">校区标签（必选）</div>
        <div class="campus-options">
          <button
            v-for="campus in campusOptions"
            :key="campus"
            class="campus-btn"
            :class="{ active: campusTag === campus }"
            @click="campusTag = campus"
          >
            {{ campus }}
          </button>
        </div>
      </div>

      <div v-if="error" class="error-msg">{{ error }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { postApi } from '../services/api'
import { useAuthStore } from '../store/auth'
import TagInput from '../components/TagInput.vue'
import ImageUploader from '../components/ImageUploader.vue'

const router = useRouter()
const authStore = useAuthStore()
const title = ref('')
const content = ref('')
const postType = ref('DISCUSSION')
const error = ref(null)
const submitting = ref(false)
const imageUrls = ref([])
const tags = ref([])
const campusTag = ref('')

const postTypes = [
  { value: 'DISCUSSION', label: '讨论' },
  { value: 'SHOWCASE', label: '展示' },
  { value: 'HELP', label: '求助' },
  { value: 'ACTIVITY', label: '活动' }
]

const presetTags = [
  '数码', '书籍', '运动', '美食', '音乐',
  '学习', '求职', '考研', '二手', '闲置',
  '分享', '经验', '求助', '摄影', '旅行',
  '电影', '游戏', '留学', '吐槽', '美妆'
]

const campusOptions = ['南三区', '南二区', '南一区', '中区', '东区', '西区']

const canSubmit = computed(() =>
  title.value.trim().length >= 2 &&
  content.value.trim().length > 0 &&
  tags.value.length > 0 &&
  campusTag.value &&
  !submitting.value
)

async function submitPost() {
  if (!canSubmit.value) return
  error.value = null
  submitting.value = true
  try {
    const res = await postApi.createPost({
      title: title.value.trim(),
      content: content.value.trim(),
      postType: postType.value,
      imageUrls: imageUrls.value.length > 0 ? imageUrls.value : null,
      coverImage: imageUrls.value.length > 0 ? imageUrls.value[0] : null,
      tags: tags.value.length > 0 ? tags.value.join(',') : null,
      campusTag: campusTag.value
    })
    if (res.code === 200) {
      router.push(`/community/posts/${res.data.id}`)
    } else {
      error.value = res.message || '发布失败'
    }
  } catch (e) {
    error.value = e.message || '发布失败，请检查网络连接'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  const user = authStore.currentUser.value
  if (user?.campus) {
    campusTag.value = user.campus
  }
})
</script>

<style scoped>
.post-create-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 40px; }
.header {
  display: flex; align-items: center; padding: 12px 16px;
  background: #fff; border-bottom: 1px solid #f0f0f0;
  position: sticky; top: 0; z-index: 10;
}
.back-btn { background: none; border: none; font-size: 15px; color: #666; cursor: pointer; }
.header-title { flex: 1; text-align: center; font-size: 16px; font-weight: 600; }
.submit-btn {
  padding: 6px 20px; border-radius: 16px; border: none;
  background: linear-gradient(135deg, #ff6a00, #ff9500);
  color: #fff; font-size: 14px; cursor: pointer;
}
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.form-area { padding: 16px; }
.section-label { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 10px; }
.upload-section { background: #fff; border-radius: 12px; padding: 16px; margin-bottom: 16px; }
.title-input {
  width: 100%; padding: 12px; border: none; border-radius: 8px;
  font-size: 18px; font-weight: 600; outline: none; background: #fff;
  margin-bottom: 12px; box-sizing: border-box;
}
.type-selector { display: flex; gap: 8px; margin-bottom: 12px; }
.type-selector button {
  padding: 6px 14px; border-radius: 16px; border: 1px solid #e0e0e0;
  background: #fff; font-size: 13px; cursor: pointer; color: #666;
}
.type-selector button.active { border-color: #ff6a00; color: #ff6a00; background: #fff8f2; }
.content-input {
  width: 100%; min-height: 200px; padding: 12px; border: none;
  border-radius: 8px; font-size: 15px; line-height: 1.8;
  outline: none; background: #fff; resize: vertical; box-sizing: border-box;
}
.char-count { text-align: right; font-size: 12px; color: #999; padding: 4px 0; }
.tag-section { background: #fff; border-radius: 12px; padding: 16px; margin-top: 16px; }
.campus-tag-section { background: #fff; border-radius: 12px; padding: 16px; margin-top: 16px; }
.campus-options { display: flex; flex-wrap: wrap; gap: 8px; }
.campus-btn {
  padding: 6px 14px; border-radius: 16px; border: 1px solid #e0e0e0;
  background: #fff; font-size: 13px; cursor: pointer; color: #666;
  transition: all 0.15s;
}
.campus-btn.active { border-color: #4CAF50; color: #4CAF50; background: #E8F5E9; }
.error-msg { color: #ff4757; font-size: 14px; padding: 8px 0; }
</style>
