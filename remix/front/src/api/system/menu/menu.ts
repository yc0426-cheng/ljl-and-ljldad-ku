import { SysMenu } from '@/types/system/menu'
import { request } from '@/utils/request'

/**
 * 系统菜单Api（动态路由/菜单管理）
 */
export class MenuApi {
  /**
   * 查询菜单树
   * 前端登录后拉取，渲染左侧菜单 + 前端据此动态注册路由
   *
   * @return 顶级菜单（含嵌套 children）
   */
  static listTree(): Promise<SysMenu[]> {
    return request.get<SysMenu[]>('/system/menu/list/tree')
  }

  /**
   * 新增菜单/路由
   *
   * @param menu 菜单 JSON（menuId 可空）
   * @return 新菜单 ID
   */
  static addMenu(menu: SysMenu): Promise<number> {
    // 增删改的错误提示由页面自行弹出（skipGlobalError），避免与全局提示重复
    return request.post<number>('/system/menu/add', menu, { skipGlobalError: true })
  }

  /**
   * 编辑菜单/路由
   *
   * @param menu 菜单 JSON（menuId 必填）
   */
  static editMenu(menu: SysMenu): Promise<void> {
    return request.post('/system/menu/edit', menu, { skipGlobalError: true })
  }

  /**
   * 删除菜单/路由（软删除自身 + 子孙）
   *
   * @param menuId 菜单 ID
   */
  static deleteMenu(menuId: number): Promise<void> {
    return request.post('/system/menu/delete', undefined, {
      params: { menuId },
      skipGlobalError: true
    })
  }
}
