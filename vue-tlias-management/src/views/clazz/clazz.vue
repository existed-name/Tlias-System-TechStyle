<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getClazzList, deleteClazz, addClazz, getClazzById, updateClazz } from '@/api/clazz'
import {queryAllApi} from "../../api/emp";

// ─── 查询参数 ───────────────────────────────────────────
const queryParams = reactive({ name: '', begin: '', end: '', page: 1, pageSize: 10 })
const total = ref(0)
const tableData = ref([])
const loading = ref(false)

// ─── 枚举映射 (建议兼容数字和多种可能字符串) ────────────────
const subjectMap = { 1: 'Java', 2: '前端', 3: '大数据', 4: 'Python', 5: 'Go', 6: '嵌入式' }

// 新增: 状态 → CSS 类名后缀
const statusClassMap = {
  1: 'pending', '未开班': 'pending', '未开课': 'pending', '未开': 'pending',
  2: 'active',  '已开班': 'active',  '已开课': 'active',  '在读中': 'active', '进行中': 'active',
  3: 'done',    '已结课': 'done',    '结课': 'done'
}
// 新增: 状态 → emoji
const statusEmojiMap = {
  1: '⏳', '未开班': '⏳', '未开课': '⏳', '未开' : '⏳',
  2: '🚀', '已开班': '🚀',  '已开课': '🚀',  '在读中': '🚀', '进行中': '🚀',
  3: '🏁', '已结课': '🏁', '结课': '🏁'
}

// 班主任下拉列表（只取 job===1 的班主任员工）
const empList = ref([])

const loadEmpList = async () => {
  const res = await queryAllApi()
  /*
  接口文档说 job=1 为班主任，其他职位无需出现在下拉里。
如果业务上允许任意员工担任班主任，去掉 .filter(e => e.job === 1) 即可。
   */
  if (res.code === 1) {
    // 只展示职位为班主任(job=1)的员工，减少无关选项
    empList.value = res.data;
    // .filter(e => e.job === 1)
  }
}

/** * 新增：状态格式化工具函数
 * 如果找不到映射，则返回默认样式，防止界面“裸奔”
 */
const getStatusLabel = (row) => {
  const status = row.status
  // 获取 Emoji，如果没有匹配则不显示
  const emoji = statusEmojiMap[status] || '❓'
  return `${emoji} ${status || '未知'}`
}

const getStatusClass = (row) => {
  const status = row.status
  // 如果找不到对应的 CSS 类，返回一个默认类名 status-unknown
  return `status-${statusClassMap[status] || 'unknown'}`
}

// 新增: 行选中记录（用于高亮行）
const currentRow = ref(null)

// 新增: 行 class 控制（选中行高亮）
const rowClassName = ({ row }) => currentRow.value?.id === row.id ? 'row-selected' : ''

// ─── 查询列表 ───────────────────────────────────────────
const fetchList = async () => {
  loading.value = true
  const res = await getClazzList(queryParams)
  if (res.code === 1) {
    tableData.value = res.data.rows
    total.value = res.data.total
  }
  loading.value = false
}

// 点击"查询"按钮：重置到第1页再查
const handleSearch = () => {
  queryParams.page = 1
  fetchList()
}

// 分页切换
const handlePageChange = (page) => {
  queryParams.page = page
  fetchList()
}

// ─── Dialog 控制 ────────────────────────────────────────
const dialogVisible = ref(false)
const isEdit = ref(false) // true=编辑, false=新增
const formRef = ref(null)

// 表单数据（新增/编辑共用）
const form = reactive({
  id: null, name: '', room: '', beginDate: '', endDate: '', masterId: null, subject: null
})

