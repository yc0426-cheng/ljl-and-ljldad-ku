<template>
  <!--
    登录页
    位置说明：src/views/login/index.vue，由 router/index.ts 中 /login 路由懒加载
    调用链路：本页 handleLogin → store/user.ts 的 login → api/auth.ts 的 login → 后端 POST /auth/login
    样式说明：与主页（/home/index）同一套纸感绿色主题
             （米色底 #f5f2ea / 墨绿 #22302a / 主题绿 #2d5a4a / 卡片 #fdfcf8 / 边框 #e3ded1）
    提示方式：登录结果一律由页面内 fail-box 提示卡展示，本页不弹任何 ElMessage
  -->
  <div class="page">
    <!-- ============ 左侧：品牌展示区（深绿画布，图片位待补） ============ -->
    <div class="page-main-static">
      <div class="brand-canvas">
        <p class="crumb">LEARNING SYSTEM</p>
        <h1>学习管理系统</h1>
        <p class="slogan">记录每一天的学习足迹</p>
        <!-- 装饰热力格：米黄透明度阶梯，呼应主页"每日学习记录" -->
        <div class="deco-grid">
          <i v-for="n in 40" :key="n" :class="`d${(n * 7) % 5}`" />
        </div>
        <!-- 图片占位区 -->
        <div class="pic-slot">展示区 · 可放置图片</div>
      </div>
    </div>
    <!-- ============ 右侧：登录表单卡片（主页卡片同款材质） ============ -->
    <div class="page-main-login">
      <div class="login-head">
        <p class="crumb">SIGN IN / CONTINUE</p>
        <h2>学习管理系统登录</h2>
        <p class="sub">请输入账号与密码继续</p>
      </div>
      <!-- 账号输入框：v-model.trim 双向绑定并去掉首尾空格 -->
      <div class="form-group">
        <el-input
          v-model.trim="form.account"
          class="input-border"
          type="text"
          placeholder="请输入账号"
          @input="onAccountRuleInput"
        />
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
        <el-button class="login-btn" :disabled="loading" native-type="submit" @click="handleLogin">
          {{ loading ? '登录中...' : '登录' }}
        </el-button>
      </div>
    </div>
    <!-- 失败提示卡：fixed 居中（主页 heat-tip 同款墨绿材质 + 主题红描边），3 秒自动消失，点击可关闭 -->
    <transition name="fail-fade">
      <div v-if="showFailBox" class="fail-box" @click="showFailBox = false">
        <b class="fb-title">登录失败</b>
        <p class="fb-msg">{{ failBoxMsg }}</p>
        <span class="fb-hint">点击任意处关闭</span>
      </div>
    </transition>
  </div>
