<!--<script setup>-->

<!--</script>-->

<!--<template>-->
<!--  学员信息统计-->
<!--</template>-->

<!--<style scoped>-->

<!--</style>-->











<script setup>
import { onMounted, onUnmounted, ref, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getStudentDegreeData, getStudentCountData, getStudentSummaryData } from '@/api/report/student'

// 持有图表实例，用于 resize 和销毁
let degreeChart = null
let countChart = null

// 学员综合数据（响应式）
const summary = ref(null)

// 数字滚动动画：从 0 滚到目标值
const animateCount = (el, target, duration = 1200) => {
  const start = performance.now()
  const update = (now) => {
    const progress = Math.min((now - start) / duration, 1)
    // easeOutQuart 缓动，结尾减速更自然
    const ease = 1 - Math.pow(1 - progress, 4)
    el.textContent = Math.round(ease * target)
    if (progress < 1) requestAnimationFrame(update)
  }
  requestAnimationFrame(update)
}

/** 初始化学历饼状图 */
// 后端返回：[{"name": "本科", "value": 182}, ...]
const initDegreeChart = (rawData) => {
  // 这里的 data 直接就是 List<VO>，且字段名符合 Echarts 默认的 name/value
  // 直接赋值即可，逻辑非常简洁
  const data = rawData || []

  degreeChart = echarts.init(document.getElementById('degreeChart'))
  degreeChart.setOption({
    backgroundColor: 'transparent',
    title: {
      text: '学员学历分布',
      left: 'center',
      top: 16,
      textStyle: { color: '#a8f0e8', fontSize: 15, fontWeight: 600, fontFamily: 'monospace' }
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(10,30,60,0.92)',
      borderColor: '#00ffe7',
      borderWidth: 1,
      textStyle: { color: '#e0fffa' },
      formatter: '{b}: {c} 人 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 12,
      bottom: 12,
      textStyle: { color: '#7ecfcf', fontSize: 12 }
    },
    series: [{
      type: 'pie',
      radius: ['38%', '68%'], // 环形饼图更有科技感
      center: ['55%', '52%'],
      data,
      // 高亮时扩大扇区
      emphasis: {
        itemStyle: {
          shadowBlur: 20,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0,255,231,0.5)'
        },
        scaleSize: 8
      },
      label: {
        color: '#b0f0e8',
        fontSize: 12,
        formatter: '{b}\n{d}%'
      },
      labelLine: { lineStyle: { color: '#2dd4c0' } },
      // 科技感配色
      color: ['#00ffe7', '#00bfff', '#7b61ff', '#ff6eb4', '#36d68a', '#ffbe5c']
    }]
  })
}

/** 初始化班级柱状图 */
// 修改点：参数由解构对象 { clazzList, dataList } 改为原始数组 data
// 后端返回：[{"clazzName": "Java就业100期", "studentCount": 77}, ...]
const initCountChart = ( rawData ) => {
  const data = rawData || []

  // 1. 统一在前端进行转换，解耦字段名
  const clazzList = data.map(item => item.clazzName)
  const dataList = data.map(item => item.studentCount)

  countChart = echarts.init(document.getElementById('countChart'))
  countChart.setOption({
    backgroundColor: 'transparent',
    title: {
      text: '班级人数统计',
      left: 'center',
      top: 16,
      textStyle: { color: '#a8f0e8', fontSize: 15, fontWeight: 600, fontFamily: 'monospace' }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(10,30,60,0.92)',
      borderColor: '#00ffe7',
      borderWidth: 1,
      textStyle: { color: '#e0fffa' },
      axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(0,255,231,0.06)' } }
    },
    grid: { left: '4%', right: '4%', bottom: '14%', containLabel: true },
    xAxis: {
      type: 'category',
      data: clazzList,
      axisLabel: {
        color: '#7ecfcf',
        fontSize: 11,
        rotate: 20,              // 班级名较长时旋转避免重叠
        interval: 0
      },
      axisLine: { lineStyle: { color: '#1e4a5a' } },
      axisTick: { lineStyle: { color: '#1e4a5a' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#7ecfcf', fontSize: 11 },
      splitLine: { lineStyle: { color: '#122535', type: 'dashed' } },
      axisLine: { lineStyle: { color: '#1e4a5a' } }
    },
    series: [{
      type: 'bar',
      data: dataList,
      barMaxWidth: 36,
      // 渐变色柱子
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#00ffe7' },
          { offset: 1, color: '#0077cc' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ffffff' },
            { offset: 1, color: '#00ffe7' }
          ])
        }
      },
      // 柱顶数值标签
      label: {
        show: true,
        position: 'top',
        color: '#a8f0e8',
        fontSize: 11,
        fontFamily: 'monospace'
      }
    }]
  })
}

