/**
 * 系统菜单 / 动态路由 —— 类型定义
 * 与 system 模块实体 SysMenu 对应（JSON 字段同名）
 */
export interface SysMenu {
  /** 菜单 ID */
  menuId?: number
  /** 父菜单 ID：顶级为 null */
  parentId?: number | null
  /** 菜单名称 */
  menuName: string
  /** 菜单类型：1 = 目录（分组），2 = 菜单（叶子路由） */
  menuType: number
  /** 路由地址（叶子有效），如 /system/menu */
  routePath?: string
  /** 组件路径（src/views 下相对路径），如 system/menu/index */
  component?: string
  /** 图标名（对应 @element-plus/icons-vue） */
  icon?: string
  /** 排序号 */
  orderNo?: number
  /** 是否在菜单显示 */
  visible?: boolean
  /** 状态：1 启用，0 停用 */
  status?: number
  /** 子菜单 */
  children?: SysMenu[]
}
