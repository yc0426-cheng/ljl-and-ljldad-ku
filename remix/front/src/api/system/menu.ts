import { request } from '@/utils/request'
import type { SysMenu } from '@/types/system/menu'

/**
 * 系统菜单（动态路由管理）接口
 * 对应后端：system 模块 SysMenuController
 *   GET  /system/menu/list/tree  菜单树
 *   POST /system/menu/add        新增
 *   POST /system/menu/edit       编辑
 *   POST /system/menu/delete     删除（menuId 走 query）
 * 增删改的错误提示由页面自行弹出（skipGlobalError），避免与全局提示重复
 */

/** 查询菜单树（供主页左侧菜单渲染 + 动态注册路由） */
export function listMenuTree(): Promise<SysMenu[]> {
  return request.get<SysMenu[]>('/system/menu/list/tree')
}

/** 新增菜单/路由 */
export function addMenu(data: SysMenu): Promise<number> {
  return request.post<number>('/system/menu/add', data, { skipGlobalError: true })
}

/** 编辑菜单/路由 */
export function editMenu(data: SysMenu): Promise<unknown> {
  return request.post('/system/menu/edit', data, { skipGlobalError: true })
}

/** 删除菜单/路由（软删除自身 + 子孙） */
export function deleteMenu(menuId: number): Promise<unknown> {
  return request.post('/system/menu/delete', undefined, {
    params: { menuId },
    skipGlobalError: true
  })
}
