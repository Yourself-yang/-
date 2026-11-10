import request from '@/utils/request'

export function pageOrders(params) {
  return request({ url: '/v1/orders/page', method: 'get', params })
}

export function getOrder(id) {
  return request({ url: `/v1/orders/${id}`, method: 'get' })
}

export function createOrder(data) {
  return request({ url: '/v1/orders', method: 'post', data })
}

export function pay(id) {
  return request({ url: `/v1/orders/${id}/pay`, method: 'put' })
}

export function shipOrder(id, data) {
  return request({ url: `/v1/orders/${id}/ship`, method: 'put', data })
}

export function confirm(id) {
  return request({ url: `/v1/orders/${id}/confirm`, method: 'put' })
}

export function cancel(id) {
  return request({ url: `/v1/orders/${id}/cancel`, method: 'put' })
}

export function refund(id) {
  return request({ url: `/v1/orders/${id}/refund`, method: 'put' })
}

export function orderStat() {
  return request({ url: '/v1/orders/stat', method: 'get' })
}
