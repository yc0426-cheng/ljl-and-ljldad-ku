<template>
  <!--
    登录页
    位置说明：src/views/login/index.vue，由 router/index.ts 中 /login 路由懒加载
    调用链路：本页 handleLogin → store/user.ts 的 login → api/auth.ts 的 login → 后端 POST /auth/login
  -->
  <div class="page">
    <div class="page-main-static">
      <div class="page-picture">
        懒得做(应该可以放点图片)
      </div>
    </div>
    <div class="page-main-login">
      <div class="login">
        <h2>学习管理系统登录</h2>
      </div>
      <!-- 账号输入框：v-model.trim 双向绑定并去掉首尾空格 -->
      <div class="form-group">
        <el-input
            v-model.trim="form.account"
            class="input-border"
            type="text"
            placeholder="请输入账号"
            @input="onAccountRuleInput"/>
        <el-input
            v-model="form.password"
            class="input-border"
            type="password"
            placeholder="请输入密码"
            show-password
        />
      </div>
      <!-- 登录按钮：loading 时禁用，防止重复提交 -->
      <div class="login-button">
        <el-button type="success" :disabled="loading" native-type="submit" @click="handleLogin">
          {{ loading ? '登录中...' : '登录' }}
        </el-button>
      </div>
    </div>

    <!-- 失败红色边框弹窗：fixed 居中显示，3 秒后自动消失 -->
    <transition name="fail-fade">
      <div v-if="showFailBox" class="fail-box" @click="showFailBox = false">
        {{ failBoxMsg }}
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
// ---------------- import 区 ----------------
import {reactive, ref} from 'vue' // 组合式 API：响应式状态
import {useRouter} from 'vue-router' // 路由跳转（登录成功后进入首页）
import {useUserStore} from '@/store/user' // 用户状态仓库（登录动作封装在这里）
import {ElLoading} from 'element-plus' // 全屏 loading 服务（点击登录后转圈遮罩）
import 'element-plus/dist/index.css'

// ---------------- 常量 ----------------
// 最大失败次数（与后端 SysUserServiceImpl.editError 里 passErrorCount + 1 > 3 的阈值对齐）
const MAX_FAIL_COUNT = 3
// loading 最短显示时间（毫秒），避免响应太快导致遮罩闪烁
const MIN_LOADING_MS = 2000
// 失败弹窗自动隐藏时间（毫秒）
const FAIL_BOX_VISIBLE_MS = 3000

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
// 已失败次数：前端本地维护，用于显示"剩余 X 次机会"
// 注意：本计数器仅在浏览器未刷新/未清缓存时有效，与后端 Redis 里的失败计数不严格同步
const failCount = ref(0)
// 是否显示失败弹窗
const showFailBox = ref(false)
// 失败弹窗文案
const failBoxMsg = ref('')
// 失败弹窗自动隐藏定时器引用（用于点击重新登录时清掉旧定时器）
let failBoxTimer: ReturnType<typeof setTimeout> | null = null

// ---------------- 登录逻辑 ----------------
/**
 * 处理登录：
 * 1. 前端校验账号、密码非空（与后端 LoginDTO 的 @NotBlank 校验对应）
 * 2. 弹全屏 loading 遮罩（ElLoading.service），转圈并锁定点击
 * 3. 调 store 的 login 动作；无论成败，loading 至少持续 2 秒（避免闪烁）
 * 4. 成功 → 跳转首页；失败 → 屏幕正中央红色边框弹窗，文案带"剩余 X 次机会"
 * todo 请求逻辑问题，不应该是前端自己控制
 *
 */
async function handleLogin(): Promise<void> {
  // 清空上一次的错误提示
  errorMsg.value = ''

  // 前端校验：账号不能为空
  if (!form.account) {
    errorMsg.value = '账号不能为空'
    showFail('账号不能为空')
    return
  }
  // 前端校验：密码不能为空
  if (!form.password) {
    errorMsg.value = '密码不能为空'
    showFail('密码不能为空')
    return
  }

  // 开始登录，禁用提交按钮，弹全屏遮罩
  loading.value = true
  const loadingInstance = ElLoading.service({
    lock: true,                              // 锁定点击，防止重复提交
    text: '登录中...',                        // 转圈下方文案
    background: 'rgba(0, 0, 0, 0.7)'         // 半透明黑色背景
  })

  // 记录开始时间，保证 loading 至少持续 MIN_LOADING_MS 毫秒
  const start = Date.now()
  let loginFailed = false
  try {
    // 调 store 登录动作；成功后 token 已写入 localStorage，登录态由路由守卫接管
    await userStore.login({account: form.account, password: form.password})
  } catch (e) {
    loginFailed = true
    errorMsg.value = e instanceof Error ? e.message : '登录失败，请稍后重试'
  }

  // 至少等够 2 秒再关 loading（响应快也不能让遮罩一闪而过）
  const elapsed = Date.now() - start
  if (elapsed < MIN_LOADING_MS) {
    await new Promise(resolve => setTimeout(resolve, MIN_LOADING_MS - elapsed))
  }

  // 关闭 loading
  loadingInstance.close()
  loading.value = false

  if (loginFailed) {
    // 失败：累加本地失败次数，显示红色弹窗
    failCount.value++
    const remaining = Math.max(0, MAX_FAIL_COUNT - failCount.value)
    if (remaining > 0) {
      showFail(`账号或密码错误，您还有 ${remaining} 次机会`)
    } else {
      showFail('错误次数已达上限，账号已被锁定，请 5 分钟后再试')
    }
  } else {
    // 成功：跳转首页
    router.push('/')
  }
}

