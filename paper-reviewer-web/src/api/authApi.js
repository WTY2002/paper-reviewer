import { apiRequest } from './http'

export const authApi = {
  register(payload) {
    return apiRequest('/api/auth/register', { method: 'POST', body: JSON.stringify(payload) })
  },
  login(payload) {
    return apiRequest('/api/auth/login', { method: 'POST', body: JSON.stringify(payload) })
  },
  me() {
    return apiRequest('/api/auth/me')
  },
  logout() {
    return apiRequest('/api/auth/logout', { method: 'POST' })
  },
}
