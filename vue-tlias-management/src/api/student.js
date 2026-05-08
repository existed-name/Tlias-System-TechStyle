import request from '../utils/request'

// 获取班级列表（用于下拉框，需配合班级接口）
// 注意：班级列表接口请参考 clazz 模块，此处复用

/**
 * 分页查询学员列表
 * @param {Object} params - { name, degree, clazzId, page, pageSize }
 */
export const getStudentList = (params) =>
    request.get('/students', { params })

/**
 * 根据ID查询单个学员（编辑回显用）
 * @param {number} id
 */
export const getStudentById = (id) =>
    request.get(`/students/${id}`)

/**
 * 新增学员
 * @param {Object} data - 学员表单数据
 */
export const addStudent = (data) =>
    request.post('/students', data)

/**
 * 修改学员
 * @param {Object} data - 含 id 的学员完整数据
 */
export const updateStudent = (data) =>
    request.put('/students', data)

/**
 * 批量删除学员（单删/多删统一走此接口）
 * @param {number[]} ids - 学员ID数组，路径拼接为 /students/1,2,3
 */
export const deleteStudents = (ids) =>
    request.delete(`/students/${ids.join(',')}`)

/**
 * 违纪处理：给指定学员扣分
 * @param {number} id    - 学员ID
 * @param {number} score - 扣除分数
 */
export const handleViolation = (id, score) =>
    request.put(`/students/violation/${id}/${score}`)