/**
 * 显示失败弹窗：屏幕正中央红色边框，FAIL_BOX_VISIBLE_MS 毫秒后自动隐藏
 * @param msg 弹窗文案
 */
function showFail(msg: string): void {
  failBoxMsg.value = msg
  showFailBox.value = true
  // 清掉上一次的定时器，避免连续点击导致弹窗提前消失
  if (failBoxTimer) {
    clearTimeout(failBoxTimer)
  }
  failBoxTimer = setTimeout(() => {
    showFailBox.value = false
    failBoxTimer = null
  }, FAIL_BOX_VISIBLE_MS)
}

/**
 * 登录账号输入规则
 *
 * 允许英文字母,数字,-,_
 * 第一位必须为字母
 */
const onAccountRuleInput = (event: Event): void => {
  const input = event.target as HTMLInputElement
  let value = input.value

  // 先移除所有非法字符（只保留字母、数字、-、_）
  value = value.replace(/[^a-zA-Z0-9_-]/g, '')

  // 更新输入框和绑定值
  if (input.value !== value) {
    input.value = value
    form.account = value
  }

  // 检查首位字母
  if (value.length > 0 && !/^[a-zA-Z]/.test(value)) {
    errorMsg.value = '账号必须以字母开头'
  } else if (errorMsg.value === '账号必须以字母开头') {
    // 如果已经修正了，清空错误提示
    errorMsg.value = ''
  }
}

</script>

<style scoped>

.page {
  min-height: 100vh;
  width: 100%;
  display: flex;
  flex-direction: row;
}

.page-main-static {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 94vh;
  width: 60%;
  margin: 20px;
  border-radius: 20px;
  border: 2px solid #595959;
}

.page-main-static .page-picture {
  display: flex;
  text-align: center;

}

.page-main-login {
  width: 40%;
  height: 94vh;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column; /** 纵向排列 */
  background: #ffffff;
  border-radius: 15px;
  margin: 20px 10px;
  padding: 20px;
  border: 1px solid #595959;
  box-shadow: 5px 5px 5px #595959;
}

.login {
  display: flex;
  flex: 0 0 40%;
  height: 200px;
  width: 300px;
  border: 2px solid #000;
  justify-content: center;
  align-items: center;
  border-radius: 20px;
  background: linear-gradient(#5cff1c, #98f8ff); /** 径向渐变 */
}

.form-group {
  display: flex;
  flex: 0 0 20%;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.input-border {
  margin-top: 10px;
  width: 150px;
  height: 30px;
  border: 2px solid #8a00fa;
  border-radius: 5px;
}

.login-button {
  margin-top: 10px;
  display: flex;
  flex: 0 0 6%;
  text-align: center;
  align-items: center;
  justify-content: center;
}

/** element plus 穿透样式*/

/* input样式 */
:deep(.input-border .el-input__inner) {
  text-align: center;
  border-radius: 10px;
}

/* input内的placeholder样式 */
:deep(.input-border .el-input__inner::placeholder) {
  font-size: 14px;
  font-weight: bold;
  font-family: '楷体', serif;
  color: #595959;
}

/**
 * 失败弹窗：屏幕正中央居中，红色边框
 * - position: fixed + transform 居中：脱离文档流，不受父容器影响
 * - z-index: 10000：高于 ElLoading 遮罩（ElLoading 默认 2000+），保证关闭 loading 后弹窗浮在最上层
 */
.fail-box {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10000;
  padding: 24px 36px;
  background: #fff;
  border: 3px solid #f56c6c; /* 红色边框 */
  border-radius: 8px;
  color: #f56c6c; /* 红色文字 */
  font-size: 16px;
  font-weight: bold;
  text-align: center;
  cursor: pointer; /* 提示用户点击可关闭 */
  box-shadow: 0 6px 24px rgba(245, 108, 108, 0.3);
  user-select: none;
}

/* 弹窗淡入淡出 */
.fail-fade-enter-active,
.fail-fade-leave-active {
  transition: opacity 0.25s ease;
}

.fail-fade-enter-from,
.fail-fade-leave-to {
  opacity: 0;
}

</style>
