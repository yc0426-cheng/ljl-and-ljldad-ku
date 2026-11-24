import request from "@/api/request";
import type { SysMenu } from "@/types/system/menu";

/**
 * 系统菜单Api
 */
export class SysMenuApi {
  /** 菜单树（登录后拉取，渲染左侧菜单 + 动态注册路由） */
  static listTree(): Promise<SysMenu[]> {
    return request.get("/sys/menu/list/tree");
  }

  /** 新增菜单/路由 */
  static add(menu: SysMenu): Promise<string> {
    return request.post("/sys/menu/add", menu);
  }

  /** 编辑菜单/路由 */
  static edit(menu: SysMenu): Promise<void> {
    return request.post("/sys/menu/edit", menu);
  }

  /** 删除菜单/路由（软删除自身 + 子孙） */
  static delete(menuId: string): Promise<void> {
    return request.post("/sys/menu/delete", menuId);
  }
}
