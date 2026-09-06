/**
 * 学习系统主页 —— 类型定义
 * 位置说明：src/views/home/index.d.ts
 * API 层（src/api/home.ts）与视图层共用本文件类型
 */
import type { Component } from 'vue'

/** 答题统计维度：day=近30天每日 / month=近12月每月 / year=近6年每年 */
export type StatUnit = 'day' | 'month' | 'year'

/** 颜色档位：0~4，数值越大颜色越深，由学习进度划分（见 api/home.ts progressLevel） */
export type HeatLevel = 0 | 1 | 2 | 3 | 4

/** 热力表单元格：某一天的学习汇总，progress 决定颜色深浅 */
export interface IHeatCell {
  date: string // YYYY-MM-DD
  progress: number // 学习进度 0~100
  level: HeatLevel // 颜色档位
  duration: number // 当日学习时长（分钟）
  answers: number // 当日答题个数
  isToday: boolean
  isFuture: boolean // 未来的日期不显示颜色、不可交互
}

/** 单条学习记录（悬停热力格时展示） */
export interface IDayRecordItem {
  time: string // HH:mm
  type: string // 阅读 / 刷题 / 笔记 / 打卡
  content: string
}

/** 某一天的具体学习记录 */
export interface IDayRecord {
  date: string
  progress: number
  duration: number
  answers: number
  items: IDayRecordItem[]
}

/** 答题个数统计（柱图数据源） */
export interface IAnswerStats {
  unit: StatUnit
  labels: string[]
  values: number[]
}

/** 左侧菜单树节点 */
export interface IMenuNode {
  id: string
  label: string
  icon: Component // 局部引入的图标组件，需 markRaw 包裹
  view: string // 点击后切换的内容视图标识
  children?: IMenuNode[]
}
