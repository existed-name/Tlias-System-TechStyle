<script setup>
import { MoonNight } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
</script>

<template>
  <div class="not-found-container">
    <!-- 背景光晕 -->
    <div class="bg-glow"></div>

    <!-- 主卡片 -->
    <div class="card">
      <el-result title="404" sub-title="抱歉，您访问的路径去外太空旅行了...">

        <!-- 图标区域 -->
        <template #icon>
          <div class="error-illustration">
            <el-icon class="icon-glow" :size="140">
              <MoonNight />
            </el-icon>
          </div>
        </template>

        <!-- 按钮区域 -->
        <template #extra>
          <div class="actions">
            <el-button class="btn-primary" @click="router.push('/')">
              返回首页
            </el-button>
            <el-button class="btn-secondary" @click="router.back()">
              返回上一页
            </el-button>
          </div>
        </template>

      </el-result>

      <!-- 路径信息 -->
      <div class="path-info">
        您尝试访问的路径为:
        <code>{{ route.path }}</code>
      </div>
    </div>
  </div>
</template>

<style scoped>

/* =======================
   1. 整体背景（深色科技风）
======================= */
.not-found-container {
  position: relative;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 20% 20%, #0f172a, #020617);
  overflow: hidden;
}

/* 背景动态光晕 */
.bg-glow {
  position: absolute;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(64,158,255,0.25), transparent 70%);
  filter: blur(80px);
  animation: moveGlow 10s infinite alternate;
}

@keyframes moveGlow {
  0% { transform: translate(-20%, -20%); }
  100% { transform: translate(20%, 20%); }
}


/* =======================
   2. 卡片（玻璃拟态）
======================= */
.card {
  position: relative;
  z-index: 2;
  padding: 40px 60px;
  border-radius: 16px;
  backdrop-filter: blur(20px);
  background: rgba(15, 23, 42, 0.7);
  border: 1px solid rgba(255,255,255,0.1);
  box-shadow: 0 0 40px rgba(64,158,255,0.15);
  text-align: center;
}


/* =======================
   3. 图标动效（悬浮 + 发光）
======================= */
.error-illustration {
  margin-bottom: 20px;
  animation: float 4s ease-in-out infinite;
}

.icon-glow {
  color: #409EFF;
  filter: drop-shadow(0 0 10px rgba(64,158,255,0.8))
  drop-shadow(0 0 20px rgba(64,158,255,0.5));
}

/* 悬浮动画 */
@keyframes float {
  0% { transform: translateY(0); }
  50% { transform: translateY(-18px); }
  100% { transform: translateY(0); }
}


/* =======================
   4. 按钮（科技霓虹风）
======================= */
.actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* 主按钮 */
.btn-primary {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  border: none;
  color: white;
  box-shadow: 0 0 15px rgba(64,158,255,0.6);
  transition: all 0.3s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 25px rgba(64,158,255,1);
}

/* 次按钮 */
.btn-secondary {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.2);
  color: #cbd5f5;
  transition: all 0.3s;
}

.btn-secondary:hover {
  border-color: #409EFF;
  color: #409EFF;
  box-shadow: 0 0 10px rgba(64,158,255,0.6);
}


/* =======================
   5. 路径信息
======================= */
.path-info {
  margin-top: 25px;
  color: #94a3b8;
  font-size: 14px;
}

code {
  background: rgba(255,255,255,0.08);
  padding: 4px 8px;
  border-radius: 6px;
  color: #f56c6c;
}

/* =======================
   6. 修复 el-result 标题颜色（深色科技风）
======================= */

/* 标题（404）颜色 - 改成亮白色或带轻微蓝光的白 */
:deep(.el-result__title) {
  color: #e0f2fe !important;     /* 浅天蓝白，非常清晰且科技感强 */
  /* 或者用纯白： color: #ffffff !important; */
  font-size: 72px;               /* 可选：让 404 更大更醒目 */
  font-weight: 700;
  text-shadow: 0 0 20px rgba(64, 158, 255, 0.6);   /* 加上轻微发光，更酷 */

  background: linear-gradient(90deg, #60a5fa, #a5b4fc, #c4d0ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: none;   /* 渐变文字时可去掉阴影 */
}

/* 子标题颜色 - 保持清晰的浅灰白 */
:deep(.el-result__subtitle) {
  color: #cbd5e1 !important;     /* 接近你想要的“白色”效果，更柔和不刺眼 */
  font-size: 18px;
  margin-top: 12px;
}


</style>