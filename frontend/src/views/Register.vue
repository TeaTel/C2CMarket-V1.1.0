<template>
  <div class="register-page">
    <!-- 顶部导航 -->
    <header class="page-header">
      <button @click="$router.back()" class="back-btn">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <polyline points="15,18 9,12 15,6"/>
        </svg>
      </button>
      <h1 class="header-title">注册账号</h1>
      <div style="width: 36px;"></div>
    </header>

    <!-- 注册表单 -->
    <main class="form-container">
      <form @submit.prevent="handleRegister" class="register-form">
        <div class="input-group">
          <div class="input-wrapper" :class="{ focused: focusState.username }">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <input
              type="text"
              v-model="form.username"
              placeholder="学号/手机号"
              @focus="focusState.username = true"
              @blur="focusState.username = false"
            />
          </div>
        </div>

        <div class="input-group">
          <div class="input-wrapper" :class="{ focused: focusState.nickname }">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
              <line x1="1" y1="1" x2="23" y2="23"/>
            </svg>
            <input
              type="text"
              v-model="form.nickname"
              placeholder="昵称（2-12字）"
              maxlength="12"
              @focus="focusState.nickname = true"
              @blur="focusState.nickname = false"
            />
          </div>
        </div>

        <div class="input-group">
          <div class="input-wrapper" :class="{ focused: focusState.email }">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
              <polyline points="22,6 12,13 2,6"/>
            </svg>
            <input
              type="email"
              v-model="form.email"
              placeholder="校园邮箱（选填）"
              @focus="focusState.email = true"
              @blur="focusState.email = false"
            />
          </div>
        </div>

        <div class="input-group">
          <div class="input-wrapper" :class="{ focused: focusState.password }">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2"/>
              <path d="M7 11V7a5 5 0 1110 0v4"/>
            </svg>
            <input
              :type="showPassword ? 'text' : 'password'"
              v-model="form.password"
              placeholder="密码（至少6位）"
              @focus="focusState.password = true"
              @blur="focusState.password = false"
            />
            <button type="button" @click="showPassword = !showPassword" class="toggle-pwd">
              <svg v-if="!showPassword" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="input-group">
          <div class="input-wrapper" :class="{ focused: focusState.confirmPwd }">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
            <input
              :type="showConfirm ? 'text' : 'password'"
              v-model="form.confirmPassword"
              placeholder="确认密码"
              @focus="focusState.confirmPwd = true"
              @blur="focusState.confirmPwd = false"
            />
          </div>
        </div>

        <label class="agreement-check">
          <input type="checkbox" v-model="agreeTerms" />
          <span>我已阅读并同意</span>
          <a href="#" @click.prevent>《用户协议》</a>
          <span>和</span>
          <a href="#" @click.prevent>《隐私政策》</a>
        </label>

        <button
          type="submit"
          :disabled="loading || !isFormValid"
          class="submit-btn"
          :class="{ active: isFormValid }"
        >
          {{ loading ? '注册中...' : '注 册' }}
        </button>

        <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>

        <p class="login-link">
          已有账号？
          <router-link to="/login">立即登录</router-link>
        </p>
      </form>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)
const showConfirm = ref(false)
const agreeTerms = ref(false)

const focusState = ref({
  username: false,
  nickname: false,
  email: false,
  password: false,
  confirmPwd: false
})

const isFormValid = computed(() => {
  return (
    form.value.username.trim().length >= 4 &&
    form.value.nickname.trim().length >= 2 &&
    form.value.password.length >= 6 &&
    form.value.password === form.value.confirmPassword &&
    agreeTerms.value
  )
})

async function handleRegister() {
  if (!isFormValid.value || loading.value) return

  if (form.value.password !== form.value.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }

  try {
    loading.value = true
    errorMessage.value = ''

    // 使用authStore的register方法（自动处理token和用户信息的保存）
    const result = await authStore.register({
      username: form.value.username,
      nickname: form.value.nickname,
      email: form.value.email || undefined,
      password: form.value.password
    })

    if (result.success) {
      // 注册成功后跳转到登录页
      router.push({
        path: '/login',
        query: { registered: 'true', username: form.value.username }
      })
    } else {
      errorMessage.value = result.message || '注册失败，请稍后重试'
    }
  } catch (error) {
    console.error('注册失败:', error)

    // 根据错误类型显示不同提示
    if (error.code === 'NETWORK_ERROR' || error.code === 'TIMEOUT') {
      errorMessage.value = error.message || '网络错误，请稍后重试'
    } else if (error.status === 400) {
      errorMessage.value = error.message || '注册信息有误，请检查输入'
    } else if (error.status === 409) {
      errorMessage.value = '该用户名已被注册'
    } else {
      errorMessage.value = error.message || '注册失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.back-btn {
  width: 36px; height: 36px;
  display: flex; align-items: center; justify-content: center;
  color: #333; border-radius: 50%;
}
.back-btn svg { width: 22px; height: 22px; }

.header-title {
  font-size: 17px; font-weight: 600; color: #333; margin: 0;
}

.form-container {
  padding: 28px 24px;
  max-width: 420px;
  margin: 0 auto;
}

.input-group { margin-bottom: 18px; }

.input-wrapper {
  display: flex; align-items: center; gap: 12px;
  padding: 13px 15px;
  background-color: #fafafa;
  border: 2px solid transparent;
  border-radius: 12px;
  transition: all 0.25s ease;
}
.input-wrapper.focused, .input-wrapper:focus-within {
  background-color: #fff;
  border-color: #FF6A00;
  box-shadow: 0 0 0 3px rgba(255, 106, 0, 0.08);
}
.input-icon { width: 20px; height: 20px; color: #bbb; flex-shrink: 0; }
.input-wrapper.focused .input-icon { color: #FF6A00; }
.input-wrapper input {
  flex: 1; border: none; background: none;
  font-size: 15px; color: #333; outline: none;
}
.input-wrapper input::placeholder { color: #ccc; }
.toggle-pwd {
  color: #ccc; cursor: pointer;
  display: flex; align-items: center;
}

.agreement-check {
  display: flex; flex-wrap: wrap; gap: 4px;
  font-size: 13px; color: #666; margin-bottom: 22px;
  cursor: pointer; line-height: 1.6;
}
.agreement-check input[type="checkbox"] {
  width: 16px; height: 16px; accent-color: #FF6A00; margin-top: 2px;
}
.agreement-check a { color: #FF6A00; }

.submit-btn {
  width: 100%; padding: 14px;
  background-color: #e0e0e0; color: #999;
  border: none; border-radius: 14px;
  font-size: 17px; font-weight: 700;
  cursor: not-allowed; transition: all 0.3s ease;
  letter-spacing: 4px;
}
.submit-btn.active {
  background: linear-gradient(135deg, #FF6A00 0%, #FF8533 100%);
  color: white; cursor: pointer;
  box-shadow: 0 8px 24px rgba(255, 106, 0, 0.35);
}
.submit-btn.active:active { transform: scale(0.98); }
.submit-btn:disabled { opacity: 0.7; }

.error-msg {
  margin-top: 14px; padding: 10px;
  background-color: #FFF1F0; border: 1px solid #FFCCC7;
  border-radius: 8px; color: #FF4D4F;
  font-size: 13px; text-align: center;
}

.login-link {
  text-align: center; margin-top: 24px;
  font-size: 14px; color: #999;
}
.login-link a { color: #FF6A00; font-weight: 600; }
</style>
