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
        <div class="image-grid">
          <div v-for="(img, idx) in images" :key="idx" class="image-item">
            <img :src="img.url" class="preview-img" />
            <div v-if="img.uploading" class="uploading-mask">
              <div class="upload-spinner"></div>
              <span class="upload-progress">{{ img.progress || 0 }}%</span>
            </div>
            <button class="img-remove-btn" @click="removeImage(idx)">
              <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="#fff" stroke-width="3"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
          <label v-if="images.length < 9" class="upload-trigger">
            <input type="file" accept="image/jpeg,image/png,image/webp" multiple @change="handleFiles" hidden />
            <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="#bbb" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21,15 16,10 5,21"/></svg>
            <span class="upload-count">{{ images.length }}/9</span>
          </label>
        </div>
        <p class="upload-tip">支持 JPG/PNG/WEBP，单张不超过 5MB，最多 9 张</p>
      </section>

      <input v-model="title" class="title-input" placeholder="请输入帖子标题（2-200字）" maxlength="200" />
      <div class="type-selector">
        <button v-for="t in postTypes" :key="t.value" :class="{ active: postType === t.value }" @click="postType = t.value">{{ t.label }}</button>
      </div>
      <textarea v-model="content" class="content-input" placeholder="分享你的想法..." maxlength="10000"></textarea>
      <div class="char-count">{{ content.length }}/10000</div>

      <div class="tag-section">
        <div class="section-label">标签（选填，最多 5 个）</div>
        <TagInput v-model="tags" :preset-tags="presetTags" :max-tags="5" placeholder="输入标签后按回车或逗号分隔..." />
      </div>

      <div v-if="error" class="error-msg">{{ error }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { postApi, productApi } from '../services/api'
import TagInput from '../components/TagInput.vue'

const router = useRouter()
const title = ref('')
const content = ref('')
const postType = ref('DISCUSSION')
const error = ref(null)
const submitting = ref(false)
const images = ref([])
const tags = ref([])

const postTypes = [
  { value: 'DISCUSSION', label: '讨论' },
  { value: 'SHOWCASE', label: '展示' },
  { value: 'HELP', label: '求助' },
  { value: 'ACTIVITY', label: '活动' }
]

const presetTags = [
  '数码', '书籍', '生活', '运动', '美食', '旅行', '摄影',
  '音乐', '电影', '游戏', '学习', '求职', '考研', '留学',
  '二手', '闲置', '求助', '分享', '经验', '吐槽'
]

const canSubmit = computed(() => title.value.trim().length >= 2 && content.value.trim().length > 0 && !submitting.value)

function handleFiles(e) {
  const files = Array.from(e.target.files)
  files.forEach(file => {
    if (images.value.length >= 9) return
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      error.value = '仅支持 JPG / PNG / WEBP 格式'
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      error.value = '单张图片不能超过 5MB'
      return
    }
    error.value = null
    const imgObj = { url: '', uploading: true, progress: 0 }
    images.value.push(imgObj)
    const idx = images.value.length - 1

    const reader = new FileReader()
    reader.onprogress = (evt) => {
      if (evt.lengthComputable) {
        images.value[idx].progress = Math.round((evt.loaded / evt.total) * 100)
      }
    }
    reader.onload = () => {
      images.value[idx].url = reader.result
      images.value[idx].uploading = false
    }
    reader.readAsDataURL(file)
  })
  e.target.value = ''
}

function removeImage(idx) {
  images.value.splice(idx, 1)
}

async function submitPost() {
  if (!canSubmit.value) return
  error.value = null
  submitting.value = true
  try {
    const imageUrls = images.value.filter(i => i.url).map(i => i.url)
    const res = await postApi.createPost({
      title: title.value.trim(),
      content: content.value.trim(),
      postType: postType.value,
      imageUrls: imageUrls.length > 0 ? imageUrls : null,
      coverImage: imageUrls.length > 0 ? imageUrls[0] : null,
      tags: tags.value.length > 0 ? tags.value : null
    })
    if (res.code === 200) {
      router.push(`/community/posts/${res.data.id}`)
    } else {
      error.value = res.message || '发布失败'
    }
  } catch (e) {
    error.value = '发布失败，请检查网络连接'
  } finally {
    submitting.value = false
  }
}
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
.image-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.image-item { position: relative; aspect-ratio: 1; border-radius: 8px; overflow: hidden; background: #f5f5f5; }
.preview-img { width: 100%; height: 100%; object-fit: cover; }
.uploading-mask { position: absolute; inset: 0; background: rgba(0,0,0,0.4); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 6px; }
.upload-progress { color: #fff; font-size: 12px; font-weight: 600; }
.upload-spinner { width: 24px; height: 24px; border: 3px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.img-remove-btn {
  position: absolute; top: 4px; right: 4px; width: 22px; height: 22px;
  background: rgba(0,0,0,0.5); border-radius: 50%; display: flex;
  align-items: center; justify-content: center; cursor: pointer; border: none;
}
.upload-trigger {
  aspect-ratio: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 4px;
  background: #fafafa; border: 2px dashed #ddd; border-radius: 8px; cursor: pointer;
}
.upload-trigger:active { background: #FFF7E6; }
.upload-count { font-size: 12px; color: #bbb; }
.upload-tip { margin-top: 10px; font-size: 12px; color: #999; }

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

.error-msg { color: #ff4757; font-size: 14px; padding: 8px 0; }
</style>
