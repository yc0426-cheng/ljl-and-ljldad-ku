<template>
  <!--
    学习系统主页
    位置说明：src/views/home/index.vue，由 router/index.ts 中 /home/index 路由懒加载（/ 会重定向到 /home/index）
    调用链路：本页 → api/home.ts（getHeatmap / getDayRecord / getAnswerStats）
    布局：左侧模块树（图书馆/学习进度/学习题目/用户管理）
         右侧 = 每日学习记录热力表（颜色深浅=学习进度，悬停显示具体记录）
              + 每日答题个数柱图（日/月/年切换）
    登录态：由路由守卫保证进入本页时已登录；左下角为登出入口（onLogout）
  -->
  <div class="home">
    <!-- ============ 左侧：模块树 ============ -->
    <aside class="side">
      <div class="side-brand">
        <b>学习管理系统</b>
        <span>LEARNING SYSTEM</span>
      </div>

      <el-tree
        ref="treeRef"
        class="menu"
        :data="menuTree"
        node-key="id"
        default-expand-all
        highlight-current
        @node-click="onNodeClick"
      >
        <template #default="{ data }">
          <span class="menu-node" :class="{ root: data.id === data.view }">
            <el-icon><component :is="data.icon" /></el-icon>
            <span>{{ data.label }}</span>
          </span>
        </template>
      </el-tree>

      <!-- 底部用户区：整块可点击进入个人设置（/user/setting）；右侧登出按钮用 .stop 不触发跳转 -->
      <div
        class="side-user"
        title="个人设置"
        role="button"
        tabindex="0"
        @click="goToUserSetting"
        @keydown.enter="goToUserSetting"
      >
        <span class="avatar">{{ username.slice(0, 1) }}</span>
        <div class="u-info">
          <b>{{ username }}</b>
          <span>已登录 · 点击进入个人设置</span>
        </div>
      </div>
    </aside>

    <!-- ============ 右侧：主区 ============ -->
    <main class="main">
      <!-- ===== 学习进度（主页默认视图） ===== -->
      <template v-if="activeView === 'progress'">
        <header class="page-head">
          <div>
            <p class="crumb">PROGRESS / DAILY RECORD</p>
            <h1>学习进度</h1>
          </div>
          <p class="head-date">{{ headDate }}</p>
        </header>

        <!-- 统计横条（由热力数据推算，无需单独接口） -->
        <section class="stat-strip">
          <div class="stat">
            <div class="num">{{ anim.duration }}<i>MIN</i></div>
            <p>今日学习时长</p>
          </div>
          <div class="stat">
            <div class="num">{{ anim.answers }}<i>道</i></div>
            <p>今日答题个数</p>
          </div>
          <div class="stat">
            <div class="num accent">{{ anim.streak }}<i>天</i></div>
            <p>连续打卡</p>
          </div>
          <div class="stat">
            <div class="num">{{ anim.weekAvg }}<i>%</i></div>
            <p>本周平均进度</p>
          </div>
        </section>

        <!-- 每日学习记录：颜色深浅 = 学习进度 -->
        <section v-loading="pageLoading" class="panel">
          <div class="panel-head">
            <h2>
              每日学习记录<small>颜色深浅 = 学习进度 · 悬停查看具体记录 · 点击查看当日明细</small>
            </h2>
            <div class="legend">
              <span>0</span>
              <i v-for="n in 5" :key="n" :class="`lv${n - 1}`" />
              <span>100%</span>
              <i class="lv0 today-mark" /><span>今日</span>
            </div>
          </div>

          <!-- 热力表：列=周，行=周一~周日 -->
          <div class="heat-scroll">
            <div class="heat">
              <div class="heat-weekdays">
                <span v-for="w in ['一', '', '三', '', '五', '', '日']" :key="w">{{ w }}</span>
              </div>
              <div class="heat-weeks">
                <div v-for="(week, wi) in weekCols" :key="wi" class="heat-week">
                  <div class="heat-month">{{ week.label }}</div>
                  <span
                    v-for="cell in week.cells"
                    :key="cell.date"
                    class="heat-cell"
                    :class="[
                      cell.isFuture ? 'future' : `lv${cell.level}`,
                      {
                        today: cell.isToday,
                        selected: cell.date === selectedDate
                      }
                    ]"
                    @mouseenter="onCellEnter(cell, $event)"
                    @mouseleave="onCellLeave"
                    @click="onCellClick(cell)"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 当日明细（点击热力格展开） -->
          <div v-if="selected" class="day-detail">
            <div class="dd-head">
              <b>{{ selected.date }} 学习明细</b>
              <span>时长 {{ selected.duration }} min · 答题 {{ selected.answers }} 道</span>
            </div>
            <template v-if="selected.loading">
              <div class="skel" />
              <div class="skel" />
              <div class="skel" />
            </template>
            <template v-else-if="selected.items.length">
              <div v-for="(it, i) in selected.items" :key="i" class="dd-item">
                <span class="dd-time">{{ it.time }}</span>
                <span class="dd-text">{{ it.content }}</span>
                <el-tag size="small" effect="plain">{{ it.type }}</el-tag>
              </div>
            </template>
            <p v-else class="dd-empty">这一天没有学习记录</p>
          </div>
        </section>

        <!-- 每日答题个数柱图：日 / 月 / 年 -->
        <section class="panel">
          <div class="panel-head">
            <h2>答题统计<small>ANSWER COUNT</small></h2>
            <!-- 注：element-plus < 2.6 请把 value 换成 label -->
            <el-radio-group v-model="statUnit" size="small">
              <el-radio-button value="day">日</el-radio-button>
              <el-radio-button value="month">月</el-radio-button>
              <el-radio-button value="year">年</el-radio-button>
            </el-radio-group>
          </div>
          <div
            ref="chartEl"
            v-loading="chartLoading"
            class="chart"
            element-loading-background="transparent"
          />
        </section>
      </template>

      <!-- ===== 其它模块占位：后续按 views/library 等拆分为独立页面 ===== -->
      <section v-else class="panel module-empty">
        <h2>{{ activeLabel }}</h2>
        <el-empty :description="`「${activeLabel}」模块规划中`" />
      </section>
    </main>

    <!-- 悬停提示：具体学习记录（teleport 到 body，不受面板 overflow 裁剪） -->
    <teleport to="body">
      <transition name="tip-fade">
        <div
          v-if="tip.show && tip.record"
          class="heat-tip"
          :class="{ below: tip.below }"
          :style="{ left: `${tip.x}px`, top: `${tip.y}px` }"
        >
          <p class="tip-date">{{ tip.record.date }}</p>
          <p class="tip-progress">
            <span class="tip-bar"><i :style="{ width: `${tip.record.progress}%` }" /></span>
            <b>进度 {{ tip.record.progress }}%</b>
          </p>
          <template v-if="tip.record.items.length">
            <p v-for="(it, i) in tip.record.items" :key="i" class="tip-item">
              <span class="tip-time">{{ it.time }}</span
              >{{ it.type }} · {{ it.content }}
            </p>
          </template>
          <p v-else class="tip-empty">当天暂无学习记录</p>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup lang="ts">
