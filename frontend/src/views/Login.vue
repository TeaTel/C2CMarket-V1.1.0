<template>
  <div class="login-page">
    <!-- 品牌Logo区域 -->
    <div class="brand-section">
      <div class="logo-wrapper">
        <div class="logo-icon">🎓</div>
      </div>
      <h1 class="brand-title">校园二手</h1>
      <p class="brand-subtitle">让闲置流动起来</p>
    </div>

    <!-- 登录表单 -->
    <main class="form-container">
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="input-group">
          <div class="input-wrapper" :class="{ focused: focusState.username, filled: form.username }">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <input
              type="text"
              v-model="form.username"
              placeholder="学号/手机号/邮箱"
              @focus="focusState.username = true"
              @blur="focusState.username = false"
              autocomplete="username"
            />
          </div>
        </div>

        <div class="input-group">
          <div class="input-wrapper" :class="{ focused: focusState.password, filled: form.password }">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2"/>
              <path d="M7 11V7a5 5 0 1110 0v4"/>
            </svg>
            <input
              :type="showPassword ? 'text' : 'password'"
              v-model="form.password"
              placeholder="请输入密码"
              @focus="focusState.password = true"
              @blur="focusState.password = false"
              autocomplete="current-password"
            />
            <button
              type="button"
              @click="showPassword = !showPassword"
              class="toggle-password"
            >
              <svg v-if="!showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="form-options">
          <label class="remember-me">
            <input type="checkbox" v-model="rememberMe" />
            <span>记住我</span>
          </label>
          <router-link to="/forgot-password" class="forgot-link">忘记密码?</router-link>
        </div>

        <button
          type="submit"
          :disabled="loading || !isFormValid"
          class="submit-btn"
          :class="{ active: isFormValid }"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </button>

        <!-- 错误提示 -->
        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <!-- 分割线 -->
        <div class="divider">
          <span>其他登录方式</span>
        </div>

        <!-- 第三方登录 -->
        <div class="social-login">
          <button type="button" class="social-btn wechat" title="微信登录">
            <svg viewBox="0 0 24 24" fill="#07C160" width="24" height="24">
              <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178A1.17 1.17 0 014.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178 1.17 1.17 0 01-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 01.598.082l1.584.926a.272.272 0 00.14.045c.134 0 .24-.111.24-.247 0-.06-.023-.118-.038-.177l-.327-1.233a.582.582 0 01.176-.554C23.438 18.142 24 16.494 24 14.694c0-3.442-3.155-6.282-7.062-6.836zm-2.821 2.571c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.969-.982z"/>
            </svg>
          </button>
          <button type="button" class="social-btn student" title="校园认证">
            <span>📚</span>
          </button>
        </div>
      </form>

      <p class="register-link">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </p>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { userApi } from '../services/api'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 表单状态
const form = ref({
  username: '',
  password: ''
})

const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)
const rememberMe = ref(false)

const focusState = ref({
  username: false,
  password: false
})

// 表单验证
const isFormValid = computed(() => {
  return form.value.username.trim().length > 0 && form.value.password.length >= 6
})

