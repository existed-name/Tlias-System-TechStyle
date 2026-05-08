import request from '../../utils/request'

/**
 * 操作日志相关接口
 */

/**
 * 分页查询操作日志
 *
 * params 包含: page, pageSize, operateEmpId, beginDate, endDate
 */
export const getPageApi = (params) => {
    return request({
        url: '/logs/operation',
        method: 'GET',
        params
    })
}

/**
 * 获取统计数据 (今日总数、累计总数、图表数据)
 */
export const getSummaryDataApi = () => {
    return request({
        url: '/logs/operation/summary',
        method: 'GET'
    })
}