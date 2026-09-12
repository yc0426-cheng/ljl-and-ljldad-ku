<template>
  <!--
    常驻公共布局：左侧模块树 + 底部用户区 + 右侧内容（router-view）
    所有登录后的业务页面（主页 / 用户管理 / 菜单管理等）都作为本布局的子路由渲染在右侧主区，
    页面切换时左侧树不重建（"一直存在"）。左侧树样式来自公共样式 src/style/common.css。
    点击树节点：内置内容叶子切主页内视图（?view=），其余走路由跳转 / "建设中"占位页。
  -->
  <div class="side-layout">
    <!-- ============ 左侧：模块树 ============ -->
    <aside class="side">
      <div class="side-brand">
        <b>学习管理系统</b>
        <span>LEARNING SYSTEM</span>
      </div>

      <el-tree
        ref="treeRef"
        v-loading="menuLoading"
        class="menu"
        :data="menuTree"
        node-key="id"
        default-expand-all
        highlight-current
        @node-click="onNodeClick"
      >
        <template #default="{ data }">
          <span class="menu-node">
            <el-icon v-if="data.icon"><component :is="data.icon" /></el-icon>
            <span>{{ data.label }}</span>
          </span>
        </template>
      </el-tree>

      <!-- 底部用户区：整块可点击打开个人设置浮层；右侧登出按钮用 .stop 不冒泡 -->
      <div class="user-area" @click="settingVisible = true">
        <!-- 头像：有 avatar（OSS URL）显示图片，否则首字母兜底；共用 store，浮层改完自动联动更新 -->
        <img
          v-if="userStore.userInfo?.avatar"
          class="ua-avatar"
          :src="userStore.userInfo.avatar"
          alt="头像"
        />
        <span v-else class="ua-avatar">
          {{ (userStore.userInfo?.name || userStore.userInfo?.account || '未').slice(0, 1) }}
        </span>
        <div class="u-info">
          <b>{{ username }}</b>
          <span>已登录 · 点击进入个人设置</span>
        </div>
        <el-tooltip content="退出登录" placement="top">
          <el-button text :loading="loggingOut" @click.stop="onLogout">
            <el-icon>
              <SwitchButton />
            </el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </aside>

    <!-- ============ 右侧：主区（子路由页面渲染处） ============ -->
    <main class="main">
      <router-view />
    </main>

    <!-- 个人设置浮层：透传组件（无遮罩/不虚化），v-if 卸载干净，关闭后不留任何 DOM -->
    <UserSetting v-if="settingVisible" @close="settingVisible = false" />
  </div>
</template>

