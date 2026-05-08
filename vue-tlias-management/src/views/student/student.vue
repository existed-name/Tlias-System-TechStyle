<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getStudentList, getStudentById,
  addStudent, updateStudent, deleteStudents
} from '@/api/student'
// 班级下拉数据复用班级模块接口 —— 按项目实际路径引入
import { getAllClazz } from '@/api/clazz'

// ========== 字典 ==========
const degreeOptions = [
  { label: '初中', value: 1 }, { label: '高中', value: 2 },
  { label: '大专', value: 3 }, { label: '本科', value: 4 },
  { label: '硕士', value: 5 }, { label: '博士', value: 6 },
]
const degreeMap = { 1: '初中', 2: '高中', 3: '大专', 4: '本科', 5: '硕士', 6: '博士' }

// ② 学历 Tag 样式类映射（对应下方 style 里的 CSS 类）
const degreeTagClass = {
  1: 'tag-degree-1',  // 初中 - 青
  2: 'tag-degree-2',  // 高中 - 绿
  3: 'tag-degree-3',  // 大专 - 蓝
  4: 'tag-degree-4',  // 本科 - 紫
  5: 'tag-degree-5',  // 硕士 - 橙
  6: 'tag-degree-6',  // 博士 - 红
}

// <!-- 学历颜色映射，index 对应 value 1~6 -->
const degreeTagTypes = {
  1: 'info',      // 初中 - 灰
  2: '',          // 高中 - 蓝（默认）
  3: 'success',   // 大专 - 绿
  4: 'warning',   // 本科 - 橙
  5: 'danger',    // 硕士 - 红
  6: 'primary',   // 博士 - 深蓝（自定义，见下方 style）
}

// 将 "2024-06-01T17:08:23" 转为 "2024-06-01 17:08:23"
const formatTime = (val) => val ? val.replace('T', ' ') : '-'

// ========== 搜索 & 分页 ==========
const searchForm = reactive({ name: '', degree: null, clazzId: null })
const page      = ref(1)
const pageSize  = ref(10)
const total     = ref(0)
const loading   = ref(false)
const tableData = ref([])
const clazzOptions = ref([])

const loadData = async () => {
  loading.value = true
  const res = await getStudentList({
    ...searchForm,
    page: page.value,
    pageSize: pageSize.value,
  })
  if (res.code === 1) {
    tableData.value = res.data.rows
    total.value     = res.data.total
  }
  loading.value = false
}

// 查询按钮：重置到第1页再请求
const handleSearch = () => { page.value = 1; loadData() }

const handleReset = () => {
  Object.assign(searchForm, { name: '', degree: null, clazzId: null })
  handleSearch()
}

// 加载班级列表（data 直接是数组）
// ⑤ 修正 loadClazzOptions：clazzLoading 防止空白闪烁
const clazzLoading = ref(false)
const loadClazzOptions = async () => {
  clazzLoading.value = true
  const res = await getAllClazz()           // 函数名已修正
  if (res.code === 1) {
    clazzOptions.value = res.data           // data 直接是数组
  }
  clazzLoading.value = false
}

// ④ 行选中样式（科技感高亮）
const tableRowClass = ({ row }) => {
  return selectedIds.value.includes(row.id) ? 'row-selected' : ''
}

onMounted(() => { loadData(); loadClazzOptions() })

// ========== 多选删除 ==========
const selectedIds = ref([])
const handleSelectionChange = (rows) => {
  selectedIds.value = rows.map(r => r.id)
}

const handleDelete = (id) => confirmDelete([id])
const handleBatchDelete = () => confirmDelete(selectedIds.value)

const confirmDelete = (ids) => {
  ElMessageBox.confirm(
      `⚠️ 确定删除选中的 ${ids.length} 条记录吗？此操作不可恢复！`,
      '🗑️ 确认删除',
      {
        type: 'warning',
        customClass: 'cyber-confirm',   // 对应上方 :global 样式
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      }
  )
      .then(async () => {
        const res = await deleteStudents(ids)
        if (res.code === 1) {
          ElMessage.success('✅ 删除成功')
          loadData()
        }
      })
      .catch(() => {})
}

