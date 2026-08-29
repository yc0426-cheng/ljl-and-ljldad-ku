import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import {checkToken} from "@/api/auth";
import {useUserStore} from "@/store/user"; // 路由创建函数 + history 模式 + 路由类型

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
//
// 校验策略（避免"假登录态"）：
// localStorage 里的 token 关机重启不会丢，但后端 redis 里的 token 会过期，
// 所以"本地有 token"不等于"token 有效"，必须调后端 /auth/check 校验。
// 校验通过后用内存标志 tokenValidated 记录，同一会话内后续跳转不再重复请求后端；
// token 被清除（登出/校验失败踢出）时标志重置，下次进入页面会重新校验。
let tokenValidated = false

router.beforeEach(async (to) => {
  // 与 store 保持一致，从 localStorage 判断是否已登录
  const token = localStorage.getItem('token')

  // 未登录：重置校验标志，且非登录页一律跳登录页
  if (!token) {
    tokenValidated = false
    if (to.path !== '/login') {
      return { path: '/login' }
    }
    return true
  }

  // 已登录却访问登录页 → 跳回首页（避免重复登录）
  if (to.path === '/login') {
    return { path: '/' }
  }

  // 本会话内已校验过 → 直接放行
  if (tokenValidated) {
    return true
  }

  // 首次进入（刷新/重启浏览器后）：调后端校验 token 是否仍有效
  try {
    await checkToken()
    // 有效：记录标志，放行
    tokenValidated = true
    return true
  } catch {
    // 无效（后端 redis 已过期/被拉黑）：清本地登录态，踢回登录页
    tokenValidated = false
    useUserStore().clearToken()
    return { path: '/login' }
  }
})

export default router
