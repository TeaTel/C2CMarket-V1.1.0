<template>
  <div class="categories">
    <NavBar />
    
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1 class="page-title">分类管理</h1>
          <div class="page-actions">
            <button @click="showCreateModal = true" class="create-btn" v-if="isAdmin">
              <span>+</span> 添加分类
            </button>
          </div>
        </div>
        
        <div v-if="loading" class="loading">加载中...</div>
        
        <div v-else-if="categories.length === 0" class="empty-state">
          <div class="empty-icon">📂</div>
          <h3>暂无分类</h3>
          <p>还没有创建任何商品分类</p>
          <button v-if="isAdmin" @click="showCreateModal = true" class="empty-action-btn">
            创建第一个分类
          </button>
        </div>
        
        <div v-else class="categories-content">
          <!-- 分类树 -->
          <div class="categories-tree">
            <h3 class="section-title">分类结构</h3>
            <div class="tree-container">
              <div
                v-for="category in categoryTree"
                :key="category.id"
                class="tree-item"
              >
                <div class="tree-node" :style="{ paddingLeft: `${category.level * 20}px` }">
                  <div class="node-content">
                    <div class="node-icon">{{ getCategoryIcon(category) }}</div>
                    <div class="node-info">
                      <div class="node-name">{{ category.name }}</div>
                      <div class="node-description">{{ category.description || '暂无描述' }}</div>
                    </div>
                    <div class="node-actions" v-if="isAdmin">
                      <button @click="editCategory(category)" class="action-btn edit-btn">
                        编辑
                      </button>
                      <button @click="deleteCategory(category.id)" class="action-btn delete-btn">
                        删除
                      </button>
                    </div>
                  </div>
                </div>
                
                <!-- 子分类 -->
                <div
                  v-for="child in category.children"
                  :key="child.id"
                  class="tree-item"
                >
                  <div class="tree-node" :style="{ paddingLeft: `${child.level * 20}px` }">
                    <div class="node-content">
                      <div class="node-icon">{{ getCategoryIcon(child) }}</div>
                      <div class="node-info">
                        <div class="node-name">{{ child.name }}</div>
                        <div class="node-description">{{ child.description || '暂无描述' }}</div>
                      </div>
                      <div class="node-actions" v-if="isAdmin">
                        <button @click="editCategory(child)" class="action-btn edit-btn">
                          编辑
                        </button>
                        <button @click="deleteCategory(child.id)" class="action-btn delete-btn">
                          删除
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 分类统计 -->
          <div class="categories-stats">
            <h3 class="section-title">分类统计</h3>
            <div class="stats-grid">
              <div class="stat-card">
                <div class="stat-icon">📂</div>
                <div class="stat-info">
                  <div class="stat-value">{{ totalCategories }}</div>
                  <div class="stat-label">总分类数</div>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon">📦</div>
                <div class="stat-info">
                  <div class="stat-value">{{ totalProducts }}</div>
                  <div class="stat-label">总商品数</div>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon">🌳</div>
                <div class="stat-info">
                  <div class="stat-value">{{ maxDepth }}</div>
                  <div class="stat-label">最大深度</div>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon">📊</div>
                <div class="stat-info">
                  <div class="stat-value">{{ avgProductsPerCategory }}</div>
                  <div class="stat-label">平均商品数</div>
                </div>
              </div>
            </div>
            
            <!-- 热门分类 -->
            <div class="popular-categories">
              <h4>热门分类</h4>
              <div class="popular-list">
                <div
                  v-for="category in popularCategories"
                  :key="category.id"
                  class="popular-item"
                >
                  <div class="popular-icon">{{ getCategoryIcon(category) }}</div>
                  <div class="popular-info">
                    <div class="popular-name">{{ category.name }}</div>
                    <div class="popular-count">{{ category.productCount }} 个商品</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
    
    <!-- 创建/编辑分类模态框 -->
    <div v-if="showCreateModal || showEditModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑分类' : '创建分类' }}</h3>
          <button @click="closeModal" class="modal-close">×</button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label for="name">分类名称 *</label>
              <input
                type="text"
                id="name"
                v-model="formData.name"
                required
                placeholder="请输入分类名称"
              />
            </div>
            
            <div class="form-group">
              <label for="description">分类描述</label>
              <textarea
                id="description"
                v-model="formData.description"
                rows="3"
                placeholder="请输入分类描述"
              ></textarea>
            </div>
            
            <div class="form-group">
              <label for="parentId">父级分类</label>
              <select id="parentId" v-model="formData.parentId">
                <option value="">无父级分类（顶级分类）</option>
                <option
                  v-for="category in availableParentCategories"
                  :key="category.id"
                  :value="category.id"
                >
                  {{ '—'.repeat(category.level) }} {{ category.name }}
                </option>
              </select>
            </div>
            
            <div class="form-group">
              <label for="iconUrl">图标URL</label>
              <input
                type="text"
                id="iconUrl"
                v-model="formData.iconUrl"
                placeholder="请输入图标URL"
              />
            </div>
            
            <div class="form-group">
              <label for="sortOrder">排序序号</label>
              <input
                type="number"
                id="sortOrder"
                v-model="formData.sortOrder"
                min="0"
                placeholder="请输入排序序号"
              />
            </div>
            
            <div class="form-actions">
              <button type="button" @click="closeModal" class="cancel-btn">取消</button>
              <button type="submit" :disabled="submitting" class="submit-btn">
                {{ submitting ? '提交中...' : '提交' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../store/auth'
import NavBar from '../components/NavBar.vue'
import { categoryApi } from '../services/api'

const authStore = useAuthStore()

const categories = ref([])
const categoryTree = ref([])
const loading = ref(true)
const showCreateModal = ref(false)
const showEditModal = ref(false)
const submitting = ref(false)

const formData = ref({
  id: null,
  name: '',
  description: '',
  parentId: '',
  iconUrl: '',
  sortOrder: 0
})

const isAuthenticated = computed(() => authStore.isAuthenticated)
const isAdmin = computed(() => {
  // 这里可以根据用户角色判断是否为管理员
  // 暂时假设所有登录用户都是管理员
  return isAuthenticated.value
})

const isEditing = computed(() => !!formData.value.id)

const totalCategories = computed(() => categories.value.length)
const totalProducts = computed(() => {
  return categories.value.reduce((total, category) => total + (category.productCount || 0), 0)
})
const maxDepth = computed(() => {
  let max = 0
  const calculateDepth = (node, depth) => {
    max = Math.max(max, depth)
    if (node.children) {
      node.children.forEach(child => calculateDepth(child, depth + 1))
    }
  }
  categoryTree.value.forEach(root => calculateDepth(root, 1))
  return max
})
const avgProductsPerCategory = computed(() => {
  return totalCategories.value > 0 ? Math.round(totalProducts.value / totalCategories.value) : 0
})

const popularCategories = computed(() => {
  return [...categories.value]
    .sort((a, b) => (b.productCount || 0) - (a.productCount || 0))
    .slice(0, 5)
})

const availableParentCategories = computed(() => {
  const flattenTree = (nodes, level = 0) => {
    let result = []
    nodes.forEach(node => {
      result.push({ ...node, level })
      if (node.children && node.children.length > 0) {
        result = result.concat(flattenTree(node.children, level + 1))
      }
    })
    return result
  }
  
  let available = flattenTree(categoryTree.value)
  
  // 编辑时排除自身及其子分类
  if (isEditing.value) {
    const excludeIds = new Set()
    const collectIds = (node) => {
      excludeIds.add(node.id)
      if (node.children) {
        node.children.forEach(collectIds)
      }
    }
    
    const currentCategory = categories.value.find(c => c.id === formData.value.id)
    if (currentCategory) {
      collectIds(currentCategory)
    }
    
    available = available.filter(category => !excludeIds.has(category.id))
  }
  
  return available
})

onMounted(async () => {
  await loadCategories()
})

async function loadCategories() {
  try {
    loading.value = true
    const [listResponse, treeResponse] = await Promise.all([
      categoryApi.getCategories(),
      categoryApi.getCategoryTree()
    ])
    
    if (listResponse.code === 200) {
      categories.value = listResponse.data || []
    }
    
    if (treeResponse.code === 200) {
      categoryTree.value = treeResponse.data || []
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  } finally {
    loading.value = false
  }
}

function getCategoryIcon(category) {
  const iconMap = {
    '电子产品': '📱',
    '书籍教材': '📚',
    '生活用品': '🏠',
    '服装鞋帽': '👕',
    '运动器材': '⚽',
    '其他': '📦'
  }
  return iconMap[category.name] || '📂'
}

function editCategory(category) {
  formData.value = {
    id: category.id,
    name: category.name,
    description: category.description || '',
    parentId: category.parentId || '',
    iconUrl: category.iconUrl || '',
    sortOrder: category.sortOrder || 0
  }
  showEditModal.value = true
}

async function deleteCategory(categoryId) {
  if (!confirm('确定要删除这个分类吗？此操作会同时删除所有子分类，且不可撤销。')) {
    return
  }
  
  try {
    const response = await categoryApi.deleteCategory(categoryId)
    if (response.code === 200) {
      alert('分类删除成功')
      await loadCategories()
    } else {
      alert(response.message || '删除失败')
    }
  } catch (error) {
    console.error('删除分类失败:', error)
    alert('删除失败，请稍后重试')
  }
}

function closeModal() {
  showCreateModal.value = false
  showEditModal.value = false
  resetForm()
}

function resetForm() {
  formData.value = {
    id: null,
    name: '',
    description: '',
    parentId: '',
    iconUrl: '',
    sortOrder: 0
  }
}

async function handleSubmit() {
  if (!formData.value.name.trim()) {
    alert('请输入分类名称')
    return
  }

  try {
    submitting.value = true
    
    const data = {
      name: formData.value.name.trim(),
      description: formData.value.description.trim(),
      parentId: formData.value.parentId || null,
      iconUrl: formData.value.iconUrl.trim(),
      sortOrder: parseInt(formData.value.sortOrder) || 0
    }
    
    let response
    if (isEditing.value) {
      response = await categoryApi.updateCategory(formData.value.id, data)
    } else {
      response = await categoryApi.createCategory(data)
    }
    
    if (response.code === 200) {
      alert(isEditing.value ? '分类更新成功' : '分类创建成功')
      closeModal()
      await loadCategories()
    } else {
      alert(response.message || (isEditing.value ? '更新失败' : '创建失败'))
    }
  } catch (error) {
    console.error('提交分类失败:', error)
    alert('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.categories {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  color: #333;
  margin: 0;
}

.create-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.create-btn:hover {
  background-color: #45a049;
}

.loading {
  text-align: center;
  padding: 3rem;
  font-size: 1.2rem;
  color: #666;
}

.empty-state {
  text-align: center;
  padding: 3rem;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: #666;
  margin-bottom: 1.5rem;
}

.empty-action-btn {
  display: inline-block;
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
}

.categories-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
}

.section-title {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #eee;
}

.categories-tree {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tree-container {
  max-height: 500px;
  overflow-y: auto;
}

.tree-item {
  margin-bottom: 0.5rem;
}

.tree-node {
  transition: background-color 0.2s;
}

.tree-node:hover {
  background-color: #f9f9f9;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem;
  border-radius: 4px;
  background: #f5f5f5;
}

.node-icon {
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 8px;
}

.node-info {
  flex: 1;
  min-width: 0;
}

.node-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 0.25rem;
}

.node-description {
  font-size: 0.8rem;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.node-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  padding: 0.25rem 0.75rem;
  border: none;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-btn {
  background-color: #2196F3;
  color: white;
}

.edit-btn:hover {
  background-color: #1976D2;
}

.delete-btn {
  background-color: #f44336;
  color: white;
}

.delete-btn:hover {
  background-color: #d32f2f;
}

.categories-stats {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  font-size: 2rem;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
}

.popular-categories {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.popular-categories h4 {
  font-size: 1rem;
  color: #333;
  margin-bottom: 1rem;
}

.popular-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.popular-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem;
  background: #f9f9f9;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.popular-item:hover {
  background-color: #f0f0f0;
}

.popular-icon {
  font-size: 1.2rem;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 8px;
}

.popular-info {
  flex: 1;
}

.popular-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 0.25rem;
}

.popular-count {
  font-size: 0.8rem;
  color: #666;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  font-size: 1.2rem;
  color: #333;
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #666;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.modal-close:hover {
  background-color: #f5f5f5;
}

.modal-body {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #4CAF50;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.cancel-btn {
  padding: 0.75rem 1.5rem;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  color: #666;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn:hover {
  background: #e0e0e0;
}

.submit-btn {
  padding: 0.75rem 1.5rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background-color: #45a049;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>