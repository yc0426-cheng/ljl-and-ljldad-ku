<template>
  <!-- 根组件：只放路由出口，具体页面由 router 按当前路径渲染 -->
  <router-view />
</template>

<script setup lang="ts">
// 本文件无需额外 import；<router-view> 是 vue-router 安装后全局注册的组件
</script>
