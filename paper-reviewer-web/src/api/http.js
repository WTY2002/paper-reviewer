import { useAuthStore } from '../stores/authStore'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

export async function apiRequest(path, options = {}) {
  const auth = useAuthStore()
  const headers = new Headers(options.headers || {})
  if (auth.token) headers.set('Authorization', `Bearer ${auth.token}`)
  if (options.body && !(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers })
  const contentType = response.headers.get('content-type') || ''
  const body = contentType.includes('application/json') ? await response.json() : await response.blob()
  if (!response.ok || (body && body.success === false)) {
    if (response.status === 401) auth.clearSession()
    const error = new Error(body?.error?.message || `Request failed (${response.status})`)
    error.code = body?.error?.code
    error.status = response.status
    throw error
  }
  return body?.success === true ? body.data : body
}
