import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router' // 路由创建函数 + history 模式 + 路由类型
import { checkToken } from '@/api/auth' // token 校验接口（调后端查 redis）
import { useUserStore } from '@/store/user' // 用户 store（校验失败时清登录态）
import type { SysMenu } from '@/types/system/menu'

// ---------------- 路由表 ----------------
// 静态路由：登录页 + 常驻布局（左侧模块树 + 右侧 router-view）及其子页面
// 动态路由：登录后由菜单数据（sys_menu）动态注册为布局的子路由（见 registerMenuRoutes）
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    // meta.public = true：唯一允许未登录访问的页面（守卫据此放行）
    meta: { public: true },
    component: () => import('@/views/login/index.vue')
  },
  {
    // 常驻布局：左侧模块树 + 底部用户区全站常驻，业务页面统一渲染在右侧主区
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    children: [
      {
        // 根路径直接重定向到主页
        path: '',
        redirect: '/home/index'
      },
      {
        // 主页（右侧内容：学习进度热力表等），左侧菜单由布局渲染，sys_menu 下发
        path: '/home/index',
        name: 'Home',
        component: () => import('@/views/home/index.vue')
      },
      {
        // 个人设置
        path: '/user/setting',
        name: 'UserSetting',
        component: () => import('@/views/system/user/setting/UserSetting.vue')
      },
      {
        // 系统管理 → 菜单管理（在系统路由中增删改动态路由的页面）
        path: '/system/menu',
        name: 'SysMenu',
        component: () => import('@/views/system/menu/index.vue')
      },
      {
        // 动态路由占位页：菜单里新加的路由组件未实现时跳这里，避免落到 404
        path: '/system/coming-soon',
        name: 'ComingSoon',
        component: () => import('@/views/system/coming-soon/index.vue')
      }
    ]
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

// ---------------- 动态路由注册（菜单数据驱动） ----------------
// views 下所有组件索引：菜单 component 字段（如 system/menu/index）据此解析到真实组件
const viewModules = import.meta.glob('/src/views/**/*.vue')
// 已注册过的动态路由路径（去重）
const dynamicPaths = new Set<string>()

/**
 * 根据菜单树注册"可解析组件"的叶子路由（幂等：已注册的跳过）
 * 约定：菜单 component 形如 system/menu/index，会依次尝试
 *   /src/views/system/menu/index.vue 与 /src/views/system/menu/index/index.vue
 * 解析不到组件的叶子不注册 —— 点击时由主页跳"建设中"占位页。
 *
 * @param menus sys_menu 树
 */
export function registerMenuRoutes(menus: SysMenu[]): void {
  const walk = (list: SysMenu[]): void => {
    for (const menu of list) {
      if (menu.menuType === 2 && menu.routePath && menu.component) {
        const loader = resolveComponent(menu.component)
        const name = `menu-${menu.routePath}`
        if (
          loader &&
          !dynamicPaths.has(menu.routePath) &&
          !router.hasRoute(name) &&
          !isMenuRouteReady(menu.routePath) // 已有同名静态路由（如 /system/menu）不再重复注册
        ) {
          // 挂在常驻布局 Layout 下：动态页面同样渲染在左侧树右侧的主区
          router.addRoute('Layout', { path: menu.routePath, name, component: loader })
          dynamicPaths.add(menu.routePath)
        }
      }
      if (menu.children?.length) {
        walk(menu.children)
      }
    }
  }
  walk(menus)
}

/**
 * 某路径当前是否已是可用路由（静态路由或已动态注册）
 *
 * @param path 完整路由路径，如 /system/menu
 * @returns true = 可直接 router.push
 */
export function isMenuRouteReady(path: string): boolean {
  return router.getRoutes().some((r) => r.path === path)
}

/**
 * 解析组件路径为懒加载函数
 *
 * @param componentPath views 下相对路径，如 system/menu/index
 * @returns 懒加载函数；找不到返回 undefined
 */
function resolveComponent(componentPath: string): (() => Promise<unknown>) | undefined {
  const exact = viewModules[`/src/views/${componentPath}.vue`]
  const index = viewModules[`/src/views/${componentPath}/index.vue`]
  return (exact ?? index) as unknown as (() => Promise<unknown>) | undefined
}

// ---------------- 全局前置守卫：登录校验 ----------------
// 判定规则：只有 meta.public（/login）允许未登录；其余路径无 token 一律回登录页并带 redirect；
// 本地有 token 不代表有效，首入调后端 /auth/check，通过后回写用户信息并放行。
let tokenValidated = false

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const token = userStore.token

  if (!token) {
    tokenValidated = false
    if (!to.meta.public) {
      return { path: '/login', query: { redirect: to.fullPath }, replace: true }
    }
    return true
  }

  if (to.path === '/login') {
    return { path: '/home/index', replace: true }
  }

  if (tokenValidated) {
    return true
  }

  try {
    const userInfo = await checkToken()
    if (userInfo) {
      userStore.setUserInfo(userInfo)
    }
    tokenValidated = true
    return true
  } catch {
    tokenValidated = false
    userStore.clearToken()
    return { path: '/login', replace: true }
  }
})

export default router