/** 窗口 resize 时让图表自适应 */
const handleResize = () => {
  degreeChart?.resize()
  countChart?.resize()
}

onMounted(async () => {
  // 并行请求，提升加载速度
  const [ degreeRes, countRes, summaryRes ] = await Promise.all([
    getStudentDegreeData(),
    getStudentCountData(),
    getStudentSummaryData()
  ])
  if (degreeRes.code === 1) initDegreeChart(degreeRes.data)
  if (countRes.code === 1) initCountChart(countRes.data)
  // 新增：赋值后触发数字滚动
  if (summaryRes.code === 1) {
    summary.value = summaryRes.data
    // 等 DOM 更新后再取元素执行动画
    await nextTick()
    document.querySelectorAll('.stat-num[data-target]').forEach(el => {
      animateCount(el, Number(el.dataset.target))
    })
  }

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例，防止内存泄漏
  degreeChart?.dispose()
  countChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <div class="stu-report-page">

    <!-- 页面标题 -->
    <div class="page-header">
      <span class="header-dot"></span>
      <span class="header-title">学员信息统计</span>
      <span class="header-line"></span>
    </div>

    <!-- 学员综合数据卡片组 -->
    <div v-if="summary" class="summary-grid">

      <!-- 卡片：学生总数 -->
      <div class="stat-card">
        <div class="stat-icon">🧑‍🎓</div>
        <div class="stat-num" :data-target="summary.totalStudent">0</div>
        <div class="stat-label">学生总数</div>
      </div>

      <!-- 卡片：未分配班级 -->
      <div class="stat-card warn">
        <div class="stat-icon">⚠️</div>
        <div class="stat-num" :data-target="summary.studentHaveNoClazz">0</div>
        <div class="stat-label">未分配班级的学生</div>
      </div>

      <!-- 卡片：班级总数 -->
      <div class="stat-card">
        <div class="stat-icon">🏫</div>
        <div class="stat-num" :data-target="summary.totalClazz">0</div>
        <div class="stat-label">班级总数</div>
      </div>

      <!-- 卡片：在读班级 -->
      <div class="stat-card active">
        <div class="stat-icon">✍️</div>
        <div class="stat-num" :data-target="summary.openClazzCount">0</div>
        <div class="stat-label">在读班级</div>
      </div>

      <!-- 卡片：已结课 -->
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-num" :data-target="summary.endClazzCount">0</div>
        <div class="stat-label">已结课班级</div>
      </div>

      <!-- 卡片：未开班 -->
      <div class="stat-card">
        <div class="stat-icon">⏳</div>
        <div class="stat-num" :data-target="summary.notStartClazzCount">0</div>
        <div class="stat-label">未开班级</div>
      </div>

    </div>

    <!-- 图表区域 -->
    <div class="chart-grid">
      <!-- 饼状图卡片 -->
      <div class="chart-card">
        <div class="card-scan-line"></div>   <!-- 扫描线装饰 -->
        <div class="card-corner tl"></div>
        <div class="card-corner tr"></div>
        <div class="card-corner bl"></div>
        <div class="card-corner br"></div>
        <div id="degreeChart" class="chart-box"></div>
      </div>

      <!-- 柱状图卡片 -->
      <div class="chart-card">
        <div class="card-scan-line"></div>
        <div class="card-corner tl"></div>
        <div class="card-corner tr"></div>
        <div class="card-corner bl"></div>
        <div class="card-corner br"></div>
        <div id="countChart" class="chart-box"></div>
      </div>
    </div>

    <!-- 底部说明 -->
    <div class="footer-tips">
      <span class="tip-icon">◈</span> 统计学员的学历比例，以饼状图形式展示
      <span class="tip-sep">｜</span>
      <span class="tip-icon">◈</span> 统计每个班级的学生数量，以柱状图展示
    </div>

    <!-- 背景网格装饰 -->
    <div class="bg-grid" aria-hidden="true"></div>
  </div>
</template>

<style scoped>
/* ─── CSS 变量 ─────────────────────────────── */
.stu-report-page {
  --bg-deep:    #0b1e2d;        /* 主背景：深靛青（调亮版） */
  --bg-card:    rgba(8,32,50,0.82);
  --glow:       #00ffe7;        /* 主发光色：青绿 */
  --glow-blue:  #00bfff;
  --glow-soft:  rgba(0,255,231,0.18);
  --text-main:  #d4f5f0;
  --text-sub:   #5fa8b0;
  --border:     rgba(0,255,231,0.28);

  position: relative;
  min-height: 100%;
  padding: 28px 32px 40px;
  background: var(--bg-deep);
  overflow: hidden;
  font-family: 'Courier New', monospace;
}

/* ─── 背景网格 ────────────────────────────── */
.bg-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
      linear-gradient(rgba(0,255,231,0.04) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0,255,231,0.04) 1px, transparent 1px);
  background-size: 40px 40px;
  z-index: 0;
}

