<template>
  <!--
    系统管理 → 菜单管理
    位置：src/views/system/menu/index.vue，由路由 /system/menu 加载
    作用：维护 sys_menu（菜单/路由数据）——新增、编辑、删除"路由/菜单"。
         主页左侧菜单树据此渲染；叶子菜单的 component 能在 src/views 下找到组件时，
         进入主页会自动动态注册该路由，找不到则点击跳"建设中"占位页。
  -->
  <div class="page">
    <div class="card">
      <!-- 页头 -->
      <div class="head">
        <div>
          <p class="crumb">SYSTEM / MENU</p>
          <h1>菜单管理</h1>
          <span class="tip">新增的路由保存后，回到主页刷新即可在左侧菜单看到</span>
        </div>
        <div class="head-actions">
          <el-button @click="goHome">返回主页</el-button>
          <el-button type="primary" @click="openAdd(null)">新增顶级菜单</el-button>
        </div>
      </div>

      <!-- 菜单树表格 -->
      <el-table
        v-loading="loading"
        :data="tree"
        row-key="menuId"
        :tree-props="{ children: 'children' }"
        default-expand-all
        border
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.menuType === 1 ? 'warning' : 'success'" size="small" effect="plain">
              {{ row.menuType === 1 ? '目录' : '菜单' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="routePath" label="路由地址" min-width="140">
          <template #default="{ row }">{{ row.routePath || '—' }}</template>
        </el-table-column>
        <el-table-column prop="component" label="组件路径" min-width="170">
          <template #default="{ row }">{{ row.component || '—' }}</template>
        </el-table-column>
        <el-table-column prop="orderNo" label="排序" width="70" />
        <el-table-column label="显示" width="80">
          <template #default="{ row }">{{ row.visible === false ? '否' : '是' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openAdd(row)">新增子级</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.menuId ? '编辑菜单' : '新增菜单'"
      width="520px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级菜单">
          <el-select
            v-model="form.parentId"
            clearable
            placeholder="不选 = 顶级菜单"
            style="width: 100%"
          >
            <el-option label="（顶级）" :value="null" />
            <el-option
              v-for="opt in parentOptions"
              :key="opt.menuId"
              :label="opt.label"
              :value="opt.menuId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-radio-group v-model="form.menuType">
            <el-radio-button :value="1">目录（分组）</el-radio-button>
            <el-radio-button :value="2">菜单（叶子路由）</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model.trim="form.menuName" placeholder="如：报表中心" />
        </el-form-item>
        <el-form-item label="路由地址" prop="routePath">
          <el-input
            v-model.trim="form.routePath"
            :disabled="form.menuType === 1"
            placeholder="叶子菜单必填，如 /report/index"
          />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input
            v-model.trim="form.component"
            placeholder="views 相对路径，如 report/index（留空 = 占位页）"
          />
        </el-form-item>
        <el-form-item label="图标名">
          <el-input v-model.trim="form.icon" placeholder="可选，如 DataLine / Setting" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.orderNo" :min="0" />
        </el-form-item>
        <el-form-item label="是否显示">
          <el-switch v-model="form.visible" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
// ---------------- import 区 ----------------
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { listMenuTree, addMenu, editMenu, deleteMenu } from '@/api/system/menu'
import { registerMenuRoutes } from '@/router'
import type { SysMenu } from '@/types/system/menu'

// ---------------- 实例化 ----------------
const router = useRouter()

// ---------------- 数据 ----------------
const loading = ref(false)
const tree = ref<SysMenu[]>([])

/** 返回主页 */
function goHome(): void {
  router.push('/home/index')
}

/** 加载菜单树，并同步注册可解析组件的动态路由 */
async function loadTree(): Promise<void> {
  loading.value = true
  try {
    tree.value = await listMenuTree()
    registerMenuRoutes(tree.value) // 新增/编辑后再次注册（幂等）
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '菜单加载失败')
  } finally {
    loading.value = false
  }
}

// ---------------- 新增 / 编辑弹窗 ----------------
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)