// ========== 新增 / 编辑 ==========
const dialogVisible = ref(false)
const isEdit        = ref(false)
const formRef       = ref()

const defaultForm = () => ({
  id: null, name: '', no: '', gender: 1, phone: '',
  degree: null, clazzId: null, idCard: '', isCollege: 0,
  address: '', graduationDate: '',
  violationCount: 0, violationScore: 0,
})
const form = reactive(defaultForm())

const rules = {
  name:    [{ required: true, message: '请输入姓名',   trigger: 'blur' }],
  no:      [{ required: true, message: '请输入学号',   trigger: 'blur' }],
  gender:  [{ required: true, message: '请选择性别',   trigger: 'change' }],
  phone:   [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  degree:  [{ required: true, message: '请选择学历',   trigger: 'change' }],
  clazzId: [{ required: true, message: '请选择班级',   trigger: 'change' }],
}

// id 存在则为编辑，否则新增
const openDialog = async (id) => {
  isEdit.value = !!id
  if (id) {
    const res = await getStudentById(id)
    if (res.code === 1) Object.assign(form, res.data)
  }
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, defaultForm())
}

const handleSubmit = async () => {
  await formRef.value.validate()
  const api = isEdit.value ? updateStudent : addStudent
  const res = await api({ ...form })
  if (res.code === 1) {
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    loadData()
  }
}
</script>

