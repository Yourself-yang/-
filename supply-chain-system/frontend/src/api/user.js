import request from '@/utils/request'

export function pageUsers(params) {
  return request({ url: '/v1/users/page', method: 'get', params })
}

export function addUser(data) {
  return request({ url: '/v1/users', method: 'post', data })
}

export function updateUser(id, data) {
  return request({ url: `/v1/users/${id}`, method: 'put', data })
}

export function deleteUser(id) {
  return request({ url: `/v1/users/${id}`, method: 'delete' })
}

export function assignRoles(id, roleCode) {
  return request({ url: `/v1/users/${id}/role`, method: 'put', data: { roleCode } })
}

export function listRoles() {
  return request({ url: '/v1/roles', method: 'get' })
}
