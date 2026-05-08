<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getEmpGenderData, getEmpJobData } from '@/api/report/emp'

// ─── ECharts 实例引用 ───────────────────────────────────────────
let genderChart = null
let jobChart = null

// ─── 统计摘要数字（用于顶部卡片展示） ──────────────────────────
const totalEmp = ref(0)
const maleCount = ref(0)
const femaleCount = ref(0)
const jobTypeCount = ref(0)

// ─── 初始化员工性别饼图 ─────────────────────────────────────────
const initGenderChart = async () => {
  const res = await getEmpGenderData()
  if (res.code !== 1) return

  // 这里的 data 直接就是 List<VO> [{ name, value }, ...]
  const data = res.data || []

  // 重置统计值，避免重复调用时叠加
  totalEmp.value = 0

  // 计算摘要数字
  // 修改后（带滚动动画赋值）
  let total = 0
  data.forEach(item => {
    total += item.value
    if (item.name.includes('男')) animateCount(maleCount, item.value)
    if (item.name.includes('女')) animateCount(femaleCount, item.value)
  })
  animateCount(totalEmp, total)

  genderChart = echarts.init(document.getElementById('genderChart'))
  genderChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 人 ({d}%)' },
    legend: {
      bottom: '5%',
      left: 'center',
      textStyle: { color: '#94a3b8', fontSize: 13 }
    },
    series: [{
      type: 'pie',
      radius: ['45%', '72%'], // 环形饼图
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#0f172a',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 16, fontWeight: 'bold', color: '#e2e8f0' }
      },
      data,
      color: ['#f472b6', '#38bdf8' ] // 女左男右, 天蓝对应男性，玫瑰粉对应女性
    }]
  })
}

// ─── 初始化员工职位柱状图 ───────────────────────────────────────
const initJobChart = async () => {
  const res = await getEmpJobData()
  if (res.code !== 1) return

  // 1. 获取后端返回的原始数组
  const rawData = res.data || []

  // 2. 核心：通过 map 转换数据格式
  // 提取所有的职位名称作为 X 轴坐标
  const jobList = rawData.map(item => item.job)
  // 提取所有的统计数字作为 Y 轴数据
  const dataList = rawData.map(item => item.count)

  // 3. 赋值给你的响应式变量（假设你之前定义了这些 ref）
  animateCount(jobTypeCount, jobList.length, 600)

  // 4. 接下来把 jobList 和 dataList 喂给 ECharts 的 setOption 即可
  jobChart = echarts.init(document.getElementById('jobChart'))
  jobChart.setOption({
    // 在 setOption 顶层加上多色数组（与 series 同级）
    color: [
      new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#38bdf8'},{offset:1,color:'#0369a1'}]),
      new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#a78bfa'},{offset:1,color:'#6d28d9'}]),
      new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#34d399'},{offset:1,color:'#059669'}]),
      new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#fb923c'},{offset:1,color:'#c2410c'}]),
      new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#f472b6'},{offset:1,color:'#be185d'}]),
      new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#facc15'},{offset:1,color:'#b45309'}]),
    ],

    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: jobList,
      axisLine: { lineStyle: { color: '#334155' } },
      axisLabel: { color: '#94a3b8', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#1e293b' } },
      axisLabel: { color: '#94a3b8' }
    },

    series: [{
      type: 'bar',
      data: dataList,
      barMaxWidth: 48,
      // 每根柱子独立颜色（ECharts colorBy: 'data' + 全局 color 数组）
      colorBy: 'data',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        // 由下方 color 数组自动轮转，无需手写渐变
      },
      emphasis: {
        itemStyle: { opacity: 0.85 }
      },
      label: { show: true, position: 'top', color: '#94a3b8', fontSize: 12 }
    }],



  })
}

// ─── 窗口 resize 时，图表自适应 ────────────────────────────────
const handleResize = () => {
  genderChart?.resize()
  jobChart?.resize()
}

/**
 * 数字滚动动画
 * @param {Ref} refVal   - 要更新的 ref
 * @param {number} target - 目标值
 * @param {number} duration - 动画时长 ms，默认 800
 */
