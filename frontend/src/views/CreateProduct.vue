<template>
  <div class="create-product">
    <NavBar />
    
    <main class="main-content">
      <div class="container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1 class="page-title">发布商品</h1>
          <div class="page-subtitle">
            填写商品信息，让闲置物品找到新主人
          </div>
        </div>
        
        <!-- 发布表单 -->
        <div class="create-form-container">
          <form @submit.prevent="handleSubmit" class="create-form">
            <!-- 基本信息 -->
            <div class="form-section">
              <h3 class="section-title">
                <span class="section-icon">📝</span>
                基本信息
              </h3>
              
              <div class="form-grid">
                <!-- 商品名称 -->
                <div class="form-group">
                  <label for="name" class="form-label required">
                    <span class="label-icon">🏷️</span>
                    商品名称
                  </label>
                  <input
                    type="text"
                    id="name"
                    v-model="formData.name"
                    class="form-input"
                    placeholder="请输入商品名称（如：二手iPhone 13）"
                    required
                    :disabled="submitting"
                  />
                  <div class="form-hint">简洁明了的名称更容易吸引买家</div>
                </div>
                
                <!-- 商品价格 -->
                <div class="form-group">
                  <label for="price" class="form-label required">
                    <span class="label-icon">💰</span>
                    商品价格
                  </label>
                  <div class="input-with-unit">
                    <input
                      type="number"
                      id="price"
                      v-model="formData.price"
                      class="form-input"
                      placeholder="0.00"
                      min="0"
                      step="0.01"
                      required
                      :disabled="submitting"
                    />
                    <span class="input-unit">元</span>
                  </div>
                  <div class="form-hint">请输入合理的价格</div>
                </div>
              </div>
              
              <!-- 商品描述 -->
              <div class="form-group">
                <label for="description" class="form-label required">
                  <span class="label-icon">📄</span>
                  商品描述
                </label>
                <textarea
                  id="description"
                  v-model="formData.description"
                  class="form-textarea"
                  placeholder="请详细描述商品信息，包括：品牌、型号、使用情况、有无瑕疵、配件情况等"
                  rows="5"
                  required
                  :disabled="submitting"
                ></textarea>
                <div class="form-hint">
                  <span class="char-count">{{ formData.description.length }}/500</span>
                  详细描述有助于买家了解商品
                </div>
              </div>
            </div>
            
            <!-- 商品图片 -->
            <div class="form-section">
              <h3 class="section-title">
                <span class="section-icon">🖼️</span>
                商品图片
              </h3>
              
              <div class="image-upload-section">
                <div class="upload-hint">
                  <p>上传清晰的商品图片能大大提高成交率</p>
                  <p class="hint-small">支持 JPG、PNG 格式，单张图片不超过 5MB</p>
                </div>
                
                <div class="image-upload-area" @click="triggerFileInput">
                  <div class="upload-placeholder" v-if="!formData.imageUrl">
                    <span class="upload-icon">📷</span>
                    <p class="upload-text">点击上传商品图片</p>
                    <p class="upload-subtext">或拖拽图片到这里</p>
                  </div>
                  <div class="image-preview" v-else>
                    <img :src="formData.imageUrl" alt="商品预览" class="preview-image" />
                    <button type="button" class="remove-image-btn" @click.stop="removeImage">
                      <span>×</span>
                    </button>
                  </div>
                  <input
                    type="file"
                    ref="fileInput"
                    @change="handleImageUpload"
                    accept="image/*"
                    class="file-input"
                    :disabled="submitting"
                  />
                </div>
                
                <div class="image-tips">
                  <div class="tip-item">
                    <span class="tip-icon">✅</span>
                    <span>建议上传多角度图片</span>
                  </div>
                  <div class="tip-item">
                    <span class="tip-icon">✅</span>
                    <span>确保图片清晰明亮</span>
                  </div>
                  <div class="tip-item">
                    <span class="tip-icon">✅</span>
                    <span>展示商品细节和瑕疵</span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 商品分类 -->
            <div class="form-section">
              <h3 class="section-title">
                <span class="section-icon">🏷️</span>
                商品分类
              </h3>
              
              <div class="form-grid">
                <!-- 主分类 -->
                <div class="form-group">
                  <label for="categoryId" class="form-label required">
                    <span class="label-icon">📂</span>
                    商品分类
                  </label>
                  <select
                    id="categoryId"
                    v-model="formData.categoryId"
                    class="form-select"
                    required
                    :disabled="submitting || loadingCategories"
                  >
                    <option value="" disabled>请选择商品分类</option>
                    <option
                      v-for="category in categories"
                      :key="category.id"
                      :value="category.id"
                    >
                      {{ category.name }}
                    </option>
                  </select>
                  <div class="form-hint">选择合适的分类让商品更容易被找到</div>
                </div>
                
                <!-- 商品状态 -->
                <div class="form-group">
                  <label class="form-label required">
                    <span class="label-icon">📊</span>
                    商品状态
                  </label>
                  <div class="status-options">
                    <label class="radio-label">
                      <input
                        type="radio"
                        v-model="formData.status"
                        value="1"
                        class="radio-input"
                        :disabled="submitting"
                      />
                      <span class="radio-custom"></span>
                      <span class="radio-text">在售</span>
                    </label>
                    <label class="radio-label">
                      <input
                        type="radio"
                        v-model="formData.status"
                        value="0"
                        class="radio-input"
                        :disabled="submitting"
                      />
                      <span class="radio-custom"></span>
                      <span class="radio-text">待审核</span>
                    </label>
                  </div>
                  <div class="form-hint">新发布的商品需要管理员审核</div>
                </div>
              </div>
            </div>
            
            <!-- 联系方式 -->
            <div class="form-section">
              <h3 class="section-title">
                <span class="section-icon">📞</span>
                联系方式
              </h3>
              
              <div class="form-grid">
                <!-- 联系电话 -->
                <div class="form-group">
                  <label for="contactPhone" class="form-label">
                    <span class="label-icon">📱</span>
                    联系电话
                  </label>
                  <input
                    type="tel"
                    id="contactPhone"
                    v-model="formData.contactPhone"
                    class="form-input"
                    placeholder="请输入联系电话（选填）"
                    :disabled="submitting"
                  />
                  <div class="form-hint">买家可以通过电话联系您</div>
                </div>
                
                <!-- 微信 -->
                <div class="form-group">
                  <label for="contactWechat" class="form-label">
                    <span class="label-icon">💬</span>
                    微信
                  </label>
                  <input
                    type="text"
                    id="contactWechat"
                    v-model="formData.contactWechat"
                    class="form-input"
                    placeholder="请输入微信号（选填）"
                    :disabled="submitting"
                  />
                  <div class="form-hint">买家可以通过微信联系您</div>
                </div>
              </div>
            </div>
            
            <!-- 错误提示 -->
            <div v-if="errorMessage" class="alert alert-error">
              <span class="alert-icon">⚠️</span>
              <span class="alert-text">{{ errorMessage }}</span>
            </div>
            
            <!-- 表单操作 -->
            <div class="form-actions">
              <router-link to="/products" class="btn btn-outline btn-lg">
                取消
              </router-link>
              <button
                type="submit"
                class="btn btn-primary btn-lg"
                :disabled="submitting || !isFormValid"
              >
                <span v-if="submitting" class="loading-spinner"></span>
                <span>{{ submitting ? '发布中...' : '发布商品' }}</span>
              </button>
            </div>
          </form>
          
          <!-- 发布提示 -->
          <div class="publish-tips">
            <h4 class="tips-title">
              <span class="tips-icon">💡</span>
              发布小贴士
            </h4>
            <ul class="tips-list">
              <li class="tip-item">
                <span class="tip-bullet">📸</span>
                <span>上传真实、清晰的商品图片</span>
              </li>
              <li class="tip-item">
                <span class="tip-bullet">💰</span>
                <span>设置合理的价格，参考市场价</span>
              </li>
              <li class="tip-item">
                <span class="tip-bullet">📝</span>
                <span>详细描述商品状况，包括瑕疵</span>
              </li>
              <li class="tip-item">
                <span class="tip-bullet">🔒</span>
                <span>交易时注意安全，建议校内面交</span>
              </li>
              <li class="tip-item">
                <span class="tip-bullet">📞</span>
                <span>保持联系方式畅通，及时回复</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import NavBar from '../components/NavBar.vue'
