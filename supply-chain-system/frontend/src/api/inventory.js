import request from '@/utils/request'

export function pageInventory(params) {
  return request({ url: '/v1/inventory/page', method: 'get', params })
}

export function changeStock(data) {
  return request({ url: '/v1/inventory/stock', method: 'post', data })
}

export function transfer(data) {
  return request({ url: '/v1/inventory/transfer', method: 'post', data })
}

export function check(data) {
  return request({ url: '/v1/inventory/check', method: 'post', data })
}

export function inventoryRecords(params) {
  return request({ url: '/v1/inventory/records', method: 'get', params })
}

export function alerts() {
  return request({ url: '/v1/inventory/alerts', method: 'get' })
}

export function getByProduct(pid) {
  return request({ url: `/v1/inventory/product/${pid}`, method: 'get' })
}
