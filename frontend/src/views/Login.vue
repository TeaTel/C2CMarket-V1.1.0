<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>
    
    <!-- 登录卡片 -->
    <div class="login-container">
      <div class="login-card card">
        <!-- 卡片头部 -->
        <div class="card-header">
          <div class="brand-section">
            <div class="brand-icon">
              <span>🛒</span>
            </div>
            <div class="brand-text">
              <h1 class="brand-title">校园二手交易平台</h1>
              <p class="brand-subtitle">欢迎回来，请登录您的账户</p>
            </div>
          </div>
        </div>
        
        <!-- 卡片主体 -->
        <div class="card-body">
          <form @submit.prevent="handleLogin" class="login-form">
            <!-- 用户名输入 -->
            <div class="form-group">
              <label for="username" class="form-label">
                <span class="label-icon">👤</span>
                用户名
              </label>
              <div class="input-wrapper">
                <input
                  type="text"
                  id="username"
                  v-model="username"
                  class="form-input"
                  placeholder="请输入用户名"
                  required
                  :disabled="loading"
                />
                <div class="input-icon">
                  <span>📝</span>
                </div>
              </div>
            </div>
            
            <!-- 密码输入 -->
            <div class="form-group">
              <label for="password" class="form-label">
                <span class="label-icon">🔒</span>
                密码
              </label>
              <div class="input-wrapper">
                <input
                  :type="showPassword ? 'text' : 'password'"
                  id="password"
                  v-model="password"
                  class="form-input"
                  placeholder="请输入密码"
                  required
                  :disabled="loading"
                />
                <button
                  type="button"
                  class="password-toggle"
                  @click="showPassword = !showPassword"
                  :disabled="loading"
                >
                  <span>{{ showPassword ? '👁️' : '👁️‍🗨️' }}</span>
                </button>
              </div>
            </div>
            
            <!-- 记住我选项 -->
            <div class="form-options">
              <label class="checkbox-label">
                <input
                  type="checkbox"
                  v-model="rememberMe"
                  class="checkbox-input"
                  :disabled="loading"
                />
                <span class="checkbox-custom"></span>
                <span class="checkbox-text">记住我</span>
              </label>
              
              <router-link to="/forgot-password" class="forgot-password">
                忘记密码？
              </router-link>
            </div>
            
            <!-- 错误提示 -->
            <div v-if="errorMessage" class="alert alert-error">
              <span class="alert-icon">⚠️</span>
              <span class="alert-text">{{ errorMessage }}</span>
            </div>
            
            <!-- 提交按钮 -->
            <button
              type="submit"
              class="btn btn-primary btn-lg w-full"
              :disabled="loading"
            >
              <span v-if="loading" class="loading-spinner"></span>
              <span>{{ loading ? '登录中...' : '登录' }}</span>
            </button>
            
            <!-- 社交登录 -->
            <div class="social-login">
              <div class="divider">
                <span class="divider-text">或使用以下方式登录</span>
              </div>
              
              <div class="social-buttons">
                <button type="button" class="social-btn wechat-btn" :disabled="loading">
                  <span class="social-icon">💬</span>
                  <span class="social-text">微信登录</span>
                </button>
                <button type="button" class="social-btn qq-btn" :disabled="loading">
                  <span class="social-icon">🐧</span>
                  <span class="social-text">QQ登录</span>
                </button>
              </div>
            </div>
            
            <!-- 注册链接 -->
            <div class="register-section">
              <p class="register-text">
                还没有账号？
                <router-link to="/register" class="register-link">
                  立即注册
                </router-link>
              </p>
            </div>
          </form>
        </div>
        
        <!-- 卡片底部 -->
        <div class="card-footer">
          <p class="footer-text">
            登录即表示您同意我们的
            <a href="#" class="footer-link">服务条款</a>
            和
            <a href="#" class="footer-link">隐私政策</a>
          </p>
        </div>
      </div>
      
      <!-- 返回首页链接 -->
      <div class="back-home">
        <router-link to="/" class="back-link">
          <span class="back-icon">←</span>
          <span class="back-text">返回首页</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)
