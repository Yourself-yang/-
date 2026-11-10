import request from '@/utils/request'

export function runPrediction() {
  return request({ url: '/v1/ai/prediction/run', method: 'post' })
}

export function getPrediction(params) {
  return request({ url: '/v1/ai/prediction', method: 'get', params })
}

export function listReplenishment(params) {
  return request({ url: '/v1/ai/replenishment', method: 'get', params })
}

export function convertReplenishment() {
  return request({ url: '/v1/ai/replenishment/convert', method: 'post' })
}

export function detectAnomaly() {
  return request({ url: '/v1/ai/anomaly/detect', method: 'post' })
}

export function listAnomaly(params) {
  return request({ url: '/v1/ai/anomaly', method: 'get', params })
}

export function handleAnomaly(id) {
  return request({ url: `/v1/ai/anomaly/${id}/handle`, method: 'put' })
}

export function dashboard() {
  return request({ url: '/v1/ai/dashboard', method: 'get' })
}