</template>
<script setup lang="ts">
// ---------------- import 区 ----------------
import { reactive, ref } from 'vue' // 组合式 API：响应式状态
import { useRoute, useRouter } from 'vue-router' // 路由（读取回跳参数 + 登录成功后跳转）
import { useUserStore } from '@/store/user' // 用户状态仓库（登录动作封装在这里）
import { ElLoading } from 'element-plus' // 全屏 loading 服务（点击登录后转圈遮罩）
import 'element-plus/dist/index.css'
// 注意：本页不引入 ElMessage —— 登录结果统一由页面内 fail-box 提示，
// 避免与 store / axios 拦截器里的全局 message 重复弹出
// ---------------- 常量 ----------------
// 最大失败次数（与后端 SysUserServiceImpl.editError 里 passErrorCount + 1 > 3 的阈值对齐）
const MAX_FAIL_COUNT = 3
// loading 最短显示时间（毫秒），避免响应太快导致遮罩闪烁
const MIN_LOADING_MS = 2000
// 失败弹窗自动隐藏时间（毫秒）
const FAIL_BOX_VISIBLE_MS = 3000
// ---------------- 实例化 ----------------
const router = useRouter() // 路由实例
const route = useRoute() // 当前路由（读取守卫跳来时携带的 ?redirect= 回跳地址）
const userStore = useUserStore() // 用户状态仓库实例
// ---------------- 响应式状态 ----------------
// 表单数据
const form = reactive({
  account: '', // 账号
  password: '' // 密码
})
// 登录中标志：防止重复提交
const loading = ref(false)
// 已失败次数：前端本地维护，用于显示"剩余 X 次机会"
// 注意：本计数器仅在浏览器未刷新/未清缓存时有效，与后端 Redis 里的失败计数不严格同步
const failCount = ref(0)
// 是否显示失败弹窗
const showFailBox = ref(false)
// 失败弹窗文案
const failBoxMsg = ref('')
// 失败弹窗自动隐藏定时器引用（用于连续弹窗时清掉旧定时器）
let failBoxTimer: ReturnType<typeof setTimeout> | null = null
// ---------------- 登录逻辑 ----------------
/**
 * 处理登录：
 * 1. 前端校验账号、密码非空（与后端 LoginDTO 的 @NotBlank 校验对应）
 * 2. 弹全屏 loading 遮罩（ElLoading.service），转圈并锁定点击
 * 3. 调 store 的 login 动作；无论成败，loading 至少持续 2 秒（避免闪烁）
 * 4. 成功 → 跳转首页；失败 → 屏幕正中央提示卡，文案带"剩余 X 次机会"
 * todo 请求逻辑问题，不应该是前端自己控制
 */
async function handleLogin(): Promise<void> {
  // 前端校验：账号不能为空
  if (!form.account) {
    showFail('账号不能为空')
    return
  }
  // 前端校验：密码不能为空
  if (!form.password) {
    showFail('密码不能为空')
    return
  }
  // 开始登录，禁用提交按钮，弹全屏遮罩
  loading.value = true
  const loadingInstance = ElLoading.service({
    lock: true, // 锁定点击，防止重复提交
    text: '登录中...', // 转圈下方文案
    background: 'rgba(34, 48, 42, 0.65)' // 墨绿半透明，与主题一致（原为纯黑）
  })
  // 记录开始时间，保证 loading 至少持续 MIN_LOADING_MS 毫秒
  const start = Date.now()
  let loginFailed = false
  try {
    // 调 store 登录动作；成功后 token 已写入 localStorage，登录态由路由守卫接管
    await userStore.login({ account: form.account, password: form.password })
  } catch {
    // 失败文案统一走下方 failCount 分支（账号或密码错误 / 锁定），无需读取 e.message
    loginFailed = true
  }
  // 至少等够 2 秒再关 loading（响应快也不能让遮罩一闪而过）
  const elapsed = Date.now() - start
  if (elapsed < MIN_LOADING_MS) {
    await new Promise((resolve) => setTimeout(resolve, MIN_LOADING_MS - elapsed))
  }
  // 关闭 loading
  loadingInstance.close()
  loading.value = false
  if (loginFailed) {
    // 失败：累加本地失败次数，显示提示卡
    failCount.value++
    const remaining = Math.max(0, MAX_FAIL_COUNT - failCount.value)
    if (remaining > 0) {
      showFail(`账号或密码错误，您还有 ${remaining} 次机会`)
    } else {
      showFail('错误次数已达上限，账号已被锁定，请 5 分钟后再试')
    }
  } else {
    // 成功：优先回到守卫拦截时记录的原路径（?redirect=），否则进主页
    // （redirect 仅接受本站内路径，避免被外部拼接地址利用）
    const redirect = route.query.redirect
    const target =
      typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/home/index'
    router.push(target)
  }
}
/**
 * 显示失败提示卡：屏幕正中央墨绿卡片，FAIL_BOX_VISIBLE_MS 毫秒后自动隐藏
 * @param msg 提示文案
 */
