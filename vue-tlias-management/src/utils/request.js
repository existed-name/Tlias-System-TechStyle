// src/utils/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'   // 建议改名为 router，更清晰

// 创建 axios 实例
const request = axios.create({
    baseURL: '/api',
    timeout: 60000,        // 建议生产环境改成 30~60 秒，10分钟太长了
})

// 请求拦截器：携带 Token
request.interceptors.request.use(
    (config) => {
        const loginUser = localStorage.getItem('loginUser')
        if (loginUser) {
            try {
                const user = JSON.parse(loginUser)
                if (user?.token) {
                    config.headers.token = user.token
                }
            } catch (e) {
                console.error('解析 loginUser 失败', e)
            }
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 响应拦截器（核心修改部分）
request.interceptors.response.use(
    // 成功响应（状态码 2xx）
    (response) => {
        return response.data   // 直接返回 data，后续调用更方便
    },

    // 失败响应（状态码非 2xx 或请求失败）
    (error) => {
        console.error('请求失败详情：', error)   // 调试用，生产可去掉

        let errorMessage = '接口访问异常'   // 默认提示

        if (error.response) {
            // 后端返回了 HTTP 错误状态码（400、500 等）
            const data = error.response.data

            // ==================== 重点：这里读取后端返回的具体 message ====================
            if (data?.message) {
                errorMessage = data.message
            } else if (data?.msg) {
                errorMessage = data.msg
            } else if (data?.errorMsg) {
                errorMessage = data.errorMsg
            } else if (typeof data === 'string') {
                errorMessage = data
            } else if (data?.error) {
                // 兼容 Spring Boot 默认错误格式
                errorMessage = data.error
            }

            // 401 特殊处理：跳转登录页
            if (error.response.status === 401) {
                ElMessage.error('登录失效，请重新登录')
                router.push('/login')
                return Promise.reject(error)
            }

        } else if (error.request) {
            // 请求已发出，但没有收到响应（网络断开、超时、后端崩溃等）
            errorMessage = '网络连接异常，请检查网络或稍后重试'
        } else {
            // 请求配置出错
            errorMessage = error.message || '请求配置异常'
        }

        // 显示错误提示
        ElMessage.error(errorMessage)

        return Promise.reject(error)   // 继续抛出，让具体的业务代码可以 catch
    }
)

export default request