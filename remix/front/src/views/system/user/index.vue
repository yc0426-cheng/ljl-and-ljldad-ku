<template>
  <!--
    用户管理页
    位置说明：本页是常驻布局 src/layout/index.vue 的子路由（左侧树点击"用户列表"跳 /users-list），
    只渲染右侧主区内容：页头 + 搜索工具栏 + 用户表格 + 新建/编辑弹窗
    （左侧模块树 / 底部用户区在布局中全站常驻，样式来自 src/style/common.css）
  -->
  <div v-loading="loading" class="user-page">
    <!-- 页头：与主页 page-head 同款版式 -->
    <header class="page-head">
      <div>
        <p class="crumb">SYSTEM / USER LIST</p>
        <h1>用户管理</h1>
      </div>
      <span class="head-tip">共 {{ tableData.length }} 个账号</span>
    </header>

    <!-- 工具栏 + 表格：面板卡片（与主页 .panel 同款视觉） -->
    <section class="panel">
      <div class="toolbar">
        <el-input v-model="queryParam.account" placeholder="按账号搜索" clearable />
        <el-input v-model="queryParam.name" placeholder="按姓名搜索" clearable />
        <div class="toolbar-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="primary" plain @click="handleAdd">新建</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe class="user-table">
        <el-table-column type="index" label="#" width="56" />
        <el-table-column prop="account" label="账号" sortable min-width="110" />
        <el-table-column prop="name" label="姓名" sortable min-width="100" />
        <el-table-column
          prop="phone"
          label="电话号码"
          sortable
          min-width="130"
          show-overflow-tooltip
        />
        <el-table-column
          prop="idNumber"
          label="身份证号"
          sortable
          min-width="170"
          show-overflow-tooltip
        />
        <el-table-column prop="email" label="邮箱" sortable min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" sortable min-width="100">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row.status).type" size="small" effect="plain">
              <span class="status-cell">
                <el-icon><component :is="statusMeta(row.status).icon" /></el-icon>
                {{ statusMeta(row.status).label }}
              </span>
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录时间" sortable min-width="160" />
        <el-table-column label="操作" align="center" fixed="right" width="130">
          <template #default="row">
            <el-button link type="warning" @click="handleEdit(row)">修改</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <UserForm
      v-model:visible="visible"
      :data="selectedData"
      :model="isEdit ? 'edit' : 'add'"
      @submit="handleForm"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { Component } from 'vue'
import { CircleCheckFilled, CircleCloseFilled, Lock, Moon } from '@element-plus/icons-vue'
import { SysUser, SysUserDTO, SysUserParam, SysUserVO } from '@/types/system/user'
import { SysUserApi } from '@/api/system/user/user'
import UserForm from '@/views/system/user/UserForm.vue'

// ---------------- 状态列展示映射（纯展示，不参与数据流） ----------------
// 对应后端 SysUserStatusEnum：ENABLE(1,启用) / FROZEN(0,冻结) / DORMANT(-1,休眠)；
// DISABLE(禁用) 为前端预留，后端枚举补充后自动生效。
// 样式对齐菜单管理页"类型"列：el-tag plain 小标签，type 决定配色
type StatusTagType = 'success' | 'warning' | 'info' | 'danger'

const STATUS_META: Record<string, { icon: Component; label: string; type: StatusTagType }> = {
  ENABLE: { icon: CircleCheckFilled, label: '启用', type: 'success' },
  FROZEN: { icon: Lock, label: '冻结', type: 'warning' },
  DORMANT: { icon: Moon, label: '休眠', type: 'info' },
  DISABLE: { icon: CircleCloseFilled, label: '禁用', type: 'danger' }
}

/** 未知状态兜底：灰色"未知" */
const UNKNOWN_META = { icon: CircleCloseFilled, label: '未知', type: 'info' as const }

function statusMeta(status?: string): { icon: Component; label: string; type: StatusTagType } {
  return (status && STATUS_META[status]) || UNKNOWN_META
}

// ---------------- 用户列表 ----------------
/** 加载状态 */
const loading = ref(false)
/** 表数据 */
const tableData = ref<SysUser[]>([])
/** 弹窗可见性 */
const visible = ref(false)
/** 是否为编辑页面 */
const isEdit = ref(false)
/** 选中行数据 */
const selectedData = ref<SysUserVO>()
/** 查询条件 */
const queryParam = reactive<SysUserParam>({
  account: '',
  name: ''
})

/** 加载数据 */
const loadData = async () => {
  loading.value = true

  tableData.value = await SysUserApi.page().finally(() => {
    loading.value = false
  })
}

/**
 * 提交刷新表格
 */
const handleForm = () => {
  loadData()
}

/**
 * 查询对应用户
 */
const handleSearch = async () => {
  loading.value = true

  tableData.value = await SysUserApi.list(queryParam).finally(() => {
    loading.value = false
  })
}

/**
 * 新建用户
 */
const handleAdd = () => {
  isEdit.value = false
  visible.value = true
  selectedData.value = undefined
}

/**
 * 修改用户信息
 */
const handleEdit = (row: SysUserVO) => {
  isEdit.value = true
  visible.value = true
  selectedData.value = row
}

/**
 * 删除用户
 */
const handleDelete = (row: SysUserVO) => {
  loading.value = true
  // 实体转dto
  const dto = <SysUserDTO>{
    ...row
  }

  SysUserApi.delete(dto).finally(() => {
    loading.value = false
  })
}

// 初始化页面
onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* ============ 页头（与主页 page-head 同款版式） ============ */
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
}

.crumb {
  font-size: 10px;
  letter-spacing: 3px;
  color: #9aa093;
  margin: 0 0 8px;
}

.page-head h1 {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 4px;
  color: #22302a;
  margin: 0;
}

.head-tip {
  font-size: 12px;
  color: #9aa093;
}

/* ============ 面板卡片（与主页 .panel 同款视觉） ============ */
.panel {
  background: #fdfcf8;
  border: 1px solid #e3ded1;
  border-radius: 14px;
  padding: 20px 24px;
}

/* ---- 搜索工具栏 ---- */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.toolbar .el-input {
  width: 200px;
}

.toolbar-actions {
  margin-left: auto;
  display: flex;
}

/* ---- 表格 ---- */
.user-table {
  width: 100%;
}

/* 状态列：标签内图标 + 文字 */
.status-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 表头：贴合纸感主题的浅底色 */
.user-table :deep(.el-table th.el-table__cell) {
  background: #f4f1e7;
  color: #22302a;
  font-weight: 600;
}

/* 去掉默认外边框的生硬感，圆角与面板一致 */
.user-table :deep(.el-table--border) {
  border-radius: 10px;
  overflow: hidden;
}

.user-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}
</style>
