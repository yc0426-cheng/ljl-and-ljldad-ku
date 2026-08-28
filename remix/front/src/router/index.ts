import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router' // 路由创建函数 + history 模式 + 路由类型

// ---------------- 路由表 ----------------
// 登录页写在这里：src/views/login/index.vue
// 后续新增页面只需往 routes 里追加（组件懒加载，按需打包）
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    // 懒加载：访问时才加载登录页组件
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/',
    name: 'Home',
    // 首页占位：验证登录成功后能否进入（可后续替换为真正的首页）
    component: () => import('@/views/home/index.vue')
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(), // history 模式：URL 不带 #，更美观
  routes
})

// ---------------- 全局前置守卫：登录校验 ----------------
// 每次路由跳转前都会执行，返回值决定放行或重定向
router.beforeEach((to) => {
  // 与 store 保持一致，从 localStorage 判断是否已登录
  const token = localStorage.getItem('token')

  // 未登录且访问的不是登录页 → 强制跳转到登录页
  if (!token && to.path !== '/login') {
    return { path: '/login' }
  }

  // 已登录却访问登录页 → 跳回首页（避免重复登录）
  if (token && to.path === '/login') {
    return { path: '/' }
  }

  // 其余情况放行
  return true
})

export default router
