<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="layout__aside">
      <Sidebar :collapsed="collapsed" @toggle="collapsed = !collapsed" />
    </el-aside>
    <el-container>
      <el-header class="layout__header">
        <span class="layout__title">{{ route.meta.title }}</span>
      </el-header>
      <el-main class="layout__main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRoute } from "vue-router";
import Sidebar from "./components/Sidebar.vue";

const route = useRoute();
const collapsed = ref(false);
</script>

<style scoped>
.layout {
  height: 100vh;
}
.layout__aside {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
}
.layout__header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e8e8e8;
}
.layout__title {
  font-size: 16px;
  font-weight: 500;
}
.layout__main {
  background-color: #f0f2f5;
}
</style>
