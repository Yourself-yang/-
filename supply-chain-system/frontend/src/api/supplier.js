import request from '@/utils/request'

export function pageSuppliers(params) {
  return request({ url: '/v1/suppliers/page', method: 'get', params })
}

export function listSuppliers() {
  return request({ url: '/v1/suppliers', method: 'get' })
}

export function addSupplier(data) {
  return request({ url: '/v1/suppliers', method: 'post', data })
}

export function updateSupplier(id, data) {
  return request({ url: `/v1/suppliers/${id}`, method: 'put', data })
}

export function deleteSupplier(id) {
  return request({ url: `/v1/suppliers/${id}`, method: 'delete' })
}

export function updateSupplierStatus(id, status) {
  return request({ url: `/v1/suppliers/${id}/status`, method: 'put', data: { status } })
}
