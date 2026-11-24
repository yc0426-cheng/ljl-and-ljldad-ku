import type { BaseEntity } from "@/types/common";
import type { MenuTypeValue } from "./constants";

/**
 * 系统菜单
 */
export interface SysMenu extends BaseEntity {
  menuId: string;
  parentId: string | null;
  menuName: string;
  menuType: MenuTypeValue;
  routePath: string;
  component: string;
  icon: string;
  orderNo: number;
  visible: boolean;
  status: number;
  delFlag: boolean;
  children: SysMenu[];
}
