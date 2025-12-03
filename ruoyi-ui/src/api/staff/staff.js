import request from '@/utils/request'

// 查询排班人员信息列表
export function listStaff(query) {
  return request({
    url: '/staff/staff/list',
    method: 'get',
    params: query
  })
}

// 查询排班人员信息详细
export function getStaff(id) {
  return request({
    url: '/staff/staff/' + id,
    method: 'get'
  })
}

// 新增排班人员信息
export function addStaff(data) {
  return request({
    url: '/staff/staff',
    method: 'post',
    data: data
  })
}

// 修改排班人员信息
export function updateStaff(data) {
  return request({
    url: '/staff/staff',
    method: 'put',
    data: data
  })
}

// 删除排班人员信息
export function delStaff(id) {
  return request({
    url: '/staff/staff/' + id,
    method: 'delete'
  })
}

// 批量设置标签
export function batchTagStaff(data) {
  return request({
    url: '/staff/staff/batchTag',
    method: 'post',
    data: data
  })
}

// 批量取消标签
export function batchCancelTag(data) {
  return request({
    url: '/staff/staff/batchCancelTag',
    method: 'post',
    data: data
  })
}