/* ─── 页面标题 ─────────────────────────────── */
.page-header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}
.header-dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  background: var(--glow);
  box-shadow: 0 0 10px var(--glow), 0 0 24px var(--glow);
  animation: pulse-dot 2.2s ease-in-out infinite;
}
.header-title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 3px;
  color: var(--text-main);
  text-shadow: 0 0 14px var(--glow);
}
.header-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, var(--glow), transparent);
  opacity: 0.4;
}

/* ─── 图表网格布局 ─────────────────────────── */
.chart-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

/* ─── 图表卡片 ─────────────────────────────── */
.chart-card {
  position: relative;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 12px;
  /* 发光描边 - 替代沉重阴影 */
  box-shadow:
      0 0 0 1px rgba(0,255,231,0.08),
      0 0 24px rgba(0,255,231,0.10),
      inset 0 0 40px rgba(0,30,50,0.6);
  overflow: hidden;
  transition: box-shadow 0.4s ease, border-color 0.4s ease;
}
.chart-card:hover {
  border-color: rgba(0,255,231,0.65);
  box-shadow:
      0 0 0 1px rgba(0,255,231,0.2),
      0 0 40px rgba(0,255,231,0.22),
      inset 0 0 40px rgba(0,30,50,0.6);
}

/* 四角装饰（科技感） */
.card-corner {
  position: absolute;
  width: 14px; height: 14px;
  border-color: var(--glow);
  border-style: solid;
  opacity: 0.7;
  z-index: 2;
}
.card-corner.tl { top: 8px; left: 8px;  border-width: 2px 0 0 2px; border-radius: 4px 0 0 0; }
.card-corner.tr { top: 8px; right: 8px; border-width: 2px 2px 0 0; border-radius: 0 4px 0 0; }
.card-corner.bl { bottom: 8px; left: 8px;  border-width: 0 0 2px 2px; border-radius: 0 0 0 4px; }
.card-corner.br { bottom: 8px; right: 8px; border-width: 0 2px 2px 0; border-radius: 0 0 4px 0; }