// 表单校验规则
const rules = {
  name:      [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  room:      [{ required: true, message: '请输入班级教室', trigger: 'blur' }],
  beginDate: [{ required: true, message: '请选择开课时间', trigger: 'change' }],
  endDate:   [{ required: true, message: '请选择结课时间', trigger: 'change' }],
  subject:   [{ required: true, message: '请选择学科',     trigger: 'change' }],
}

// 重置表单
const resetForm = () => {
  Object.assign(form, { id: null, name: '', room: '', beginDate: '', endDate: '', masterId: null, subject: null })
  formRef.value?.resetFields()
}

// 打开新增 Dialog
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 打开编辑 Dialog：先按ID查询回显
const handleEdit = async (row) => {
  isEdit.value = true
  resetForm()
  const res = await getClazzById(row.id)
  if (res.code === 1) {
    Object.assign(form, res.data)
    dialogVisible.value = true
  }
}

// 提交表单（新增 or 修改）
const handleSubmit = async () => {
  await formRef.value.validate()
  const api = isEdit.value ? updateClazz : addClazz
  const res = await api(form)
  if (res.code === 1) {
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    fetchList()
  }
}

// ─── 删除 ───────────────────────────────────────────────
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除班级「${row.name}」？`, '警告', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const res = await deleteClazz(row.id)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      // 删最后一页唯一一条时退回上一页
      if (tableData.value.length === 1 && queryParams.page > 1) queryParams.page--
      fetchList()
    }
  }).catch(() => {})
}

// ─── 工具函数 ───────────────────────────────────────────
// 格式化时间：2023-06-01T17:51:21 → 2023-06-01 17:51:21
const formatTime = (val) => val?.replace('T', ' ') ?? '-'

onMounted(() => {
  fetchList()
  loadEmpList()   // 预加载班主任列表
})

</script>



<template>
  <div class="clazz-wrap">

    <!-- ── 页头 ── -->
    <div class="page-header">
      <span class="page-title">🖥️ 班级管理</span>
      <span class="page-sub">CLASS MANAGEMENT SYSTEM</span>
    </div>

    <!-- ── 搜索栏 ── -->
    <div class="search-panel">
      <div class="search-row">
        <!-- 班级名称 -->
        <div class="search-field">
          <span class="field-label">🔍 班级名称</span>
          <el-input
              v-model="queryParams.name"
              placeholder="请输入班级名称"
              clearable
              class="tech-input"
          />
        </div>

        <!-- 结课时间范围 -->
        <div class="search-field">
          <span class="field-label">📅 结课时间</span>
          <div class="date-range">
            <el-date-picker
                v-model="queryParams.begin"
                type="date" value-format="YYYY-MM-DD"
                placeholder="开始日期" class="tech-date"
            />
            <span class="date-sep">—</span>
            <el-date-picker
                v-model="queryParams.end"
                type="date" value-format="YYYY-MM-DD"
                placeholder="结束日期" class="tech-date"
            />
          </div>
        </div>

        <!-- 按钮组 -->
        <div class="search-actions">
          <el-button class="btn-query" @click="handleSearch">⚡ 查询</el-button>
        </div>
      </div>
    </div>

    <!-- ── 操作栏 ── -->
    <div class="action-bar">
      <el-button class="btn-add" @click="handleAdd">＋ 新增班级</el-button>
      <span class="total-badge">共 {{ total }} 条记录</span>
    </div>

    <!-- ── 数据表格 ── -->
    <div class="table-wrap">
      <el-table
          :data="tableData"
          v-loading="loading"
          class="tech-table"
          :highlight-current-row="true"
          @row-click="(row) => currentRow = row"
          :row-class-name="rowClassName"
      >
        <!-- 序号 -->
        <el-table-column
            type="index" label="#" width="56"
            :index="(i) => (queryParams.page - 1) * queryParams.pageSize + i + 1"
        >
          <template #default="{ $index }">
            <span class="col-index">{{ (queryParams.page - 1) * queryParams.pageSize + $index + 1 }}</span>
          </template>
        </el-table-column>

        <!-- 班级名称 -->
        <el-table-column label="🏫 班级名称" min-width="190" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="col-name">{{ row.name }}</span>
          </template>
        </el-table-column>

        <!-- 教室 -->
        <el-table-column label="🚪 教室" width="90">
          <template #default="{ row }">
            <span class="col-room">{{ row.room }}</span>
          </template>
        </el-table-column>

        <!-- 班主任 -->
        <el-table-column label="👤 班主任" width="110">
          <template #default="{ row }">
            <span class="col-master">{{ row.masterName ?? '-' }}</span>
          </template>
        </el-table-column>

        <!-- 开课时间 -->
        <el-table-column label="🟢 开课" width="120">
          <template #default="{ row }">
            <span class="col-begin">{{ row.beginDate }}</span>
          </template>
        </el-table-column>

        <!-- 结课时间 -->
        <el-table-column label="🔴 结课" width="120">
          <template #default="{ row }">
            <span class="col-end">{{ row.endDate }}</span>
          </template>
        </el-table-column>

        <!-- 状态 -->
        <el-table-column label="📡 状态" width="100">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass(row)]">
              {{ getStatusLabel(row) }}
            </span>
          </template>
        </el-table-column>


        <!-- 最后操作时间 -->
        <el-table-column label="🕐 最后操作" width="170">
          <template #default="{ row }">
            <span class="col-time">{{ formatTime(row.updateTime) }}</span>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="⚙️ 操作" width="130" fixed="right">
          <template #default="{ row }">
            <button class="op-btn op-edit" @click.stop="handleEdit(row)">✏️ 编辑</button>
            <button class="op-btn op-del"  @click.stop="handleDelete(row)">🗑️ 删除</button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- ── 分页 ── -->
    <div class="pagination-bar">
      <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handleSearch"
          class="tech-pagination"
      />
    </div>

    <!-- ── 新增/编辑 Dialog ── -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '✏️ 编辑班级' : '➕ 新增班级'"
        width="520px"
        class="tech-dialog"
        @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="tech-form">
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入班级名称" class="tech-input" />
        </el-form-item>
        <el-form-item label="班级教室" prop="room">
          <el-input v-model="form.room" placeholder="请输入教室编号" class="tech-input" />
        </el-form-item>
        <el-form-item label="开课时间" prop="beginDate">
          <el-date-picker v-model="form.beginDate" type="date" value-format="YYYY-MM-DD"
                          placeholder="请选择" style="width:100%" class="tech-date" />
        </el-form-item>
        <el-form-item label="结课时间" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD"
                          placeholder="请选择" style="width:100%" class="tech-date" />
        </el-form-item>
        <el-form-item label="学科" prop="subject">
          <el-select v-model="form.subject" placeholder="请选择学科" style="width:100%" class="tech-input">
            <el-option v-for="(label, val) in subjectMap" :key="val" :label="label" :value="Number(val)" />
          </el-select>
        </el-form-item>

        <!-- 班主任：下拉选择，仅展示 job=1 的班主任员工 -->
        <el-form-item label="班主任">
          <el-select
              v-model="form.masterId"
              placeholder="请选择班主任"
              style="width:100%"
              class="tech-input"
              filterable
          >
            <el-option
                v-for="emp in empList"
                :key="emp.id"
                :label="emp.name"
                :value="emp.id"
            />
          </el-select>
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button class="btn-cancel" @click="dialogVisible = false">取消</el-button>
        <el-button class="btn-confirm" @click="handleSubmit">✅ 确认</el-button>
      </template>
    </el-dialog>

  </div>
</template>



<style scoped>
/* ── 全局字体 ── */
.clazz-wrap {
  padding: 20px;
  font-family: 'JetBrains Mono', 'Courier New', monospace;
  background: #0a0e1a;
  min-height: 100vh;
  color: #c8d8f0;
}

/* ── 页头 ── */
.page-header {
  display: flex;
  align-items: baseline;
  gap: 14px;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid #1e3a5f;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #4fc3f7;
  letter-spacing: 2px;
  text-shadow: 0 0 12px rgba(79, 195, 247, 0.5);
}
.page-sub {
  font-size: 11px;
  color: #37607a;
  letter-spacing: 4px;
}

/* ── 搜索面板 ── */
.search-panel {
  background: #0d1526;
  border: 1px solid #1a3356;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 14px;
  position: relative;
  /* 左侧光条 */
  box-shadow: inset 3px 0 0 #1565c0;
}
.search-row {
  display: flex;
  align-items: flex-end;
  gap: 20px;
  flex-wrap: wrap;
}
.search-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.field-label {
  font-size: 11px;
  color: #4a7fa5;
  letter-spacing: 1px;
  text-transform: uppercase;
}
.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}
.date-sep { color: #2e5f7a; font-size: 14px; }
.search-actions { margin-left: auto; }

/* ── 按钮 ── */
.btn-query {
  background: transparent;
  border: 1px solid #0d47a1;
  color: #4fc3f7;
  border-radius: 6px;
  padding: 8px 22px;
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  letter-spacing: 1px;
  transition: all 0.2s;
  box-shadow: 0 0 8px rgba(21, 101, 192, 0.3);
}
.btn-query:hover {
  background: rgba(21, 101, 192, 0.25);
  box-shadow: 0 0 16px rgba(79, 195, 247, 0.4);
  color: #81d4fa;
}
.btn-add {
  background: transparent;
  border: 1px solid #00796b;
  color: #4db6ac;
  border-radius: 6px;
  padding: 8px 20px;
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  letter-spacing: 1px;
  transition: all 0.2s;
  box-shadow: 0 0 8px rgba(0, 121, 107, 0.3);
}
.btn-add:hover {
  background: rgba(0, 121, 107, 0.2);
  box-shadow: 0 0 16px rgba(77, 182, 172, 0.45);
  color: #80cbc4;
}

/* ── 操作栏 ── */
.action-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 10px;
}
.total-badge {
  font-size: 12px;
  color: #37607a;
  letter-spacing: 1px;
}

/* ── 表格容器 ── */
.table-wrap {
  border: 1px solid #1a3356;
  border-radius: 8px;
  overflow: hidden;
}

/* ── Element Plus 表格深色覆写 ── */
/* 表头 */
.tech-table :deep(.el-table__header-wrapper th) {
  background: #071020 !important;
  color: #4fc3f7;
  font-size: 12px;
  letter-spacing: 1px;
  border-bottom: 1px solid #1565c0 !important;
  border-right: 1px solid #0d1e3a !important;
}
/* 行 */
.tech-table :deep(.el-table__row td) {
  background: #0a1628 !important;
  border-bottom: 1px solid #0f2040 !important;
  border-right: 1px solid #0d1e3a !important;
  transition: background 0.15s;
}
.tech-table :deep(.el-table__row:hover td) {
  background: #0e1e38 !important;
}
/* 选中行 */
.tech-table :deep(.row-selected td) {
  background: #0d2847 !important;
  box-shadow: inset 0 0 0 1px rgba(79, 195, 247, 0.25);
}
/* 无数据 */
.tech-table :deep(.el-table__empty-text) { color: #2e5f7a; }
/* 去掉斑马纹（以防父级组件有默认） */
.tech-table :deep(.el-table__row--striped td) {
  background: #0a1628 !important;
}

/* ── 列样式 ── */
.col-index {
  display: inline-block;
  width: 26px;
  height: 26px;
  line-height: 26px;
  text-align: center;
  border-radius: 50%;
  background: #0d1e3a;
  border: 1px solid #1565c0;
  color: #4fc3f7;
  font-size: 11px;
}
.col-name  { color: #e3f2fd; font-weight: 600; }
.col-room  {
  display: inline-block;
  background: #0d1e3a;
  border: 1px solid #1a3a6a;
  color: #80d8ff;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 12px;
}
.col-master {
  display: inline-block;
  background: #1a1a2e;
  border: 1px solid #4a1a6a;
  color: #ce93d8;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 12px;
}
.col-begin { color: #69f0ae; font-size: 12px; }
.col-end   { color: #ff8a65; font-size: 12px; }
.col-time  { color: #607d8b; font-size: 11px; font-family: monospace; }

/* ── 状态标签 ── */
.status-tag {
  display: inline-block;
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 12px;
  letter-spacing: 0.5px;
  font-weight: 600;
}
.status-pending {
  background: rgba(33, 33, 70, 0.8);
  border: 1px solid #3949ab;
  color: #7986cb;
}
.status-active {
  background: rgba(0, 50, 30, 0.8);
  border: 1px solid #00897b;
  color: #4db6ac;
  box-shadow: 0 0 8px rgba(0, 137, 123, 0.3);
}
.status-done {
  background: rgba(50, 30, 0, 0.8);
  border: 1px solid #f57c00;
  color: #ffb74d;
}

/* 兜底：当状态匹配失败时的样式 */
.status-unknown {
  background: rgba(128, 128, 128, 0.2);
  border: 1px solid #546e7a;
  color: #90a4ae;
}

/* 确保标签有最小宽度，避免文字长短不一导致晃动 */
.status-tag {
  display: inline-block;
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 12px;
  letter-spacing: 0.5px;
  font-weight: 600;
  min-width: 70px; /* 增加最小宽度 */
  text-align: center;
}

/* ── 操作按钮 ── */
.op-btn {
  font-size: 12px;
  font-family: inherit;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 3px 7px;
  border-radius: 4px;
  transition: all 0.15s;
}
.op-edit { color: #4fc3f7; }
.op-edit:hover { background: rgba(21, 101, 192, 0.25); }
.op-del  { color: #ef9a9a; }
.op-del:hover  { background: rgba(183, 28, 28, 0.2); }

/* ── 分页 ── */
.pagination-bar {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
/* Element Plus 分页深色 */
.tech-pagination :deep(.el-pagination__total),
.tech-pagination :deep(.el-pagination__jump) {
  color: #37607a;
}
.tech-pagination :deep(.el-pager li) {
  background: #0d1526;
  color: #4a7fa5;
  border: 1px solid #1a3356;
}
.tech-pagination :deep(.el-pager li.is-active) {
  background: #1565c0;
  color: #e3f2fd;
  border-color: #1976d2;
}
.tech-pagination :deep(.btn-prev),
.tech-pagination :deep(.btn-next) {
  background: #0d1526;
  color: #4a7fa5;
  border: 1px solid #1a3356;
}

/* ── Input / DatePicker 深色 ── */
/* 统一覆写 el-input */
.tech-input :deep(.el-input__wrapper),
.tech-date.el-date-editor :deep(.el-input__wrapper) {
  background: #071020 !important;
  border: 1px solid #1a3356 !important;
  box-shadow: none !important;
}
.tech-input :deep(.el-input__wrapper:hover),
.tech-date.el-date-editor :deep(.el-input__wrapper:hover) {
  border-color: #1976d2 !important;
}
.tech-input :deep(.el-input__inner),
.tech-date.el-date-editor :deep(.el-input__inner) {
  color: #c8d8f0 !important;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
}
.tech-input :deep(.el-input__inner::placeholder),
.tech-date.el-date-editor :deep(.el-input__inner::placeholder) {
  color: #2e5f7a !important;
}

/* ── Dialog 深色 ── */
.tech-dialog :deep(.el-dialog) {
  background: #0a1628 !important;
  border: 1px solid #1a3356;
  border-radius: 10px;
}
.tech-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #1a3356;
  padding-bottom: 14px;
}
.tech-dialog :deep(.el-dialog__title) {
  color: #4fc3f7;
  font-family: 'JetBrains Mono', monospace;
  font-size: 15px;
  letter-spacing: 1px;
}
.tech-dialog :deep(.el-dialog__body) {
  padding-top: 20px;
}
.tech-dialog :deep(.el-dialog__footer) {
  border-top: 1px solid #1a3356;
  padding-top: 14px;
}
.tech-form :deep(.el-form-item__label) { color: #4a7fa5; font-size: 13px; }

/* Dialog 底部按钮 */
.btn-cancel {
  background: transparent !important;
  border: 1px solid #37607a !important;
  color: #607d8b !important;
  border-radius: 6px;
  font-family: inherit;
}
.btn-confirm {
  background: transparent !important;
  border: 1px solid #00897b !important;
  color: #4db6ac !important;
  border-radius: 6px;
  font-family: inherit;
  box-shadow: 0 0 10px rgba(0, 137, 123, 0.25);
}
.btn-confirm:hover { box-shadow: 0 0 18px rgba(77, 182, 172, 0.4) !important; }



</style>