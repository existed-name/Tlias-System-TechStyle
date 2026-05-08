<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox, ElDialog, ElForm, ElFormItem, ElInput, ElButton } from 'element-plus';
import { useRouter, useRoute } from 'vue-router'
// 确保已安装 @element-plus/icons-vue
import {
  EditPen,
  SwitchButton,
  Promotion,
  Menu,
  HomeFilled,
  UserFilled,
  Tools,
  HelpFilled,
  Avatar,
  Histogram,
  InfoFilled,
  Share,
  Document,
  Monitor
} from '@element-plus/icons-vue'

import { updatePasswordApi } from '@/api/emp'
import router from "../../router";   // ← 如果路径不对，请改成你实际的导入路径

const index = useRouter()
const route = useRoute()
const loginName = ref('')

// ==================== 新增：修改密码对话框相关 ====================
const passwordDialogVisible = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: ''
})

const openPasswordDialog = () => {
  // 重置表单
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordDialogVisible.value = true
}

const handlePasswordSubmit = async () => {
  // 基础前端非空校验
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('⚠️ 原密码和新密码不能为空')
    return
  }

  try {
    const res = await updatePasswordApi(passwordForm)
    if (res.code === 1) {
      ElMessage.success('🔑 密码修改成功！')
      passwordDialogVisible.value = false
    } else {
      ElMessage.error(res.msg || '❌ 修改失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('❌ 操作失败，请检查网络或稍后重试')
  }
}

const handleCloseDialog = () => {
  passwordDialogVisible.value = false
}

// ==================== 原有代码（保持不变） ====================
onMounted(() => {
  let loginUser = JSON.parse(localStorage.getItem('loginUser'))
  if (loginUser) {
    loginName.value = loginUser.name
  }
})

const logout = () => {
  ElMessageBox.confirm('确认退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('退出登录成功')
    localStorage.removeItem('loginUser')
    router.push('/login')
  })
}
</script>

<template>
  <div class="common-layout">
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <div class="logo-scanner"></div> <span class="title">TLIAS <small>TECH-STYLE-SYSTEM</small></span>
        </div>
        <div class="right_tool">
          <span class="user-badge">
            <span class="status-dot"></span>
            {{ loginName }}
          </span>
          <el-divider direction="vertical" />

          <a href="javascript:void(0)" @click="openPasswordDialog" class="tool-link">
            <el-icon><EditPen /></el-icon> 修改密码
          </a>

          <a href="javascript:void(0)" @click="logout" class="tool-link logout">
            <el-icon><SwitchButton /></el-icon> 退出登录
          </a>

        </div>
      </el-header>

      <el-container class="sub-container">
        <el-aside width="240px" class="aside">
          <el-menu
              router
              class="el-menu-vertical"
              :default-active="route.path"
              background-color="transparent"
              text-color="#94a3b8"
              active-text-color="#38bdf8"
          >
            <el-menu-item index="/index">
              <el-icon><Promotion /></el-icon>
              <span>控制中心首页</span>
            </el-menu-item>

            <el-sub-menu index="/manage">
              <template #title>
                <el-icon><Menu /></el-icon>
                <span>学员架构管理</span>
              </template>
              <el-menu-item index="/clazz"><el-icon><HomeFilled /></el-icon>班级档案</el-menu-item>
              <el-menu-item index="/student"><el-icon><UserFilled /></el-icon>学员名册</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="/system">
              <template #title>
                <el-icon><Tools /></el-icon>
                <span>内部信息配置</span>
              </template>
              <el-menu-item index="/dept"><el-icon><HelpFilled /></el-icon>部门资源</el-menu-item>
              <el-menu-item index="/emp"><el-icon><Avatar /></el-icon>员工矩阵</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="/report">

              <template #title>
                <el-icon><Histogram /></el-icon>
                <span>数据分析终端</span>
              </template>

              <el-menu-item index="/report/emp"><el-icon><InfoFilled /></el-icon>人力统计</el-menu-item>
              <el-menu-item index="/report/student"><el-icon><Share /></el-icon>学员模型</el-menu-item>
              <el-menu-item index="/log/operate"><el-icon><Document /></el-icon>操作记录</el-menu-item>
              <el-menu-item index="/log/login"><el-icon><Monitor /></el-icon>登录日志</el-menu-item>

            </el-sub-menu>
          </el-menu>
        </el-aside>

        <el-main class="main-content">
          <div class="view-wrapper">
            <router-view></router-view>
          </div>
        </el-main>
      </el-container>
    </el-container>


    <!-- ==================== 新增：修改密码弹窗（深色科技风） ==================== -->
    <el-dialog
        v-model="passwordDialogVisible"
        title="🔑 修改密码"
        width="420px"
        :before-close="handleCloseDialog"
        custom-class="tech-password-dialog"
    >
      <el-form :model="passwordForm" label-width="90px" label-position="right">
        <el-form-item label="原密码">
          <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入原密码"
              show-password
              clearable
          />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
              clearable
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCloseDialog">取消</el-button>
          <el-button type="primary" @click="handlePasswordSubmit">
            🔄 确认修改
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<style scoped>

/* ==================== 新增：最外层容器强制占满视口 ==================== */
.common-layout {
  /* 关键修复1：强制 100% 视口高度 + 宽度 */
  width: 100%;
  height: 100vh;
  margin: 0 !important;
  padding: 0 !important;

  /* 防止内容溢出产生滚动条偏移 */
  overflow-x: hidden;
  overflow-y: auto;

  /* 背景兜底，防止浏览器默认白边露出来 */
  background-color: #0f172a;
}

