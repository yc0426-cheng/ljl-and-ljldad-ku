import { defineStore } from "pinia";
import { ref } from "vue";
import { SysMenuApi } from "@/api/system/menu/menu";
import type { SysMenu } from "@/types/system/menu";

/**
 * 菜单状态：缓存菜单树，供路由注册与侧边栏渲染共用
 */
export const useMenuStore = defineStore("menu", () => {
  const menus = ref<SysMenu[]>([]);
  const loaded = ref(false);

  /** 拉取菜单树（已加载则直接返回缓存） */
  async function fetchMenus(): Promise<SysMenu[]> {
    if (loaded.value) return menus.value;
    menus.value = await SysMenuApi.listTree();
    loaded.value = true;
    return menus.value;
  }

  /** 重置（退出登录时调用） */
  function reset(): void {
    menus.value = [];
    loaded.value = false;
  }

  return { menus, loaded, fetchMenus, reset };
});