import { productApi, categoryApi } from '../services/api'

const router = useRouter()
const authStore = useAuthStore()
const fileInput = ref(null)

// 表单数据
const formData = ref({
  name: '',
  price: '',
  description: '',
  imageUrl: '',
  categoryId: '',
  status: '1', // 默认在售
  contactPhone: '',
  contactWechat: ''
})

// 状态管理
const categories = ref([])
const loadingCategories = ref(true)
const submitting = ref(false)
const errorMessage = ref('')

// 表单验证
const isFormValid = computed(() => {
  return (
    formData.value.name.trim() &&
    formData.value.price &&
    parseFloat(formData.value.price) > 0 &&
    formData.value.description.trim() &&
    formData.value.categoryId &&
    formData.value.status
  )
})

// 预设分类数据
const defaultCategories = [
  { id: 1, name: '电子产品', description: '手机、电脑、平板等电子设备' },
  { id: 2, name: '书籍教材', description: '教科书、参考书、小说等' },
  { id: 3, name: '生活用品', description: '日常生活中的各种用品' },
  { id: 4, name: '服装鞋帽', description: '衣服、鞋子、帽子等' },
  { id: 5, name: '运动器材', description: '体育用品和健身器材' },
  { id: 6, name: '其他', description: '其他未分类的物品' }
]

