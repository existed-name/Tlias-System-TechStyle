<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { pageLoginLog, getLoginLogSummaryData } from '@/api/log/loginLog'

// 注册 ECharts 必要组件
use([CanvasRenderer, PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

// ===================== 响应式数据 =====================
// 搜索表单
const searchForm = reactive({ username: '', beginDate: '', endDate: '' })

// 分页
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

// 表格数据与加载状态
const tableData = ref([])
const tableLoading = ref(false)

// 卡片统计（动画用）
const displayCount = ref(0)   // 今日登录人数（动画显示值）
const displayFail = ref(0)    // 今日失败次数（动画显示值）

// ECharts 配置
const pieOption = ref({})
const barOption = ref({})

// ===================== 数字滚动动画 =====================
/**
 * 让数字从 0 缓慢滚动到目标值
 * @param {Ref} refVal - 要更新的 ref
 * @param {number} target - 目标数值
 * @param {number} duration - 动画时长(ms)
 */
function animateCount(refVal, target, duration = 1000) {
  const start = Date.now()
  const tick = () => {
    const elapsed = Date.now() - start
    const progress = Math.min(elapsed / duration, 1)
    // easeOutExpo 缓动，开始快、结束慢
    const ease = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress)
    refVal.value = Math.floor(ease * target)
    if (progress < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

// ===================== ECharts 配置构建 =====================
/** 构建饼图配置（绿=成功 红=失败，动画加载） */
function buildPieOption(data) {
  pieOption.value = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(10,20,40,0.92)',
      borderColor: '#00ffe7',
      borderWidth: 1,
      textStyle: { color: '#e0f7ff', fontSize: 13 },
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: 4,
      textStyle: { color: '#8ecfef', fontSize: 12 },
      icon: 'circle'
    },
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],   // 环形饼图
      center: ['50%', '46%'],
      animationType: 'expansion',
      animationDuration: 1500,
      animationEasing: 'cubicOut',
      label: {
        color: '#b0d8f0',
        fontSize: 12,
        formatter: '{b}\n{d}%'
      },
      labelLine: { lineStyle: { color: '#2a5f8f' } },
      data: data.map(item => ({
        name: item.name,
        value: item.value,
        itemStyle: {
          color: item.name === '成功'
              ? { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#00ff9d' }, { offset: 1, color: '#00b36b' }] }
              : { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#ff4d6d' }, { offset: 1, color: '#c1001f' }] },
          shadowBlur: 18,
          shadowColor: item.name === '成功' ? '#00ff9d66' : '#ff4d6d66'
        }
      }))
    }]
  }
}

/** 构建柱状图配置（多彩渐变色，缓慢均匀生长） */
function buildBarOption(data) {
  // Top5 对应的渐变色方案
  const gradients = [
    ['#00c6ff', '#0072ff'],
    ['#f7971e', '#ffd200'],
    ['#ee0979', '#ff6a00'],
    ['#11998e', '#38ef7d'],
    ['#8e2de2', '#4a00e0'],
  ]
  barOption.value = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(10,20,40,0.92)',
      borderColor: '#00ffe7',
      borderWidth: 1,
      textStyle: { color: '#e0f7ff' },
      formatter: (params) => `👤 ${params[0].name}<br/>❌ 失败 ${params[0].value} 次`
    },
    grid: { left: 12, right: 12, bottom: 10, top: 10, containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(d => d.username),
      axisLabel: { color: '#7bbdd4', fontSize: 11, rotate: 15 },
      axisLine: { lineStyle: { color: '#1a3a5c' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#7bbdd4', fontSize: 11 },
      splitLine: { lineStyle: { color: '#0d2a44', type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      barMaxWidth: 36,
      animationDuration: 2000,
      animationEasing: 'cubicOut',   // 每根柱子同时、均匀从 0 生长
      data: data.map((d, i) => ({
        value: d.count,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: {
            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: gradients[i % 5][0] },
              { offset: 1, color: gradients[i % 5][1] }
            ]
          },
          shadowBlur: 14,
          shadowColor: gradients[i % 5][0] + '88'
        }
      }))
    }]
  }
}

