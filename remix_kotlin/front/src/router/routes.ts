import type { RouteRecordRaw } from "vue-router";

/**
 * 静态路由（无需登录即可访问）
 */
export const staticRoutes: RouteRecordRaw[] = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/login/index.vue"),
    meta: { title: "登录" },
  },
  {
    path: "/404",
    name: "NotFound",
    component: () => import("@/views/error/404.vue"),
    meta: { title: "404" },
  },
];

/**
 * 主布局路由（动态路由挂载的父级）
 */
export const layoutRoute: RouteRecordRaw = {
  path: "/",
  name: "Layout",
  component: () => import("@/layouts/DefaultLayout.vue"),
  redirect: "/home",
  children: [
    {
      path: "home",
      name: "Home",
      component: () => import("@/views/home/index.vue"),
      meta: { title: "主页" },
    },
  ],
};