const rememberMe = ref(false)

// 从本地存储加载记住的用户名
onMounted(() => {
  const savedUsername = localStorage.getItem('rememberedUsername')
  if (savedUsername) {
    username.value = savedUsername
    rememberMe.value = true
  }
})

async function handleLogin() {
  // 验证输入
  if (!username.value.trim()) {
    errorMessage.value = '请输入用户名'
    return
  }
  
  if (!password.value) {
    errorMessage.value = '请输入密码'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const result = await authStore.login(username.value.trim(), password.value)
    
    if (result.success) {
      // 如果选择了记住我，保存用户名
      if (rememberMe.value) {
        localStorage.setItem('rememberedUsername', username.value.trim())
      } else {
        localStorage.removeItem('rememberedUsername')
      }
      
      // 显示成功消息
      showSuccessMessage()
      
      // 延迟跳转到首页
      setTimeout(() => {
        router.push('/')
      }, 1500)
    } else {
      errorMessage.value = result.message || '登录失败，请检查用户名和密码'
    }
  } catch (error) {
    console.error('登录错误:', error)
    errorMessage.value = '登录过程中发生错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

function showSuccessMessage() {
  // 这里可以添加登录成功的动画或提示
  // 暂时使用控制台日志
  console.log('登录成功！')
}

// 社交登录函数（占位符）
function handleWechatLogin() {
  alert('微信登录功能正在开发中...')
}

function handleQQLogin() {
  alert('QQ登录功能正在开发中...')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary-50) 0%, var(--color-secondary-50) 100%);
  padding: var(--space-4);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.1), rgba(255, 152, 0, 0.1));
  animation: float 20s infinite ease-in-out;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -150px;
  right: -150px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -100px;
  left: -100px;
  animation-delay: 5s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 10%;
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  33% {
    transform: translateY(-20px) rotate(120deg);
  }
  66% {
    transform: translateY(20px) rotate(240deg);
  }
}

/* 登录容器 */
.login-container {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 440px;
}

/* 登录卡片 */
.login-card {
  background-color: var(--color-bg-primary);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  margin-bottom: var(--space-6);
}

/* 卡片头部 */
.card-header {
  padding: var(--space-8) var(--space-8) var(--space-6);
  background: linear-gradient(135deg, var(--color-primary-500), var(--color-primary-700));
  color: white;
  text-align: center;
}

.brand-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
}

.brand-icon {
  width: 64px;
  height: 64px;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  backdrop-filter: blur(4px);
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.brand-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: white;
  margin: 0;
}

.brand-subtitle {
  font-size: var(--font-size-sm);
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

/* 卡片主体 */
.card-body {
  padding: var(--space-8);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

/* 表单组 */
.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
}

.label-icon {
  font-size: 1rem;
}

.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  padding: var(--space-3) var(--space-10) var(--space-3) var(--space-10);
  border: 1px solid var(--color-border-medium);
  border-radius: var(--radius-lg);
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  background-color: var(--color-bg-primary);
  transition: var(--transition-fast);
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary-500);
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
}

.form-input::placeholder {
  color: var(--color-text-tertiary);
}

.form-input:disabled {
  background-color: var(--color-gray-50);
  cursor: not-allowed;
}

.input-icon {
  position: absolute;
  left: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-gray-500);
  font-size: 1.125rem;
  pointer-events: none;
}

.password-toggle {
  position: absolute;
  right: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--color-gray-500);
  cursor: pointer;
  padding: var(--space-1);
  border-radius: var(--radius-sm);
  transition: var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
}

.password-toggle:hover:not(:disabled) {
  background-color: var(--color-gray-100);
  color: var(--color-gray-700);
}

.password-toggle:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--space-1);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
  user-select: none;
}

.checkbox-input {
  display: none;
}

.checkbox-custom {
  width: 16px;
  height: 16px;
  border: 2px solid var(--color-border-medium);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition-fast);
}

.checkbox-input:checked + .checkbox-custom {
  background-color: var(--color-primary-500);
  border-color: var(--color-primary-500);
}