// 加载分类数据
async function loadCategories() {
  try {
    loadingCategories.value = true
    const response = await categoryApi.getCategories()
    if (response.code === 200) {
      categories.value = response.data || []
      // 如果API返回空数组，使用预设分类
      if (categories.value.length === 0) {
        categories.value = defaultCategories
      }
    } else {
      // API返回错误，使用预设分类
      console.warn('分类API返回错误，使用预设分类:', response.message)
      categories.value = defaultCategories
    }
  } catch (error) {
    console.error('加载分类失败，使用预设分类:', error)
    // 网络错误或其他异常，使用预设分类
    categories.value = defaultCategories
    errorMessage.value = '分类数据加载异常，已使用预设分类'
  } finally {
    loadingCategories.value = false
  }
}

// 触发文件选择
function triggerFileInput() {
  if (!submitting.value) {
    fileInput.value.click()
  }
}

// 处理图片上传
function handleImageUpload(event) {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    errorMessage.value = '请选择图片文件'
    return
  }

  // 验证文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    errorMessage.value = '图片大小不能超过5MB'
    return
  }

  // 创建本地预览URL
  const reader = new FileReader()
  reader.onload = (e) => {
    formData.value.imageUrl = e.target.result
  }
  reader.readAsDataURL(file)
  
  // 重置文件输入，允许选择同一文件
  event.target.value = ''
}

// 移除图片
function removeImage() {
  formData.value.imageUrl = ''
}

