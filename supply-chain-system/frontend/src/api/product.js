import request from '@/utils/request'

export function pageProducts(params) {
  return request({ url: '/v1/products/page', method: 'get', params })
}

export function getProduct(id) {
  return request({ url: `/v1/products/${id}`, method: 'get' })
}

export function addProduct(data) {
  return request({ url: '/v1/products', method: 'post', data })
}

export function updateProduct(id, data) {
  return request({ url: `/v1/products/${id}`, method: 'put', data })
}

export function updateStatus(id, status) {
  return request({ url: `/v1/products/${id}/status`, method: 'put', data: { status } })
}

export function deleteProduct(id) {
  return request({ url: `/v1/products/${id}`, method: 'delete' })
}
