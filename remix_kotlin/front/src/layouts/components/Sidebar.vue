<template>
  <div class="sidebar">
    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      :collapse-transition="false"
      background-color="#001529"
      text-color="#a6adb4"
      active-text-color="#ffffff"
      @select="handleSelect"
    >
      <MenuTree :menus="menuStore.menus" :parent-path="''" />
    </el-menu>
    <div class="sidebar__toggle" @click="$emit('toggle')">
      <el-icon :size="18">
        <component :is="collapsed ? 'Expand' : 'Fold'" />
      </el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from "vue-router";
import { useMenuStore } from "@/stores/menu";
import MenuTree from "./MenuTree.vue";

defineProps<{ collapsed: boolean }>();
defineEmits<{ toggle: [] }>();

const route = useRoute();
const router = useRouter();
const menuStore = useMenuStore();

const activeMenu = route.path;

function handleSelect(index: string): void {
  router.push(index);
}
</script>

<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.sidebar :deep(.el-menu) {
  flex: 1;
  border-right: none;
  overflow-y: auto;
}
.sidebar__toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  cursor: pointer;
  color: #a6adb4;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  transition: color 0.3s;
}
.sidebar__toggle:hover {
  color: #ffffff;
}
</style>
