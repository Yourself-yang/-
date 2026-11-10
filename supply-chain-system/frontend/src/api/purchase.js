import request from '@/utils/request'

export function pagePOs(params) {
  return request({ url: '/v1/purchase/page', method: 'get', params })
}

export function getPO(id) {
  return request({ url: `/v1/purchase/${id}`, method: 'get' })
}

export function createPO(data) {
  return request({ url: '/v1/purchase', method: 'post', data })
}

export function approve(id, data) {
  return request({ url: `/v1/purchase/${id}/approve`, method: 'put', data })
}

export function ship(id) {
  return request({ url: `/v1/purchase/${id}/ship`, method: 'put' })
}

export function receive(id, data) {
  return request({ url: `/v1/purchase/${id}/receive`, method: 'put', data })
}

export function deletePO(id) {
  return request({ url: `/v1/purchase/${id}`, method: 'delete' })
}

export function poStat() {
  return request({ url: '/v1/purchase/stat', method: 'get' })
}