/* 扫描线动画 */
.card-scan-line {
  position: absolute;
  left: 0; right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--glow), transparent);
  opacity: 0;
  animation: scan 4s ease-in-out infinite;
  z-index: 3;
  pointer-events: none;
}
.chart-card:nth-child(2) .card-scan-line {
  animation-delay: 2s; /* 两卡片错开扫描 */
}

/* ─── ECharts 容器高度 ─────────────────────── */
.chart-box {
  width: 100%;
  height: 400px;  /* 必须设固定高度，否则 ECharts 不渲染 */
}

/* ─── 底部说明 ─────────────────────────────── */
.footer-tips {
  position: relative;
  z-index: 1;
  margin-top: 22px;
  text-align: center;
  font-size: 12px;
  color: var(--text-sub);
  letter-spacing: 1px;
}
.tip-icon { color: var(--glow); margin-right: 4px; }
.tip-sep  { margin: 0 14px; opacity: 0.4; }

/* ─── 关键帧动画 ───────────────────────────── */
@keyframes pulse-dot {
  0%, 100% { opacity: 1; box-shadow: 0 0 10px var(--glow), 0 0 24px var(--glow); }
  50%       { opacity: 0.5; box-shadow: 0 0 4px var(--glow); }
}

@keyframes scan {
  0%   { top: 0%;   opacity: 0; }
  10%  { opacity: 0.7; }
  90%  { opacity: 0.7; }
  100% { top: 100%; opacity: 0; }
}

/* ─── 响应式：小屏单列 ─────────────────────── */
@media (max-width: 900px) {
  .chart-grid { grid-template-columns: 1fr; }
}

/* ─── 综合数据卡片组 ───────────────────────── */
.summary-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(6, 1fr);  /* 六列等宽 */
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  position: relative;
  background: rgba(0, 40, 60, 0.75);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 20px 12px 16px;
  text-align: center;
  overflow: hidden;
  transition: border-color 0.35s, box-shadow 0.35s;
  /* 底部彩线：默认青绿 */
  border-bottom: 2px solid rgba(0, 255, 231, 0.5);
}
.stat-card::before {
  /* 悬停时的内发光背景 */
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 50% 0%, rgba(0,255,231,0.06), transparent 70%);
  opacity: 0;
  transition: opacity 0.35s;
}
.stat-card:hover {
  border-color: rgba(0, 255, 231, 0.6);
  box-shadow: 0 0 20px rgba(0, 255, 231, 0.15);
}
.stat-card:hover::before { opacity: 1; }

/* 警示色：未分配班级 */
.stat-card.warn {
  border-bottom-color: rgba(255, 160, 50, 0.7);
}
.stat-card.warn .stat-num { color: #ffbe5c; text-shadow: 0 0 12px #ffbe5c; }
.stat-card.warn:hover { border-color: rgba(255, 160, 50, 0.7); box-shadow: 0 0 20px rgba(255,160,50,0.15); }

/* 强调色：在读班级 */
.stat-card.active {
  border-bottom-color: rgba(54, 214, 138, 0.7);
}
.stat-card.active .stat-num { color: #36d68a; text-shadow: 0 0 12px #36d68a; }
.stat-card.active:hover { border-color: rgba(54, 214, 138, 0.7); box-shadow: 0 0 20px rgba(54,214,138,0.15); }

.stat-icon {
  font-size: 18px;
  margin-bottom: 8px;
  opacity: 0.85;
}
.stat-num {
  font-size: 32px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
  color: var(--glow);
  text-shadow: 0 0 14px var(--glow);
  line-height: 1;
  margin-bottom: 8px;
}
.stat-label {
  font-size: 11px;
  color: var(--text-sub);
  letter-spacing: 1.5px;
}

/* 响应式：中屏 3 列，小屏 2 列 */
@media (max-width: 1100px) {
  .summary-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 600px) {
  .summary-grid { grid-template-columns: repeat(2, 1fr); }
}

</style>