import { createRouter, createWebHistory } from 'vue-router'

// 导入视图组件
import IndexView from '../views/index/index.vue'
import ClazzView from '../views/clazz/clazz.vue'
import DeptView from '../views/dept/dept.vue'
import EmpView from '../views/emp/emp.vue'
import LoginLogLogView from '../views/log/login/loginLog.vue'
import OperateLog from '../views/log/operate/operateLog.vue'
import Student from '../views/student/student.vue'
import EmpReportReportView from '../views/report/emp/empReport.vue'
import StudentReport from '../views/report/student/studentReport.vue'
import LayoutView from '../views/layout/layout.vue'
import LoginView from '../views/login/login.vue'

const index = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 1. 登录页面 (无需拦截)
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },

    // 2. 主页面布局 (受保护的路由，必须登录才能访问其子路由)
    {
      path: '/',
      name: 'layout',
      component: LayoutView,
      redirect: '/index', // 默认重定向到首页
      children: [
        { path: 'index', name: 'index', component: IndexView },
        { path: 'clazz', name: 'clazz', component: ClazzView },
        { path: 'student', name: 'student', component: Student },
        { path: 'dept', name: 'dept', component: DeptView },
        { path: 'emp', name: 'emp', component: EmpView },
        // log 层级
        {
          path: 'log',
          redirect: '/log/operate',
          children: [
            { path: 'operate', name: 'operateLog', component: OperateLog },
            { path: 'login', name: 'loginLog', component: LoginLogLogView },
          ]
        },
        // 新增一个 report 层级
        {
          path: 'report',
          redirect: '/report/emp', // 访问 /report 时，默认跳转到 /report/emp，不然用户只输入 /report 可能会看到空白
          children: [
            { path: 'emp', name: 'empReport', component: EmpReportReportView }, // 访问路径变为 /report/emp
            { path: 'student', name: 'stuReport', component: StudentReport }, // 访问路径变为 /report/student
          ]
        },
      ]
    },

    // 3. 匹配不到任何路径时, 懒加载 404 页面
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('../views/NotFoundView.vue')
    }

  ]
})

/**
 * 全局前置路由守卫
 * to: 即将要进入的目标路由对象
 * from: 当前正要离开的路由
 * next: 放行函数
 */
index.beforeEach((to, from, next) => {
  // 1. 获取本地存储中的登录用户信息
  const loginUser = JSON.parse(localStorage.getItem('loginUser'))

  // 2. 判断逻辑
  if (to.path === '/login') {
    // 如果要去登录页，直接放行
    next()
  } else {
    // 如果要去其他页面 (如 /index, /emp 等)
    if (loginUser && loginUser.token) {
      // 检查到有 token，说明已登录，放行
      next()
    } else {
      // 检查到没有登录信息，强制跳转到登录页
      next('/login')
    }
  }
})

export default index