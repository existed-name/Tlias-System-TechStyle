<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  queryAllApi,
  addDeptApi,
  queryInfoApi,
  updateDeptApi,
  deleteDeptApi
} from '@/api/dept'

// ─── 列表数据 ───────────────────────────────────────────
const tableData = ref([])   // 部门列表
const loading = ref(false)  // 表格加载状态

// ─── 对话框 ─────────────────────────────────────────────
const dialogVisible = ref(false)  // 对话框显示
const dialogTitle = ref('')       // 对话框标题（新增 / 修改）
const isEdit = ref(false)         // 当前是否为编辑模式

// ─── 表单 ────────────────────────────────────────────────
const formRef = ref(null)
const form = ref({ id: null, name: '' })
const rules = {
  name: [{ required: true, message: '部门名称不能为空', trigger: 'blur' }]
}

// ─── 工具函数：格式化时间（去掉中间的 T）────────────────
const formatTime = (t) => t ? t.replace('T', ' ') : '-'

// ─── 查询所有部门 ────────────────────────────────────────
const loadList = async () => {
  loading.value = true
  const res = await queryAllApi()
  if (res.code === 1) tableData.value = res.data
  loading.value = false
}

// ─── 打开新增对话框 ──────────────────────────────────────
const openAdd = () => {
  isEdit.value = false
  dialogTitle.value = '✨ 新增部门'
  form.value = { id: null, name: '' }
  dialogVisible.value = true
}

// ─── 打开编辑对话框 ──────────────────────────────────────
const openEdit = async (row) => {
  const res = await queryInfoApi(row.id)
  if (res.code !== 1) return
  isEdit.value = true
  dialogTitle.value = '✏️ 修改部门'
  form.value = { id: res.data.id, name: res.data.name }
  dialogVisible.value = true
}

// ─── 提交表单（新增 / 修改）──────────────────────────────
const submitForm = async () => {
  await formRef.value.validate()
  const res = isEdit.value
      ? await updateDeptApi(form.value)
      : await addDeptApi({ name: form.value.name })
  if (res.code === 1) {
    ElMessage.success(isEdit.value ? '修改成功 🎉' : '新增成功 🎉')
    dialogVisible.value = false
    loadList()
  }
}