// ===================== 数据加载 =====================
/** 加载汇总卡片 + 图表数据 */
async function loadSummary() {
  const res = await getLoginLogSummaryData()
  if (res.code === 1) {
    const d = res.data
    animateCount(displayCount, d.todayLoginCount)
    animateCount(displayFail, d.todayFailCount)
    buildPieOption(d.totalSuccessFail)
    buildBarOption(d.failRank)
  }
}

/** 加载表格分页数据 */
async function loadTable() {
  tableLoading.value = true
  try {
    const res = await pageLoginLog({
      page: pagination.page,
      pageSize: pagination.pageSize,
      username: searchForm.username || undefined,
      beginDate: searchForm.beginDate || undefined,
      endDate: searchForm.endDate || undefined
    })
    if (res.code === 1) {
      tableData.value = res.data.rows
      pagination.total = res.data.total
    }
  } finally {
    tableLoading.value = false
  }
}

/** 搜索（重置到第 1 页） */
function handleSearch() {
  pagination.page = 1
  loadTable()
}

/** 重置表单并刷新 */
function handleReset() {
  searchForm.username = ''
  searchForm.beginDate = ''
  searchForm.endDate = ''
  pagination.page = 1
  loadTable()
}

/** 格式化登录时间，去掉中间的 T */
function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').slice(0, 19)
}

onMounted(() => {
  loadSummary()
  loadTable()
})
</script>

<template>
  <div class="login-log-page">

    <!-- ========== 顶部统计行 ========== -->
    <div class="top-row">

      <!-- 今日登录人数 -->
      <div class="stat-card">
        <div class="stat-glow green"></div>
        <div class="stat-icon">🟢</div>
        <div class="stat-label">今日登录人数</div>
        <div class="stat-num green-num">{{ displayCount }}</div>
        <div class="stat-sub">Today Active Users</div>
      </div>

      <!-- 今日失败次数 -->
      <div class="stat-card">
        <div class="stat-glow red"></div>
        <div class="stat-icon">🔴</div>
        <div class="stat-label">今日登录失败</div>
        <div class="stat-num red-num">{{ displayFail }}</div>
        <div class="stat-sub">Today Fail Count</div>
      </div>

      <!-- 累计成功/失败饼图 -->
      <div class="chart-card">
        <div class="chart-title">📊 累计登录成功 / 失败</div>
        <v-chart class="echart" :option="pieOption" autoresize />
      </div>

      <!-- 失败 Top5 柱状图 -->
      <div class="chart-card wide">
        <div class="chart-title">🏆 登录失败 Top 5 用户</div>
        <v-chart class="echart" :option="barOption" autoresize />
      </div>

    </div>

    <!-- ========== 搜索栏 ========== -->
    <div class="search-bar">
      <div class="search-item">
        <span class="search-label">👤 用户名</span>
        <el-input
            v-model="searchForm.username"
            placeholder="模糊匹配"
            clearable
            class="cyber-input"
        />
      </div>
      <div class="search-item">
        <span class="search-label">📅 起始日期</span>
        <el-date-picker
            v-model="searchForm.beginDate"
            type="date"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
            class="cyber-input"
        />
      </div>
      <div class="search-item">
        <span class="search-label">📅 截止日期</span>
        <el-date-picker
            v-model="searchForm.endDate"
            type="date"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="cyber-input"
        />
      </div>
      <el-button class="btn-search" @click="handleSearch">🔍 搜索</el-button>
      <el-button class="btn-reset" @click="handleReset">↺ 重置</el-button>
    </div>

    <!-- ========== 日志表格 ========== -->
    <div class="table-wrap">
      <el-table
          :data="tableData"
          v-loading="tableLoading"
          element-loading-background="rgba(5,15,35,0.85)"
          element-loading-text="数据加载中..."
          element-loading-svg-view-box="-10, -10, 50, 50"
          highlight-current-row
          class="cyber-table"
          :header-cell-style="headerStyle"
          :row-class-name="rowClassName"
      >
        <!-- 序号 -->
        <el-table-column type="index" label="#" width="58" align="center">
          <template #default="{ $index }">
            <span class="col-index">{{ (pagination.page - 1) * pagination.pageSize + $index + 1 }}</span>
          </template>
        </el-table-column>

        <!-- 用户名 -->
        <el-table-column prop="username" label="用户名" min-width="130">
          <template #default="{ row }">
            <span class="col-username">👤 {{ row.username }}</span>
          </template>
        </el-table-column>

        <!-- 登录时间 -->
        <el-table-column label="登录时间" min-width="175">
          <template #default="{ row }">
            <span class="col-time">🕐 {{ formatTime(row.loginTime) }}</span>
          </template>
        </el-table-column>

        <!-- 状态 -->
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <span v-if="row.isSuccess === 1" class="badge success">✅ 成功</span>
            <span v-else class="badge fail">❌ 失败</span>
          </template>
        </el-table-column>

        <!-- 耗时 -->
        <el-table-column label="耗时 (ms)" width="120" align="center">
          <template #default="{ row }">
            <span class="col-cost">⚡ {{ row.costTime }}</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页器 -->
      <div class="pagination-wrap">
        <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            class="cyber-pager"
            @current-change="loadTable"
            @size-change="handleSearch"
        />
      </div>
    </div>

  </div>
