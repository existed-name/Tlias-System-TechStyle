import request from '../../utils/request'

/**
 * 获取学员学历统计数据
 * 返回 data: [{ name: '本科', value: 182 }, ...]
 * 用于饼状图渲染
 */
export const getStudentDegreeData = () => {
    return request.get('/report/studentDegreeData')
}

/**
 * 获取班级人数统计数据
 * 返回 data: { clazzList: [...], dataList: [...] }
 * 用于柱状图渲染
 */
export const getStudentCountData = () => {
    return request.get('/report/studentCountData')
}

/**
 * 获取学生综合数据 - 学生总数, 未分配班级的学生人数, 班级总数, 在读/结课/未开班级数量
 */
export const getStudentSummaryData = () => {
    return request.get( 'report/studentSummaryData' )
}