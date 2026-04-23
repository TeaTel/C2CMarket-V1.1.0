import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/products',
    name: 'Products',
    component: () => import('../views/Products.vue')
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    component: () => import('../views/ProductDetail.vue')
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
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/Orders.vue'),
    meta: { 
      requiresAuth: true,
      title: '我的订单'
    }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('../views/Messages.vue'),
    meta: { 
      requiresAuth: true,
      title: '消息中心'
    }
  },
  {
    path: '/categories',
    name: 'Categories',
    component: () => import('../views/Categories.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true, title: '个人中心' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('../views/Settings.vue'),
    meta: { requiresAuth: true, title: '设置' }
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
const publicPaths = ['/', '/login', '/register', '/products', '/products/:id', '/categories']

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 校园二手交易平台` : '校园二手交易平台'
  
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
    // 已登录用户访问登录/注册页，重定向到首页
    const token = localStorage.getItem('token')
    if ((to.path === '/login' || to.path === '/register') && token) {
      return next({ path: '/' })
    }
    return next()
  }
  
  // 需要认证的页面
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    // 未登录，保存目标路径并跳转到登录页
    return next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  }
  
  next()
})

export default router
