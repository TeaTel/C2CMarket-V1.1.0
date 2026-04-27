import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { title: '首页', showTabBar: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/ForgotPassword.vue'),
    meta: { title: '找回密码' }
  },
  {
    path: '/products',
    name: 'Products',
    component: () => import('../views/Products.vue'),
    meta: { title: '商品列表', showTabBar: true }
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    component: () => import('../views/ProductDetail.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: '/products/create',
    name: 'CreateProduct',
    component: () => import('../views/CreateProduct.vue'),
    meta: {
      requiresAuth: true,
      title: '发布商品'
    }
  },
  {
    path: '/categories',
    name: 'Categories',
    component: () => import('../views/Categories.vue'),
    meta: { title: '分类浏览', showTabBar: true }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('../views/Messages.vue'),
    meta: {
      requiresAuth: true,
      title: '消息',
      showTabBar: true
    }
  },
  // 聊天室页面（核心新功能）
  {
    path: '/chat/:userId',
    name: 'ChatRoom',
    component: () => import('../views/ChatRoom.vue'),
    meta: {
      requiresAuth: true,
      title: '聊天'
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: {
      requiresAuth: true,
      title: '我的',
      showTabBar: true
    }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/Orders.vue'),
    meta: {
      requiresAuth: true,
      title: '我的订单'
    }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('../views/Settings.vue'),
    meta: {
      requiresAuth: true,
      title: '设置'
    }
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 白名单路径（无需登录即可访问）
const publicPaths = ['/', '/login', '/register', '/forgot-password', '/products', '/products/:id', '/categories']

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title
    ? `${to.meta.title} - 校园二手`
    : '校园二手交易平台'

  // 检查当前路径是否在白名单中
  const isPublicPath = publicPaths.some(path => {
    if (path.includes(':')) {
      const regex = new RegExp('^' + path.replace(/:\w+/g, '[^/]+') + '$')
      return regex.test(to.path)
    }
    return to.path === path
  })

  // 公开路径直接放行
  if (isPublicPath) {
    const token = localStorage.getItem('token')

    // 已登录用户访问登录/注册页，重定向到首页
    if ((to.path === '/login' || to.path === '/register') && token && isValidToken(token)) {
      return next({ path: '/' })
    }
    return next()
  }

  // 需要认证的页面
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth && (!token || !isValidToken(token))) {
    // 无效token或未登录
    if (token && !isValidToken(token)) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }

    return next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  }

  next()
})

// 验证token有效性的辅助函数
function isValidToken(token) {
  if (!token) return false

  // 拒绝演示模式的假token
  if (token.startsWith('demo_token_') || token.includes('demo')) {
    return false
  }

  // JWT token基本格式验证
  return token.length > 10
}

export default router