const animateCount = (refVal, target, duration = 5000 ) => {
  const start = performance.now()
  const tick = (now) => {
    const progress = Math.min((now - start) / duration, 1)
    // easeOutExpo：先快后慢，更有冲击感
    const ease = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress)
    refVal.value = Math.round(ease * target)
    if (progress < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}


onMounted(() => {
  initGenderChart()
  initJobChart()
  window.addEventListener('resize', handleResize)
})

// ─── 组件卸载时释放资源，防止内存泄漏 ─────────────────────────
onUnmounted(() => {
  genderChart?.dispose()
  jobChart?.dispose()
  window.removeEventListener('resize', handleResize)
})

</script>

<template>
  <div class="report-page">

    <!-- ── 顶部标题 ── -->
    <div class="page-header">
      <div class="page-title">
        <span class="title-accent"></span>
        <h2>员工信息统计</h2>
      </div>
      <p class="page-sub">Employee Analytics Dashboard</p>
    </div>

    <!-- ── 摘要卡片区 ── -->
    <div class="stat-cards">
      <div class="stat-card">
        <span class="stat-icon">👥</span>
        <div>
          <p class="stat-label">员工总数</p>
          <p class="stat-value">{{ totalEmp }}</p>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon">🧑</span>
        <div>
          <p class="stat-label">男性员工</p>
          <p class="stat-value indigo">{{ maleCount }}</p>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon">👩</span>
        <div>
          <p class="stat-label">女性员工</p>
          <p class="stat-value pink">{{ femaleCount }}</p>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon">💼</span>
        <div>
          <p class="stat-label">职位种类</p>
          <p class="stat-value violet">{{ jobTypeCount }}</p>
        </div>
      </div>
    </div>

    <!-- ── 图表区 ── -->
    <div class="charts-row">

      <div class="chart-card">
        <div class="chart-card-header">
          <div class="dot pink"></div>
          <span>员工性别统计</span>
          <span class="chart-badge">Pie Chart</span>
        </div>
        <!-- 饼图容器，必须设定高度 ECharts 才能正常渲染 -->
        <div id="genderChart" class="chart-body"></div>
      </div>

      <div class="chart-card">
        <div class="chart-card-header">
          <div class="dot indigo"></div>
          <span>员工职位分布</span>
          <span class="chart-badge">Bar Chart</span>
        </div>
        <!-- 柱状图容器 -->
        <div id="jobChart" class="chart-body"></div>
      </div>

    </div>
  </div>
</template>

<style scoped>

/* ── 全局基础 ──────────────────────────────────────── */
.report-page {
  min-height: 100%;
  padding: 32px 36px;
  background: #060d18;
  /* 双层网格：主格 40px + 细格 8px，营造电路板质感 */
  background-image:
      linear-gradient(rgba(34,211,238,.06) 1px, transparent 1px),
      linear-gradient(90deg, rgba(34,211,238,.06) 1px, transparent 1px),
      linear-gradient(rgba(34,211,238,.02) 1px, transparent 1px),
      linear-gradient(90deg, rgba(34,211,238,.02) 1px, transparent 1px);
  background-size: 40px 40px, 40px 40px, 8px 8px, 8px 8px;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  color: #e2e8f0;
}

/* ── 顶部标题 ──────────────────────────────────────── */
.page-header { margin-bottom: 28px; }

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 装饰条：更强的荧光脉冲 */
.title-accent {
  display: inline-block;
  width: 4px;
  height: 24px;
  border-radius: 2px;
  background: linear-gradient(180deg, #22d3ee, #6366f1);
  animation: pulse-bar 2s ease-in-out infinite;
}
@keyframes pulse-bar {
  0%,100% { box-shadow: 0 0 6px #22d3ee, 0 0 12px #22d3ee66; }
  50%      { box-shadow: 0 0 18px #22d3ee, 0 0 40px #6366f188, 0 0 60px #22d3ee33; }
}

.page-title h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1px;
  background: linear-gradient(90deg, #e2e8f0 40%, #38bdf8 80%, #818cf8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-sub {
  margin: 6px 0 0 14px;
  font-size: 11px;
  color: #22d3ee;
  letter-spacing: 3px;
  text-transform: uppercase;
  opacity: 0.6;
}

/* ── 摘要卡片 ──────────────────────────────────────── */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  /* 半透明玻璃态底 */
  background: linear-gradient(135deg, #0d1f35cc, #112340cc);
  backdrop-filter: blur(12px);
  border: 1px solid #1e3f60;
  border-radius: 16px;
  animation: fade-up 0.5s ease both;
  transition: border-color 0.25s, box-shadow 0.25s, transform 0.2s;
  position: relative;
  overflow: hidden;
}
/* 卡片顶部荧光高光线 */
.stat-card::before {
  content: '';
  position: absolute;
  top: 0; left: 10%; right: 10%;
  height: 1px;
  background: linear-gradient(90deg, transparent, #22d3ee88, transparent);
}

.stat-card:nth-child(1) { animation-delay: 0.05s; }
.stat-card:nth-child(2) { animation-delay: 0.12s; }
.stat-card:nth-child(3) { animation-delay: 0.19s; }
.stat-card:nth-child(4) { animation-delay: 0.26s; }

@keyframes fade-up {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}

.stat-card:hover {
  border-color: #22d3ee;
  box-shadow: 0 0 24px #22d3ee22, 0 0 48px #22d3ee0a, inset 0 0 24px #22d3ee08;
  transform: translateY(-4px);
}

.stat-icon { font-size: 28px; line-height: 1; }

.stat-label {
  margin: 0 0 4px;
  font-size: 12px;
  color: #4b82a8; /* 比之前亮，避免看不清 */
}

/* 数字：等宽字体让滚动更稳 */
.stat-value {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #f1f5f9;
  font-variant-numeric: tabular-nums;
  font-family: 'Courier New', monospace;
}
.stat-value.indigo { color: #818cf8; text-shadow: 0 0 12px #818cf855; }
.stat-value.pink   { color: #f472b6; text-shadow: 0 0 12px #f472b655; }
.stat-value.violet { color: #a78bfa; text-shadow: 0 0 12px #a78bfa55; }

/* ── 图表卡片 ──────────────────────────────────────── */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1.6fr;
  gap: 20px;
  animation: fade-up 0.5s 0.3s ease both;
}

.chart-card {
  background: linear-gradient(135deg, #0d1f35cc, #112340cc);
  backdrop-filter: blur(12px);
  border: 1px solid #1e3f60;
  border-radius: 16px;
  padding: 20px 24px;
  position: relative;
  overflow: hidden;
  transition: border-color 0.25s, box-shadow 0.25s;
}
/* 图表卡片顶部荧光线 */
.chart-card::before {
  content: '';
  position: absolute;
  top: 0; left: 10%; right: 10%;
  height: 1px;
  background: linear-gradient(90deg, transparent, #38bdf866, transparent);
}
.chart-card:hover {
  border-color: #2e5578;
  box-shadow: 0 8px 40px #00000050, 0 0 20px #22d3ee0a;
}

.chart-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 600;
  color: #cbd5e1;
}

/* 圆点持续呼吸 + 荧光 */
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  animation: dot-pulse 2s ease-in-out infinite;
}
.dot.pink   { background: #f472b6; box-shadow: 0 0 8px #f472b6; }
.dot.indigo { background: #38bdf8; box-shadow: 0 0 8px #38bdf8; }

@keyframes dot-pulse {
  0%,100% { opacity: 1;   transform: scale(1);   }
  50%      { opacity: 0.5; transform: scale(0.75); }
}

.chart-badge {
  margin-left: auto;
  padding: 2px 10px;
  font-size: 11px;
  color: #38bdf8;
  background: #071828;
  border: 1px solid #1e3f60;
  border-radius: 20px;
  letter-spacing: 0.5px;
  /* badge 也加轻微荧光 */
  box-shadow: 0 0 8px #38bdf822;
}

/* ECharts 容器必须有明确高度 */
.chart-body {
  width: 100%;
  height: 360px;
}

/* ── 响应式 ──────────────────────────────────────── */
@media (max-width: 960px) {
  .stat-cards { grid-template-columns: repeat(2, 1fr); }
  .charts-row { grid-template-columns: 1fr; }
}

</style>