import { apiRequest } from './http'

export const reviewApi = {
  analyze(payload) { return apiRequest('/api/reviews/analysis', { method: 'POST', body: JSON.stringify(payload) }) },
  list(params = {}) { const query = new URLSearchParams(params); return apiRequest(`/api/reviews${query.size ? `?${query}` : ''}`) },
  get(id) { return apiRequest(`/api/reviews/${id}`) },
  remove(id) { return apiRequest(`/api/reviews/${id}`, { method: 'DELETE' }) },
  team(id) { return apiRequest(`/api/reviews/${id}/team`) },
  updateTeam(id, team) { return apiRequest(`/api/reviews/${id}/team`, { method: 'PUT', body: JSON.stringify(team) }) },
  confirmTeam(id) { return apiRequest(`/api/reviews/${id}/confirm-team`, { method: 'POST' }) },
  start(id) { return apiRequest(`/api/reviews/${id}/start`, { method: 'POST' }) },
}