// ─── 删除部门 ────────────────────────────────────────────
const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除部门「${row.name}」？`, '⚠️ 删除确认', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  const res = await deleteDeptApi(row.id)
  if (res.code === 1) {
    ElMessage.success('删除成功')
    loadList()
  }
}

onMounted(loadList)
</script>

<template>
  <div class="dept-page">

    <!-- ── 顶栏 ── -->
    <div class="toolbar">
      <div class="title-area">
        <span class="icon-dept">🏢</span>
        <span class="page-title">部门管理</span>
        <span class="badge">DEPT</span>
      </div>
      <el-button class="btn-add" @click="openAdd">
        <span class="btn-icon">＋</span> 新增部门
      </el-button>
    </div>

    <!-- ── 数据表格 ── -->
    <div class="table-wrap">
      <el-table
          :data="tableData"
          v-loading="loading"
          class="cyber-table"
          row-class-name="cyber-row"
      >
        <!-- 序号 -->
        <el-table-column type="index" label="#" width="60" align="center">
          <template #default="{ $index }">
            <span class="idx-badge">{{ $index + 1 }}</span>
          </template>
        </el-table-column>

        <!-- 部门名称 -->
        <el-table-column prop="name" label="🏷️ 部门名称" align="center">
          <template #default="{ row }">
            <span class="cell-name">{{ row.name }}</span>
          </template>
        </el-table-column>

        <!-- 创建时间 -->
        <el-table-column prop="createTime" label="📅 创建时间" align="center">
          <template #default="{ row }">
            <span class="cell-time create">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>

        <!-- 修改时间 -->
        <el-table-column prop="updateTime" label="🔄 修改时间" align="center">
          <template #default="{ row }">
            <span class="cell-time update">{{ formatTime(row.updateTime) }}</span>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="⚡ 操作" width="200" align="center">
          <template #default="{ row }">
            <el-button class="btn-edit" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button class="btn-del"  size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- ── 新增 / 修改对话框 ── -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="420px"
        class="cyber-dialog"
        align-center
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button class="btn-confirm" @click="submitForm">确 认</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<style scoped>
/* ═══════════════════════════════════════════
   CSS 变量 / 调色板
═══════════════════════════════════════════ */
.dept-page {
  --bg-page:      #0a0e1a;
  --bg-card:      #0d1322;
  --bg-row:       #0f1628;
  --bg-hover:     #112240;
  --border-glow:  #1e3a5f;
  --accent-cyan:  #00d4ff;
  --accent-teal:  #00ffcc;
  --accent-blue:  #3d8ef8;
  --accent-red:   #ff4d6d;
  --text-primary: #cce6ff;
  --text-muted:   #4a7fa5;
  --radius:       8px;

  min-height: 100%;
  background: var(--bg-page);
  padding: 24px;
  font-family: 'Courier New', Courier, monospace;
  color: var(--text-primary);
}

/* ─── 顶栏 ───────────────────────────────── */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title-area { display: flex; align-items: center; gap: 10px; }
.icon-dept  { font-size: 22px; }
.page-title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 2px;
  color: var(--accent-cyan);
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.6);
}
.badge {
  font-size: 11px;
  padding: 2px 8px;
  border: 1px solid var(--accent-cyan);
  border-radius: 4px;
  color: var(--accent-cyan);
  letter-spacing: 2px;
  opacity: 0.7;
}

/* 新增按钮 */
.btn-add {
  background: linear-gradient(135deg, #003d80, #005fbf) !important;
  border: 1px solid var(--accent-blue) !important;
  color: #aed6ff !important;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.25s;
}
.btn-add:hover {
  box-shadow: 0 0 14px rgba(61, 142, 248, 0.55);
  transform: translateY(-1px);
}
.btn-icon { font-size: 16px; margin-right: 4px; }

/* ─── 表格容器 ───────────────────────────── */
.table-wrap {
  border: 1px solid var(--border-glow);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: 0 0 24px rgba(0, 212, 255, 0.07);
}

/* ─── el-table 覆盖 ──────────────────────── */
.dept-page :deep(.cyber-table) {
  background: var(--bg-card) !important;
  color: var(--text-primary);
  border: none;
}

/* 表头 */
.dept-page :deep(.el-table__header-wrapper th) {
  background: #071020 !important;
  color: var(--accent-cyan) !important;
  font-size: 13px;
  letter-spacing: 1px;
  border-bottom: 1px solid var(--border-glow) !important;
  text-shadow: 0 0 6px rgba(0, 212, 255, 0.4);
}

/* 行 */
.dept-page :deep(.cyber-row td) {
  background: var(--bg-row) !important;
  border-bottom: 1px solid #141d30 !important;
  color: var(--text-primary);
  transition: background 0.2s, box-shadow 0.2s;
}

/* hover 行 */
.dept-page :deep(.cyber-table .el-table__row:hover td) {
  background: var(--bg-hover) !important;
  box-shadow: inset 0 0 12px rgba(0, 212, 255, 0.08);
}

/* 选中行（可选） */
.dept-page :deep(.el-table__row.current-row td) {
  background: #0d2a4d !important;
}

/* loading 遮罩 */
.dept-page :deep(.el-loading-mask) {
  background: rgba(10, 14, 26, 0.75) !important;
}

/* ─── 单元格样式 ─────────────────────────── */
.idx-badge {
  display: inline-block;
  width: 24px; height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 50%;
  background: #0d2a4d;
  border: 1px solid var(--border-glow);
  font-size: 12px;
  color: var(--accent-blue);
}

.cell-name {
  display: inline-block;
  padding: 3px 14px;
  border-radius: 20px;
  background: rgba(0, 212, 255, 0.08);
  border: 1px solid rgba(0, 212, 255, 0.25);
  color: var(--accent-cyan);
  font-size: 13px;
  letter-spacing: 0.5px;
  transition: box-shadow 0.2s;
}
.cell-name:hover {
  box-shadow: 0 0 8px rgba(0, 212, 255, 0.3);
}

.cell-time {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 4px;
  letter-spacing: 0.5px;
}
.cell-time.create {
  background: rgba(0, 255, 204, 0.07);
  border: 1px solid rgba(0, 255, 204, 0.2);
  color: var(--accent-teal);
}
.cell-time.update {
  background: rgba(61, 142, 248, 0.08);
  border: 1px solid rgba(61, 142, 248, 0.25);
  color: var(--accent-blue);
}

/* ─── 操作按钮 ───────────────────────────── */
.btn-edit {
  background: rgba(0, 212, 255, 0.1) !important;
  border: 1px solid rgba(0, 212, 255, 0.35) !important;
  color: var(--accent-cyan) !important;
  transition: all 0.2s;
}
.btn-edit:hover {
  background: rgba(0, 212, 255, 0.2) !important;
  box-shadow: 0 0 8px rgba(0, 212, 255, 0.4);
}
.btn-del {
  background: rgba(255, 77, 109, 0.1) !important;
  border: 1px solid rgba(255, 77, 109, 0.35) !important;
  color: var(--accent-red) !important;
  transition: all 0.2s;
}
.btn-del:hover {
  background: rgba(255, 77, 109, 0.2) !important;
  box-shadow: 0 0 8px rgba(255, 77, 109, 0.4);
}

/* ─── 对话框 ─────────────────────────────── */
.dept-page :deep(.cyber-dialog .el-dialog) {
  background: #0d1322 !important;
  border: 1px solid var(--border-glow) !important;
  box-shadow: 0 0 30px rgba(0, 212, 255, 0.12) !important;
}
.dept-page :deep(.cyber-dialog .el-dialog__title) {
  color: var(--accent-cyan) !important;
  font-size: 15px;
  letter-spacing: 1px;
}
.dept-page :deep(.cyber-dialog .el-dialog__header) {
  border-bottom: 1px solid var(--border-glow);
  padding-bottom: 14px;
}
.dept-page :deep(.cyber-dialog .el-dialog__footer) {
  border-top: 1px solid var(--border-glow);
}
.dept-page :deep(.cyber-dialog .el-form-item__label) {
  color: var(--text-muted) !important;
}
.dept-page :deep(.cyber-dialog .el-input__wrapper) {
  background: #07101f !important;
  border: 1px solid var(--border-glow) !important;
  box-shadow: none !important;
}
.dept-page :deep(.cyber-dialog .el-input__inner) {
  color: var(--text-primary) !important;
}
.dept-page :deep(.cyber-dialog .el-input__wrapper:focus-within) {
  border-color: var(--accent-cyan) !important;
  box-shadow: 0 0 8px rgba(0, 212, 255, 0.25) !important;
}
.btn-confirm {
  background: linear-gradient(135deg, #004a8f, #006bd4) !important;
  border: 1px solid var(--accent-blue) !important;
  color: #aed6ff !important;
  font-weight: 600;
}
.btn-confirm:hover {
  box-shadow: 0 0 12px rgba(61, 142, 248, 0.5) !important;
}
</style>