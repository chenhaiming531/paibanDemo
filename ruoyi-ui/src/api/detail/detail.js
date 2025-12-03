import request from '@/utils/request'

// 查询安排排班列表
export function listDetail(query) {
  return request({
    url: '/detail/detail/list',
    method: 'get',
    params: query
  })
}

export function distinctDates() {
  return request({
    url: '/detail/detail/distinctDates',
    method: 'get'
  })
}

// 查询安排排班详细
export function getDetail(id) {
  return request({
    url: '/detail/detail/' + id,
    method: 'get'
  })
}

// 新增安排排班
export function addDetail(data) {
  return request({
    url: '/detail/detail',
    method: 'post',
    data: data
  })
}
//
export function intelligent(data){
	return request({
		url: '/detail/detail/intelligent',
		method: 'post',
		data: data
	})
}
//
export function view(data){
	return request({
		url: '/detail/detail/view',
		method: 'post',
		data: data
	})
}
//
export function monthlyDuty(data){
	return request({
		url: '/detail/detail/monthlyDuty',
		method: 'post',
		data: data
	})
}

// 修改安排排班
export function updateDetail(data) {
  return request({
    url: '/detail/detail',
    method: 'put',
    data: data
  })
}

// 删除安排排班
export function delDetail(id) {
  return request({
    url: '/detail/detail/' + id,
    method: 'delete'
  })
}

export function listRosterDuty() {
  return request({
    url: '/detail/detail/listRosterDuty',
    method: 'get'
  })
}

// 更新值班人员
export function updateRosterDuty(data) {
  return request({
    url: '/duty/duty',
    method: 'put',
    data: data
  })
}


// 月度导出
export function monthlyExport(params) {
  return request({
    url: '/detail/detail/monthlyExport',
    method: 'post',
    data: params,
    responseType: 'blob' // 重要：指定响应类型为blob，用于文件下载
  })
}
