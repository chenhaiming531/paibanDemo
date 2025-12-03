import request from '@/utils/request'

// 查询人员类型列表
export function listType(query) {
  return request({
    url: '/type/type/list',
    method: 'get',
    params: query
  })
}

// 导入人员数据
export function importStaff(data) {
  return request({
    url: '/type/type/importData', // 已修正为正确的后端接口路径
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data' // 关键：指定为多部分表单数据类型
    }
  })
}

// 查询人员类型详细
export function getType(id) {
  return request({
    url: '/type/type/' + id,
    method: 'get'
  })
}

// 新增人员类型
export function addType(data) {
  return request({
    url: '/type/type',
    method: 'post',
    data: data
  })
}

// 修改人员类型
export function updateType(data) {
  return request({
    url: '/type/type',
    method: 'put',
    data: data
  })
}

// 删除人员类型
export function delType(id) {
  return request({
    url: '/type/type/' + id,
    method: 'delete'
  })
}
