<!--
  用户设置卡片（透传浮层组件）
  位置：src/components/UserSettingCard.vue
  用法：<UserSettingCard v-if="visible" @close="visible = false" />
  特性：
    1. 透传浮层：无全屏遮罩/背景/虚化，仅一张卡片悬浮在当前页面（主页）之上
    2. 默认屏幕正中央；按住卡片空白处（按钮除外）可拖动，范围限制在视口内；Esc 关闭
    3. 头像：点击头像 → el-dialog + el-upload + el-button，调后端接口转存 OSS
    4. avatar 本地兜底：用户信息接口暂未返回 avatar 字段时，按 userId 从 localStorage 恢复
-->
<template>
  <Teleport to="body">
    <!-- 卡片本体：fixed + transform 定位，无遮罩，主页完全可见 -->
    <div
      ref="cardRef"
      class="setting-card"
      :class="{ ready: cardReady, dragging: isDragging }"
      :style="{ transform: `translate(${pos.x}px, ${pos.y}px)` }"
      @pointerdown="onDragStart"
    >
      <div v-if="showDragTip" class="drag-tip">
        <el-icon>
          <Rank />
        </el-icon>
      </div>
      <!-- 关闭按钮 -->
      <button class="close-btn" type="button" aria-label="关闭" @click="emit('close')">
        <el-icon>
          <Close />
        </el-icon>
      </button>

      <div class="head">
        <!-- 头像：有 avatar（OSS URL）显示图片，否则显示首字母兜底 -->
        <el-button circle @click="avatarDialogVisible = true">
          <img
            v-if="userStore.userInfo?.avatar"
            class="avatar-img"
            :src="userStore.userInfo.avatar"
            alt="头像"
          />
          <span v-else class="avatar">{{ displayName.slice(0, 1) }}</span>
        </el-button>
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
        <!-- 已在主页上，关闭浮层即“返回主页” -->
        <el-button type="primary" plain @click="openSettingForm">修改用户信息</el-button>
        <el-button type="danger" plain :loading="loggingOut" @click="onLogout">退出登录</el-button>
      </div>
    </div>

    <!-- 更改头像弹窗（Element Plus 自行 teleport，z-index 高于卡片） -->
    <el-dialog
      v-model="avatarDialogVisible"
      title="更改头像"
      width="420px"
      align-center
      destroy-on-close
      @closed="resetAvatarDialog"
    >
      <div class="avatar-preview">
        <img v-if="avatarPreview" :src="avatarPreview" alt="头像预览" />
        <span v-else class="avatar">{{ displayName.slice(0, 1) }}</span>
      </div>
      <!-- 选图：不自动上传，仅本地校验 + blob 预览，点“保存头像”才调后端 -->
      <el-upload
        ref="uploadRef"
        :show-file-list="false"
        :auto-upload="false"
        accept="image/png,image/jpeg,image/gif,image/webp"
        :on-change="onFileChange"
      >
        <el-button type="primary" plain>
          <el-icon class="mr4">
            <Plus />
          </el-icon>
          选择图片
        </el-button>
      </el-upload>
      <div class="upload-tip">支持 JPG / PNG / GIF / WebP，大小不超过 2MB</div>
      <template #footer>
        <el-button @click="avatarDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="!selectedFile"
          :loading="uploading"
          @click="onUploadAvatar"
        >
          保存头像
        </el-button>
      </template>
    </el-dialog>
  </Teleport>

  <UserSettingForm v-model="openSettingVisible" />
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile, UploadInstance } from 'element-plus'
import { Close, Plus, Rank } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { uploadApi } from '@/api/system/user'
import UserSettingForm from '@/views/system/user/setting/UserSettingForm.vue'

const emit = defineEmits<{ (e: 'close'): void }>()
const router = useRouter()
const userStore = useUserStore()