<template>
  <div class="stu-page">

    <!-- ===== 顶部标题栏 ===== -->
    <div class="page-header">
      <div class="page-title">
        <span class="title-icon">🎓</span>
        <span class="title-text">学员管理</span>
        <span class="title-badge">TLIAS</span>
      </div>
      <div class="page-meta">共 <b>{{ total }}</b> 名学员</div>
    </div>

    <!-- ===== 搜索卡片 ===== -->
    <div class="cyber-card search-card">
      <div class="card-label">🔍 条件筛选</div>
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item>
          <div class="input-wrap">
            <span class="input-icon">👤</span>
            <el-input v-model="searchForm.name" placeholder="学员姓名" clearable class="cyber-input" />
          </div>
        </el-form-item>
        <el-form-item>
          <div class="input-wrap">
            <span class="input-icon">🎓</span>
            <el-select v-model="searchForm.degree" placeholder="最高学历" clearable class="cyber-select" style="width:140px">
              <el-option v-for="d in degreeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item>
          <div class="input-wrap">
            <span class="input-icon">🏫</span>
            <!-- 班级下拉：数据来自 getAllClazz，格式为 { code:1, data:[{id,name}] } -->
            <el-select v-model="searchForm.clazzId" placeholder="所属班级" clearable class="cyber-select"
                       style="width:160px" :loading="clazzLoading">
              <el-option v-for="c in clazzOptions" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button class="btn-query" @click="handleSearch">⚡ 查询</el-button>
          <el-button class="btn-reset" @click="handleReset">↺ 重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- ===== 操作行 ===== -->
    <div class="action-row">
      <el-button class="btn-danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
        🗑️ 批量删除
        <span v-if="selectedIds.length" class="selected-badge">{{ selectedIds.length }}</span>
      </el-button>
      <el-button class="btn-primary" @click="openDialog()">➕ 新增学员</el-button>
    </div>

    <!-- ===== 数据表格 ===== -->
    <div class="cyber-card table-card">
      <el-table
          :data="tableData"
          v-loading="loading"
          element-loading-text="数据加载中..."
          element-loading-background="rgba(10,20,40,0.85)"
          class="cyber-table"
          @selection-change="handleSelectionChange"
          :row-class-name="tableRowClass"
      >
        <el-table-column type="selection" width="46" align="center" />

        <el-table-column label="👤 姓名" prop="name" min-width="90">
          <template #default="{ row }">
            <span class="cell-name">{{ row.name }}</span>
          </template>
        </el-table-column>

        <el-table-column label="🔖 学号" prop="no" min-width="120">
          <template #default="{ row }">
            <span class="cell-no">{{ row.no }}</span>
          </template>
        </el-table-column>

        <el-table-column label="🏫 班级" prop="clazzName" min-width="130">
          <template #default="{ row }">
            <span class="cell-clazz">{{ row.clazzName }}</span>
          </template>
        </el-table-column>

        <el-table-column label="⚧ 性别" prop="gender" width="80" align="center">
          <template #default="{ row }">
            <el-tag :class="row.gender === 1 ? 'tag-male' : 'tag-female'" effect="dark" size="small">
              {{ row.gender === 1 ? '♂ 男' : '♀ 女' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="📱 手机号" prop="phone" min-width="125">
          <template #default="{ row }">
            <span class="cell-phone">{{ row.phone }}</span>
          </template>
        </el-table-column>

        <el-table-column label="📚 学历" prop="degree" width="96" align="center">
          <template #default="{ row }">
            <el-tag :class="degreeTagClass[row.degree]" effect="dark" size="small">
              {{ degreeMap[row.degree] }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="🕐 最后操作" prop="updateTime" min-width="158">
          <template #default="{ row }">
            <span class="cell-time">{{ formatTime(row.updateTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="⚙️ 操作" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <el-button class="op-edit" link @click="openDialog(row.id)">✏️ 编辑</el-button>
            <el-button class="op-del"  link @click="handleDelete(row.id)">🗑️ 删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            class="cyber-pagination"
            @change="loadData"
        />
      </div>
    </div>

    <!-- ===== 新增/编辑 弹窗 ===== -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '✏️ 编辑学员信息' : '➕ 新增学员'"
        width="min(620px, 95vw)"
        class="cyber-dialog"
        @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="88px" class="dialog-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" class="cyber-input" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="学号" prop="no">
              <el-input v-model="form.no" placeholder="请输入学号" class="cyber-input" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender" class="cyber-radio">
                <el-radio :label="1">♂ 男</el-radio>
                <el-radio :label="2">♀ 女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" class="cyber-input" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="学历" prop="degree">
              <el-select v-model="form.degree" placeholder="请选择" style="width:100%" class="cyber-select">
                <el-option v-for="d in degreeOptions" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="所属班级" prop="clazzId">
              <el-select v-model="form.clazzId" placeholder="请选择" style="width:100%" class="cyber-select">
                <el-option v-for="c in clazzOptions" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="身份证">
              <el-input v-model="form.idCard" placeholder="选填" class="cyber-input" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="毕业时间">
              <el-date-picker v-model="form.graduationDate" type="date" placeholder="选填"
                              value-format="YYYY-MM-DD" style="width:100%" class="cyber-input" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="院校学生">
              <el-radio-group v-model="form.isCollege" class="cyber-radio">
                <el-radio :label="1">✅ 是</el-radio>
                <el-radio :label="0">❌ 否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item label="联系地址">
              <el-input v-model="form.address" placeholder="选填" class="cyber-input" />
            </el-form-item>
          </el-col>
          <template v-if="isEdit">
            <el-col :xs="24" :sm="12">
              <el-form-item label="违纪次数" prop="violationCount">
                <el-input-number v-model="form.violationCount" :min="0" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="违纪扣分" prop="violationScore">
                <el-input-number v-model="form.violationScore" :min="0" style="width:100%" />
              </el-form-item>
            </el-col>
          </template>
        </el-row>
      </el-form>
      <template #footer>
        <el-button class="btn-reset" @click="dialogVisible = false">取消</el-button>
        <el-button class="btn-primary" @click="handleSubmit">💾 保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<style scoped>

/* ========== 基础布局 ========== */
.stu-page {
  padding: 16px;
  min-height: 100vh;
  background: #05101f;           /* 深海夜色底色 */
  color: #c8e6ff;
  font-family: 'Segoe UI', 'PingFang SC', sans-serif;
  box-sizing: border-box;
}

/* ========== 顶部标题 ========== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}
.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
}
.title-icon { font-size: 22px; }
.title-text {
  font-size: 20px;
  font-weight: 700;
  color: #7df4ff;
  letter-spacing: 2px;
  text-shadow: 0 0 12px #00d4ff88;
}
.title-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #00d4ff22;
  border: 1px solid #00d4ff55;
  color: #7df4ff;
  letter-spacing: 1px;
}
.page-meta { font-size: 13px; color: #5a8fc0; }
.page-meta b { color: #7df4ff; }

/* ========== 科技风卡片 ========== */
.cyber-card {
  background: #0a1828;
  border: 1px solid #1a3a5c;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 14px;
  position: relative;
  /* 顶边高亮线 */
  border-top: 2px solid #00d4ff44;
  transition: border-color 0.3s;
}
.cyber-card:hover { border-color: #00d4ff88; }
.card-label {
  font-size: 12px;
  color: #00d4ffaa;
  letter-spacing: 1px;
  margin-bottom: 12px;
}

/* ========== 搜索表单 ========== */
.search-form { display: flex; flex-wrap: wrap; gap: 4px; }
.input-wrap {
  display: flex;
  align-items: center;
  background: #0d1f36;
  border: 1px solid #1e4068;
  border-radius: 6px;
  padding: 0 8px;
  transition: border-color 0.25s, box-shadow 0.25s;
}
.input-wrap:focus-within {
  border-color: #00d4ff;
  box-shadow: 0 0 0 2px #00d4ff22;
}
.input-icon { font-size: 14px; margin-right: 4px; }

/* 覆盖 Element Plus 输入框背景 */
.cyber-input :deep(.el-input__wrapper),
.cyber-select :deep(.el-select__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  border: none !important;
  color: #c8e6ff;
}
.cyber-input :deep(.el-input__inner),
.cyber-select :deep(.el-select__selected-item) {
  color: #c8e6ff !important;
  background: transparent !important;
}
/* 下拉面板深色 */
:deep(.el-select__popper.el-popper) {
  background: #0d1f36 !important;
  border: 1px solid #1e4068 !important;
}
:deep(.el-select-dropdown__item) {
  color: #a0c8e8 !important;
}
:deep(.el-select-dropdown__item.is-hovering),
:deep(.el-select-dropdown__item:hover) {
  background: #1a3a5c !important;
  color: #7df4ff !important;
}

/* ========== 按钮 ========== */
.btn-primary {
  background: linear-gradient(135deg, #0077b6, #023e8a) !important;
  border: 1px solid #00b4d8 !important;
  color: #e0f7ff !important;
  border-radius: 6px !important;
  transition: box-shadow 0.25s, transform 0.15s !important;
}
.btn-primary:hover {
  box-shadow: 0 0 12px #00b4d888 !important;
  transform: translateY(-1px) !important;
}
.btn-danger {
  background: linear-gradient(135deg, #7b1e1e, #3d0c0c) !important;
  border: 1px solid #e05555 !important;
  color: #ffd6d6 !important;
  border-radius: 6px !important;
  transition: box-shadow 0.25s, transform 0.15s !important;
}
.btn-danger:hover:not(:disabled) {
  box-shadow: 0 0 12px #e0555588 !important;
  transform: translateY(-1px) !important;
}
.btn-query {
  background: #003d66 !important;
  border: 1px solid #0096c7 !important;
  color: #7df4ff !important;
  border-radius: 6px !important;
}
.btn-query:hover {
  box-shadow: 0 0 8px #0096c788 !important;
}
.btn-reset {
  background: #0a1828 !important;
  border: 1px solid #2a4a6a !important;
  color: #7a9fc0 !important;
  border-radius: 6px !important;
}
.btn-reset:hover { border-color: #4a7a9a !important; }

.selected-badge {
  display: inline-block;
  margin-left: 6px;
  background: #e05555;
  color: #fff;
  border-radius: 10px;
  font-size: 11px;
  padding: 0 6px;
  line-height: 16px;
}

/* ========== 操作行 ========== */
.action-row {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

/* ========== 表格 ========== */
.table-card { padding: 0; overflow: hidden; }

/* 覆盖 Element Plus 表格整体背景 */
.cyber-table :deep(.el-table__inner-wrapper),
.cyber-table :deep(.el-table),
.cyber-table :deep(table) {
  background: #0a1828 !important;
  color: #c8e6ff !important;
}
/* 表头 */
.cyber-table :deep(.el-table__header th) {
  background: #0d2040 !important;
  color: #7df4ff !important;
  font-weight: 600;
  border-bottom: 1px solid #1a4070 !important;
  font-size: 13px;
}
/* 行 */
.cyber-table :deep(.el-table__row td) {
  background: #0a1828 !important;
  border-bottom: 1px solid #112235 !important;
  color: #b0d4f0 !important;
  transition: background 0.2s;
}
.cyber-table :deep(.el-table__row:hover td) {
  background: #0e2a44 !important;
}
/* 选中行高亮 */
.cyber-table :deep(.row-selected td) {
  background: #003d6688 !important;
  border-color: #00b4d855 !important;
}
/* 复选框 */
.cyber-table :deep(.el-checkbox__inner) {
  background: #0d1f36 !important;
  border-color: #2a5a8a !important;
}
.cyber-table :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: #0096c7 !important;
  border-color: #0096c7 !important;
}

/* ========== 单元格样式 ========== */
.cell-name  { color: #90e0ff; font-weight: 600; }
.cell-no    { color: #a0d8b0; font-family: monospace; font-size: 13px; }
.cell-clazz { color: #d4a8ff; }
.cell-phone { color: #ffd88a; font-family: monospace; font-size: 13px; }
.cell-time  { color: #6a9fc8; font-size: 12px; }

/* 性别 Tag */
.tag-male   { background: #0a3060 !important; border-color: #0096c7 !important; color: #7df4ff !important; }
.tag-female { background: #3d0a2a !important; border-color: #d45f8a !important; color: #ffa8c8 !important; }

/* 学历 Tag 六色 */
.tag-degree-1 { background: #003333 !important; border-color: #00b0b0 !important; color: #00e5e5 !important; } /* 初中-青 */
.tag-degree-2 { background: #003320 !important; border-color: #00b060 !important; color: #00e080 !important; } /* 高中-绿 */
.tag-degree-3 { background: #001f40 !important; border-color: #0070cc !important; color: #4db8ff !important; } /* 大专-蓝 */
.tag-degree-4 { background: #2a0060 !important; border-color: #8844dd !important; color: #cc88ff !important; } /* 本科-紫 */
.tag-degree-5 { background: #3d2000 !important; border-color: #cc7700 !important; color: #ffbb33 !important; } /* 硕士-橙 */
.tag-degree-6 { background: #3d0000 !important; border-color: #cc2200 !important; color: #ff6644 !important; } /* 博士-红 */

/* 操作按钮 */
.op-edit { color: #4db8ff !important; font-size: 13px !important; }
.op-edit:hover { color: #7df4ff !important; text-shadow: 0 0 6px #7df4ff88; }
.op-del  { color: #ff6666 !important; font-size: 13px !important; }
.op-del:hover  { color: #ff9999 !important; text-shadow: 0 0 6px #ff666688; }

/* ========== 分页 ========== */
.pagination-wrap {
  padding: 12px 16px;
  background: #081422;
  border-top: 1px solid #1a3050;
  display: flex;
  justify-content: flex-end;
}
.cyber-pagination :deep(.el-pager li),
.cyber-pagination :deep(.btn-prev),
.cyber-pagination :deep(.btn-next) {
  background: #0d1f36 !important;
  color: #7aa8cc !important;
  border: 1px solid #1e3a58 !important;
  border-radius: 4px !important;
}
.cyber-pagination :deep(.el-pager li.is-active) {
  background: #0077b6 !important;
  color: #fff !important;
  border-color: #00b4d8 !important;
}
.cyber-pagination :deep(.el-pager li:hover),
.cyber-pagination :deep(.btn-prev:hover),
.cyber-pagination :deep(.btn-next:hover) {
  border-color: #00b4d8 !important;
  color: #7df4ff !important;
}
.cyber-pagination :deep(.el-pagination__total),
.cyber-pagination :deep(.el-pagination__jump),
.cyber-pagination :deep(.el-pagination__sizes) {
  color: #5a8fc0 !important;
}
.cyber-pagination :deep(.el-input__wrapper) {
  background: #0d1f36 !important;
  box-shadow: none !important;
  border: 1px solid #1e3a58 !important;
}
.cyber-pagination :deep(.el-input__inner) {
  color: #c8e6ff !important;
}

/* ========== 弹窗 ========== */
.cyber-dialog :deep(.el-dialog) {
  background: #081422 !important;
  border: 1px solid #1a4070 !important;
  border-top: 2px solid #00d4ff66 !important;
  border-radius: 10px !important;
  box-shadow: 0 0 40px #00d4ff22 !important;
}
.cyber-dialog :deep(.el-dialog__header) {
  background: #0d1f36 !important;
  border-bottom: 1px solid #1a3a5c !important;
  border-radius: 10px 10px 0 0 !important;
  padding: 14px 20px !important;
}
.cyber-dialog :deep(.el-dialog__title) {
  color: #7df4ff !important;
  font-weight: 600 !important;
}
.cyber-dialog :deep(.el-dialog__body) {
  padding: 20px !important;
}
.cyber-dialog :deep(.el-dialog__footer) {
  background: #0a1828 !important;
  border-top: 1px solid #1a3050 !important;
  border-radius: 0 0 10px 10px !important;
  padding: 12px 20px !important;
}
/* 弹窗表单 label */
.dialog-form :deep(.el-form-item__label) {
  color: #7aafcc !important;
  font-size: 13px !important;
}
/* 弹窗输入框 */
.dialog-form :deep(.el-input__wrapper),
.dialog-form :deep(.el-select__wrapper) {
  background: #0d1f36 !important;
  border: 1px solid #1e4068 !important;
  box-shadow: none !important;
  border-radius: 6px !important;
  transition: border-color 0.2s, box-shadow 0.2s !important;
}
.dialog-form :deep(.el-input__wrapper:hover),
.dialog-form :deep(.el-select__wrapper:hover) {
  border-color: #0096c7 !important;
}
.dialog-form :deep(.el-input__wrapper.is-focus),
.dialog-form :deep(.el-select__wrapper.is-focused) {
  border-color: #00d4ff !important;
  box-shadow: 0 0 0 2px #00d4ff22 !important;
}
.dialog-form :deep(.el-input__inner),
.dialog-form :deep(.el-select__selected-item) {
  color: #c8e6ff !important;
}
/* radio */
.cyber-radio :deep(.el-radio__label) { color: #a0c8e8 !important; }
.cyber-radio :deep(.el-radio__inner) {
  background: #0d1f36 !important;
  border-color: #2a5a8a !important;
}
.cyber-radio :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: #0096c7 !important;
  border-color: #0096c7 !important;
}

/* ========== 删除确认弹窗（全局覆盖） ========== */
:global(.el-message-box.cyber-confirm) {
  background: #081422 !important;
  border: 1px solid #1a4070 !important;
  border-top: 2px solid #e0555566 !important;
  border-radius: 10px !important;
}
:global(.el-message-box.cyber-confirm .el-message-box__header) {
  background: #0d1f36 !important;
  border-bottom: 1px solid #1a3a5c !important;
}
:global(.el-message-box.cyber-confirm .el-message-box__title) {
  color: #ff9999 !important;
}
:global(.el-message-box.cyber-confirm .el-message-box__content) {
  color: #b0d4f0 !important;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .stu-page { padding: 10px; }
  .search-form { gap: 8px; }
  .title-text { font-size: 16px; }
  .cyber-table :deep(.el-table__header th),
  .cyber-table :deep(.el-table__row td) { font-size: 12px !important; }
}

</style>