/* ==================== 修改：main-container 改为 100% 继承 ==================== */
.main-container {
  /* 原来是 100vh，现在改成 100% 继承父级（更稳定） */
  height: 100%;
  background-color: #0f172a;
}

/* 2. Header：深色磨砂玻璃（保持不变，但增加高度限制） */
.header {
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(12px);
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(56, 189, 248, 0.2);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
  z-index: 100;
  /* Element Plus 默认 header 有 padding，这里强制去掉可能的多余间距 */
  padding: 0 20px !important;
  height: 60px; /* 建议固定高度，防止子元素撑高 */
  flex-shrink: 0; /* 防止被压缩 */
}

/* Logo 扫描特效（保持不变） */
.logo-scanner {
  width: 4px;
  height: 20px;
  background: #38bdf8;
  margin-right: 12px;
  border-radius: 10px;
  box-shadow: 0 0 12px #38bdf8;
  animation: scan 1.5s infinite ease-in-out;
}

@keyframes scan {
  0%, 100% { opacity: 0.3; transform: scaleY(0.8); }
  50% { opacity: 1; transform: scaleY(1.2); }
}

.title {
  color: #f1f5f9;
  font-size: 22px;
  font-family: 'Orbitron', sans-serif; /* 如果有科幻字体可以替换 */
  font-weight: 900;
  letter-spacing: 2px;
}

.title small {
  font-size: 10px;
  color: #38bdf8;
  border: 1px solid #38bdf8;
  padding: 1px 4px;
  margin-left: 8px;
  vertical-align: middle;
}

/* 用户状态标牌 */
.user-badge {
  background: rgba(56, 189, 248, 0.1);
  padding: 4px 12px;
  border-radius: 20px;
  color: #38bdf8;
  font-size: 13px;
  border: 1px solid rgba(56, 189, 248, 0.3);
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #22c55e;
  border-radius: 50%;
  margin-right: 6px;
  box-shadow: 0 0 8px #22c55e;
}

.tool-link {
  color: #94a3b8;
  text-decoration: none;
  font-size: 13px;
  margin: 0 10px;
  transition: all 0.3s;
}

.tool-link:hover {
  color: #38bdf8;
  text-shadow: 0 0 8px #38bdf8;
}

/* 3. 侧边栏：透明化处理 */
.aside {
  background-color: #1e293b; /* 中度深蓝 */
  border-right: 1px solid rgba(255, 255, 255, 0.05);
}

.el-menu {
  border-right: none !important;
  margin-top: 10px;
}

/* 菜单项深度定制 */
:deep(.el-menu-item), :deep(.el-sub-menu__title) {
  margin: 4px 10px !important;
  border-radius: 4px !important;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

/* 悬停效果：外发光 */
:deep(.el-menu-item:hover), :deep(.el-sub-menu__title:hover) {
  background: rgba(56, 189, 248, 0.1) !important;
  color: #38bdf8 !important;
}

/* 激活状态：霓虹边框 */
:deep(.el-menu-item.is-active) {
  background: rgba(56, 189, 248, 0.15) !important;
  color: #ffffff !important;
  box-shadow: inset 3px 0 0 #38bdf8;
  text-shadow: 0 0 10px rgba(56, 189, 248, 0.8);
}

/* 4. 主内容区：暗色背景无缝衔接 */
.main-content {
  background-color: #0f172a;
  padding: 15px;
  /* 新增：让主内容区也能继承高度 */
  flex: 1;
  min-height: 0; /* 解决 flex 子元素高度塌陷 */
}

.view-wrapper {
  background: #1e293b; /* 内容容器稍微亮一点，形成层级 */
  min-height: 100%;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 5px;
}
::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 10px;
}
::-webkit-scrollbar-thumb:hover {
  background: #38bdf8;
}

/* ==================== 新增：修改密码弹窗科技风样式 ==================== */
.tech-password-dialog :deep(.el-dialog) {
  background-color: #1e293b !important;
  border: 1px solid rgba(56, 189, 248, 0.4) !important;
  box-shadow: 0 10px 40px rgba(56, 189, 248, 0.3) !important;
  border-radius: 12px;
}

.tech-password-dialog :deep(.el-dialog__header) {
  background: linear-gradient(to right, #0f172a, #1e293b);
  border-bottom: 1px solid rgba(56, 189, 248, 0.3);
}

.tech-password-dialog :deep(.el-dialog__title) {
  color: #38bdf8 !important;
  font-weight: 700;
  letter-spacing: 2px;
}

.tech-password-dialog :deep(.el-dialog__body) {
  padding: 30px 25px 10px;
  background-color: #1e293b;
}

.tech-password-dialog :deep(.el-form-item__label) {
  color: #cbd5e1;
  font-size: 14px;
}

.tech-password-dialog :deep(.el-input__inner) {
  background-color: #334155 !important;
  border: 1px solid rgba(56, 189, 248, 0.3) !important;
  color: #f1f5f9;
  border-radius: 6px;
}

.tech-password-dialog :deep(.el-input__inner:focus) {
  border-color: #38bdf8 !important;
  box-shadow: 0 0 8px rgba(56, 189, 248, 0.5);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding-top: 10px;
}

.dialog-footer .el-button {
  min-width: 100px;
  transition: all 0.3s;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(90deg, #38bdf8, #0ea5e9);
  border: none;
  box-shadow: 0 0 15px rgba(56, 189, 248, 0.6);
}

.dialog-footer .el-button--primary:hover {
  box-shadow: 0 0 25px rgba(56, 189, 248, 0.8);
  transform: scale(1.05);
}

</style>