/** 头像上传弹窗状态 */
const avatarDialogVisible = ref(false)
/** 隐藏拖拽按钮 */
const showDragTip = ref(false)
/** 头像上传状态 */
const uploading = ref(false)
/** el-upload 实例 */
const uploadRef = ref<UploadInstance>()
/** 已选择的原始文件 */
const selectedFile = ref<File | null>(null)
/** 本地预览地址（blob:URL，仅用于弹窗内预览） */
const avatarPreview = ref('')
/** 表单页面的状态 */
const openSettingVisible = ref(false)
// 展示名：name 优先，缺失回退 account
const displayName = computed(
  () => userStore.userInfo?.name || userStore.userInfo?.account || '未知用户'
)
/** 本地兜底存取（后端用户信息接口返回 avatar 后，此逻辑自动失效、无副作用） */
const avatarKey = (userId: unknown) => `avatar:${userId}`

/** 图片最大内存 */
const MAX_SIZE = 2 * 1024 * 1024 // 2MB
/** 允许上传格式 */
const ALLOW_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']

function onFileChange(file: UploadFile): void {
  const raw = file.raw
  if (!raw) return
  if (!ALLOW_TYPES.includes(raw.type)) {
    ElMessage.error('仅支持 JPG / PNG / GIF / WebP 格式')
    uploadRef.value?.clearFiles()
    return
  }
  if (raw.size > MAX_SIZE) {
    ElMessage.error('图片大小不能超过 2MB')
    uploadRef.value?.clearFiles()
    return
  }
  selectedFile.value = raw
  if (avatarPreview.value) URL.revokeObjectURL(avatarPreview.value)
  avatarPreview.value = URL.createObjectURL(raw)
}

/**
 * 保存头像：FormData 提交后端 → 后端转存 OSS → 返回 URL → 回写 store（+本地兜底）
 */