.checkbox-input:checked + .checkbox-custom::after {
  content: '✓';
  color: white;
  font-size: 10px;
  font-weight: bold;
}

.checkbox-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.forgot-password {
  font-size: var(--font-size-sm);
  color: var(--color-primary-600);
  text-decoration: none;
  transition: var(--transition-fast);
}

.forgot-password:hover {
  color: var(--color-primary-700);
  text-decoration: underline;
}

/* 警告框 */
.alert {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
}

.alert-error {
  background-color: #fee2e2;
  border: 1px solid #fecaca;
  color: #991b1b;
}

.alert-icon {
  font-size: 1rem;
}

.alert-text {
  flex: 1;
}

/* 按钮 */
.w-full {
  width: 100%;
}

.btn-lg {
  padding: var(--space-3) var(--space-6);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: var(--space-2);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 社交登录 */
.social-login {
  margin-top: var(--space-4);
}

.divider {
  display: flex;
  align-items: center;
  margin: var(--space-4) 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background-color: var(--color-border-light);
}

.divider-text {
  padding: 0 var(--space-3);
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  white-space: nowrap;
}

.social-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-3);
}

.social-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border-medium);
  border-radius: var(--radius-lg);
  background-color: var(--color-bg-primary);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: var(--transition-fast);
}

.social-btn:hover:not(:disabled) {
  background-color: var(--color-gray-50);
  transform: translateY(-1px);
}

.social-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.wechat-btn:hover:not(:disabled) {
  border-color: #07C160;
  color: #07C160;
}

.qq-btn:hover:not(:disabled) {
  border-color: #12B7F5;
  color: #12B7F5;
}

.social-icon {
  font-size: 1.125rem;
}

.social-text {
  white-space: nowrap;
}

/* 注册部分 */
.register-section {
  margin-top: var(--space-4);
  text-align: center;
}

.register-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin: 0;
}

.register-link {
  color: var(--color-primary-600);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
  transition: var(--transition-fast);
}

.register-link:hover {
  color: var(--color-primary-700);
  text-decoration: underline;
}

/* 卡片底部 */
.card-footer {
  padding: var(--space-4) var(--space-8);
  border-top: 1px solid var(--color-border-light);
  background-color: var(--color-bg-secondary);
  text-align: center;
}

.footer-text {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin: 0;
}

.footer-link {
  color: var(--color-primary-600);
  text-decoration: none;
  transition: var(--transition-fast);
}

.footer-link:hover {
  color: var(--color-primary-700);
  text-decoration: underline;
}

/* 返回首页链接 */
.back-home {
  text-align: center;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: var(--font-size-sm);
  border-radius: var(--radius-md);
  transition: var(--transition-fast);
}

.back-link:hover {
  background-color: var(--color-gray-50);
  color: var(--color-text-primary);
}

.back-icon {
  font-size: 1rem;
}

.back-text {
  white-space: nowrap;
}

/* 响应式设计 */
@media (max-width: 640px) {
  .login-page {
    padding: var(--space-2);
  }
  
  .login-card {
    margin-bottom: var(--space-4);
  }
  
  .card-header {
    padding: var(--space-6) var(--space-6) var(--space-4);
  }
  
  .card-body {
    padding: var(--space-6);
  }
  
  .brand-icon {
    width: 48px;
    height: 48px;
    font-size: 1.5rem;
  }
  
  .brand-title {
    font-size: var(--font-size-xl);
  }
  
  .brand-subtitle {
    font-size: var(--font-size-xs);
  }
  
  .social-buttons {
    grid-template-columns: 1fr;
  }
  
  .card-footer {
    padding: var(--space-3) var(--space-6);
  }
  
  .circle-1,
  .circle-2,
  .circle-3 {
    display: none;
  }
}

@media (max-width: 480px) {
  .card-header {
    padding: var(--space-4) var(--space-4) var(--space-3);
  }
  
  .card-body {
    padding: var(--space-4);
  }
  
  .form-options {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-2);
  }
  
  .forgot-password {
    align-self: flex-end;
  }
}
</style>