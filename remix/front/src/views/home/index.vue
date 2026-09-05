<template>
  <!--
    首页（数据看板）：
    - 经典后台布局：侧边导航 + 顶栏 + 内容区，侧边栏可折叠、小屏自适应
    - 保留原"登录 → 首页"链路验证能力：展示当前账号、支持登出（含二次确认）
    - 页面内数据均为演示数据，接入后端后按注释标记处替换即可
  -->
  <el-container class="layout">
    <!-- ==================== 侧边栏 ==================== -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <!-- Logo（点击回首页） -->
      <div class="aside-logo" @click="router.push('/')">
        <el-icon :size="26" color="#5b8cff"><Platform /></el-icon>
        <span v-show="!isCollapse" class="aside-logo__text">Nova Admin</span>
      </div>
      <!-- 导航菜单：index 请替换为你项目中的真实路由路径 -->
      <el-scrollbar class="aside-scroll">
        <el-menu
            class="aside-menu"
            :default-active="activeMenu"
            :collapse="isCollapse"
            :collapse-transition="false"
            @select="handleMenuSelect"
        >
          <el-menu-item index="/">
            <el-icon><Odometer /></el-icon>
            <template #title>数据看板</template>
          </el-menu-item>
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><ShoppingCart /></el-icon>
            <template #title>订单管理</template>
          </el-menu-item>
          <el-menu-item index="/settings">
            <el-icon><Setting /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>
    <el-container class="layout-body">
      <!-- ==================== 顶部栏 ==================== -->
      <el-header class="layout-header">
        <!-- 左侧：折叠按钮 + 面包屑 -->
        <div class="header-left">
          <el-tooltip :content="isCollapse ? '展开菜单' : '收起菜单'" placement="bottom">
            <el-icon class="header-action" :size="18" @click="isCollapse = !isCollapse">
              <Expand v-if="isCollapse" />
              <Fold v-else />
            </el-icon>
          </el-tooltip>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentMenuTitle">{{ currentMenuTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <!-- 右侧：通知 / 全屏 / 用户下拉 -->
        <div class="header-right">
          <el-tooltip content="消息通知" placement="bottom">
            <el-badge :value="3" class="msg-badge">
              <el-icon :size="18"><Bell /></el-icon>
            </el-badge>
          </el-tooltip>
          <el-tooltip :content="isFullscreen ? '退出全屏' : '全屏'" placement="bottom">
            <el-icon class="header-action" :size="18" @click="toggleFullscreen">
              <FullScreen />
            </el-icon>
          </el-tooltip>
          <!-- 用户信息下拉（来自 user store） -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-box">
              <el-avatar :size="32" class="user-avatar">{{ avatarText }}</el-avatar>
              <span class="user-name">{{ userStore.userInfo?.account ?? '未登录' }}</span>
              <el-icon :size="12" color="var(--el-text-color-secondary)"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" :icon="User">个人中心</el-dropdown-item>
                <el-dropdown-item command="password" :icon="Lock">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" :icon="SwitchButton" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <!-- ==================== 主内容区 ==================== -->
      <el-main class="layout-main">
        <!-- 欢迎横幅 -->
        <section class="welcome-banner">
          <div class="welcome-text">
            <h2>{{ greeting }}，{{ userStore.userInfo?.account ?? '朋友' }}</h2>
            <p>今天也是元气满满的一天，欢迎回来！</p>
          </div>
          <div class="welcome-date">
            <div class="welcome-date__day">{{ today.day }}</div>
            <div class="welcome-date__detail">{{ today.week }} · {{ today.date }}</div>
          </div>
        </section>
        <!-- 数据统计卡片 -->
        <el-row :gutter="16" class="stat-row">
          <el-col v-for="item in statList" :key="item.title" :xs="12" :sm="12" :md="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-card__body">
                <div class="stat-card__icon" :style="{ backgroundColor: item.bg, color: item.color }">
                  <el-icon :size="24"><component :is="item.icon" /></el-icon>
                </div>
                <div>
                  <div class="stat-card__value">{{ formatNumber(item.value) }}</div>
                  <div class="stat-card__title">{{ item.title }}</div>
                </div>
              </div>
              <div class="stat-card__footer">
                <span>较上周</span>
                <span :class="item.trend >= 0 ? 'is-up' : 'is-down'">
	                  {{ item.trend >= 0 ? '↑' : '↓' }} {{ Math.abs(item.trend).toFixed(1) }}%
	                </span>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <!-- 访问趋势 + 快捷入口 -->
        <el-row :gutter="16" class="panel-row">
          <el-col :xs="24" :md="16">
            <el-card shadow="never" class="panel-card">
              <template #header>
                <div class="panel-header">
                  <span class="panel-title">访问趋势</span>
                  <!-- 自定义分段控制器（避免 radio 版本兼容问题） -->
                  <div class="range-switch">
	                    <span
                          v-for="opt in rangeOptions"
                          :key="opt.key"
                          class="range-switch__item"
                          :class="{ 'is-active': chartRange === opt.key }"
                          @click="chartRange = opt.key"
                      >
	                      {{ opt.label }}
	                    </span>
                  </div>
                </div>
              </template>
              <!-- 纯 CSS 柱状图，零依赖 -->
              <div class="chart">
                <div v-for="d in chartData" :key="d.label" class="chart-col">
                  <div class="chart-col__bars">
                    <el-tooltip :content="`${d.label} · ${formatNumber(d.value)} 次访问`" placement="top">
                      <div class="chart-bar" :style="{ height: barHeight(d.value) + 'px' }"></div>
                    </el-tooltip>
                  </div>
                  <span class="chart-label">{{ d.label }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-card shadow="never" class="panel-card">
              <template #header>
                <div class="panel-header">
                  <span class="panel-title">快捷入口</span>
                </div>
              </template>
              <div class="quick-grid">
                <div v-for="q in quickList" :key="q.label" class="quick-item" @click="handleQuick(q.label)">
                  <el-icon :size="22" :color="q.color"><component :is="q.icon" /></el-icon>
                  <span>{{ q.label }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <!-- 最近动态 -->
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">最近动态</span>
              <el-button text type="primary" :icon="RefreshRight" @click="handleRefresh">刷新</el-button>
            </div>
          </template>
          <el-table :data="recentList" stripe style="width: 100%">
            <el-table-column prop="title" label="动态内容" min-width="220" show-overflow-tooltip />
            <el-table-column prop="operator" label="操作人" width="110" />
            <el-table-column prop="ip" label="IP 地址" width="140" />
            <el-table-column label="结果" width="90" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="statusMap[row.status].type">
                  {{ statusMap[row.status].label }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="时间" width="130" />
          </el-table>
        </el-card>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, markRaw, onMounted, onUnmounted, ref } from 'vue'
import type { Component } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown, Bell, Coin, DataAnalysis, Document, EditPen, Expand, Fold,
  FullScreen, Lock, Odometer, Picture, Platform, RefreshRight, Setting,
  ShoppingCart, SwitchButton, Tools, User, UserFilled, View,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
// ---------------- 实例化 ----------------
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
// ---------------- 布局状态 ----------------
const isCollapse = ref(false)    // 侧边栏折叠状态
const isFullscreen = ref(false)  // 全屏状态
// 小屏（< 992px）自动折叠侧边栏
function handleResize(): void {
  isCollapse.value = window.innerWidth < 992
}

// 全屏切换
async function toggleFullscreen(): Promise<void> {
  try {
    if (!document.fullscreenElement) {
      await document.documentElement.requestFullscreen()
    } else {
      await document.exitFullscreen()
    }
    isFullscreen.value = Boolean(document.fullscreenElement)
  } catch {
    ElMessage.warning('当前浏览器不支持全屏操作')
  }
}
// ---------------- 菜单 / 面包屑 ----------------
const menuTitleMap: Record<string, string> = {
  '/': '数据看板',
  '/users': '用户管理',
  '/orders': '订单管理',
  '/settings': '系统设置',
}
// 当前激活菜单（跟随路由）
const activeMenu = computed(() => route.path)
const currentMenuTitle = computed(() => (route.path === '/' ? '' : menuTitleMap[route.path] ?? ''))
/**
 * 菜单选择：路由已注册则跳转，否则给出友好提示
 * （避免点击尚未配置的菜单时页面跳空白）
 */
function handleMenuSelect(index: string): void {
  if (index === route.path) return
  if (router.resolve(index).matched.length === 0) {
    ElMessage.warning(`「${menuTitleMap[index] ?? index}」页面尚未配置，请先在路由表中注册`)
    return
  }
  router.push(index)
}
// ---------------- 用户信息 ----------------
// 头像展示账号首字符
const avatarText = computed(() =>
    (userStore.userInfo?.account ?? 'U').charAt(0).toUpperCase(),
)
// 按当前时间生成问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})
// 今日日期（横幅右侧展示）
const today = (() => {
  const d = new Date()
  const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return {
    day: String(d.getDate()).padStart(2, '0'),
    week: weeks[d.getDay()],
    date: `${d.getFullYear()} 年 ${d.getMonth() + 1} 月`,
  }
})()
// ---------------- 统计卡片（演示数据，接入后端后替换为接口返回） ----------------
interface StatItem {
  title: string
  value: number
  icon: Component
  color: string
  bg: string
  trend: number // 正数上升 / 负数下降
}
const statList: StatItem[] = [
  { title: '用户总数', value: 12864, icon: markRaw(User), color: '#409eff', bg: 'rgba(64, 158, 255, .12)', trend: 12.5 },
  { title: '今日活跃', value: 3210, icon: markRaw(View), color: '#67c23a', bg: 'rgba(103, 194, 58, .12)', trend: 8.2 },
  { title: '订单数量', value: 896, icon: markRaw(ShoppingCart), color: '#e6a23c', bg: 'rgba(230, 162, 60, .12)', trend: -3.1 },
  { title: '销售额（元）', value: 126560, icon: markRaw(Coin), color: '#f56c6c', bg: 'rgba(245, 108, 108, .12)', trend: 5.6 },
]
// 千分位格式化
function formatNumber(n: number): string {
  return n.toLocaleString('zh-CN')
}
// ---------------- 访问趋势（纯 CSS 柱状图，零第三方依赖） ----------------
type RangeKey = 'week' | 'month'
const chartRange = ref<RangeKey>('week')
const rangeOptions: Array<{ key: RangeKey; label: string }> = [
  { key: 'week', label: '本周' },
  { key: 'month', label: '本月' },
]
const chartDataMap: Record<RangeKey, Array<{ label: string; value: number }>> = {
  week: [
    { label: '周一', value: 820 }, { label: '周二', value: 932 },
    { label: '周三', value: 1201 }, { label: '周四', value: 890 },
    { label: '周五', value: 1490 }, { label: '周六', value: 1120 },
    { label: '周日', value: 1380 },
  ],
  month: [
    { label: '1月', value: 2890 }, { label: '2月', value: 3120 },
    { label: '3月', value: 3680 }, { label: '4月', value: 3450 },
    { label: '5月', value: 4120 }, { label: '6月', value: 3890 },
    { label: '7月', value: 4350 }, { label: '8月', value: 4020 },
    { label: '9月', value: 4580 }, { label: '10月', value: 4890 },
    { label: '11月', value: 4650 }, { label: '12月', value: 5210 },
  ],
}
const chartData = computed(() => chartDataMap[chartRange.value])
const chartMax = computed(() => Math.max(...chartData.value.map((d) => d.value)))
// 柱高按最大值等比换算（最低 8px 保证可见）
function barHeight(value: number): number {
  return Math.max((value / chartMax.value) * 180, 8)
}
// ---------------- 快捷入口 ----------------
interface QuickItem {
  label: string
  icon: Component
  color: string
}
const quickList: QuickItem[] = [
  { label: '新增用户', icon: markRaw(UserFilled), color: '#409eff' },
  { label: '数据报表', icon: markRaw(DataAnalysis), color: '#67c23a' },
  { label: '内容发布', icon: markRaw(EditPen), color: '#e6a23c' },
  { label: '素材库', icon: markRaw(Picture), color: '#f56c6c' },
  { label: '系统工具', icon: markRaw(Tools), color: '#909399' },
  { label: '系统设置', icon: markRaw(Setting), color: '#7b5cff' },
]
function handleQuick(label: string): void {
  ElMessage.info(`「${label}」功能规划中，敬请期待`)
}
// ---------------- 最近动态（演示数据） ----------------
// types.ts 或直接在组件中
type StatusKey = 'success' | 'pending' | 'failed'
type TagType = 'success' | 'warning' | 'danger' | 'info'

type StatusConfig = {
  label: string,
  type: TagType
}

const statusMap: Record<StatusKey, StatusConfig> = {
  success: { label: '成功', type: 'success' },
  pending: { label: '进行中', type: 'warning' },
  failed: { label: '异常', type: 'danger' },
}

interface RecentItem {
  title: string
  operator: string
  ip: string
  status: keyof typeof statusMap
  time: string
}
const recentList = ref<RecentItem[]>([
  { title: '管理员创建了新用户账号「zhangwei」', operator: 'admin', ip: '192.168.1.101', status: 'success', time: '今天 10:24' },
  { title: '导出了 6 月份用户增长数据报表', operator: 'admin', ip: '192.168.1.101', status: 'success', time: '今天 09:47' },
  { title: '系统每日自动备份任务执行中', operator: 'system', ip: '—', status: 'pending', time: '今天 08:00' },
  { title: '用户「lisi」提交了企业认证材料', operator: 'lisi', ip: '10.24.3.87', status: 'pending', time: '昨天 17:32' },
  { title: '订单「DD2025061200031」支付回调超时', operator: 'system', ip: '—', status: 'failed', time: '昨天 15:08' },
  { title: '角色权限「内容审核员」更新了 3 条规则', operator: 'admin', ip: '192.168.1.101', status: 'success', time: '昨天 11:20' },
])
function handleRefresh(): void {
  ElMessage.success('数据已刷新（演示）')
}
function handleMessage(): void {
  ElMessage.info('消息中心开发中')
}
// ---------------- 顶栏下拉指令分发 ----------------
async function handleCommand(command: string | number | object): Promise<void> {
  if (command === 'logout') {
    await handleLogout()
    return
  }
  ElMessage.info('功能规划中，敬请期待')
}
// ---------------- 登出逻辑 ----------------
/**
 * 登出：二次确认 → 清空本地 token/用户信息 → 跳回登录页
 * （后端 /auth/logout 在 store.logout 内调用，接口未实现不影响本地清理）
 */
async function handleLogout(): Promise<void> {
  try {
    await ElMessageBox.confirm('确定要退出当前账号吗？', '退出确认', {
      confirmButtonText: '退 出',
      cancelButtonText: '取 消',
      type: 'warning',
      icon: markRaw(SwitchButton),
    })
  } catch {
    return // 用户取消了登出
  }
  await userStore.logout()
  await router.push('/login')
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => window.removeEventListener('resize', handleResize))
</script>
<style scoped>
/* ==================== 整体布局 ==================== */
.layout {
  height: 100vh;
  overflow: hidden;
}
/* ==================== 侧边栏 ==================== */
.layout-aside {
  display: flex;
  flex-direction: column;
  background: #1c2333;
  transition: width 0.25s ease;
  overflow: hidden;
}
.aside-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 60px;
  flex-shrink: 0;
  cursor: pointer;
}
.aside-logo__text {
  color: #fff;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 1px;
  white-space: nowrap;
}
.aside-scroll {
  flex: 1;
}
/* 菜单深色化 + 圆角高亮 */
.aside-menu {
  --el-menu-bg-color: transparent;
  --el-menu-text-color: rgba(255, 255, 255, 0.65);
  --el-menu-active-color: #fff;
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.08);
  border-right: none;
  padding: 6px 10px;
}
.aside-menu.el-menu--collapse {
  padding: 6px 0;
}
.aside-menu :deep(.el-menu-item) {
  height: 46px;
  margin-bottom: 4px;
  border-radius: 8px;
}
.aside-menu :deep(.el-menu-item:hover) {
  color: #fff;
}
.aside-menu :deep(.el-menu-item.is-active) {
  background-color: var(--el-color-primary);
}
/* ==================== 顶部栏 ==================== */
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.05);
  position: relative;
  z-index: 5;
}
.layout-body {
  flex: 1;
  min-width: 0; /* 防止内容撑破布局 */
}
.header-left {
  display: flex;
  align-items: center;
  gap: 18px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.header-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border-radius: 8px;
  color: var(--el-text-color-primary);
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
}
.header-action:hover {
  background-color: var(--el-fill-color-light);
  color: var(--el-color-primary);
}
.msg-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 34px;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--el-text-color-primary);
  transition: background-color 0.2s, color 0.2s;
}
.msg-badge:hover {
  background-color: var(--el-fill-color-light);
  color: var(--el-color-primary);
}
/* ---------- 用户信息 ---------- */
.user-box {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 8px;
  padding: 4px 12px 4px 5px;
  border-radius: 999px;
  cursor: pointer;
  transition: background-color 0.2s;
  outline: none;
}
.user-box:hover {
  background-color: var(--el-fill-color-light);
}
.user-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, #4c7dff, #9a7bff);
  color: #fff;
  font-weight: 600;
  font-size: 14px;
}
.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: var(--el-text-color-primary);
}
/* ==================== 主内容区 ==================== */
.layout-main {
  padding: 16px;
  background-color: #f5f7fa;
  overflow-y: auto;
}
.layout-main::-webkit-scrollbar {
  width: 6px;
}
.layout-main::-webkit-scrollbar-thumb {
  background: #c8d0dc;
  border-radius: 3px;
}
/* ---------- 欢迎横幅 ---------- */
.welcome-banner {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 28px;
  margin-bottom: 16px;
  border-radius: 12px;
  background: linear-gradient(115deg, #4c7dff 0%, #6a5cff 55%, #9a7bff 100%);
  color: #fff;
  overflow: hidden;
}
.welcome-banner::before,
.welcome-banner::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}
.welcome-banner::before {
  width: 220px;
  height: 220px;
  top: -90px;
  right: -40px;
}
.welcome-banner::after {
  width: 140px;
  height: 140px;
  bottom: -70px;
  right: 160px;
}
.welcome-text {
  position: relative;
  z-index: 1;
}
.welcome-text h2 {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
}
.welcome-text p {
  margin: 0;
  font-size: 13px;
  opacity: 0.85;
}
.welcome-date {
  position: relative;
  z-index: 1;
  text-align: right;
}
.welcome-date__day {
  font-size: 38px;
  font-weight: 700;
  line-height: 1;
}
.welcome-date__detail {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.85;
}
/* ---------- 统计卡片 ---------- */
.stat-row {
  margin-bottom: 16px;
}
.stat-card {
  border-radius: 10px;
}
.stat-card :deep(.el-card__body) {
  padding: 18px 20px;
}
.stat-card__body {
  display: flex;
  align-items: center;
  gap: 14px;
}
.stat-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  border-radius: 12px;
}
.stat-card__value {
  font-size: 22px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  line-height: 1.2;
}
.stat-card__title {
  margin-top: 3px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.stat-card__footer {
  display: flex;
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed var(--el-border-color-lighter);
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.is-up {
  color: var(--el-color-success);
  font-weight: 600;
}
.is-down {
  color: var(--el-color-danger);
  font-weight: 600;
}
/* ---------- 通用面板卡片 ---------- */
.panel-row {
  margin-bottom: 16px;
}
.panel-card {
  border-radius: 10px;
  border: 1px solid var(--el-border-color-lighter);
}
.panel-card :deep(.el-card__header) {
  padding: 14px 20px;
}
.panel-card :deep(.el-card__body) {
  padding: 20px;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.panel-title {
  position: relative;
  padding-left: 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.panel-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 14px;
  border-radius: 2px;
  background-color: var(--el-color-primary);
}
/* ---------- 访问趋势 ---------- */
.range-switch {
  display: inline-flex;
  padding: 2px;
  border-radius: 6px;
  background-color: var(--el-fill-color-light);
  cursor: pointer;
}
.range-switch__item {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  transition: all 0.2s;
}
.range-switch__item.is-active {
  background-color: #fff;
  color: var(--el-color-primary);
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}
.chart {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  height: 216px;
}
.chart-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  min-width: 0;
}
.chart-col__bars {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  width: 100%;
  height: 180px;
}
.chart-bar {
  width: 100%;
  max-width: 30px;
  border-radius: 6px 6px 2px 2px;
  background: linear-gradient(180deg, #7ea1ff 0%, #4c7dff 100%);
  cursor: pointer;
  transition: height 0.5s cubic-bezier(0.25, 1, 0.5, 1), filter 0.2s;
}
.chart-bar:hover {
  filter: brightness(1.15);
}
.chart-label {
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
/* ---------- 快捷入口 ---------- */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 8px;
  border-radius: 10px;
  background-color: var(--el-fill-color-light);
  font-size: 13px;
  color: var(--el-text-color-regular);
  cursor: pointer;
  transition: all 0.2s;
}
.quick-item:hover {
  transform: translateY(-2px);
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  box-shadow: 0 4px 12px rgba(76, 125, 255, 0.15);
}
/* ---------- 响应式：小屏卡片堆叠时保留垂直间距 ---------- */
@media (max-width: 991px) {
  .stat-row .el-col,
  .panel-row .el-col {
    margin-bottom: 12px;
  }
}
</style>