// ---------------- import 区 ----------------
import { computed, markRaw, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import type { TreeInstance } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus' // 登出确认框 + 成功提示
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, MarkLineComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { EChartsCoreOption } from 'echarts/core'
// 图标局部引入，不依赖 main.ts 全局注册
import {
  Reading,
  DataLine,
  EditPen,
  UserFilled,
  Collection,
  List,
  Avatar
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getHeatmap, getDayRecord, getAnswerStats } from '@/api/home'
import type { IHeatCell, IDayRecord, IMenuNode, StatUnit } from '@/types/home'

echarts.use([BarChart, GridComponent, TooltipComponent, MarkLineComponent, CanvasRenderer])

// ---------------- 实例化 ----------------
const router = useRouter()
const userStore = useUserStore() // 登录用户信息（name 优先，缺失时回退到 account，避免显示"无名之人"）
const username = computed(
  () => userStore.userInfo?.name || userStore.userInfo?.account || '未知用户'
)

// ---------------- 左侧模块树 ----------------
const menuTree: IMenuNode[] = [
  {
    id: 'progress',
    label: '学习进度',
    icon: markRaw(DataLine),
    view: 'progress',
    children: [
      { id: 'progress-daily', label: '每日记录', icon: markRaw(DataLine), view: 'progress' }
    ]
  },
  {
    id: 'library',
    label: '图书馆',
    icon: markRaw(Reading),
    view: 'library',
    children: [
      { id: 'library-shelf', label: '我的书架', icon: markRaw(Collection), view: 'library' }
    ]
  },
  {
    id: 'quiz',
    label: '学习题目',
    icon: markRaw(EditPen),
    view: 'quiz',
    children: [{ id: 'quiz-list', label: '题库练习', icon: markRaw(List), view: 'quiz' }]
  },
  {
    id: 'users',
    label: '用户管理',
    icon: markRaw(UserFilled),
    view: 'users',
    children: [{ id: 'users-list', label: '用户列表', icon: markRaw(Avatar), view: 'users' }]
  }
]

const treeRef = ref<TreeInstance>()
const activeView = ref('progress')
const activeLabel = ref('学习进度')

function onNodeClick(data: IMenuNode): void {
  activeView.value = data.view
  activeLabel.value = data.label
}

// ---------------- 每日学习记录（热力表） ----------------
const pageLoading = ref(true)
const cells = ref<IHeatCell[]>([])

/** 按周分列（7 天一列），列顶标注月份 */
const weekCols = computed(() => {
  const cols: { label: string; cells: IHeatCell[] }[] = []
  let lastMonth = -1
  for (let w = 0; w * 7 < cells.value.length; w++) {
    const weekCells = cells.value.slice(w * 7, w * 7 + 7)
    const month = Number(weekCells[0].date.slice(5, 7))
    const label = month !== lastMonth ? `${month}月` : ''
    lastMonth = month
    cols.push({ label, cells: weekCells })
  }
  return cols
})

const todayCell = computed(() => cells.value.find((c) => c.isToday))

/** 连续打卡：从今天往回数连续有记录的天数 */
const streak = computed(() => {
  let n = 0
  for (let i = cells.value.length - 1; i >= 0; i--) {
    const c = cells.value[i]
    if (c.isFuture) continue
    if (c.progress > 0) n++
    else break
  }
  return n
})

/** 本周平均进度 */
const weekAvg = computed(() => {
  const recent = cells.value
    .filter((c) => !c.isFuture)
    .slice(-7)
    .filter((c) => c.progress > 0)
  return recent.length ? Math.round(recent.reduce((s, c) => s + c.progress, 0) / recent.length) : 0
})

const headDate = computed(() => {
  const n = new Date()
  return `${n.getFullYear()}年${n.getMonth() + 1}月${n.getDate()}日 · 星期${'日一二三四五六'[n.getDay()]}`
})

/* ---- 悬停提示：拉取某日具体学习记录（按日期缓存，避免重复请求） ---- */
const tip = reactive({ show: false, x: 0, y: 0, below: false, record: null as IDayRecord | null })
const recordCache = new Map<string, IDayRecord>()

function onCellEnter(cell: IHeatCell, e: MouseEvent): void {
  if (cell.isFuture) return
  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect()
  tip.x = Math.min(Math.max(rect.left + rect.width / 2, 150), window.innerWidth - 150)
  tip.y = rect.top
  tip.below = rect.top < 180 // 太靠上时改为在格子下方弹出

  const cached = recordCache.get(cell.date)
  if (cached) {
    tip.record = cached
    tip.show = true
    return
  }
  tip.record = null
  tip.show = true // 先弹汇总，明细到了再补
  getDayRecord(cell.date).then((record) => {
    recordCache.set(cell.date, record)
    if (tip.show) tip.record = record // 鼠标已移走（tip.show=false）则不再显示
  })
}

function onCellLeave(): void {
  tip.show = false
  tip.record = null
}

/* ---- 点击热力格：展开当日明细 ---- */
const selected = ref<{
  date: string
  duration: number
  answers: number
  items: IDayRecord['items']
  loading: boolean
} | null>(null)
const selectedDate = ref('')

function onCellClick(cell: IHeatCell): void {
  if (cell.isFuture) return
  onCellLeave()
  selectedDate.value = cell.date
  selected.value = {
    date: cell.date,
    duration: cell.duration,
    answers: cell.answers,
    items: [],
    loading: true
  }
  getDayRecord(cell.date).then((record) => {
    if (selected.value?.date !== cell.date) return // 快速切换时丢弃过期响应
    selected.value = {
      date: cell.date,
      duration: record.duration,
      answers: record.answers,
      items: record.items,
      loading: false
    }
  })
}

/* ---- 顶部统计数字滚动动画 ---- */
const anim = reactive({ duration: 0, answers: 0, streak: 0, weekAvg: 0 })

function tween(key: keyof typeof anim, target: number, dur = 800): void {
  const from = anim[key]
  const t0 = performance.now()
  const step = (now: number): void => {
    const t = Math.min(1, (now - t0) / dur)
    anim[key] = Math.round(from + (target - from) * (1 - Math.pow(1 - t, 3)))
    if (t < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

// ---------------- 答题个数柱图（日/月/年） ----------------
const chartEl = ref<HTMLDivElement>()
const statUnit = ref<StatUnit>('day')
const chartLoading = ref(false)
let chart: echarts.ECharts | null = null

function buildOption(labels: string[], values: number[], unit: StatUnit): EChartsCoreOption {
  return {
    grid: { left: 48, right: 56, top: 30, bottom: 32 },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: unknown) => {
        const p = (Array.isArray(params) ? params[0] : params) as { name: string; value: number }
        return `${p.name}<br/>答题个数：<b style="color:#f5d9a8">${p.value}</b> 道`
      }
    },
    xAxis: {
      type: 'category',
      data: labels,
      axisTick: { show: false },
      axisLabel: { color: '#9aa093', fontSize: 10, interval: unit === 'day' ? 3 : 0 } // 日视图 30 个标签隔 3 显示
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#eee9dc', type: 'dashed' } },
      axisLabel: { color: '#9aa093', fontSize: 10 }
    },
    series: [
      {
        name: '答题个数',
        type: 'bar',
        data: values,
        barMaxWidth: 26,
        itemStyle: { color: '#2d5a4a', borderRadius: [4, 4, 0, 0] },
        emphasis: { itemStyle: { color: '#b5482f' } },
        markLine: {
          silent: true,
          symbol: 'none',
          lineStyle: { color: '#b5482f', type: 'dashed', width: 1 },
          label: { formatter: '均值 {c}', color: '#b5482f', fontSize: 10, position: 'end' },
          data: [{ type: 'average' }]
        }
      }
    ],
    animationDuration: 550
  }
}

async function loadStats(): Promise<void> {
  if (!chartEl.value) return
  chartLoading.value = true
  const stats = await getAnswerStats(statUnit.value)
  chart ??= echarts.init(chartEl.value)
  chart.setOption(buildOption(stats.labels, stats.values, stats.unit), true)
  chartLoading.value = false
}

watch(statUnit, loadStats)

const onResize = (): void => chart?.resize()

// ---------------- 初始化 ----------------
onMounted(async () => {
  // 双保险：路由守卫之外再确认一次登录态。
  // 若因任何原因（缓存残留/守卫被绕过）未登录仍渲染到本页，立即清登录态踢回登录页
  if (!userStore.isLoggedIn) {
    userStore.clearToken()
    await router.replace('/login')
    return
  }

  treeRef.value?.setCurrentKey('progress') // 默认选中"学习进度"
  cells.value = await getHeatmap(18)
  pageLoading.value = false
  tween('duration', todayCell.value?.duration ?? 0)
  tween('answers', todayCell.value?.answers ?? 0)
  tween('streak', streak.value)
  tween('weekAvg', weekAvg.value)
  await nextTick()
  await loadStats()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart?.dispose()
  chart = null
})

// ---------------- 底部用户区 → 个人设置 ----------------
function goToUserSetting(): void {
  router.push('/user/setting')
}

// ---------------- 退出登录 ----------------
// 流程：确认弹窗 → 调 store.logout（调后端登出 + 清理本地 token/userInfo）→ 提示 → 回登录页。
// 用 replace 跳转：登出后浏览器"后退"不应再回到主页。
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
  router.replace('/login')
}
</script>

<style scoped>
.home {
  display: flex;
  height: 100vh;
  background: #f5f2ea;
}

/* ============ 左侧 ============ */
.side {
  width: 232px;
  flex: none;
  background: #f0ede3;
  border-right: 1px solid #e3ded1;
  display: flex;
  flex-direction: column;
}

.side-brand {
  padding: 20px 20px 16px;
  border-bottom: 1px solid #e3ded1;
}

.side-brand b {
  display: block;
  font-size: 16px;
  letter-spacing: 2px;
  color: #22302a;
}

.side-brand span {
  font-size: 9px;
  letter-spacing: 3px;
  color: #9aa093;
}

.menu {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: transparent;
}

.menu :deep(.el-tree) {
  background: transparent;
  --el-tree-node-hover-bg-color: transparent;
}

.menu :deep(.el-tree-node__content) {
  height: 38px;
  border-radius: 9px;
  margin-bottom: 2px;
}

.menu :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: #2d5a4a;
  color: #fff;
}

