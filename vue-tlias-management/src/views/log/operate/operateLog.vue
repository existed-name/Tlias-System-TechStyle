<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getPageApi, getSummaryDataApi } from '@/api/log/operateLog'
import { queryAllApi } from '@/api/emp'

// ─── 统计看板数据 ───────────────────────────────────────────────
const summary = ref({ todayCount: 0, totalCount: 0, avgCostTimes: [], totalTypeCounts: [] })

// ─── 搜索条件 ───────────────────────────────────────────────────
const searchForm = ref({
  operateEmpId: null,
  beginDate: null,
  endDate: null
})
const empList = ref([])

// ─── 分页 & 表格 ─────────────────────────────────────────────────
const tableData  = ref([])
const total      = ref(0)
const page       = ref(1)
const pageSize   = ref(10)
const tableLoading = ref(false)

// ─── 图表实例 (使用 let 定义，不需要 ref 包裹实例本身) ──────────────
const barChartRef = ref(null)
const pieChartRef = ref(null)
let barChart = null
let pieChart = null

// ─── 工具函数 ────────────────────────────────────────────────────
const shortClass = (fullName) => fullName ? fullName.split('.').pop() : '-'
const fmtTime = (t) => t ? t.replace('T', ' ') : '-'

const costLevel = (ms) => {
  if (ms < 300)  return 'lvl-fast'
  if (ms < 500)  return 'lvl-ok'
  if (ms < 1000) return 'lvl-warn'
  return 'lvl-slow'
}

// ─── 核心逻辑：图表自适应 ──────────────────────────────────────────
const handleResize = () => {
  barChart && barChart.resize()
  pieChart && pieChart.resize()
}

// ─── 初始化图表 ──────────────────────────────────────────────────
const initCharts = () => {
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }
}

// ─── 渲染图表 (修复动画失效的关键) ──────────────────────────────────
const renderCharts = () => {
  if (!barChart || !pieChart) return

  const types = summary.value.avgCostTimes.map(i => i.type)
  const avgTimes = summary.value.avgCostTimes.map(i => i.avgTime)

  const BAR_COLORS = [
    { start: '#00e5ff', end: '#0057b7' },
    { start: '#1de9b6', end: '#00796b' },
    { start: '#7c4dff', end: '#311b92' },
    { start: '#ff6d00', end: '#bf360c' },
  ]

  // 柱状图配置
  barChart.setOption({
    animation: true,
    animationDuration: 2000, // 动画时长 2秒
    animationEasing: 'exponentialOut',
    animationDelay: (idx) => idx * 200, // 每个柱子间隔 0.2秒 生长
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#0d1f3c',
      borderColor: '#00e5ff33',
      textStyle: { color: '#e0f7fa' }
    },
    grid: { left: '8%', right: '4%', top: '15%', bottom: '10%' },
    xAxis: {
      type: 'category',
      data: types,
      axisLabel: { color: '#90caf9' }
    },
    yAxis: {
      type: 'value',
      name: 'ms',
      axisLabel: { color: '#90caf9' },
      splitLine: { lineStyle: { color: '#00e5ff15' } }
    },
    series: [{
      type: 'bar',
      data: avgTimes.map((val, i) => ({
        value: val,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: BAR_COLORS[i % BAR_COLORS.length].start },
            { offset: 1, color: BAR_COLORS[i % BAR_COLORS.length].end }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      })),
      barMaxWidth: 40,
      label: { show: true, position: 'top', color: '#90caf9', formatter: '{c} ms' }
    }]
  }, { notMerge: true }) // <--- 关键：notMerge 确保动画重新触发

  // 饼图配置
  const PIE_COLORS = ['#00e5ff','#1de9b6','#7c4dff','#ff6d00','#ff4081','#ffd740']
  pieChart.setOption({
    // 1. 全局动画强化
    animation: true,
    animationDuration: 0,
    animationEasing: 'cubicOut',

    // 2. 针对饼图特有的生长效果
    animationType: 'expansion', // 像扇子一样展开，而不是直接淡入
    animationDelay: (idx) => idx * 250, // 每个扇区稍微错开一点时间

    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: summary.value.totalTypeCounts.map((i, idx) => ({
        name: i.type,
        value: i.count,
        itemStyle: { color: PIE_COLORS[idx % PIE_COLORS.length] }
      })),
      label: { color: '#90caf9' }
    }]
  }, { notMerge: true })
}