</template>

<style scoped>
/* ============================
   CSS 变量 & 页面基础
   ============================ */
.login-log-page {
  --c-bg: #050f23;
  --c-card: #091828;
  --c-border: #0d3a5c;
  --c-border-glow: #00ffe7;
  --c-text: #c8e8f8;
  --c-text-dim: #4a7fa0;
  --c-green: #00ff9d;
  --c-red: #ff4d6d;
  --c-cyan: #00e5ff;
  --c-blue: #0072ff;

  min-height: 100vh;
  background: var(--c-bg);
  padding: 20px 24px 32px;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
  color: var(--c-text);
}

/* ============================
   顶部统计行
   ============================ */
.top-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

/* 卡片公共样式 */
.stat-card, .chart-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: 12px;
  position: relative;
  overflow: hidden;
  transition: border-color .3s, box-shadow .3s;
}
.stat-card:hover, .chart-card:hover {
  border-color: var(--c-border-glow);
  box-shadow: 0 0 22px #00ffe733;
}

/* 数字卡片 */
.stat-card {
  flex: 0 0 170px;
  padding: 22px 18px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

/* 光晕背景 */
.stat-glow {
  position: absolute;
  width: 110px; height: 110px;
  border-radius: 50%;
  filter: blur(40px);
  opacity: .18;
  top: -20px; left: 50%;
  transform: translateX(-50%);
  pointer-events: none;
}
.stat-glow.green { background: var(--c-green); }
.stat-glow.red   { background: var(--c-red);   }

.stat-icon { font-size: 22px; }

.stat-label {
  font-size: 12px;
  color: var(--c-text-dim);
  letter-spacing: .06em;
  text-transform: uppercase;
}

.stat-num {
  font-size: 42px;
  font-weight: 700;
  letter-spacing: -.02em;
  line-height: 1;
  text-shadow: 0 0 18px currentColor;
  transition: color .2s;
}
.green-num { color: var(--c-green); }
.red-num   { color: var(--c-red);   }

.stat-sub {
  font-size: 10px;
  color: #1e4d6b;
  letter-spacing: .08em;
}

/* 图表卡片 */
.chart-card {
  flex: 1 1 220px;
  min-width: 200px;
  padding: 12px 14px 8px;
  display: flex;
  flex-direction: column;
}
.chart-card.wide { flex: 1.6 1 300px; }

.chart-title {
  font-size: 12px;
  color: var(--c-text-dim);
  letter-spacing: .05em;
  margin-bottom: 4px;
}

.echart { flex: 1; min-height: 170px; }

/* ============================
   搜索栏
   ============================ */
.search-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: 10px;
  padding: 14px 18px;
  margin-bottom: 16px;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-label {
  font-size: 12px;
  color: var(--c-text-dim);
  white-space: nowrap;
}

/* 覆盖 ElementPlus 输入框为深色风格 */
:deep(.cyber-input .el-input__wrapper),
:deep(.cyber-input .el-date-editor .el-input__wrapper) {
  background: #071320 !important;
  border: 1px solid #0d3a5c !important;
  box-shadow: none !important;
  border-radius: 6px;
}
:deep(.cyber-input .el-input__inner),
:deep(.cyber-input .el-date-editor input) {
  color: var(--c-cyan) !important;
  font-family: inherit;
  font-size: 13px;
}
:deep(.cyber-input .el-input__wrapper:hover),
:deep(.cyber-input .el-date-editor .el-input__wrapper:hover) {
  border-color: var(--c-border-glow) !important;
}

.btn-search {
  background: linear-gradient(135deg, #0057b8, #00b4d8);
  border: none;
  color: #fff;
  font-family: inherit;
  font-size: 13px;
  border-radius: 6px;
  padding: 8px 20px;
  cursor: pointer;
  box-shadow: 0 0 14px #00b4d855;
  transition: box-shadow .25s, transform .15s;
}
.btn-search:hover {
  box-shadow: 0 0 26px #00e5ff99;
  transform: translateY(-1px);
}

.btn-reset {
  background: transparent;
  border: 1px solid #0d3a5c;
  color: var(--c-text-dim);
  font-family: inherit;
  font-size: 13px;
  border-radius: 6px;
  padding: 8px 18px;
  cursor: pointer;
  transition: border-color .25s, color .25s;
}
.btn-reset:hover {
  border-color: var(--c-cyan);
  color: var(--c-cyan);
}

/* ============================
   表格区域
   ============================ */
.table-wrap {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 0 40px #00102a;
}

/* 覆盖 ElementPlus table 样式 */
:deep(.cyber-table) {
  background: transparent !important;
  color: var(--c-text);
  font-family: inherit;
  font-size: 13px;
}
:deep(.cyber-table .el-table__header-wrapper) {
  background: #030c1e;
}
:deep(.cyber-table .el-table__header th.el-table__cell) {
  background: #030c1e !important;
  color: var(--c-cyan);
  font-weight: 700;
  letter-spacing: .06em;
  font-size: 12px;
  border-bottom: 1px solid #0d3a5c;
}
:deep(.cyber-table .el-table__row) {
  background: transparent;
  transition: background .18s;
}
:deep(.cyber-table .el-table__row:hover > td),
:deep(.cyber-table .el-table__row.current-row > td) {
  background: rgba(0, 220, 255, 0.07) !important;
  box-shadow: inset 0 0 0 1px rgba(0,220,255,.15);
}
:deep(.cyber-table .el-table__cell) {
  border-bottom: 1px solid #0b2a42 !important;
  padding: 10px 0;
}
:deep(.cyber-table .el-table__body-wrapper) {
  background: transparent;
}

/* 列字段颜色 */
.col-index {
  color: #2a6a9e;
  font-weight: 600;
}
.col-username {
  color: #48cfff;
  font-weight: 500;
}
.col-time {
  color: #a0c8e8;
  font-size: 12.5px;
}
.col-cost {
  color: #f7c948;
  font-weight: 600;
}

/* 状态徽标 */
.badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: .04em;
}
.badge.success {
  color: var(--c-green);
  background: rgba(0, 255, 157, 0.1);
  border: 1px solid rgba(0, 255, 157, 0.3);
  text-shadow: 0 0 8px var(--c-green);
}
.badge.fail {
  color: var(--c-red);
  background: rgba(255, 77, 109, 0.1);
  border: 1px solid rgba(255, 77, 109, 0.3);
  text-shadow: 0 0 8px var(--c-red);
}

/* 分页器 */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 12px 18px;
  background: #040e20;
  border-top: 1px solid #0b2a42;
}
:deep(.cyber-pager .el-pagination__total),
:deep(.cyber-pager .el-pagination__sizes),
:deep(.cyber-pager .el-pagination__jump) {
  color: var(--c-text-dim);
  font-family: inherit;
}
:deep(.cyber-pager .btn-prev),
:deep(.cyber-pager .btn-next),
:deep(.cyber-pager .el-pager li) {
  background: #071320 !important;
  color: var(--c-text-dim) !important;
  border: 1px solid #0d3a5c;
  border-radius: 4px;
}
:deep(.cyber-pager .el-pager li.is-active) {
  background: linear-gradient(135deg,#0057b8,#00b4d8) !important;
  color: #fff !important;
  border-color: transparent;
  box-shadow: 0 0 10px #00b4d855;
}
:deep(.cyber-pager .btn-prev:hover),
:deep(.cyber-pager .btn-next:hover),
:deep(.cyber-pager .el-pager li:hover) {
  border-color: var(--c-cyan) !important;
  color: var(--c-cyan) !important;
}
</style>