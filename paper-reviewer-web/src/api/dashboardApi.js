import { apiRequest } from './http'
export const dashboardApi = { get: () => apiRequest('/api/dashboard') }