// 处理表单提交
async function handleSubmit() {
  if (!isFormValid.value) {
    errorMessage.value = '请填写所有必填项'
    return
  }

  // 验证价格
  const price = parseFloat(formData.value.price)
  if (isNaN(price) || price <= 0) {
    errorMessage.value = '请输入有效的价格'
    return
  }

  // 验证描述长度
  if (formData.value.description.length > 500) {
    errorMessage.value = '商品描述不能超过500字'
    return
  }

  submitting.value = true
  errorMessage.value = ''

  try {
    // 检查用户是否登录
    const token = localStorage.getItem('token')
    const isAuthenticated = !!token
    
    // 准备提交数据
    const productData = {
      name: formData.value.name.trim(),
      price: price,
      description: formData.value.description.trim(),
      imageUrl: formData.value.imageUrl,
      categoryId: formData.value.categoryId,
      status: parseInt(formData.value.status),
      contactPhone: formData.value.contactPhone.trim(),
      contactWechat: formData.value.contactWechat.trim()
    }

    // 如果用户未登录，使用模拟发布
    if (!isAuthenticated) {
      console.warn('用户未登录，使用模拟发布')
      errorMessage.value = '演示模式：商品发布成功（模拟）'
      
      // 模拟API响应延迟
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // 模拟成功响应
      const mockResponse = {
        code: 200,
        message: '商品发布成功',
        data: {
          id: Math.floor(Math.random() * 1000) + 1000,
          ...productData,
          createdAt: new Date().toISOString()
        }
      }
      
      // 显示成功消息
      errorMessage.value = '演示模式：商品发布成功！3秒后跳转到商品列表'
      
      // 3秒后跳转到商品列表
      setTimeout(() => {
        router.push('/products')
      }, 3000)
      
      return
    }

    // 调用API发布商品
    const response = await productApi.createProduct(productData)
    
    if (response.code === 200) {
      // 发布成功，跳转到商品详情页
      const productId = response.data?.id
      if (productId) {
        router.push(`/products/${productId}`)
      } else {
        router.push('/products')
      }
    } else {
      errorMessage.value = response.message || '发布失败，请稍后重试'
    }
  } catch (error) {
    console.error('发布商品失败:', error)
    
    // 更详细的错误处理
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        errorMessage.value = '用户未登录或登录已过期，请重新登录'
        // 清除本地token并跳转到登录页
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        setTimeout(() => {
          router.push('/login')
        }, 2000)
      } else if (status === 403) {
        errorMessage.value = '没有权限发布商品，请检查用户权限'
      } else if (status === 500) {
        errorMessage.value = '服务器内部错误，请稍后重试'
      } else if (data && data.message) {
        errorMessage.value = data.message
      } else {
        errorMessage.value = `发布失败，错误码: ${status}`
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      errorMessage.value = '网络连接失败，请检查网络设置'
    } else {
      // 请求配置错误
      errorMessage.value = '发布过程中发生错误，请检查表单数据'
    }
  } finally {
    submitting.value = false
  }
}

// 初始化
onMounted(() => {
  // 检查用户是否登录
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  
  loadCategories()
})
</script>

<style scoped>
.create-product {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: var(--space-6) 0;
  background-color: var(--color-bg-secondary);
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 var(--space-4);
}

/* 页面标题 */
.page-header {
  margin-bottom: var(--space-8);
  text-align: center;
}

.page-title {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-2);
}

.page-subtitle {
  font-size: var(--font-size-lg);
  color: var(--color-text-secondary);
  max-width: 600px;
  margin: 0 auto;
}

/* 表单容器 */
.create-form-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--space-8);
  margin-bottom: var(--space-8);
}

@media (max-width: 768px) {
  .create-form-container {
    grid-template-columns: 1fr;
  }
}

/* 表单样式 */
.create-form {
  background-color: var(--color-bg-primary);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
  padding: var(--space-8);
  box-shadow: var(--shadow-md);
}

/* 表单部分 */
.form-section {
  margin-bottom: var(--space-8);
  padding-bottom: var(--space-6);
  border-bottom: 1px solid var(--color-border-light);
}

.form-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.section-icon {
  font-size: 1.25rem;
}

/* 表单网格 */
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}

/* 表单组 */
.form-group {
  margin-bottom: var(--space-4);
}

.form-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-2);
}

.form-label.required::after {
  content: '*';
  color: var(--color-error);
  margin-left: 2px;
}

.label-icon {
  font-size: 1rem;
}

.form-input,
.form-textarea,
.form-select {
  width: 100%;
  padding: var(--space-3);
  border: 1px solid var(--color-border-medium);
  border-radius: var(--radius-lg);
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  background-color: var(--color-bg-primary);
  transition: var(--transition-fast);
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: var(--color-primary-500);
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
}

.form-input::placeholder,
.form-textarea::placeholder {
  color: var(--color-text-tertiary);
}

.form-input:disabled,
.form-textarea:disabled,
.form-select:disabled {
  background-color: var(--color-gray-50);
  cursor: not-allowed;
}

