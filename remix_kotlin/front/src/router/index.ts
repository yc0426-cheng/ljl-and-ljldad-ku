import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";
import type { DefineComponent } from "vue";
import { staticRoutes, layoutRoute } from "./routes";
import { useMenuStore } from "@/stores/menu";
import { MenuType } from "@/types/system/menu/constants";
import type { SysMenu } from "@/types/system/menu";

/**
 * 视图组件映射表：后端 component 字符串 → 实际组件
 * 约定 component 形如 "system/role/index"，对应 src/views/system/role/index.vue
 */
const viewModules = import.meta.glob<DefineComponent>("@/views/**/*.vue");

/** 根据后端 component 字段解析组件，未匹配时回退到 404 页 */
function resolveComponent(component: string) {
  const key = `/src/views/${component}.vue`;
  return viewModules[key] ?? (() => import("@/views/error/404.vue"));
}

/** 拼接路径，去除首尾斜杠 */
function joinPath(parent: string, child: string): string {
  return `${parent}/${child}`.replace(/\/+/g, "/");
}

/**
 * 将后端菜单树展平为路由（目录仅用于拼接路径，菜单生成路由）
 * @param menus 当前层菜单
 * @param parentPath 父级路径前缀
 */
function buildRoutes(menus: SysMenu[], parentPath = ""): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = [];

  for (const menu of menus) {
    if (!menu.visible) continue;

    const fullPath = parentPath
      ? joinPath(parentPath, menu.routePath)
      : menu.routePath;

    if (menu.menuType === MenuType.MENU) {
      routes.push({
        path: fullPath,
        name: menu.menuId,
        component: resolveComponent(menu.component),
        meta: { title: menu.menuName, icon: menu.icon },
      });
    } else if (menu.children?.length) {
      routes.push(...buildRoutes(menu.children, fullPath));
    }
  }

  return routes;
}

const router = createRouter({
  history: createWebHistory(),
  routes: [...staticRoutes, layoutRoute],
});

/** 标记动态路由是否已加载 */
let isLoaded = false;

/** 加载并注册动态路由 */
async function loadDynamicRoutes(): Promise<void> {
  const menuStore = useMenuStore();
  const menus = await menuStore.fetchMenus();
  const routes = buildRoutes(menus);
  routes.forEach((route) => router.addRoute("Layout", route));
  isLoaded = true;
}

/** 重置动态路由（退出登录时调用） */
export function resetRouter(): void {
  const dynamicNames = router
    .getRoutes()
    .map((route) => route.name)
    .filter((name): name is string => typeof name === "string");
  dynamicNames.forEach((name) => {
    if (name !== "Layout") router.removeRoute(name);
  });
  isLoaded = false;
}

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const token = localStorage.getItem("token");

  // 未登录：仅允许访问登录页
  if (!token) {
    if (to.path === "/login") {
      next();
    } else {
      next("/login");
    }
    return;
  }

  // 已登录访问登录页：重定向到首页
  if (to.path === "/login") {
    next("/");
    return;
  }

  // 首次进入：加载动态路由后重新导航
  if (!isLoaded) {
    try {
      await loadDynamicRoutes();
      next({ ...to, replace: true });
    } catch {
      next("/login");
    }
    return;
  }

  next();
});

export default router;