<script setup lang="ts">
// ---------------- import 区 ----------------
import { computed, markRaw, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import type { TreeInstance } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus' // 登出确认框 + 成功提示
// 图标局部引入，不依赖 main.ts 全局注册
import {
  Reading,
  DataLine,
  EditPen,
  UserFilled,
  Collection,
  List,
  Avatar,
  Setting,
  SwitchButton
} from '@element-plus/icons-vue'
import type { Component } from 'vue'
import { useUserStore } from '@/store/user'
import { listMenuTree } from '@/api/system/menu'
import { isMenuRouteReady, registerMenuRoutes } from '@/router'
import type { SysMenu } from '@/types/system/menu'
// 个人设置浮层组件（含更改头像：el-dialog + el-upload → 后端转存 OSS）
import UserSetting from '@/views/system/user/setting/UserSetting.vue'

// ---------------- 实例化 ----------------
const router = useRouter()
const userStore = useUserStore() // 登录用户信息（name 优先，缺失时回退到 account）
const username = computed(() => userStore.userInfo?.name || userStore.userInfo?.account)

/** 个人设置浮层显隐（透传浮层：无遮罩/不虚化/可拖拽/Esc 关闭） */
const settingVisible = ref(false)

// ---------------- 左侧模块树（由后端 sys_menu 下发，全站常驻只加载一次） ----------------
// 内置内容叶子的路由约定：命中以下路径时仍走"主区内切换内容"（保留原热力表等实现），
// 其余叶子走路由跳转（能解析到组件则 router.push，否则跳"建设中"占位页）
// 注：/users-list 已有独立页面（system/user/index），不再放这里占位
const BUILTIN_CONTENT: Record<string, string> = {
  '/progress-daily': 'progress',
  '/library-shelf': 'library',
  '/quiz-list': 'quiz'
}

/** 树节点展示结构（与后端 SysMenu 对应） */
interface MenuNode {
  id: number
  label: string
  icon?: Component
  menuType: number
  routePath?: string
  component?: string
  children?: MenuNode[]
}

/** 图标名 → 组件映射（对应 sys_menu.icon；未知图标给兜底 List） */
const ICON_MAP: Record<string, Component> = {
  DataLine: markRaw(DataLine),
  Reading: markRaw(Reading),
  EditPen: markRaw(EditPen),
  UserFilled: markRaw(UserFilled),
  Collection: markRaw(Collection),
  List: markRaw(List),
  Avatar: markRaw(Avatar),
  Setting: markRaw(Setting)
}

const menuTree = ref<MenuNode[]>([])
const menuLoading = ref(true)
const treeRef = ref<TreeInstance>()

/** 后端菜单 → 树节点（icon 名映射成组件） */
function toMenuNodes(list: SysMenu[]): MenuNode[] {
  return list.map((m) => ({
    id: m.menuId!,
    label: m.menuName,
    icon: m.icon ? ICON_MAP[m.icon] : undefined,
    menuType: m.menuType,
    routePath: m.routePath,
    component: m.component,
    children: m.children?.length ? toMenuNodes(m.children) : undefined
  }))
}

/** 兜底菜单：后端拉不到（表未建/服务未起）时仍能展示旧内置模块，页面不白屏 */
function buildFallbackMenus(): SysMenu[] {
  return [
    {
      menuId: 1,
      menuName: '学习进度',
      menuType: 1,
      icon: 'DataLine',
      orderNo: 1,
      visible: true,
      children: [
        {
          menuId: 11,
          menuName: '每日记录',
          menuType: 2,
          routePath: '/progress-daily',
          icon: 'DataLine',
          orderNo: 1,
          visible: true
        }
      ]
    },
    {
      menuId: 2,
      menuName: '图书馆',
      menuType: 1,
      icon: 'Reading',
      orderNo: 2,
      visible: true,
      children: [
        {
          menuId: 21,
          menuName: '我的书架',
          menuType: 2,
          routePath: '/library-shelf',
          icon: 'Collection',
          orderNo: 1,
          visible: true
        }
      ]
    },
    {
      menuId: 3,
      menuName: '学习题目',
      menuType: 1,
      icon: 'EditPen',
      orderNo: 3,
      visible: true,
      children: [
        {
          menuId: 31,
          menuName: '题库练习',
          menuType: 2,
          routePath: '/quiz-list',
          icon: 'List',
          orderNo: 1,
          visible: true
        }
      ]
    },
    {
      menuId: 4,
      menuName: '用户管理',
      menuType: 1,
      icon: 'UserFilled',
      orderNo: 4,
      visible: true,
      children: [
        {
          menuId: 41,
          menuName: '用户列表',
          menuType: 2,
          routePath: '/users-list',
          component: 'system/user/index',
          icon: 'Avatar',
          orderNo: 1,
          visible: true
        }
      ]
    },
    {
      menuId: 5,
      menuName: '系统管理',
      menuType: 1,
      icon: 'Setting',
      orderNo: 5,
      visible: true,
      children: [
        {
          menuId: 51,
          menuName: '菜单管理',
          menuType: 2,
          routePath: '/system/menu',
          component: 'system/menu/index',
          icon: 'Setting',
          orderNo: 1,
          visible: true
        }
      ]
    }
  ]
}

/**
 * 加载菜单：拉后端树 → 渲染左侧 + 动态注册路由；失败用兜底菜单
 */
async function loadMenus(): Promise<void> {
  menuLoading.value = true
  try {
    const list = await listMenuTree()
    menuTree.value = toMenuNodes(list.filter((m) => m.visible !== false))
    registerMenuRoutes(list) // 解析到组件的叶子注册成路由（挂在布局下，渲染在右侧主区）
  } catch (e) {
    console.warn('菜单加载失败，使用内置兜底菜单', e)
    const fallback = buildFallbackMenus()
    menuTree.value = toMenuNodes(fallback)
    registerMenuRoutes(fallback)
  } finally {
    menuLoading.value = false
    syncCurrentKey()
  }
}

/** 在菜单树中查找指定路由的叶子节点 */
function findLeafByRoute(routePath: string): MenuNode | undefined {
  const walk = (nodes: MenuNode[]): MenuNode | undefined => {
    for (const n of nodes) {
      if (n.routePath === routePath) return n
      if (n.children?.length) {
        const hit = walk(n.children)
        if (hit) return hit
      }
    }
    return undefined
  }
  return walk(menuTree.value)
}

/**
 * 高亮与当前路由对应的叶子：
 * 普通页面按路径匹配；主页内置视图（?view=xx）按 BUILTIN_CONTENT 反查叶子路由
 */
function syncCurrentKey(): void {
  const r = router.currentRoute.value
  let path = r.path
  if (r.path === '/home/index' && typeof r.query.view === 'string') {
    const entry = Object.entries(BUILTIN_CONTENT).find(([, view]) => view === r.query.view)
    if (entry) path = entry[0]
  }
  const leaf = findLeafByRoute(path)
  if (leaf) treeRef.value?.setCurrentKey(leaf.id)
}

// 路由变化（点树/浏览器前进后退）时同步左侧树高亮
watch(() => router.currentRoute.value, syncCurrentKey)

/** 点击菜单：目录仅展开；内置内容切主页内视图；其余路由跳转/占位 */
function onNodeClick(data: MenuNode): void {
  if (data.menuType === 1) {
    return
  }
  const path = data.routePath ?? ''
  const view = BUILTIN_CONTENT[path]
  if (view) {
    // 内置内容叶子：右侧仍是主页，通过 query 通知主页切换视图
    void router.push({ path: '/home/index', query: { view, label: data.label } })
    return
  }
  if (!path) {
    return
  }
  if (isMenuRouteReady(path)) {
    void router.push(path)
  } else {
    // 组件未实现 → 跳"建设中"占位页，避免落到 404/被兜底重定向
    void router.push({ path: '/system/coming-soon', query: { title: data.label, route: path } })
  }
}

// ---------------- 退出登录 ----------------
// 流程：确认弹窗 → 调 store.logout（调后端登出 + 清理本地 token/userInfo）→ 提示 → 回登录页。
// 用 replace 跳转：登出后浏览器"后退"不应再回到系统内页面。
const loggingOut = ref(false)

async function onLogout(): Promise<void> {
  // 防重复点击：登出请求进行中不再弹框
  if (loggingOut.value) {
    return
  }

  // 确认框：用户点"取消"（Promise reject）直接结束，什么都不做
  try {
    await ElMessageBox.confirm('退出后需要重新登录才能继续使用', '退出登录', {
      confirmButtonText: '退出登录',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  loggingOut.value = true
  try {
    // store.logout 内部已 try/catch：后端登出失败也会继续本地清理，不会走到 reject
    await userStore.logout()
    ElMessage.success('已退出登录')
  } finally {
    loggingOut.value = false
  }
  await router.replace('/login')
}

// ---------------- 初始化 ----------------
onMounted(async () => {
  // 双保险：路由守卫之外再确认一次登录态。
  // 若因任何原因（缓存残留/守卫被绕过）未登录仍渲染到本布局，立即清登录态踢回登录页
  if (!userStore.isLoggedIn) {
    userStore.clearToken()
    await router.replace('/login')
    return
  }

  // 头像本地兜底：用户信息接口暂未返回 avatar 字段时，按 userId 从 localStorage 恢复
  //（与 UserSettingCard 内逻辑一致；后端接口返回 avatar 后此逻辑自动失效，无需删代码）
  const info = userStore.userInfo
  if (info && !info.avatar) {
    const saved = localStorage.getItem(`avatar:${info.userId}`)
    if (saved) userStore.userInfo = { ...info, avatar: saved }
  }

  // 左侧菜单：先加载后端菜单（渲染 + 动态注册路由），失败自动用兜底菜单
  await loadMenus()
})
</script>

<!-- 左侧树 + 底部用户区公共样式（src/style/common.css，全站唯一引入处） -->
<style src="@/style/common.css"></style>

<style scoped>
/* 主区：右侧内容（子路由页面渲染处），纵向滚动 + 主题滚动条 */
.main {
  flex: 1;
  overflow-y: auto;
  padding: 32px 40px 48px;
  scrollbar-width: thin;
  scrollbar-color: #c9d3c6 transparent;
}

/* Chrome / Edge / Safari */
.main::-webkit-scrollbar {
  width: 8px;
}

.main::-webkit-scrollbar-track {
  background: transparent; /* 轨道透明，融入 #f5f2ea 底色 */
}

.main::-webkit-scrollbar-thumb {
  background-color: #c9d3c6; /* 浅豆绿：对应热力图 lv1/lv2 色阶 */
  border-radius: 8px; /* 胶囊形 */
  border: 2px solid transparent; /* 透明描边收窄视觉宽度，留出呼吸感 */
  background-clip: padding-box;
}

/* 鼠标在主区内时滑块加深一档（热力图 lv2） */
.main:hover::-webkit-scrollbar-thumb {
  background-color: #a7c7b0;
}

/* 悬停滑块本体：主题深绿 */
.main::-webkit-scrollbar-thumb:hover {
  background-color: #2d5a4a;
}

/* 按住拖动：点缀红，与柱图 emphasis 色呼应 */
.main::-webkit-scrollbar-thumb:active {
  background-color: #b5482f;
}

.main::-webkit-scrollbar-corner {
  background: transparent;
}
</style>
