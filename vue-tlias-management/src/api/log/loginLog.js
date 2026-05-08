/**
 * 登录日志模块 API
 */
import request from '../../utils/request'

/**
 * 页查询登录日志
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码 (必填)
 * @param {number} params.pageSize - 每页记录数 (必填)
 * @param {string} [params.username] - 用户名模糊匹配 (可选)
 * @param {string} [params.beginDate] - 起始时间 (格式: YYYY-MM-DD, 可选)
 * @param {string} [params.endDate] - 截止时间 (格式: YYYY-MM-DD, 可选)
 * @returns {Promise} 返回包含 total 和 rows 的对象
 */
export function pageLoginLog(params) {
    return request({
        url: '/logs/login',
        method: 'get',
        params // GET 请求参数放入 params 中
    })
}

/**
 * 统计数据查询
 * 用于登录界面的可视化数据（今日登录数、失败统计、饼图及柱状图数据）
 * @returns {Promise} 返回 data 对象，包含 todayLoginCount, todayFailCount, totalSuccessFail, failRank
 */
export function getLoginLogSummaryData() {
    return request({
        url: '/logs/login/summary',
        method: 'get'
    })
}