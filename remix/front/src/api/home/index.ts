import { request } from '@/utils/request'
import type { IHeatCell, IDayRecord, IAnswerStats, StatUnit, HeatLevel } from '@/types/home'

/**
 * 学习主页相关接口
 * 位置说明：src/api/home.ts
 * 调用链路：views/home/index.vue → 本文件 → 后端 home 模块
 *
 * ⚠️ 后端接口尚未提供，当前全部走本文件底部的 Mock（USE_MOCK = true）。
 * 后端就绪后只需把 USE_MOCK 改为 false，视图层零改动：
 *   getHeatmap     → GET /api/home/heatmap?weeks=18      每日学习记录（热力表数据源）
 *   getDayRecord   → GET /api/home/day-record?date=xx    某日具体学习记录（悬停/点击展示）
 *   getAnswerStats → GET /api/home/answer-stats?unit=xx  答题个数统计（日/月/年）
 */
const USE_MOCK = true // TODO 后端接口就绪后改为 false

/**
 * 学习进度 → 颜色档位（0~4，档位越高颜色越深）
 * 划分规则前后端共用：0 / (0,25] / (25,50] / (50,75] / (75,100]
 * 若后端将来直接下发 level 字段，可删掉前端换算，IHeatCell.level 直接取后端值
 */
export function progressLevel(progress: number): HeatLevel {
  if (progress <= 0) return 0
  if (progress <= 25) return 1
  if (progress <= 50) return 2
  if (progress <= 75) return 3
  return 4
}

/**
 * 每日学习记录：近 N 周，颜色深浅由 progress 划分
 * 对应后端（规划）：HomeApiController#heatmap
 */
export function getHeatmap(weeks = 18): Promise<IHeatCell[]> {
  if (!USE_MOCK) return request.get<IHeatCell[]>('/home/heatmap', { params: { weeks } })
  return mockHeatmap(weeks)
}

/**
 * 某一天的具体学习记录（悬停/点击热力格时调用）
 * 视图层已按日期做缓存，同一格不会重复请求
 */
export function getDayRecord(date: string): Promise<IDayRecord> {
  if (!USE_MOCK) return request.get<IDayRecord>('/home/day-record', { params: { date } })
  return mockDayRecord(date)
}

/** 答题个数统计：unit = day | month | year */
export function getAnswerStats(unit: StatUnit): Promise<IAnswerStats> {
  if (!USE_MOCK) return request.get<IAnswerStats>('/home/answer-stats', { params: { unit } })
  return mockAnswerStats(unit)
}

/* ==================== Mock 实现（后端就绪后整段删除） ==================== */

const delay = (ms: number) => new Promise((r) => setTimeout(r, ms))
const pad = (n: number) => String(n).padStart(2, '0')
const fmtDate = (d: Date) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
const rnd = (min: number, max: number) => Math.round(min + Math.random() * (max - min))

/** Mock 数据仓库：热力表与某日明细共用，保证悬停明细与格子颜色一致 */
const cellStore = new Map<string, IHeatCell>()

async function mockHeatmap(weeks: number): Promise<IHeatCell[]> {
  await delay(400)
  cellStore.clear()
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const mondayOffset = (today.getDay() + 6) % 7 // 每列以周一开头
  const start = new Date(today)
  start.setDate(today.getDate() - mondayOffset - (weeks - 1) * 7)

  const cells: IHeatCell[] = []
  for (let i = 0; i < weeks * 7; i++) {
    const d = new Date(start)
    d.setDate(start.getDate() + i)
    const isFuture = d.getTime() > today.getTime()
    const isToday = d.getTime() === today.getTime()

    let progress = 0
    if (!isFuture) {
      const weekend = d.getDay() === 0 || d.getDay() === 6
      const isRest = Math.random() < (weekend ? 0.45 : 0.12) // 部分日子休息
      if (!isRest) {
        progress = Math.min(
          100,
          Math.max(8, Math.round(34 + Math.sin(i / 3.2) * 18 + (Math.random() - 0.5) * 38))
        )
      }
    }
    if (isToday && progress === 0) progress = 46 // 演示：今日至少有记录

    const cell: IHeatCell = {
      date: fmtDate(d),
      progress,
      level: progressLevel(progress),
      duration: progress > 0 ? Math.round(progress * 1.6 + Math.random() * 20) : 0,
      answers: progress > 0 ? Math.round(progress * 0.4 + Math.random() * 6) : 0,
      isToday,
      isFuture
    }
    cellStore.set(cell.date, cell)
    cells.push(cell)
  }
  return cells
}

const CHAPTERS = ['贪心策略', '动态规划', '分治思想', '图论基础', '排序与查找']

async function mockDayRecord(date: string): Promise<IDayRecord> {
  await delay(250)
  const cell = cellStore.get(date)
  const base: IDayRecord = {
    date,
    progress: cell?.progress ?? 0,
    duration: cell?.duration ?? 0,
    answers: cell?.answers ?? 0,
    items: []
  }
  if (!cell || cell.progress <= 0) return base

  const items = [
    {
      time: '08:30',
      type: '阅读',
      content: `完成《算法导论》第 ${2 + (cell.progress % 8)} 章 · ${CHAPTERS[cell.progress % CHAPTERS.length]}`
    },
    { time: '12:10', type: '刷题', content: `专项练习 ${cell.answers} 道，正确率 ${rnd(72, 96)}%` }
  ]
  if (cell.progress > 40)
    items.push({ time: '16:40', type: '笔记', content: '整理错题与易错点，输出 1 篇复盘笔记' })
  if (cell.progress > 65)
    items.push({ time: '21:15', type: '打卡', content: `英语单词打卡 ${50 + cell.progress} 个` })
  return { ...base, items }
}

async function mockAnswerStats(unit: StatUnit): Promise<IAnswerStats> {
  await delay(400)
  const labels: string[] = []
  const values: number[] = []
  const now = new Date()
  if (unit === 'day') {
    for (let i = 29; i >= 0; i--) {
      const d = new Date(now)
      d.setDate(now.getDate() - i)
      labels.push(`${d.getMonth() + 1}.${d.getDate()}`)
      values.push(rnd(12, 56))
    }
  } else if (unit === 'month') {
    for (let i = 11; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
      labels.push(`${d.getMonth() + 1}月`)
      values.push(rnd(600, 2100))
    }
  } else {
    for (let i = 5; i >= 0; i--) {
      labels.push(String(now.getFullYear() - i))
      values.push(rnd(9000, 24000))
    }
  }
  return { unit, labels, values }
}
