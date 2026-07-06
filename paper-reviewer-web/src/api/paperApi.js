import { apiRequest } from './http'

export const paperApi = {
  upload(file) { const body = new FormData(); body.append('file', file); return apiRequest('/api/papers', { method: 'POST', body }) },
  list(params = {}) { const query = new URLSearchParams(params); return apiRequest(`/api/papers${query.size ? `?${query}` : ''}`) },
  pdf(id) { return apiRequest(`/api/papers/${id}/pdf`) },
}
