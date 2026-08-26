<template>
  <!--
    登录页（仅登录逻辑，样式后续再设计）
    位置说明：src/views/login/index.vue，由 router/index.ts 中 /login 路由懒加载
    调用链路：本页 handleLogin → store/user.ts 的 login → api/auth.ts 的 login → 后端 POST /auth/login
  -->
  <div>
    <h2>登录</h2>
    <!-- @submit.prevent 阻止表单默认刷新行为，走自定义登录逻辑 -->
    <form @submit.prevent="handleLogin">
      <!-- 账号输入框：v-model.trim 双向绑定并去掉首尾空格 -->
      <div>
        <label for="account">账号</label>
        <input id="account" v-model.trim="form.account" type="text" placeholder="请输入账号" />
      </div>
      <!-- 密码输入框 -->
      <div>
        <label for="password">密码</label>
        <input id="password" v-model="form.password" type="password" placeholder="请输入密码" />
      </div>
      <!-- 错误提示：登录失败时展示后端返回的错误信息 -->
      <p v-if="errorMsg">{{ errorMsg }}</p>
      <!-- 登录按钮：loading 时禁用，防止重复提交 -->
      <button type="submit" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>
    </form>
  </div>
</template>

<script setup lang="ts">
// ---------------- import 区 ----------------
import { reactive, ref } from 'vue' // 组合式 API：响应式状态
import { useRouter } from 'vue-router' // 路由跳转（登录成功后进入首页）
import { useUserStore } from '@/store/user' // 用户状态仓库（登录动作封装在这里）

// ---------------- 实例化 ----------------
const router = useRouter() // 路由实例
const userStore = useUserStore() // 用户状态仓库实例

// ---------------- 响应式状态 ----------------
// 表单数据
const form = reactive({
  account: '', // 账号
  password: '' // 密码
})
// 登录中标志：防止重复提交
const loading = ref(false)
// 错误信息：登录失败时展示
const errorMsg = ref('')

// ---------------- 登录逻辑 ----------------
/**
 * 处理登录：
 * 1. 前端校验账号、密码非空（与后端 LoginDTO 的 @NotBlank 校验对应）
 * 2. 调 store 的 login 动作（内部走 axios → 后端 /auth/login）
 * 3. 成功 → 跳转首页；失败 → 展示后端返回的错误信息
 */
async function handleLogin(): Promise<void> {
  // 清空上一次的错误提示
  errorMsg.value = ''

  // 前端校验：账号不能为空
  if (!form.account) {
    errorMsg.value = '账号不能为空'
    return
  }
  // 前端校验：密码不能为空
  if (!form.password) {
    errorMsg.value = '密码不能为空'
    return
  }

  // 开始登录，禁用提交按钮
  loading.value = true
  try {
    // 调 store 登录动作；成功后 token 已写入 localStorage，登录态由路由守卫接管
    await userStore.login({ account: form.account, password: form.password })
    // 登录成功 → 跳转首页
    router.push('/')
  } catch (e) {
    // 登录失败：展示后端返回的错误信息（如"密码错误"/"账号未被启用"）
    errorMsg.value = e instanceof Error ? e.message : '登录失败，请稍后重试'
  } finally {
    // 无论成功失败都恢复按钮可用
    loading.value = false
  }
}
</script>

<!-- 样式：按需求暂不设计，后续补充 -->
