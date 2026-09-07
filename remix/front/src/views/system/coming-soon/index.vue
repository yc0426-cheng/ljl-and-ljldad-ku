<template>
  <!--
    动态路由占位页
    用途：菜单里新增的路由（组件尚未在 views 下实现）点击后跳到这里，
         提示该页面还在建设中，避免落到 404/被兜底重定向回主页
    位置：src/views/system/coming-soon/index.vue，由 /system/coming-soon 静态路由加载
  -->
  <div class="page">
    <el-card class="card">
      <el-result icon="info" :title="pageTitle" sub-title="该菜单对应页面还在建设中">
        <template #extra>
          <p v-if="routePath" class="route-info">路由地址：{{ routePath }}</p>
          <el-button type="primary" @click="goHome">返回主页</el-button>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup lang="ts">
// ---------------- import 区 ----------------
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

// ---------------- 实例化 ----------------
const route = useRoute()
const router = useRouter()

// 菜单名称与路由地址由主页点击时通过 query 传入
const pageTitle = computed(() =>
  typeof route.query.title === 'string' ? route.query.title : '页面建设中'
)
const routePath = computed(() => (typeof route.query.route === 'string' ? route.query.route : ''))

// ---------------- 动作 ----------------
function goHome(): void {
  router.push('/home/index')
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f2ea;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.card {
  width: 520px;
  max-width: 100%;
}

.route-info {
  color: #9aa093;
  font-size: 13px;
  margin: 0 0 16px;
}
</style>
