import { apiRequest } from './http'
export const settingsApi = { get: () => apiRequest('/api/settings'), update: (payload) => apiRequest('/api/settings', { method:'PUT', body:JSON.stringify(payload) }) }