interface MenuForm {
  menuId?: number
  parentId?: number | null
  menuName: string
  menuType: number
  routePath: string
  component: string
  icon: string
  orderNo: number
  visible: boolean
}

const form = reactive<MenuForm>({
  menuId: undefined,
  parentId: null,
  menuName: '',
  menuType: 2,
  routePath: '',
  component: '',
  icon: '',
  orderNo: 0,
  visible: true
})

const rules: FormRules = {
  menuName: [{ required: true, message: '请填写菜单名称', trigger: 'blur' }],
  routePath: [{ required: true, message: '请填写路由地址', trigger: 'blur' }]
}

/** 上级菜单下拉：全部节点拍平，并排除当前编辑节点自身（防自环） */
const parentOptions = computed(() => {
  const options: { menuId: number; label: string }[] = []
  const walk = (list: SysMenu[], depth: number): void => {
    for (const m of list) {
      if (m.menuId !== form.menuId) {
        options.push({ menuId: m.menuId!, label: `${'　'.repeat(depth)}${m.menuName}` })
      }
      if (m.children?.length) walk(m.children, depth + 1)
    }
  }
  walk(tree.value, 0)
  return options
})

function openAdd(parent: SysMenu | null): void {
  isEdit.value = false
  form.menuId = undefined
  form.parentId = parent?.menuId ?? null
  form.menuType = 2
  form.routePath = ''
  form.component = ''
  form.icon = ''
  form.orderNo = 0
  form.visible = true
  dialogVisible.value = true
}

function openEdit(row: SysMenu): void {
  isEdit.value = true
  form.menuId = row.menuId
  form.parentId = row.parentId ?? null
  form.menuName = row.menuName
  form.menuType = row.menuType
  form.routePath = row.routePath ?? ''
  form.component = row.component ?? ''
  form.icon = row.icon ?? ''
  form.orderNo = row.orderNo ?? 0
  form.visible = row.visible !== false
  dialogVisible.value = true
}

function resetForm(): void {
  formRef.value?.clearValidate()
}

async function onSubmit(): Promise<void> {
  // 前端校验（路由地址仅叶子必填）
  if (!form.menuName.trim()) {
    ElMessage.warning('请填写菜单名称')
    return
  }
  if (form.menuType === 2 && !form.routePath.trim()) {
    ElMessage.warning('菜单（叶子）必须填写路由地址')
    return
  }

  saving.value = true
  try {
    const payload: SysMenu = {
      menuId: form.menuId,
      parentId: form.parentId || null,
      menuName: form.menuName,
      menuType: form.menuType,
      routePath: form.menuType === 1 ? undefined : form.routePath.trim(),
      component: form.component.trim() || undefined,
      icon: form.icon.trim() || undefined,
      orderNo: form.orderNo,
      visible: form.visible,
      status: 1
    }
    if (isEdit.value) {
      await editMenu(payload)
      ElMessage.success('保存成功')
    } else {
      await addMenu(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadTree() // 刷新树 + 重新注册动态路由
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    saving.value = false
  }
}

// ---------------- 删除 ----------------
async function onDelete(row: SysMenu): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认删除「${row.menuName}」及其全部子菜单吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await deleteMenu(row.menuId!)
    ElMessage.success('删除成功')
    await loadTree()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '删除失败')
  }
}

// ---------------- 初始化 ----------------
onMounted(loadTree)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f2ea;
  padding: 28px 40px 48px;
}

.card {
  background: #fdfcf8;
  border: 1px solid #e3ded1;
  border-radius: 14px;
  padding: 22px 26px;
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.crumb {
  font-size: 10.5px;
  letter-spacing: 3px;
  color: #9aa093;
  margin: 0 0 8px;
}

.head h1 {
  font-size: 24px;
  font-weight: 800;
  margin: 0;
  letter-spacing: 3px;
  color: #22302a;
}

.tip {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #9aa093;
}

.head-actions {
  display: flex;
  gap: 10px;
}
</style>
