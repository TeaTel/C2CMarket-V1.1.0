<template>
  <div class="comment-section">
    <h4 class="section-title">评论 ({{ totalCount }})</h4>

    <div v-if="loading" class="loading">
      <div class="skeleton" v-for="i in 3" :key="i" style="height:60px;margin-bottom:12px;border-radius:8px;background:#f0f0f0;"></div>
    </div>

    <div v-else-if="comments.length === 0" class="empty">暂无评论，来说两句吧</div>

    <div v-else class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item" :class="{ 'has-replies': comment.replies?.length }">
        <div class="comment-main">
          <img :src="comment.userAvatar || defaultAvatar" class="comment-avatar" @error="onAvatarError" />
          <div class="comment-body">
            <div class="comment-header">
              <span class="comment-user">{{ comment.userName || '匿名用户' }}</span>
              <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
            </div>
            <p class="comment-content">{{ comment.content }}</p>
            <div class="comment-actions">
              <LikeButton :is-liked="comment.isLiked" :count="comment.likeCount" target-type="COMMENT" :target-id="comment.id" @toggled="(l,c) => onCommentLikeToggled(comment, l, c)" />
              <button class="reply-btn" @click="startReply(comment)">回复</button>
              <button v-if="comment.userId === currentUserId" class="delete-btn" @click="deleteComment(comment.id)">删除</button>
            </div>
          </div>
        </div>

        <div v-if="comment.replies?.length" class="replies">
          <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
            <img :src="reply.userAvatar || defaultAvatar" class="reply-avatar" @error="onAvatarError" />
            <div class="reply-body">
              <div class="reply-header">
                <span class="reply-user">{{ reply.userName || '匿名用户' }}</span>
                <span v-if="reply.parentId" class="reply-to">回复 {{ getReplyTargetName(reply) }}</span>
                <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
              </div>
              <p class="reply-content">{{ reply.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="auth.isAuthenticated" class="comment-input-bar">
      <input v-model="newComment" :placeholder="replyTo ? '回复 ' + replyTo.userName + '...' : '写下你的评论...'" @keyup.enter="submitComment" maxlength="500" />
      <button :disabled="!newComment.trim()" @click="submitComment">发送</button>
      <button v-if="replyTo" class="cancel-reply" @click="cancelReply">取消</button>
    </div>
    <div v-else class="login-hint">
      <router-link to="/login">登录</router-link> 后参与评论
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { postApi } from '../services/api'
import { useAuthStore } from '../store/auth'
import LikeButton from './LikeButton.vue'

const props = defineProps({
  postId: { type: [Number, String], required: true },
  initialComments: { type: Array, default: () => [] }
})

const auth = useAuthStore()
const comments = ref(props.initialComments || [])
const totalCount = ref(0)
const loading = ref(false)
const newComment = ref('')
const replyTo = ref(null)
const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#eee"/><circle cx="20" cy="15" r="8" fill="#ccc"/><ellipse cx="20" cy="35" rx="12" ry="8" fill="#ccc"/></svg>')

const currentUserId = auth.currentUser?.id

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return date.toLocaleDateString()
}

function onAvatarError(e) { e.target.src = defaultAvatar }

function startReply(comment) {
  replyTo.value = comment
  newComment.value = ''
}

function cancelReply() {
  replyTo.value = null
  newComment.value = ''
}

function getReplyTargetName(reply) {
  return ''
}

async function submitComment() {
  if (!newComment.value.trim()) return
  try {
    const data = { content: newComment.value.trim() }
    if (replyTo.value) data.parentId = replyTo.value.id

    const res = await postApi.addComment(props.postId, data)
    if (res.code === 200) {
      if (replyTo.value) {
        if (!replyTo.value.replies) replyTo.value.replies = []
        replyTo.value.replies.push(res.data)
        cancelReply()
      } else {
        comments.value.unshift(res.data)
      }
      newComment.value = ''
    }
  } catch (e) {}
}

async function deleteComment(commentId) {
  try {
    await postApi.deleteComment(commentId)
    comments.value = comments.value.filter(c => c.id !== commentId)
  } catch (e) {}
}

function onCommentLikeToggled(comment, isLiked, count) {
  comment.isLiked = isLiked
  comment.likeCount = count
}

async function loadComments() {
  loading.value = true
  try {
    const res = await postApi.getComments(props.postId)
    if (res.code === 200) {
      comments.value = res.data || []
    }
  } catch (e) {} finally {
    loading.value = false
  }
}
</script>

<style scoped>
.comment-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}
.empty {
  text-align: center;
  color: #999;
  padding: 30px 0;
  font-size: 14px;
}
.comment-item {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f5f5f5;
}
.comment-main {
  display: flex;
  gap: 10px;
}
.comment-avatar, .reply-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  background: #eee;
  flex-shrink: 0;
}
.comment-body { flex: 1; }
.comment-header, .reply-header {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 4px;
  flex-wrap: wrap;
}
.comment-user, .reply-user { font-size: 13px; font-weight: 600; color: #333; }
.comment-time, .reply-time { font-size: 12px; color: #999; }
.reply-to { font-size: 12px; color: #ff6a00; }
.comment-content, .reply-content {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  margin-bottom: 6px;
}
.comment-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
.reply-btn, .delete-btn {
  font-size: 12px;
  color: #999;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}
.reply-btn:hover { color: #ff6a00; }
.delete-btn:hover { color: #ff4757; }

.replies {
  margin-left: 42px;
  margin-top: 10px;
  padding-left: 12px;
  border-left: 2px solid #f0f0f0;
}
.reply-item {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}
.reply-body { flex: 1; }

.comment-input-bar {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
.comment-input-bar input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #eee;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
  background: #f8f8f8;
}
.comment-input-bar input:focus { border-color: #ff6a00; background: #fff; }
.comment-input-bar button {
  padding: 8px 18px;
  border-radius: 20px;
  border: none;
  background: linear-gradient(135deg, #ff6a00, #ff9500);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
}
.comment-input-bar button:disabled { opacity: 0.5; cursor: not-allowed; }
.cancel-reply { background: #f0f0f0 !important; color: #666 !important; }
.login-hint {
  text-align: center;
  padding: 16px;
  color: #999;
  font-size: 14px;
}
.login-hint a { color: #ff6a00; }
.loading { padding: 20px 0; }
</style>