/* 带单位的输入框 */
.input-with-unit {
  position: relative;
}

.input-unit {
  position: absolute;
  right: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-tertiary);
  font-size: var(--font-size-sm);
}

/* 文本域 */
.form-textarea {
  min-height: 120px;
  resize: vertical;
}

/* 表单提示 */
.form-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin-top: var(--space-1);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.char-count {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
}

/* 图片上传区域 */
.image-upload-section {
  margin-top: var(--space-4);
}

.upload-hint {
  margin-bottom: var(--space-4);
}

.upload-hint p {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.hint-small {
  font-size: var(--font-size-xs) !important;
  color: var(--color-text-tertiary) !important;
  margin-top: var(--space-1) !important;
}

.image-upload-area {
  border: 2px dashed var(--color-border-medium);
  border-radius: var(--radius-lg);
  background-color: var(--color-bg-secondary);
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: var(--transition-fast);
  position: relative;
  overflow: hidden;
}

.image-upload-area:hover {
  border-color: var(--color-primary-500);
  background-color: var(--color-primary-50);
}

.upload-placeholder {
  text-align: center;
  color: var(--color-text-tertiary);
}

.upload-icon {
  font-size: 3rem;
  display: block;
  margin-bottom: var(--space-2);
}

.upload-text {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-medium);
  margin-bottom: var(--space-1);
}

.upload-subtext {
  font-size: var(--font-size-sm);
}

.image-preview {
  position: relative;
  width: 100%;
  height: 100%;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-md);
}

.remove-image-btn {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  width: 32px;
  height: 32px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  border: none;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: var(--transition-fast);
  font-size: 1.25rem;
}

.remove-image-btn:hover {
  background-color: rgba(0, 0, 0, 0.9);
  transform: scale(1.1);
}

.file-input {
  display: none;
}

/* 图片提示 */
.image-tips {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-3);
  margin-top: var(--space-4);
}

@media (max-width: 640px) {
  .image-tips {
    grid-template-columns: 1fr;
  }
}

.tip-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.tip-icon {
  font-size: 1rem;
}

/* 状态选项 */
.status-options {
  display: flex;
  gap: var(--space-4);
}

.radio-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
  user-select: none;
}

.radio-input {
  display: none;
}

.radio-custom {
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-border-medium);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition-fast);
}

.radio-input:checked + .radio-custom {
  border-color: var(--color-primary-500);
  background-color: var(--color-primary-500);
}

.radio-input:checked + .radio-custom::after {
  content: '';
  width: 8px;
  height: 8px;
  background-color: white;
  border-radius: 50%;
}

.radio-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}

/* 警告框 */
.alert {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-4);
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

/* 表单操作 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-4);
  margin-top: var(--space-8);
  padding-top: var(--space-6);
  border-top: 1px solid var(--color-border-light);
}

.btn-lg {
  padding: var(--space-3) var(--space-6);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  min-width: 120px;
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

/* 发布提示 */
.publish-tips {
  background-color: var(--color-bg-primary);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
  padding: var(--space-6);
  box-shadow: var(--shadow-md);
  align-self: start;
  position: sticky;
  top: var(--space-6);
}

@media (max-width: 768px) {
  .publish-tips {
    position: static;
  }
}

.tips-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.tips-icon {
  font-size: 1.25rem;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.tip-item:last-child {
  margin-bottom: 0;
}

.tip-bullet {
  font-size: 1rem;
  flex-shrink: 0;
  margin-top: 2px;
}

/* 响应式调整 */
@media (max-width: 640px) {
  .main-content {
    padding: var(--space-4) 0;
  }
  
  .page-title {
    font-size: var(--font-size-2xl);
  }
  
  .page-subtitle {
    font-size: var(--font-size-base);
  }
  
  .create-form {
    padding: var(--space-6);
  }
  
  .form-section {
    padding-bottom: var(--space-4);
    margin-bottom: var(--space-6);
  }
  
  .section-title {
    font-size: var(--font-size-lg);
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn-lg {
    width: 100%;
  }
}
</style>