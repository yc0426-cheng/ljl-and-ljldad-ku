<template>
  <!--
    个人设置页
    位置说明：src/views/user/setting/index.vue，由 router/index.ts 中 /user/setting 路由懒加载
    进入方式：主页（/home/index）左下角用户区点击
    登录态：由路由守卫保证进入本页时已登录；刷新（会话内 token 有效）停留在本页，
            用户信息来自 store（登录/守卫校验时回写，刷新后不丢）
  -->
  <div class="page">
    <div class="card">
      <div class="head">
        <span class="avatar">{{ displayName.slice(0, 1) }}</span>
        <div class="head-text">
          <b>{{ displayName }}</b>
          <span>{{ userStore.userInfo?.account || '—' }}</span>
        </div>
      </div>

      <el-descriptions title="账号信息" :column="1" border>
        <el-descriptions-item label="用户 ID">
          {{ userStore.userInfo?.userId ?? '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="账号">
          {{ userStore.userInfo?.account || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="姓名">
          {{ userStore.userInfo?.name || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="登录状态">
          <el-tag type="success" size="small">已登录</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div class="actions">
        <el-button type="primary" plain @click="goHome">返回主页</el-button>
        <el-button type="danger" plain :loading="loggingOut" @click="onLogout">退出登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// ---------------- import 区 ----------------
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus' // 登出确认框 + 成功提示
import { useUserStore } from '@/store/user'

// ---------------- 实例化 ----------------
const router = useRouter()
const userStore = useUserStore()

// 展示名：name 优先，缺失回退 account
const displayName = computed(
  () => userStore.userInfo?.name || userStore.userInfo?.account || '未知用户'
)

// ---------------- 页面动作 ----------------
/** 返回主页 */
function goHome(): void {
  router.push('/home/index')
}

/**
 * 退出登录：确认 → 调 store.logout（后端登出 + 清理本地）→ 提示 → 回登录页。
 * 与主页的登出逻辑保持一致。
 */
const loggingOut = ref(false)

async function onLogout(): Promise<void> {
  if (loggingOut.value) {
    return
  }
  try {
    await ElMessageBox.confirm('退出后需要重新登录才能继续使用', '退出登录', {
      confirmButtonText: '退出登录',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return // 用户取消
  }

  loggingOut.value = true
  try {
    await userStore.logout()
    ElMessage.success('已退出登录')
  } finally {
    loggingOut.value = false
  }
  router.replace('/login')
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
  width: 440px;
  max-width: 100%;
  background: #fdfcf8;
  border: 1px solid #e3ded1;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 10px 30px rgba(34, 48, 42, 0.08);
}

.head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 22px;
}

.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #2d5a4a;
  color: #f0ede2;
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}

.head-text b {
  display: block;
  font-size: 18px;
  color: #22302a;
  letter-spacing: 1px;
}

.head-text span {
  font-size: 12px;
  color: #9aa093;
}

.actions {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
