<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  queryPageApi,
  addApi,
  queryInfoApi,
  updateApi,
  deleteApi
} from '@/api/emp'
import { queryAllApi as queryAllDeptApi } from '@/api/dept'
import request from '@/utils/request' // 复用已有的 axios 实例上传文件

// ==================== 列表查询 ====================
const searchForm = reactive({ name: '', gender: '', begin: '', end: '' })
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const formatTime = (t) => t ? t.replace('T', ' ') : '—'

const jobMap   = { 1: '班主任', 2: '讲师', 3: '学工主管', 4: '教研主管', 5: '咨询师' }
const jobColor = { 1: '#00ffe7', 2: '#4fc3f7', 3: '#ffd54f', 4: '#ff8a65', 5: '#ce93d8' }
const genderMap   = { 1: '♂ 男', 2: '♀ 女' }
const genderColor = { 1: '#4fc3f7', 2: '#f48fb1' }

const fetchList = async () => {
  loading.value = true
  try {
    const { data } = await queryPageApi(
        searchForm.name, searchForm.gender,
        searchForm.begin, searchForm.end,
        page.value, pageSize.value
    )
    tableData.value = data.rows
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { page.value = 1; fetchList() }
const handleReset = () => {
  searchForm.name = ''; searchForm.gender = ''
  searchForm.begin = ''; searchForm.end = ''
  handleSearch()
}
const handlePageChange = (p) => { page.value = p; fetchList() }
const handleSizeChange = (s) => { pageSize.value = s; page.value = 1; fetchList() }

// ==================== 多选删除 ====================
const selectedIds = ref([])
const handleSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }

const handleBatchDelete = async () => {
  if (!selectedIds.value.length) return ElMessage.warning('请先勾选要删除的员工')
  await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 名员工？`, '⚠️ 批量删除', { type: 'warning' })
  await deleteApi(selectedIds.value.join(','))
  ElMessage.success('删除成功')
  fetchList()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除该员工？', '⚠️ 删除确认', { type: 'warning' })
  await deleteApi(id)
  ElMessage.success('删除成功')
  fetchList()
}

// ==================== 新增/编辑弹窗 ====================
const dialogVisible = ref(false)
const dialogTitle = ref('')
const deptOptions = ref([])
const isEdit = ref(false)
const uploadLoading = ref(false) // 头像上传 loading

const defaultForm = () => ({
  id: null, username: '', name: '', gender: 1,
  phone: '',           // ← 新增手机号
  image: '', deptId: null, entryDate: '', job: null,
  salary: null, exprList: []
})
const form = reactive(defaultForm())
const formRef = ref(null)

// 校验规则：对应 DB 中 not null 字段全部 required
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name:     [{ required: true, message: '请输入姓名',   trigger: 'blur' }],
  gender:   [{ required: true, message: '请选择性别',   trigger: 'change' }],
  phone:    [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
}

const openAdd = async () => {
  Object.assign(form, defaultForm())
  isEdit.value = false
  dialogTitle.value = '🚀 新增员工'
  dialogVisible.value = true
  await loadDepts()
}

const openEdit = async (id) => {
  isEdit.value = true
  dialogTitle.value = '✏️ 编辑员工'
  dialogVisible.value = true
  await loadDepts()
  const { data } = await queryInfoApi(id)
  Object.assign(form, data)
  if (!form.exprList) form.exprList = []
}

const loadDepts = async () => {
  try {
    const { data } = await queryAllDeptApi()
    deptOptions.value = data
  } catch { deptOptions.value = [] }
}

// ==================== 头像上传 ====================
/**
 * el-upload 的 before-upload 钩子
 * 手动构造 FormData → POST /upload → 拿到 OSS 链接写入 form.image
 * 返回 false 阻止 el-upload 自行上传
 */
const handleAvatarUpload = async (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M  = file.size / 1024 / 1024 < 2
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt2M)  { ElMessage.error('图片不能超过 2MB'); return false }

  uploadLoading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file) // 字段名与接口文档一致
    const res = await request.post('/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 1) {
      form.image = res.data  // OSS 链接 → 写入表单 & 实时预览
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } finally {
    uploadLoading.value = false
  }
  return false // 必须 false，阻止 el-upload 再次自动上传
}

// 工作经历操作
const addExpr    = () => form.exprList.push({ company: '', job: '', begin: '', end: '' })
const removeExpr = (i) => form.exprList.splice(i, 1)

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()
  if (isEdit.value) {
    await updateApi({ ...form })
    ElMessage.success('修改成功')
  } else {
    await addApi({ ...form })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

onMounted(fetchList)
</script>

<template>
  <div class="emp-page">
    <!-- 顶部标题 -->
    <div class="page-header">
      <span class="page-title">👥 员工管理</span>
      <span class="page-sub">EMPLOYEE MANAGEMENT SYSTEM</span>
    </div>

    <!-- 搜索栏 -->
    <div class="search-card">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="🔍 姓名">
          <el-input v-model="searchForm.name" placeholder="输入员工姓名" clearable class="tech-input" />
        </el-form-item>
        <el-form-item label="⚧ 性别">
          <el-select v-model="searchForm.gender" placeholder="全部" clearable class="tech-select">
            <el-option label="♂ 男" :value="1" />
            <el-option label="♀ 女" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="📅 入职开始">
          <el-date-picker v-model="searchForm.begin" type="date" value-format="YYYY-MM-DD"
                          placeholder="开始日期" class="tech-date" />
        </el-form-item>
        <el-form-item label="📅 入职结束">
          <el-date-picker v-model="searchForm.end" type="date" value-format="YYYY-MM-DD"
                          placeholder="结束日期" class="tech-date" />
        </el-form-item>
        <el-form-item>
          <el-button class="btn-search" @click="handleSearch">🔎 查询</el-button>
          <el-button class="btn-reset"  @click="handleReset">↺ 重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button class="btn-add"    @click="openAdd">➕ 新增员工</el-button>
      <el-button class="btn-danger" @click="handleBatchDelete">🗑 批量删除</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrap" v-loading="loading" element-loading-background="rgba(5,15,30,0.85)">
      <el-table :data="tableData" class="tech-table"
                @selection-change="handleSelectionChange" row-key="id">
        <el-table-column type="selection" width="48" />
        <el-table-column label="🆔 ID" prop="id" width="70" align="center">
          <template #default="{ row }"><span class="id-badge">#{{ row.id }}</span></template>
        </el-table-column>

        <el-table-column label="🖼 头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :src="row.image" :size="36" class="avatar" />
          </template>
        </el-table-column>

        <el-table-column label="👤 姓名" prop="name" width="100" align="center">
          <template #default="{ row }"><span class="name-tag">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column label="🔑 用户名" prop="username" width="110" align="center">
          <template #default="{ row }"><span class="username-tag">{{ row.username }}</span></template>
        </el-table-column>
        <el-table-column label="⚧ 性别" width="80" align="center">
          <template #default="{ row }">
            <span class="gender-badge" :style="{ color: genderColor[row.gender] }">
              {{ genderMap[row.gender] || '—' }}
            </span>
          </template>
        </el-table-column>
        <!-- 新增手机号列 -->
        <el-table-column label="📱 手机号" prop="phone" width="130" align="center">
          <template #default="{ row }"><span class="phone-tag">{{ row.phone || '—' }}</span></template>
        </el-table-column>
        <el-table-column label="💼 职位" width="110" align="center">
          <template #default="{ row }">
            <span class="job-pill"
                  :style="{ borderColor: jobColor[row.job], color: jobColor[row.job], boxShadow: `0 0 8px ${jobColor[row.job]}55` }">
              {{ jobMap[row.job] || '—' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="💰 薪资" prop="salary" width="100" align="center">
          <template #default="{ row }"><span class="salary-tag">¥ {{ row.salary ?? '—' }}</span></template>
        </el-table-column>
        <el-table-column label="🏢 部门" prop="deptName" width="110" align="center">
          <template #default="{ row }"><span class="dept-tag">{{ row.deptName || '—' }}</span></template>
        </el-table-column>
        <el-table-column label="📅 入职日期" prop="entryDate" width="120" align="center">
          <template #default="{ row }"><span class="date-tag">{{ row.entryDate || '—' }}</span></template>
        </el-table-column>
        <el-table-column label="🕐 更新时间" width="160" align="center">
          <template #default="{ row }"><span class="time-tag">{{ formatTime(row.updateTime) }}</span></template>
        </el-table-column>
        <el-table-column label="⚙️ 操作" fixed="right" width="140" align="center">
          <template #default="{ row }">
            <el-button size="small" class="btn-edit" @click="openEdit(row.id)">✏️ 编辑</el-button>
            <el-button size="small" class="btn-del"  @click="handleDelete(row.id)">🗑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
          v-model:current-page="page" v-model:page-size="pageSize"
          :total="total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange" @size-change="handleSizeChange"
          class="tech-pagination"
      />
    </div>

    <!-- 新增/编辑 Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" class="tech-dialog" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" class="tech-form">
        <el-row :gutter="16">

          <!-- 左侧：头像上传预览区 -->
          <el-col :span="6" style="display:flex;flex-direction:column;align-items:center;padding-top:6px">
            <!-- 有 image 时实时预览，否则半透明占位 -->
            <el-avatar :src="form.image || ''" :size="80" class="avatar-preview"
                       :style="{ opacity: form.image ? 1 : 0.35 }" />
            <!-- before-upload 返回 false → 阻止 el-upload 自动上传，改为手动调用 /upload -->
            <el-upload :show-file-list="false" accept="image/*"
                       :before-upload="handleAvatarUpload" class="avatar-uploader">
              <el-button size="small" class="btn-upload" :loading="uploadLoading">
                🖼 {{ form.image ? '更换头像' : '上传头像' }}
              </el-button>
            </el-upload>
            <span class="upload-hint">≤ 2MB · JPG/PNG</span>
          </el-col>

          <!-- 右侧：基本信息字段 -->
          <el-col :span="18">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="form.username" placeholder="请输入用户名" class="tech-input" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="form.name" placeholder="请输入姓名" class="tech-input" />
                </el-form-item>
              </el-col>
              <!-- 手机号（DB not null → required） -->
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="form.phone" placeholder="请输入11位手机号" class="tech-input" maxlength="11" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="form.gender">
                    <el-radio :label="1">♂ 男</el-radio>
                    <el-radio :label="2">♀ 女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="职位">
                  <el-select v-model="form.job" placeholder="请选择职位" clearable class="tech-select" style="width:100%">
                    <el-option v-for="(v, k) in jobMap" :key="k" :label="v" :value="Number(k)" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="薪资">
                  <el-input-number v-model="form.salary" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="部门">
                  <!-- ⚠️ 根据后端返回字段调整 :label / :value -->
                  <el-select v-model="form.deptId" placeholder="请选择部门" clearable class="tech-select" style="width:100%">
                    <el-option v-for="d in deptOptions" :key="d.id" :label="d.name" :value="d.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="入职日期">
                  <el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD"
                                  placeholder="选择入职日期" class="tech-date" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-col>
        </el-row>

        <!-- 工作经历 -->
        <div class="expr-section">
          <div class="expr-header">
            <span>📋 工作经历</span>
            <el-button size="small" class="btn-add-expr" @click="addExpr">＋ 添加</el-button>
          </div>
          <div v-for="(expr, i) in form.exprList" :key="i" class="expr-row">
            <el-input v-model="expr.company" placeholder="公司名称" class="tech-input expr-input" />
            <el-input v-model="expr.job"     placeholder="职位"     class="tech-input expr-input" />
            <el-date-picker v-model="expr.begin" type="date" value-format="YYYY-MM-DD" placeholder="开始" class="tech-date expr-date" />
            <el-date-picker v-model="expr.end"   type="date" value-format="YYYY-MM-DD" placeholder="结束" class="tech-date expr-date" />
            <el-button size="small" class="btn-del" @click="removeExpr(i)">✕</el-button>
          </div>
        </div>
      </el-form>

      <template #footer>
        <el-button class="btn-reset"  @click="dialogVisible = false">取消</el-button>
        <el-button class="btn-search" @click="handleSubmit">💾 保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.emp-page {
  --c-bg: #050f1e; --c-card: #0a1a30; --c-border: #0d3a5c;
  --c-cyan: #00ffe7; --c-blue: #1e90ff;
  --c-hover: rgba(0,255,231,0.07); --c-text: #cce8ff; --c-sub: #5a8aaa;
  min-height: 100vh; background: var(--c-bg); padding: 24px 28px;
  font-family: 'Courier New', 'Consolas', monospace; color: var(--c-text);
}

.page-header { display: flex; align-items: baseline; gap: 14px; margin-bottom: 22px; }
.page-title {
  font-size: 24px; font-weight: 700; color: var(--c-cyan); letter-spacing: 2px;
  text-shadow: 0 0 16px var(--c-cyan), 0 0 32px rgba(0,255,231,0.3);
}
.page-sub { font-size: 11px; color: var(--c-sub); letter-spacing: 4px; animation: pulse 3s ease-in-out infinite; }
@keyframes pulse { 0%,100%{opacity:.5} 50%{opacity:1} }

.search-card {
  background: var(--c-card); border: 1px solid var(--c-border); border-radius: 10px;
  padding: 18px 20px 8px; margin-bottom: 14px; box-shadow: 0 0 20px rgba(0,80,160,0.2);
}
.search-form :deep(.el-form-item__label) { color: var(--c-sub); font-size: 13px; }
.action-bar { display: flex; gap: 10px; margin-bottom: 14px; }

/* 按钮 */
.btn-search, .btn-add {
  background: linear-gradient(135deg, #004d80, #007acc); border: 1px solid var(--c-blue);
  color: #fff; font-weight: 600; border-radius: 6px; transition: all .25s;
  box-shadow: 0 0 10px rgba(30,144,255,0.3);
}
.btn-search:hover, .btn-add:hover {
  box-shadow: 0 0 18px rgba(30,144,255,0.7); transform: translateY(-1px);
  background: linear-gradient(135deg, #006aad, #009fff);
}
.btn-reset { background: transparent; border: 1px solid var(--c-border); color: var(--c-sub); border-radius: 6px; transition: all .25s; }
.btn-reset:hover { border-color: var(--c-cyan); color: var(--c-cyan); }
.btn-danger { background: linear-gradient(135deg, #3d0010, #800020); border: 1px solid #cc0033; color: #ff6680; border-radius: 6px; transition: all .25s; }
.btn-danger:hover { box-shadow: 0 0 16px rgba(255,50,80,0.5); transform: translateY(-1px); }
.btn-edit { background: rgba(30,144,255,0.12); border: 1px solid #1e5f8a; color: #4fc3f7; border-radius: 5px; font-size: 12px; transition: all .2s; }
.btn-edit:hover { background: rgba(30,144,255,0.25); box-shadow: 0 0 10px rgba(30,144,255,0.4); }
.btn-del { background: rgba(255,50,80,0.1); border: 1px solid #5a1020; color: #ff6680; border-radius: 5px; font-size: 12px; transition: all .2s; }
.btn-del:hover { background: rgba(255,50,80,0.25); box-shadow: 0 0 10px rgba(255,50,80,0.4); }

/* 表格 */
.table-wrap { border-radius: 10px; overflow: hidden; border: 1px solid var(--c-border); box-shadow: 0 0 30px rgba(0,50,120,0.25); }
.tech-table { background: var(--c-card); }
.tech-table :deep(.el-table__header-wrapper th) {
  background: #061525 !important; color: var(--c-cyan) !important;
  font-size: 12px; letter-spacing: 1px; border-bottom: 1px solid var(--c-border) !important;
}
.tech-table :deep(.el-table__body-wrapper tr td) {
  background: var(--c-card) !important; border-bottom: 1px solid #0a2540 !important; color: var(--c-text);
}
.tech-table :deep(.el-table__body tr:hover td) {
  background: var(--c-hover) !important; box-shadow: inset 0 0 0 1px rgba(0,255,231,0.1); transition: background .2s;
}
.tech-table :deep(.el-table__body tr.el-table__row--striped td) { background: var(--c-card) !important; }

/* 单元格 */
.id-badge    { color: var(--c-sub); font-size: 12px; font-family: monospace; }
.name-tag    { color: #e0f0ff; font-weight: 600; }
.username-tag { color: #7dd3fc; font-size: 12px; font-family: monospace; background: rgba(125,211,252,0.08); padding: 2px 8px; border-radius: 4px; }
.gender-badge { font-weight: 600; font-size: 13px; }
.phone-tag   { color: #a5d6a7; font-size: 12px; font-family: monospace; letter-spacing: .5px; }
.job-pill    { display: inline-block; padding: 2px 10px; border: 1px solid; border-radius: 20px; font-size: 12px; font-weight: 600; letter-spacing: .5px; transition: box-shadow .3s; }
.salary-tag  { color: #ffd54f; font-weight: 700; letter-spacing: .5px; }
.dept-tag    { color: #ce93d8; background: rgba(206,147,216,0.1); padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.date-tag    { color: #80cbc4; font-size: 12px; }
.time-tag    { color: var(--c-sub); font-size: 11px; font-family: monospace; }

/* 头像（表格内） */
.avatar { border: 2px solid var(--c-border); box-shadow: 0 0 8px rgba(0,255,231,0.15); transition: box-shadow .25s; }
.avatar:hover { box-shadow: 0 0 14px rgba(0,255,231,0.45); }

/* 分页 */
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 14px; }
.tech-pagination :deep(.el-pagination__total),
.tech-pagination :deep(.el-pagination__sizes .el-input__inner),
.tech-pagination :deep(.el-pager li),
.tech-pagination :deep(.el-pagination button) { color: var(--c-sub); background: transparent; }
.tech-pagination :deep(.el-pager li.is-active) { color: var(--c-cyan) !important; font-weight: 700; text-shadow: 0 0 8px var(--c-cyan); }
.tech-pagination :deep(.el-pager li:hover) { color: var(--c-text) !important; }

/* 输入框 */
.tech-input :deep(.el-input__inner),
.tech-select :deep(.el-input__inner) { background: #061525; border-color: var(--c-border); color: var(--c-text); }
.tech-input :deep(.el-input__inner):focus,
.tech-select :deep(.el-input__inner):focus { border-color: var(--c-cyan); box-shadow: 0 0 8px rgba(0,255,231,0.3); }
.tech-date :deep(.el-input__inner) { background: #061525; border-color: var(--c-border); color: var(--c-text); }

/* Dialog */
.tech-dialog :deep(.el-dialog) { background: #071828; border: 1px solid var(--c-border); border-radius: 12px; box-shadow: 0 0 40px rgba(0,100,200,0.3); }
.tech-dialog :deep(.el-dialog__title) { color: var(--c-cyan); font-weight: 700; letter-spacing: 1px; text-shadow: 0 0 10px var(--c-cyan); }
.tech-dialog :deep(.el-dialog__header) { border-bottom: 1px solid var(--c-border); }
.tech-dialog :deep(.el-dialog__footer) { border-top: 1px solid var(--c-border); }
.tech-form :deep(.el-form-item__label) { color: var(--c-sub); font-size: 13px; }
.tech-form :deep(.el-radio__label) { color: var(--c-text); }
.tech-form :deep(.el-radio__input.is-checked .el-radio__inner) { border-color: var(--c-cyan); background: var(--c-cyan); }

/* 头像上传区（Dialog内） */
.avatar-preview { border: 2px solid var(--c-border); box-shadow: 0 0 12px rgba(0,255,231,0.2); margin-bottom: 10px; transition: box-shadow .3s; }
.avatar-preview:hover { box-shadow: 0 0 20px rgba(0,255,231,0.5); }
.avatar-uploader { margin-bottom: 6px; }
.btn-upload { background: rgba(0,255,231,0.08); border: 1px solid rgba(0,255,231,0.3); color: var(--c-cyan); border-radius: 6px; font-size: 12px; transition: all .25s; }
.btn-upload:hover { background: rgba(0,255,231,0.18); box-shadow: 0 0 10px rgba(0,255,231,0.3); }
.upload-hint { font-size: 11px; color: var(--c-sub); text-align: center; }

/* 工作经历 */
.expr-section { margin-top: 14px; border: 1px solid var(--c-border); border-radius: 8px; padding: 12px; background: rgba(0,20,50,0.4); }
.expr-header { display: flex; justify-content: space-between; align-items: center; color: var(--c-cyan); font-size: 13px; font-weight: 600; margin-bottom: 10px; letter-spacing: 1px; }
.btn-add-expr { background: rgba(0,255,231,0.08); border: 1px solid rgba(0,255,231,0.3); color: var(--c-cyan); border-radius: 5px; font-size: 12px; }
.btn-add-expr:hover { background: rgba(0,255,231,0.18); }
.expr-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; flex-wrap: wrap; }
.expr-input { flex: 1.5; min-width: 110px; }
.expr-date  { flex: 1; min-width: 120px; }
</style>