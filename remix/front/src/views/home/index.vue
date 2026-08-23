<template>
  <!--
    首页占位：用于验证"登录 → 进入首页"链路是否打通，同时演示登出。
    后续可替换为真正的首页内容。
  -->
  <div>
    <h2>首页</h2>
    <!-- 展示当前登录账号（来自 user store，登录成功后写入） -->
    <p>登录成功，当前账号：{{ userStore.userInfo?.account }}</p>
    <button @click="handleLogout">退出登录</button>
  </div>
</template>

<script setup lang="ts">
// ---------------- import 区 ----------------
import { useRouter } from 'vue-router' // 路由跳转
import { useUserStore } from '@/store/user' // 用户状态仓库

// ---------------- 实例化 ----------------
const router = useRouter()
const userStore = useUserStore()

// ---------------- 登出逻辑 ----------------
/**
 * 登出：清空本地 token/用户信息后跳回登录页
 * （调后端 /auth/logout 的部分在 store.logout 内，后端接口暂未实现，本地清理不受影响）
 */
async function handleLogout(): Promise<void> {
  await userStore.logout()
  router.push('/login')
}
</script>
