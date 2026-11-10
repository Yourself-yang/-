import request from '@/utils/request'

export function listCategories() {
  return request({ url: '/v1/categories', method: 'get' })
}

export function addCategory(data) {
  return request({ url: '/v1/categories', method: 'post', data })
}

export function updateCategory(id, data) {
  return request({ url: `/v1/categories/${id}`, method: 'put', data })
}

export function deleteCategory(id) {
  return request({ url: `/v1/categories/${id}`, method: 'delete' })
}
