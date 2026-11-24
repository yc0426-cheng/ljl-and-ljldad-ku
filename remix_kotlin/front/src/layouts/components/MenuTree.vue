<template>
  <template v-for="menu in menus" :key="menu.menuId">
    <template v-if="menu.visible">
      <!-- 目录：渲染子菜单 -->
      <el-sub-menu
        v-if="menu.menuType === MenuType.DIR && menu.children?.length"
        :index="fullPath(menu)"
      >
        <template #title>
          <el-icon v-if="menu.icon">
            <component :is="menu.icon" />
          </el-icon>
          <span>{{ menu.menuName }}</span>
        </template>
        <MenuTree :menus="menu.children" :parent-path="fullPath(menu)" />
      </el-sub-menu>
      <!-- 菜单：叶子节点 -->
      <el-menu-item
        v-else-if="menu.menuType === MenuType.MENU"
        :index="fullPath(menu)"
      >
        <el-icon v-if="menu.icon">
          <component :is="menu.icon" />
        </el-icon>
        <span>{{ menu.menuName }}</span>
      </el-menu-item>
    </template>
  </template>
</template>

<script setup lang="ts">
import { ElMenuItem, ElSubMenu, ElIcon } from "element-plus";
import { MenuType } from "@/types/system/menu/constants";
import type { SysMenu } from "@/types/system/menu";

const props = defineProps<{
  menus: SysMenu[];
  parentPath: string;
}>();

/** 拼接路径 */
function joinPath(parent: string, child: string): string {
  return `${parent}/${child}`.replace(/\/+/g, "/");
}

/** 计算当前菜单完整路径 */
function fullPath(menu: SysMenu): string {
  return props.parentPath
    ? joinPath(props.parentPath, menu.routePath)
    : menu.routePath;
}
</script>
