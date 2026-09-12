<!--
  修改用户信息弹窗
  位置：src/views/system/user/setting/UserSettingForm.vue
  用法：<UserSettingForm v-model="visible" />（由 UserSetting 卡片"修改用户信息"按钮打开）
  特性：
    1. 打开时从 userStore 预填当前值；仅账号只读不可改
    2. 字段去向：姓名/手机/邮箱 → sys_user 主表；昵称/个性签名 → sys_user_misc 杂项表
       （后端编辑接口待实现，提交链路已就绪）
    3. 校验通过才提交：POST /api/user/edit → 成功回写 store 并拉取后端最新用户信息
    4. 样式与项目纸感绿主题一致（卡片 #fdfcf8 / 边框 #e3ded1 / 主题绿 #2d5a4a）
-->
<template>
  <el-dialog
    v-model="visible"
    width="460px"
    align-center
    :close-on-click-modal="false"
    class="user-edit-dialog"
    @open="onOpen"
  >
    <!-- 标题：主页/登录页同款"小字 crumb + 标题"排版 -->
    <template #header>
      <div class="dlg-head">
        <p class="crumb">USER INFO / EDIT</p>
        <b>修改用户信息</b>
      </div>
    </template>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="edit-form">
      <!-- 账号：登录凭证，只读展示（灰底示意不可编辑） -->
      <el-form-item label="账号">
        <el-input v-model="form.account" class="input-border" disabled />
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input
          v-model.trim="form.name"
          class="input-border"
          placeholder="请输入姓名"
          maxlength="50"
        />
      </el-form-item>
      <el-form-item label="手机号码" prop="phone">
        <el-input
          v-model.trim="form.phone"
          class="input-border"
          placeholder="请输入手机号码"
          maxlength="11"
        />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model.trim="form.email"
          class="input-border"
          placeholder="请输入邮箱"
          maxlength="100"
        />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input
          v-model.trim="form.nickname"
          class="input-border"
          placeholder="请输入昵称（可空）"
          maxlength="50"
        />
      </el-form-item>
      <el-form-item label="个性签名" prop="signature">
        <el-input
          v-model.trim="form.signature"
          class="input-border"
          type="textarea"
          :rows="2"
          resize="none"
          placeholder="一句话介绍自己（可空）"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button class="cancel-btn" @click="visible = false">取消</el-button>
      <el-button class="save-btn" :loading="saving" @click="onSave">保存修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { nextTick, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { userApi } from '@/api/system/user'
import type { EditUserInfoForm } from '@/types/system/user'

/** 弹窗显隐：v-model 双向绑定（父组件 UserSetting 控制） */
const visible = defineModel<boolean>({ default: false })

const userStore = useUserStore()

/** 表单实例（校验用） */
const formRef = ref<FormInstance>()
/** 保存中标志：防止重复提交 */
const saving = ref(false)

/** 表单数据（account 仅展示，不随提交上传） */
const form = reactive({
  account: '',
  name: '',
  phone: '',
  email: '',
  nickname: '',
  signature: ''
})

/**
 * 校验规则：与后端表约束对齐
 * 姓名/手机/邮箱 sys_user 均为 NOT NULL → 必填；昵称/签名来自杂项表 → 可空
 */
const rules: FormRules = {
  name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  phone: [
    { required: true, message: '手机号码不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号码格式不正确', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '邮箱不能为空', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

/**
 * 弹窗打开：从 store 预填当前值并清掉上次的校验红字
 * phone/email/nickname/signature 后端接口暂未返回，首次为空，保存成功后经 store 回填
 */
function onOpen(): void {
  const info = userStore.userInfo
  form.account = info?.account ?? ''
  form.name = info?.name ?? ''
  form.phone = info?.phone ?? ''
  form.email = info?.email ?? ''
  form.nickname = info?.nickname ?? ''
  form.signature = info?.signature ?? ''
  void nextTick(() => formRef.value?.clearValidate())
}

/**
 * 保存：校验 → 提交后端 → 本地回写 store（卡片/主页立即可见）→ 拉后端权威数据 → 关窗
 * 失败不关窗：错误提示由 request 拦截器全局弹出，用户可直接修改后重试
 */
async function onSave(): Promise<void> {
  if (saving.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload: EditUserInfoForm = {
      name: form.name,
      phone: form.phone,
      email: form.email,
      nickname: form.nickname || undefined,
      signature: form.signature || undefined
    }
    await userApi.editUserInfo(payload)
    // 先本地合并保证 UI 即时更新，再异步拉后端最新数据覆盖（后端暂未返回这些字段时也不影响）
    userStore.setUserInfo(payload)
    void userStore.fetchUserInfo().catch(() => undefined)
    ElMessage.success('用户信息已更新')
    visible.value = false
  } finally {
    saving.value = false
  }
}
</script>

<!-- 弹窗外壳挂载在 body 下，scoped 样式无法命中，需用全局样式（类名唯一，不会污染其它弹窗） -->
<style>
.user-edit-dialog {
  background: #fdfcf8;
  border: 1px solid #e3ded1;
  border-radius: 18px;
  box-shadow: 0 12px 36px rgba(34, 48, 42, 0.22);
  padding: 26px 30px 18px;
}

.user-edit-dialog .el-dialog__header {
  padding: 0 0 6px;
  margin-right: 0; /* 去掉默认标题右侧留白，自定义关闭按钮位置由组件自身控制 */
}

.user-edit-dialog .el-dialog__body {
  padding: 10px 0 0;
}

.user-edit-dialog .el-dialog__footer {
  padding: 18px 0 0;
}
</style>

<style scoped>
/* 标题区：登录页 login-head 同款排版 */
.dlg-head .crumb {
  font-size: 10px;
  letter-spacing: 4px;
  color: #9aa093;
  margin: 0 0 10px;
}

.dlg-head b {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 3px;
  color: #22302a;
}

/* 表单标签：灰绿小字 */
.edit-form :deep(.el-form-item__label) {
  font-size: 13px;
  letter-spacing: 1px;
  color: #5c665f;
  padding-bottom: 4px;
}

/* 输入框：白底 + 米色边框，聚焦主题绿（登录页 input-border 同款） */
:deep(.input-border .el-input__wrapper) {
  border-radius: 10px;
  background: #ffffff;
  box-shadow: 0 0 0 1px #e3ded1 inset;
  padding: 2px 14px;
  transition: box-shadow 0.2s ease;
}

:deep(.input-border .el-input__wrapper:hover:not(.is-disabled)) {
  box-shadow: 0 0 0 1px #c9d3c6 inset;
}

:deep(.input-border .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px #2d5a4a inset;
}

:deep(.input-border .el-input__inner) {
  height: 40px;
  color: #22302a;
  font-size: 14px;
}

/* 只读账号：灰底弱化，示意不可编辑 */
:deep(.input-border .el-input__wrapper.is-disabled) {
  background: #f0ede3;
  box-shadow: 0 0 0 1px #e3ded1 inset;
}

:deep(.input-border .el-input__inner::placeholder),
:deep(.input-border .el-textarea__inner::placeholder) {
  font-size: 13px;
  color: #9aa093;
  letter-spacing: 1px;
}

/* 多行文本框：与单行输入同款描边 */
:deep(.input-border .el-textarea__inner) {
  border-radius: 10px;
  background: #ffffff;
  box-shadow: 0 0 0 1px #e3ded1 inset;
  padding: 8px 14px;
  color: #22302a;
  font-size: 14px;
  transition: box-shadow 0.2s ease;
}

:deep(.input-border .el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #c9d3c6 inset;
}

:deep(.input-border .el-textarea__inner:focus) {
  box-shadow: 0 0 0 1.5px #2d5a4a inset;
}

/* 字数统计：跟随主题弱化 */
:deep(.input-border .el-input__count) {
  color: #9aa093;
  background: transparent;
}

/* 取消按钮：米色描边幽灵款 */
.cancel-btn {
  height: 40px;
  border-radius: 10px;
  padding: 0 22px;
  --el-button-bg-color: #fdfcf8;
  --el-button-border-color: #e3ded1;
  --el-button-text-color: #5c665f;
  --el-button-hover-bg-color: #f0ede3;
  --el-button-hover-border-color: #c9d3c6;
  --el-button-hover-text-color: #22302a;
}

/* 保存按钮：主题绿实底（登录页 login-btn 同款变量覆盖） */
.save-btn {
  height: 40px;
  border-radius: 10px;
  padding: 0 26px;
  font-weight: 600;
  letter-spacing: 2px;
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
</style>