// ─── 数据获取 ────────────────────────────────────────────────────
const fetchSummary = async () => {
  const res = await getSummaryDataApi()
  if (res.code === 1) {
    summary.value = res.data
    // 数据回来后再触发渲染，保证动画是带着数据跑的
    renderCharts()
  }
}

const fetchEmpList = async () => {
  const res = await queryAllApi()
  if (res.code === 1) empList.value = res.data
}

const fetchPage = async () => {
  tableLoading.value = true
  const params = {
    page: page.value,
    pageSize: pageSize.value,
    operateEmpId: searchForm.value.operateEmpId || undefined,
    beginDate: searchForm.value.beginDate || undefined,
    endDate:   searchForm.value.endDate   || undefined
  }
  const res = await getPageApi(params)
  if (res.code === 1) {
    tableData.value = res.data.rows
    total.value = res.data.total
  }
  tableLoading.value = false
}

// ─── 生命周期合并 ──────────────────────────────────────────────────
onMounted(async () => {
  // 1. 初始化图表容器
  initCharts()

  // 2. 绑定窗口缩放
  window.addEventListener('resize', handleResize)

  // 3. 统一拉取数据 (fetchSummary 会内部调用 renderCharts)
  await Promise.all([fetchSummary(), fetchEmpList(), fetchPage()])
})

onBeforeUnmount(() => {
  // 销毁监听与实例，彻底释放内存
  window.removeEventListener('resize', handleResize)
  if (barChart) barChart.dispose()
  if (pieChart) pieChart.dispose()
})

// ─── 事件处理 ────────────────────────────────────────────────────
const handleSearch = () => { page.value = 1; fetchPage() }
const handleReset = () => {
  searchForm.value = { operateEmpId: null, beginDate: null, endDate: null }
  page.value = 1
  fetchPage()
}
const handleCurrentChange = (val) => { page.value = val; fetchPage() }
const handleSizeChange    = (val) => { pageSize.value = val; page.value = 1; fetchPage() }

</script>