async function onUploadAvatar(): Promise<void> {
  if (!selectedFile.value || uploading.value) return
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    const { url } = await uploadApi.uploadingAvatar(formData)
    userStore.userInfo = { ...userStore.userInfo!, avatar: url }
    // 本地兜底：接口暂未返回 avatar 字段时，刷新后仍能恢复（按 userId 区分账号）
    localStorage.setItem(avatarKey(userStore.userInfo!.userId), url)
    ElMessage.success('头像已更新')
    avatarDialogVisible.value = false // @closed → 回收 blob 预览地址
  } catch {
    ElMessage.error('头像上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

/** 弹窗关闭：清理预览与选中文件（回收 blob 地址） */
function resetAvatarDialog(): void {
  if (avatarPreview.value) URL.revokeObjectURL(avatarPreview.value)
  avatarPreview.value = ''
  selectedFile.value = null
  uploadRef.value?.clearFiles()
}

// ---------------- 卡片拖拽（默认屏幕正中 + 可拖动） ----------------
const cardRef = ref<HTMLElement | null>(null)
const isDragging = ref(false)
const cardReady = ref(false)
const hasDragged = ref(false)
const pos = reactive({ x: 0, y: 0 })

function centerCard(): void {
  const card = cardRef.value
  if (!card) return
  pos.x = Math.max(0, (window.innerWidth - card.offsetWidth) / 2)
  pos.y = Math.max(0, (window.innerHeight - card.offsetHeight) / 2)
}

function clamp(v: number, min: number, max: number): number {
  return Math.min(Math.max(v, min), max)
}

/**
 * 空白拖拽区白名单：
 *  - .drag-tip：顶部提示条（图标+文字均为提示装饰，整条可拖）
 *  - 卡片本体：target 恰好是 .setting-card，即四周内边距 + 各区块之间的间隙
 *  - .head / .actions 容器本身：头像/按钮之外的行内空白区域
 * 账号信息表格、姓名/账号文本、按钮等一律不触发拖动（文本可正常选中复制）。
 */
function isBlankArea(el: HTMLElement | null): boolean {
  const card = cardRef.value
  if (!el || !card) return false
  if (el.closest('.drag-tip')) return true
  if (el === card) return true
  return el.matches('.head, .actions')
}

function onDragStart(e: PointerEvent): void {
  if (e.button !== 0) return
  if ((e.target as HTMLElement).closest('button')) return // 兜底：按钮永不拖动
  if (!isBlankArea(e.target as HTMLElement)) return // ★ 仅空白处可拖

  e.preventDefault()

  isDragging.value = true
  if (e.button !== 0) return
  if ((e.target as HTMLElement).closest('button')) return
  e.preventDefault()
  isDragging.value = true
  const startX = e.clientX
  const startY = e.clientY
  const originX = pos.x
  const originY = pos.y
  const onMove = (ev: PointerEvent): void => {
    const card = cardRef.value
    const w = card?.offsetWidth ?? 0
    const h = card?.offsetHeight ?? 0
    pos.x = clamp(originX + (ev.clientX - startX), 0, Math.max(0, window.innerWidth - w))
    pos.y = clamp(originY + (ev.clientY - startY), 0, Math.max(0, window.innerHeight - h))
  }
  const onUp = (): void => {
    isDragging.value = false
    hasDragged.value = true
    window.removeEventListener('pointermove', onMove)
    window.removeEventListener('pointerup', onUp)
  }
  window.addEventListener('pointermove', onMove)
  window.addEventListener('pointerup', onUp)
}

function onWindowResize(): void {
  if (!hasDragged.value) centerCard()
}

/** Esc 关闭浮层（头像/编辑信息弹窗打开时交给弹窗自身处理，不重复关闭） */
function onKeydown(e: KeyboardEvent): void {
  if (e.key === 'Escape' && !avatarDialogVisible.value && !openSettingVisible.value) emit('close')
}

// ---------------- 页面动作 ----------------
const loggingOut = ref(false)

/** 退出登录：确认 → 调 store.logout（后端登出 + 清理本地）→ 提示 → 回登录页 */
async function onLogout(): Promise<void> {
  if (loggingOut.value) return
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
  emit('close') // 复位父组件显隐状态
  await router.replace('/login')
}

/**
 * 打开用户设置表单
 */
const openSettingForm = (): void => {
  openSettingVisible.value = true
}

onMounted(() => {
  // 本地兜底恢复 avatar（若 store 中无 avatar 而本地有记录）
  const info = userStore.userInfo
  if (info && !info.avatar) {
    const saved = localStorage.getItem(avatarKey(info.userId))
    if (saved) userStore.userInfo = { ...info, avatar: saved }
  }
  centerCard()
  cardReady.value = true
  window.addEventListener('resize', onWindowResize)
  window.addEventListener('keydown', onKeydown)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', onWindowResize)
  window.removeEventListener('keydown', onKeydown)
  if (avatarPreview.value) URL.revokeObjectURL(avatarPreview.value)
})
</script>

<style scoped>
/* 卡片：fixed 悬浮在主页上，无全屏背景、无遮罩、无虚化 */
.setting-card {
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1800;
  width: 640px;
  max-width: calc(100vw - 32px);
  background: #fdfcf8;
  border: 1px solid #e3ded1;
  border-radius: 18px;
  padding: 32px 36px;
  box-shadow: 0 12px 36px rgba(34, 48, 42, 0.22);
  cursor: grab;
  touch-action: none;
  opacity: 0;
  visibility: hidden;
  transition: box-shadow 0.2s ease;
}

.setting-card.ready {
  opacity: 1;
  visibility: visible;
}

.setting-card.dragging {
  cursor: grabbing;
  box-shadow: 0 20px 48px rgba(34, 48, 42, 0.32);
}

.drag-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #9aa093;
  margin-bottom: 14px;
}

.close-btn {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: 8px;
  color: #9aa093;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.close-btn:hover {
  background: #efeadd;
  color: #22302a;
}

.head {
  display: flex;
  flex-direction: row; /* 头像与 head-text 横向排列 */
  align-items: center; /* 纵向居中对齐 */
  justify-content: center; /* 整体在卡片内水平居中 */
  gap: 18px;
  margin-bottom: 26px;
}

.avatar-img {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
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

.head-text {
  display: flex;
  flex-direction: column; /* 两个元素纵向排列 */
  justify-content: center;
  gap: 6px; /* 姓名与账号的间距 */
  text-align: left; /* 组内文字左对齐，避免居中时参差 */
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

.avatar-preview {
  display: flex;
  justify-content: center;
  margin-bottom: 18px;
}

.avatar-preview img {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e3ded1;
}

.avatar-preview .avatar {
  width: 96px;
  height: 96px;
  font-size: 36px;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #9aa093;
}

.mr4 {
  margin-right: 4px;
}
</style>