function showFail(msg: string): void {
  failBoxMsg.value = msg
  showFailBox.value = true
  // 清掉上一次的定时器，避免连续点击导致提示提前消失
  if (failBoxTimer) {
    clearTimeout(failBoxTimer)
  }
  failBoxTimer = setTimeout(() => {
    showFailBox.value = false
    failBoxTimer = null
  }, FAIL_BOX_VISIBLE_MS)
}
/**
 * 登录账号输入规则（输入即过滤，不弹任何提示）
 *
 * 允许英文字母,数字,-,_
 * 第一位必须为字母
 */
const onAccountRuleInput = (event: Event): void => {
  const input = event.target as HTMLInputElement
  // 移除所有非法字符（只保留字母、数字、-、_）
  const value = input.value.replace(/[^a-zA-Z0-9_-]/g, '')
  // 更新输入框和绑定值
  if (input.value !== value) {
    input.value = value
    form.account = value
  }
}
</script>
<style scoped>
/* ============ 页面骨架：与主页同底色（米色纸感） ============ */
.page {
  min-height: 100vh;
  width: 100%;
  display: flex;
  flex-direction: row;
  background: #f5f2ea;
  overflow: hidden; /* 登录页不需要滚动条 */
}
/* ============ 左侧：品牌展示区 ============ */
.page-main-static {
  width: 60%;
  margin: 20px 10px 20px 20px;
}
/* 深绿画布：主题绿 → 墨绿渐变，承载品牌信息与图片占位 */
.brand-canvas {
  height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  background: linear-gradient(150deg, #2d5a4a 0%, #22302a 100%);
  border: 1px solid #e3ded1;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(34, 48, 42, 0.14);
  color: #f0ede2;
  padding: 48px;
}
.brand-canvas .crumb {
  font-size: 10px;
  letter-spacing: 4px;
  color: #9db3a8; /* 深底上的灰绿（主页 #9aa093 的提亮版） */
  margin: 0 0 14px;
}
.brand-canvas h1 {
  font-size: 34px;
  font-weight: 800;
  letter-spacing: 6px;
  margin: 0 0 12px;
  color: #f0ede2;
}
.brand-canvas .slogan {
  font-size: 13px;
  letter-spacing: 3px;
  color: #9db3a8;
  margin: 0;
}
/* 装饰热力格：米黄透明度阶梯，呼应主页"每日学习记录"色阶 */
.deco-grid {
  display: grid;
  grid-template-columns: repeat(10, 15px);
  gap: 5px;
  margin-top: 36px;
}
.deco-grid i {
  width: 15px;
  height: 15px;
  border-radius: 4px;
}
.d0 {
  background: rgba(240, 237, 226, 0.1);
}
.d1 {
  background: rgba(240, 237, 226, 0.25);
}
.d2 {
  background: rgba(240, 237, 226, 0.4);
}
.d3 {
  background: rgba(245, 217, 168, 0.55);
}
.d4 {
  background: #f5d9a8; /* 主页高亮米黄 */
}
/* 图片占位区：虚线框提示后续可放图 */
.pic-slot {
  margin-top: 36px;
  padding: 30px 44px;
  border: 1px dashed rgba(240, 237, 226, 0.35);
  border-radius: 12px;
  font-size: 12px;
  letter-spacing: 2px;
  color: rgba(240, 237, 226, 0.55);
}
/* ============ 右侧：登录卡片（主页卡片同款材质） ============ */
.page-main-login {
  width: 40%;
  margin: 20px 20px 20px 10px;
  height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fdfcf8;
  border: 1px solid #e3ded1;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(34, 48, 42, 0.08);
  padding: 40px 48px;
}
/* 标题区：主页 page-head 同款"小字 crumb + 大标题"排版 */
.login-head {
  text-align: center;
  margin-bottom: 38px;
}
.login-head .crumb {
  font-size: 10px;
  letter-spacing: 4px;
  color: #9aa093;
  margin: 0 0 12px;
}
.login-head h2 {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 4px;
  color: #22302a;
  margin: 0 0 12px;
}
.login-head .sub {
  font-size: 12px;
  letter-spacing: 2px;
  color: #9aa093;
  margin: 0;
}
/* ============ 表单 ============ */
.form-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  width: 300px;
  max-width: 100%;
}
.input-border {
  width: 300px;
  max-width: 100%;
}
/* 输入框：白底 + 米色边框，聚焦时主题绿（Element Plus 2.x 用 wrapper 的 inset box-shadow 画边框） */
:deep(.input-border .el-input__wrapper) {
  border-radius: 10px;
  background: #ffffff;
  box-shadow: 0 0 0 1px #e3ded1 inset;
  padding: 2px 14px;
  transition: box-shadow 0.2s ease;
}
:deep(.input-border .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c9d3c6 inset;
}
:deep(.input-border .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px #2d5a4a inset;
}
:deep(.input-border .el-input__inner) {
  height: 44px;
  line-height: 44px;
  text-align: center;
  color: #22302a;
  font-size: 14px;
}
/* placeholder：常规灰绿小字（替换原楷体加粗样式） */
:deep(.input-border .el-input__inner::placeholder) {
  font-size: 13px;
  font-weight: 400;
  font-family: inherit;
  color: #9aa093;
  letter-spacing: 1px;
}
/* 密码可见性图标 */
:deep(.input-border .el-input__password) {
  color: #9aa093;
}
/* ============ 登录按钮：主题绿实底（覆盖 Element 变量，替代 type="success" 的默认绿） ============ */
.login-button {
  margin-top: 30px;
  width: 300px;
  max-width: 100%;
}
.login-btn {
  width: 100%;
  height: 46px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 8px;
  text-indent: 8px; /* 抵消末字间距，让"登录"两字视觉居中 */
  --el-button-bg-color: #2d5a4a;
  --el-button-border-color: #2d5a4a;
  --el-button-text-color: #f0ede2;
  --el-button-hover-bg-color: #3a6b58;
  --el-button-hover-border-color: #3a6b58;
  --el-button-hover-text-color: #f0ede2;
  --el-button-active-bg-color: #24493c;
  --el-button-active-border-color: #24493c;
  --el-button-active-text-color: #f0ede2;
  --el-button-disabled-bg-color: #9db3a8;
  --el-button-disabled-border-color: #9db3a8;
  --el-button-disabled-text-color: #f0ede2;
}
/* ============ 失败提示卡：主页 heat-tip 同款墨绿材质 + 主题红描边 ============ */
.fail-box {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10000; /* 高于 ElLoading 遮罩（默认 2000+），关闭 loading 后浮于最上层 */
  min-width: 300px;
  max-width: 86vw;
  padding: 24px 38px;
  background: #22302a;
  border: 1px solid #b5482f; /* 主题红描边，标识错误 */
  border-radius: 14px;
  box-shadow: 0 12px 32px rgba(34, 48, 42, 0.35);
  text-align: center;
  cursor: pointer; /* 提示用户点击可关闭 */
  user-select: none;
}
.fb-title {
  display: block;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 3px;
  color: #f2a491; /* 主题红 #b5482f 的深底提亮版 */
}
.fb-msg {
  margin: 10px 0 12px;
  font-size: 15px;
  letter-spacing: 1px;
  color: #f0ede2;
}
.fb-hint {
  font-size: 10px;
  letter-spacing: 2px;
  color: rgba(240, 237, 226, 0.45);
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
/* ============ 小屏适配：上下堆叠 ============ */
@media (max-width: 860px) {
  .page {
    flex-direction: column;
    overflow: auto;
  }
  .page-main-static {
    width: auto;
    margin: 20px 20px 0;
  }
  .brand-canvas {
    height: 220px;
    padding: 24px;
  }
  .brand-canvas h1 {
    font-size: 24px;
    letter-spacing: 4px;
  }
  .deco-grid,
  .pic-slot {
    display: none; /* 小屏收纳装饰元素 */
  }
  .page-main-login {
    width: auto;
    margin: 20px;
    height: auto;
    min-height: 460px;
  }
}
</style>