<template>
  <div class="log-page">

    <!-- ══ HEADER：统计看板 ══════════════════════════════════════ -->
    <div class="stat-cards">
      <div class="stat-card" v-for="card in [
        { label: '今日操作总数', value: summary.todayCount, icon: '📊', accent: '#00e5ff' },
        { label: '累计操作总数', value: summary.totalCount, icon: '📈', accent: '#1de9b6' }
      ]" :key="card.label" :style="{ '--accent': card.accent }">
        <div class="stat-icon">{{ card.icon }}</div>
        <div class="stat-body">
          <p class="stat-label">{{ card.label }}</p>
          <p class="stat-value">{{ card.value.toLocaleString() }}</p>
        </div>
        <div class="stat-glow" />
      </div>
    </div>

    <!-- ══ MIDDLE：图表区 ════════════════════════════════════════ -->
    <div class="chart-row">
      <!-- 柱状图 -->
      <div class="chart-card">
        <div class="card-header"><span class="card-icon">⚡</span>平均响应时间</div>
        <div ref="barChartRef" class="chart-body" />
      </div>
      <!-- 饼图 -->
      <div class="chart-card">
        <div class="card-header"><span class="card-icon">🧩</span>累计操作组成</div>
        <div ref="pieChartRef" class="chart-body" />
      </div>
    </div>

    <!-- ══ BOTTOM：表格区 ════════════════════════════════════════ -->
    <div class="table-card">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <!-- 员工下拉 -->
        <el-select
            v-model="searchForm.operateEmpId"
            placeholder="🔍 操作员工"
            clearable
            class="cyber-select"
        >
          <el-option
              v-for="emp in empList"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
          />
        </el-select>

        <!-- 日期区间选择 -->
        <!-- 替换原来的 dateRange -->
        <el-date-picker
            v-model="searchForm.beginDate"
            type="date"
            placeholder="📅 开始日期"
            value-format="YYYY-MM-DD"
            class="cyber-datepicker"
        />
        <el-date-picker
            v-model="searchForm.endDate"
            type="date"
            placeholder="📅 结束日期"
            value-format="YYYY-MM-DD"
            class="cyber-datepicker"
        />

        <!-- 操作按钮 -->
        <div class="btn-group">
          <el-button class="btn-search" @click="handleSearch">🔎 查询</el-button>
          <el-button class="btn-reset"  @click="handleReset">↺ 重置</el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
          :data="tableData"
          v-loading="tableLoading"
          class="cyber-table"
          :header-cell-style="{ background: '#071e3d', color: '#00e5ff', borderBottom: '1px solid #00e5ff30', fontFamily: 'monospace' }"
          :cell-style="{ background: 'transparent', color: '#cfe8ff', borderBottom: '1px solid #0d3060' }"
      >
        <el-table-column prop="id"             label="ID"              width="70"  align="center" />
        <el-table-column prop="operateEmpName" label="操作员工"         width="110" align="center" />
        <el-table-column label="行为描述" min-width="200">
          <template #default="{ row }">
            <span class="method-tag">{{ shortClass(row.className) }}</span>
            <span class="arrow"> → </span>
            <span class="method-name">{{ row.methodName }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="costTime" label="响应时间 (ms)" width="130" align="center">

          <!-- 替换 cost-badge 模板 -->
          <template #default="{ row }">
            <span class="cost-badge" :class="costLevel(row.costTime)">
              {{ row.costTime }} ms
            </span>
          </template>

        </el-table-column>
        <el-table-column label="操作时间" width="175" align="center">
          <template #default="{ row }">{{ fmtTime(row.operateTime) }}</template>
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
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            class="cyber-pagination"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ── 整体页面 ─────────────────────────────────────────── */
.log-page {
  min-height: 100%;
  padding: 24px;
  background: #050f1f radial-gradient(ellipse 80% 40% at 50% 0%, #0a2a5240 0%, transparent 70%),
  repeating-linear-gradient(0deg, transparent, transparent 39px, #0e2d5510 40px),
  repeating-linear-gradient(90deg, transparent, transparent 39px, #0e2d5510 40px);
  font-family: '"Noto Sans SC", "PingFang SC", sans-serif',serif;
  box-sizing: border-box;

  /* 将 var(--accent, #00e5ff) 全部替换为 var(--card-accent, #00e5ff) */
  /* 同时在 :root 或 .log-page 中声明一个默认值 */
  --card-accent: #00e5ff;
}

/* ── 统计看板 ─────────────────────────────────────────── */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #071e3d 0%, #0a2d5a 100%);
  border: 1px solid var(--card-accent, #00e5ff);
  border-radius: 12px;
  overflow: hidden;
  cursor: default;
  transition: transform .25s, box-shadow .25s;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px -6px var(--card-accent, #00e5ff);
}
.stat-glow {
  position: absolute;
  top: -30%; right: -10%;
  width: 160px; height: 160px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--card-accent, #00e5ff) 0%, transparent 70%);
  opacity: .08;
  pointer-events: none;
}
.stat-icon  { font-size: 2.2rem; line-height: 1; }
.stat-label { font-size: .78rem; color: #90caf9; letter-spacing: .05em; margin: 0 0 4px; }
.stat-value { font-size: 1.9rem; font-weight: 700; color: var(--card-accent, #00e5ff);
  margin: 0; line-height: 1; font-family: 'Courier New', monospace;
  text-shadow: 0 0 12px var(--card-accent, #00e5ff); }

/* ── 图表行 ───────────────────────────────────────────── */
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}
.chart-card {
  background: linear-gradient(160deg, #071e3d 0%, #050f1f 100%);
  border: 1px solid #00e5ff25;
  border-radius: 12px;
  overflow: hidden;
  transition: border-color .3s;
}
.chart-card:hover { border-color: #00e5ff60; }
.card-header {
  padding: 14px 18px;
  font-size: .85rem;
  font-weight: 600;
  color: #90caf9;
  letter-spacing: .08em;
  border-bottom: 1px solid #00e5ff15;
  background: #071428;
}
.card-icon { margin-right: 6px; }
.chart-body { width: 100%; height: 220px; }

/* ── 表格卡片 ─────────────────────────────────────────── */
.table-card {
  background: linear-gradient(160deg, #071e3d 0%, #050f1f 100%);
  border: 1px solid #00e5ff25;
  border-radius: 12px;
  overflow: hidden;
  padding: 16px;
}

/* ── 搜索栏 ───────────────────────────────────────────── */
.search-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

/* 公共科技感输入框覆盖 */
.cyber-select, .cyber-datepicker {
  --el-border-color: #1a5080;
  --el-border-color-hover: #00e5ff;
  --el-fill-color-blank: #071428;
  --el-text-color-regular: #cfe8ff;
  --el-bg-color-overlay: #071e3d;
  --el-border-radius-base: 8px;
}
.cyber-select    { width: 160px; }
/* 原来 280px，现在两个独立的，改小 */
.cyber-datepicker { width: 150px; }

/* 核心修复：强制修改日期选择器的外壳样式 */
:deep(.cyber-datepicker .el-input__wrapper) {
  background-color: #071428 !important; /* 强制设为你的深蓝色 */
  box-shadow: 0 0 0 1px #1a5080 inset !important; /* 替换默认边框 */
}

/* 悬停时的边框发光效果 */
:deep(.cyber-datepicker .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #00e5ff inset !important;
}


/* 按钮 */
.btn-group { display: flex; gap: 8px; margin-left: auto; }
.btn-reset, .btn-search {
  border-radius: 8px;
  font-size: .83rem;
  padding: 8px 18px;
  cursor: pointer;
  transition: all .2s;
  border: 1px solid transparent;
  background: none;
  color: #cfe8ff;
}
.btn-reset  { border-color: #1a5080; }
.btn-reset:hover  { background: #0a2d5a; border-color: #00e5ff60; color: #00e5ff; }
.btn-search { background: linear-gradient(135deg, #00639a, #0057b7);
  border-color: #00e5ff60; color: #e0f7fa; }
.btn-search:hover { background: linear-gradient(135deg, #0096c7, #0077d4);
  box-shadow: 0 0 14px #00e5ff50; }





/* ── 表格本体 ─────────────────────────────────────────── */
.cyber-table { background: transparent !important; }

/* 取消奇偶行黑白相间 */
:deep(.el-table__row > td.el-table__cell)       { background: #071428 !important; }
:deep(.el-table__row:hover > td.el-table__cell) { background: #0a2d5a !important; }

.method-tag  { color: #00e5ff; font-family: 'Courier New', monospace; font-size: .82rem; }
.arrow       { color: #546e7a; margin: 0 4px; }
.method-name { color: #81d4fa; font-family: 'Courier New', monospace; font-size: .82rem; }

/* 响应时间徽章 */
.cost-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 99px;
  font-family: 'Courier New', monospace;
  font-size: .8rem;
  letter-spacing: .04em;
}
/* 替换原来仅有的 绿/橙色 */
.cost-badge { display:inline-block; padding:2px 10px; border-radius:99px;
  font-family:'Courier New',monospace; font-size:.8rem; letter-spacing:.04em; }
.lvl-fast { background:#003d1a; color:#00e676; border:1px solid #00e67640; }
.lvl-ok   { background:#003040; color:#00e5ff; border:1px solid #00e5ff40; }
.lvl-warn { background:#3d2000; color:#ffab40; border:1px solid #ffab4040; }
.lvl-slow { background:#3d0a00; color:#ff5252; border:1px solid #ff525240; }

/* ── 分页 ─────────────────────────────────────────────── */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
:deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-text-color: #90caf9;
  --el-pagination-button-color: #90caf9;
  --el-color-primary: #00e5ff;
}
:deep(.el-pagination .el-pager li) {
  background: transparent;
  color: #90caf9;
  border-radius: 6px;
}
:deep(.el-pagination .el-pager li.is-active) {
  background: #0a3d6b;
  color: #00e5ff;
  box-shadow: 0 0 8px #00e5ff50;
}
</style>