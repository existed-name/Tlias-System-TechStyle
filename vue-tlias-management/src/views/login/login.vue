<script setup>
import { ref } from 'vue'
import { loginApi } from '@/api/login'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

// 表单数据
const loginForm = ref({
  username: '',
  password: ''
})

// 表单ref（用于校验）
const loginFormRef = ref()

// loading状态（防重复点击）
const loading = ref(false)

// 校验规则
const rules = {
  username: [
    { required: true, message: '请输入用户名😶', trigger: 'blur' },
    { min: 3, max: 20, message: '长度3~20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码😶', trigger: 'blur' },
    { min: 3, max: 20, message: '长度3~20位', trigger: 'blur' }
  ]
}

// 登录
const login = () => {
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const result = await loginApi(loginForm.value)

      if (result.code) {
        ElMessage.success('登录成功🚀')
        localStorage.setItem('loginUser', JSON.stringify(result.data))
        router.push('/')
      } else {
        ElMessage.error(result.msg || '登录失败😢')
      }
    } catch (e) {
      ElMessage.error('请求异常😵')
    } finally {
      loading.value = false
    }
  })
}

// 重置
const clear = () => {
  loginFormRef.value.resetFields()
}

// 回车登录
const handleKeyDown = (e) => {
  if (e.key === 'Enter') {
    login()
  }
}
</script>

<template>
  <div id="container" @keydown="handleKeyDown">
    <div class="login-box">

      <div class="title">🚀 Tlias Admin</div>

      <el-form
          :model="loginForm"
          :rules="rules"
          ref="loginFormRef"
          label-width="70px"
      >
        <el-form-item label="账号" prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
              type="password"
              v-model="loginForm.password"
              placeholder="请输入密码"
              show-password
              clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
              class="btn"
              type="primary"
              :loading="loading"
              @click="login"
          >
            登录 ✨
          </el-button>

          <el-button class="btn" @click="clear">
            重置 🧹
          </el-button>
        </el-form-item>
      </el-form>

    </div>
  </div>
</template>

<style scoped>
/* 背景 */
#container {
  height: 100vh;
  background: url('../../assets/login-bg02.jpeg') no-repeat center center;
  background-size: cover;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 登录框（核心科技感🔥） */
.login-box {
  width: 400px;
  padding: 45px 35px;
  border-radius: 20px;

  /* 更高级的玻璃 */
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(20px);

  /* 渐变边框（重点🔥） */
  border: 1px solid rgba(0, 150, 255, 0.3);

  /* 多层阴影（悬浮感） */
  box-shadow:
      0 0 30px rgba(0, 0, 0, 0.6),
      0 0 60px rgba(0, 150, 255, 0.2),
      inset 0 0 20px rgba(255, 255, 255, 0.05);

  color: #fff;

  /* 动效 */
  transition: all 0.3s ease;
}

/* 悬浮微动（高级感关键） */
.login-box:hover {
  transform: translateY(-5px);
  box-shadow:
      0 10px 40px rgba(0, 0, 0, 0.8),
      0 0 80px rgba(0, 150, 255, 0.3);
}

/* 标题 */
.title {
  text-align: center;
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 35px;
  letter-spacing: 3px;

  background: linear-gradient(90deg, #00c6ff, #ffffff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;

  /* 微光 */
  text-shadow: 0 0 10px rgba(0, 198, 255, 0.5);
}

/* 按钮 */
.btn {
  width: 130px;
  border-radius: 25px;
  font-weight: bold;
  letter-spacing: 1px;

  transition: all 0.3s;
}

/* 登录按钮（主按钮） */
:deep(.el-button--primary) {
  background: linear-gradient(90deg, #00c6ff, #0072ff);
  border: none;

  box-shadow: 0 0 15px rgba(0, 198, 255, 0.6);
}

/* hover爆发🔥 */
:deep(.el-button--primary:hover) {
  transform: scale(1.05);
  box-shadow:
      0 0 25px rgba(0, 198, 255, 1),
      0 0 50px rgba(0, 198, 255, 0.6);
}

/* 次按钮 */
:deep(.el-button) {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.el-button:hover) {
  background: rgba(255, 255, 255, 0.2);
}

/* 输入框样式增强 */
/* 输入框整体 */
:deep(.el-input__wrapper) {
  background: rgba(0, 0, 0, 0.4);
  border-radius: 8px;
  border: 1px solid rgba(0, 150, 255, 0.3);

  transition: all 0.3s;
}

/* 聚焦效果（重点🔥） */
:deep(.el-input__wrapper.is-focus) {
  border: 1px solid #00c6ff;
  box-shadow:
      0 0 10px rgba(0, 198, 255, 0.6),
      0 0 20px rgba(0, 198, 255, 0.3);
}

/* 输入文字 */
:deep(.el-input__inner) {
  color: #fff;
}



/* placeholder */
:deep(input::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

/* placeholder颜色 */
:deep(input::placeholder) {
  color: rgba(255, 255, 255, 0.6);
}

/* label颜色 */
:deep(.el-form-item__label) {
  color: #eee;
}
</style>