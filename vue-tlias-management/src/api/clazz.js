import request from '../utils/request'

/**
 * 3.1 班级列表 - 条件分页查询
 * @param {Object} params - { name, begin, end, page, pageSize }
 */
export const getClazzList = (params) => {
    return request.get('/clazzs', { params })
}

/**
 * 3.2 删除班级
 * @param {number} id - 班级ID
 */
export const deleteClazz = (id) => {
    return request.delete(`/clazzs/${id}`)
}

/**
 * 3.3 新增班级
 * @param {Object} data - { name, room, beginDate, endDate, masterId, subject }
 */
export const addClazz = (data) => {
    return request.post('/clazzs', data)
}

/**
 * 3.4 根据ID查询班级（用于编辑回显）
 * @param {number} id - 班级ID
 */
export const getClazzById = (id) => {
    return request.get(`/clazzs/${id}`)
}

/**
 * 3.5 修改班级
 * @param {Object} data - { id, name, room, beginDate, endDate, masterId, subject }
 */
export const updateClazz = (data) => {
    return request.put('/clazzs', data)
}

/**
 * 3.6 查询所有班级（用于下拉选择等场景）
 */
export const getAllClazz = () => {
    return request.get('/clazzs/list')
}