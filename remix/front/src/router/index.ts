import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router' // 路由创建函数 + history 模式 + 路由类型
import { checkToken } from '@/api/auth' // token 校验接口（调后端查 redis）
import { useUserStore } from '@/store/user' // 用户 store（校验失败时清登录态）

// ---------------- 路由表 ----------------
// 登录页写在这里：src/views/login/index.vue
// 主页写在这里：src/views/home/index.vue（路径 /home/index）
// 个人设置写在这里：src/views/user/setting/index.vue（路径 /user/setting）
// 后续新增页面只需往 routes 里追加（组件懒加载，按需打包）
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    // meta.public = true：唯一允许未登录访问的页面（守卫据此放行）
    meta: { public: true },
    // 懒加载：访问时才加载登录页组件
    component: () => import('@/views/login/index.vue')
  },
  {
    // 根路径直接重定向到主页，避免 '/' 与 '/home/index' 两处渲染同一页面
    path: '/',
    redirect: '/home/index'
  },
  {
    // 主页：登录后的默认页（含左侧模块树 + 底部用户区/登出入口）
    path: '/home/index',
    name: 'Home',
    component: () => import('@/views/home/index.vue')
  },
  {
    // 个人设置：点击左下角用户区进入；未登录时守卫会拦截，刷新（已登录）停留本页
    path: '/user/setting',
    name: 'UserSetting',
    component: () => import('@/views/user/setting/index.vue')
  },
  {
    // 兜底：未匹配的路径统一回主页（守卫会再判断是否已登录，未登录则踢回 /login）
    path: '/:pathMatch(.*)*',
    redirect: '/home/index'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(), // history 模式：URL 不带 #，更美观
  routes
})

// ---------------- 全局前置守卫：登录校验 ----------------
// 每次路由跳转前都会执行，返回值决定放行或重定向。
//
// 判定规则（避免"假登录态"）：
// - 只有 meta.public 的路由（目前仅 /login）允许未登录访问；
//   其余任何路径（/home/index、/user/setting、直接输入 URL、深链、兜底重定向）
//   只要没有 token 一律重定向到登录页，并携带 ?redirect= 原路径，登录成功后回跳；
// - localStorage 里的 token 关机重启不会丢，但后端 redis 里的 token 会过期，
//   所以"本地有 token"不等于"token 有效"，首次进入必须调后端 /auth/check 校验，
//   校验通过把后端返回的用户信息回写 store，刷新后个人设置页也有数据可显示；
// - 校验通过后用内存标志 tokenValidated 记录，同一会话内后续跳转不再重复请求后端；
// - token 被清除（登出/校验失败踢出）时标志重置，下次进入页面会重新校验。
let tokenValidated = false

router.beforeEach(async (to) => {
  // 与 store 保持一致：从 store 读 token（store 初始化时读 localStorage）
  const userStore = useUserStore()
  const token = userStore.token

  // 未登录：只有 public 页面放行；其余一律回登录页（带原路径便于登录后回跳）
  if (!token) {
    tokenValidated = false
    if (!to.meta.public) {
      return { path: '/login', query: { redirect: to.fullPath }, replace: true }
    }
    return true
  }

  // 已登录却访问登录页 → 跳回主页（避免重复登录）
  if (to.path === '/login') {
    return { path: '/home/index', replace: true }
  }

  // 本会话内已校验过 → 直接放行
  if (tokenValidated) {
    return true
  }

  // 首次进入（刷新/重启浏览器/直接输入 URL 后）：调后端校验 token 是否仍有效
  try {
    // checkToken 返回后端 LoginUserInfo（userId/account/name/token），
    // 回写 store 并持久化，保证刷新后各页面能显示真实用户信息
    const userInfo = await checkToken()
    if (userInfo) {
      userStore.setUserInfo(userInfo)
    }
    // 有效：记录标志，放行
    tokenValidated = true
    return true
  } catch {
    // 无效（后端 redis 已过期/被拉黑）：清本地登录态，踢回登录页
    tokenValidated = false
    userStore.clearToken()
    return { path: '/login', replace: true }
  }
})

export default router
