<template>
  <!--
    学习系统主页
    位置说明：src/views/home/index.vue，由 router/index.ts 中常驻布局（src/layout/index.vue）的
             /home/index 子路由懒加载（/ 会重定向到 /home/index）
    调用链路：本页 → api/home.ts（getHeatmap / getDayRecord / getAnswerStats）
    布局：左侧模块树 + 底部用户区在常驻布局 src/layout/index.vue 中全站存在，本页只渲染右侧主区内容：
         每日学习记录热力表（颜色深浅=学习进度，悬停显示具体记录）
              + 每日答题个数柱图（日/月/年切换）
         左侧树点击"内置内容"叶子（每日记录/我的书架/题库练习）时通过 ?view= 通知本页切换视图
    登录态：由路由守卫保证进入本页时已登录
  -->
  <div class="home">
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
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, MarkLineComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { EChartsCoreOption } from 'echarts/core'
import { getHeatmap, getDayRecord, getAnswerStats } from '@/api/home'
import type { IHeatCell, IDayRecord, StatUnit } from '@/types/home'

echarts.use([BarChart, GridComponent, TooltipComponent, MarkLineComponent, CanvasRenderer])

// ---------------- 实例化 ----------------
const route = useRoute()

// ---------------- 视图切换（左侧树点击"内置内容"叶子 → 布局带 ?view= 跳转到本页） ----------------
const activeView = ref('progress')
const activeLabel = ref('学习进度')

/** 从路由 query 恢复视图（无 query 时保持默认"学习进度"） */
function applyViewQuery(): void {
  const view = typeof route.query.view === 'string' ? route.query.view : ''
  if (view) {
    activeView.value = view
    activeLabel.value = typeof route.query.label === 'string' ? route.query.label : view
  }
}

watch(() => route.query, applyViewQuery)

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
  // 路由可能带 ?view=（左侧树点击内置内容叶子跳转而来），先恢复视图
  applyViewQuery()

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
</script>

<style scoped>
.home {
  /* 渲染在常驻布局 src/layout/index.vue 的 .main 内，本页不再管页面骨架 */
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.crumb {
  font-size: 10px;
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
  font-size: 10px;
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
  font-size: 11px;
  color: #9aa093;
}

.legend i {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  display: inline-block;
}

.today-mark {
  outline: 1px solid #b5482f;
  outline-offset: 1px;
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
  font-size: 10px;
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
  font-size: 10px;
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
  outline: 1px solid #b5482f;
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
  font-size: 13px;
}

.dd-item:last-child {
  border: none;
}

.dd-time {
  font-size: 11px;
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

/* ---- 热力表横向滚动条（周数多时出现） ---- */
.heat-scroll {
  scrollbar-width: thin;
  scrollbar-color: #d6e4d8 transparent;
}

.heat-scroll::-webkit-scrollbar {
  height: 8px;
}

.heat-scroll::-webkit-scrollbar-thumb {
  background-color: #d6e4d8;
  border-radius: 8px;
}

.heat-scroll::-webkit-scrollbar-thumb:hover {
  background-color: #6ba186;
}
</style>
