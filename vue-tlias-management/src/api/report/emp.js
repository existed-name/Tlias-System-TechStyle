import request from '../../utils/request'

/**
 * 获取员工性别统计数据
 * 返回格式: [{ name: '男性员工', value: 5 }, ...]
 */
export const getEmpGenderData = () => request.get('/report/empGenderData')

/**
 * 获取员工职位人数统计数据
 * 返回格式: { jobList: [...], dataList: [...] }
 */
export const getEmpJobData = () => request.get('/report/empJobData')