.menu-node {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 14px;
}

.menu-node.root {
  font-weight: 600;
}

.side-user {
  border-top: 1px solid #e3ded1;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  gap: 11px;
  cursor: pointer;
  transition: background 0.15s;
}

.side-user:hover {
  background: rgba(45, 90, 74, 0.06);
}

.side-user:focus-visible {
  outline: 2px solid #2d5a4a;
  outline-offset: -2px;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #2d5a4a;
  color: #f0ede2;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  flex: none;
}

.u-info {
  flex: 1;
  line-height: 1.3;
}

.u-info b {
  display: block;
  font-size: 14px;
}

.u-info span {
  font-size: 11px;
  color: #9aa093;
}

/* ============ 右侧 ============ */
.main {
  flex: 1;
  overflow-y: auto;
  padding: 32px 40px 48px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.crumb {
  font-size: 10.5px;
  letter-spacing: 3px;
  color: #9aa093;
  margin: 0 0 8px;
}

.page-head h1 {
  font-size: 30px;
  font-weight: 800;
  margin: 0;
  letter-spacing: 4px;
  color: #22302a;
}

.head-date {
  color: #5c665f;
  font-size: 13px;
}

.stat-strip {
  display: flex;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.stat {
  padding: 4px 36px 4px 0;
  margin-right: 36px;
  border-right: 1px solid #e3ded1;
}

.stat:last-child {
  border: none;
}

.stat .num {
  font-size: 34px;
  font-weight: 800;
  line-height: 1.15;
  color: #22302a;
}

.stat .num i {
  font-style: normal;
  font-size: 12px;
  font-weight: 400;
  color: #9aa093;
  margin-left: 5px;
}

.stat .num.accent {
  color: #b5482f;
}

.stat p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #5c665f;
  letter-spacing: 2px;
}

.panel {
  background: #fdfcf8;
  border: 1px solid #e3ded1;
  border-radius: 14px;
  padding: 22px 26px;
  margin-bottom: 20px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  flex-wrap: wrap;
  gap: 10px;
}

.panel-head h2 {
  font-size: 17px;
  margin: 0;
  letter-spacing: 2px;
  color: #22302a;
}

.panel-head small {
  font-size: 10.5px;
  color: #9aa093;
  letter-spacing: 1px;
  margin-left: 12px;
  font-weight: 400;
}

/* 图例 */
.legend {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  color: #9aa093;
}

.legend i {
  width: 14px;
  height: 14px;
  border-radius: 3.5px;
  display: inline-block;
}

.today-mark {
  outline: 1.5px solid #b5482f;
  outline-offset: 1.5px;
  margin-left: 8px;
}

/* 热力表 */
.heat-scroll {
  overflow-x: auto;
  padding-bottom: 4px;
}

.heat {
  display: flex;
  gap: 6px;
  min-width: max-content;
}

.heat-weekdays {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 20px;
}

.heat-weekdays span {
  width: 14px;
  height: 18px;
  line-height: 18px;
  font-size: 10.5px;
  color: #9aa093;
  text-align: right;
}

.heat-weeks {
  display: flex;
  gap: 4px;
}

.heat-week {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.heat-month {
  height: 16px;
  font-size: 10.5px;
  color: #9aa093;
  white-space: nowrap;
}

.heat-cell {
  width: 18px;
  height: 18px;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.1s;
}

.heat-cell:hover {
  transform: scale(1.25);
  position: relative;
  z-index: 2;
}

.lv0 {
  background: #eae6da;
}

.lv1 {
  background: #d6e4d8;
}

.lv2 {
  background: #a7c7b0;
}

.lv3 {
  background: #6ba186;
}

.lv4 {
  background: #2d5a4a;
}

.future {
  background: #f1efe7;
  cursor: default;
}

.future:hover {
  transform: none;
}

.today {
  outline: 1.5px solid #b5482f;
  outline-offset: 2px;
}

.selected {
  box-shadow:
    0 0 0 2px #fdfcf8,
    0 0 0 4px #2d5a4a;
}

/* 当日明细 */
.day-detail {
  margin-top: 20px;
  border-top: 1px dashed #e3ded1;
  padding-top: 16px;
}

.dd-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 10px;
}

.dd-head b {
  letter-spacing: 1px;
}

.dd-head span {
  font-size: 11px;
  color: #9aa093;
}

.dd-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 8px 0;
  border-bottom: 1px solid #f0ede3;
  font-size: 13.5px;
}