// 登录处理
async function handleLogin() {
  if (!isFormValid.value || loading.value) return

  try {
    loading.value = true
    errorMessage.value = ''

    // 使用authStore的login方法（自动处理token和用户信息的保存）
    const result = await authStore.login(
      form.value.username,
      form.value.password
    )

    if (result.success) {
      // 跳转到之前的页面或首页
      const redirectPath = route.query.redirect || '/'
      router.push(redirectPath)
    } else {
      errorMessage.value = result.message || '登录失败，请检查账号密码'
    }
  } catch (error) {
    console.error('登录失败:', error)

    // 根据错误类型显示不同提示
    if (error.code === 'NETWORK_ERROR' || error.code === 'TIMEOUT') {
      errorMessage.value = error.message || '网络错误，请稍后重试'
    } else if (error.status === 401) {
      errorMessage.value = '账号或密码错误'
    } else {
      errorMessage.value = error.message || '登录失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FF6A00 0%, #FF8533 50%, #f5f5f5 50%);
  display: flex;
  flex-direction: column;
}

/* ============================================
   品牌区域
   ============================================ */
.brand-section {
  padding: 80px 32px 50px;
  text-align: center;
  color: white;
}

.logo-wrapper {
  margin-bottom: 16px;
}

.logo-icon {
  font-size: 64px;
  filter: drop-shadow(0 4px 12px rgba(255, 106, 0, 0.3));
  animation: bounce 2s ease infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.brand-title {
  font-size: 32px;
  font-weight: 800;
  margin: 0 0 8px 0;
  letter-spacing: 4px;
}

.brand-subtitle {
  font-size: 15px;
  opacity: 0.9;
  margin: 0;
  letter-spacing: 2px;
}

/* ============================================
   表单容器
   ============================================ */
.form-container {
  flex: 1;
  background-color: #fff;
  border-radius: 28px 28px 0 0;
  padding: 36px 28px 40px;
  box-shadow: 0 -8px 30px rgba(0, 0, 0, 0.08);
}

.login-form {
  max-width: 400px;
  margin: 0 auto;
}

.input-group {
  margin-bottom: 20px;
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background-color: #fafafa;
  border: 2px solid transparent;
  border-radius: 14px;
  transition: all 0.25s ease;
}

.input-wrapper.focused,
.input-wrapper:focus-within {
  background-color: #fff;
  border-color: #FF6A00;
  box-shadow: 0 0 0 4px rgba(255, 106, 0, 0.08);
}

.input-icon {
  width: 22px;
  height: 22px;
  color: #bbb;
  flex-shrink: 0;
  transition: color 0.25s ease;
}

.input-wrapper.focused .input-icon {
  color: #FF6A00;
}

.input-wrapper input {
  flex: 1;
  border: none;
  background: none;
  font-size: 16px;
  color: #333;
  outline: none;
}

.input-wrapper input::placeholder {
  color: #ccc;
}

.toggle-password {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  cursor: pointer;
  transition: color 0.2s ease;
}

.toggle-password svg {
  width: 20px;
  height: 20px;
}

.toggle-password:active {
  color: #FF6A00;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
}

.remember-me input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: #FF6A00;
}

.forgot-link {
  font-size: 13px;
  color: #FF6A00;
  font-weight: 500;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  padding: 15px;
  background-color: #e0e0e0;
  color: #999;
  border: none;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 700;
  cursor: not-allowed;
  transition: all 0.3s ease;
  letter-spacing: 4px;
}

.submit-btn.active {
  background: linear-gradient(135deg, #FF6A00 0%, #FF8533 100%);
  color: white;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(255, 106, 0, 0.35);
}

.submit-btn.active:active {
  transform: scale(0.98);
  box-shadow: 0 4px 12px rgba(255, 106, 0, 0.45);
}

.submit-btn:disabled {
  opacity: 0.7;
}

.error-message {
  margin-top: 16px;
  padding: 12px;
  background-color: #FFF1F0;
  border: 1px solid #FFCCC7;
  border-radius: 10px;
  color: #FF4D4F;
  font-size: 13px;
  text-align: center;
}

/* 分割线 */
.divider {
  position: relative;
  margin: 28px 0 20px;
  text-align: center;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 40%;
  height: 1px;
  background-color: #e8e8e8;
}

.divider::before { left: 0; }
.divider::after { right: 0; }

.divider span {
  position: relative;
  padding: 0 12px;
  background-color: #fff;
  font-size: 13px;
  color: #bbb;
}

/* 社交登录 */
.social-login {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.social-btn {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s ease;
  background-color: #fff;
}

.social-btn:active {
  transform: scale(0.92);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.social-btn span {
  font-size: 24px;
}

/* 注册链接 */
.register-link {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: #999;
}

.register-link a {
  color: #FF6A00;
  font-weight: 600;
}
</style>