.dd-item:last-child {
  border: none;
}

.dd-time {
  font-size: 11.5px;
  color: #9aa093;
  width: 44px;
  flex: none;
}

.dd-text {
  flex: 1;
}

.dd-empty {
  color: #9aa093;
  font-size: 13px;
  padding: 10px 0;
}

.skel {
  height: 18px;
  border-radius: 5px;
  background: #edeade;
  margin: 12px 0;
  width: 70%;
  animation: pulse 1.1s ease-in-out infinite;
}

.skel:nth-of-type(2) {
  width: 86%;
  animation-delay: 0.12s;
}

.skel:nth-of-type(3) {
  width: 55%;
  animation-delay: 0.24s;
}

@keyframes pulse {
  50% {
    opacity: 0.45;
  }
}

/* 柱图 */
.chart {
  height: 300px;
  width: 100%;
}

/* 其它模块占位 */
.module-empty h2 {
  letter-spacing: 3px;
}

/* ============ 悬停提示（fixed，脱离面板） ============ */
.heat-tip {
  position: fixed;
  z-index: 3000;
  pointer-events: none;
  background: #22302a;
  color: #f0ede2;
  border-radius: 10px;
  padding: 11px 14px;
  font-size: 12px;
  line-height: 1.9;
  min-width: 190px;
  max-width: 320px;
  transform: translate(-50%, calc(-100% - 12px));
  box-shadow: 0 8px 24px rgba(34, 48, 42, 0.28);
}

.heat-tip.below {
  transform: translate(-50%, 14px);
}

.tip-date {
  margin: 0;
  font-size: 11px;
  opacity: 0.6;
}

.tip-progress {
  display: flex;
  align-items: center;
  gap: 9px;
  margin: 3px 0;
}

.tip-progress b {
  color: #f5d9a8;
  font-weight: 500;
  white-space: nowrap;
}

.tip-bar {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.16);
  overflow: hidden;
}

.tip-bar i {
  display: block;
  height: 100%;
  background: #8fc4a4;
}

.tip-item {
  margin: 0;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.tip-time {
  opacity: 0.55;
  margin-right: 8px;
}

.tip-empty {
  margin: 0;
  opacity: 0.55;
}

.tip-fade-enter-active,
.tip-fade-leave-active {
  transition: opacity 0.15s;
}

.tip-fade-enter-from,
.tip-fade-leave-to {
  opacity: 0;
}
</style>
