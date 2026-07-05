import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from './authStore'
import { authApi } from '../api/authApi'

vi.mock('../api/authApi', () => ({ authApi: { login: vi.fn(), register: vi.fn(), me: vi.fn(), logout: vi.fn() } }))

describe('authStore', () => {
  beforeEach(() => { localStorage.clear(); setActivePinia(createPinia()); vi.clearAllMocks() })

  it('persists a successful login', async () => {
    authApi.login.mockResolvedValue({ token: 'jwt', userId: 7, email: 'a@example.com', displayName: 'Ada' })
    const store = useAuthStore()
    await store.login({ email: 'a@example.com', password: 'secret123' })
    expect(store.isAuthenticated).toBe(true)
    expect(store.user.displayName).toBe('Ada')
    expect(localStorage.getItem('paper-reviewer-token')).toBe('jwt')
  })

  it('clears an invalid restored token', async () => {
    localStorage.setItem('paper-reviewer-token', 'bad')
    setActivePinia(createPinia())
    authApi.me.mockRejectedValue(new Error('invalid'))
    const store = useAuthStore()
    await store.restore()
    expect(store.isAuthenticated).toBe(false)
    expect(store.ready).toBe(true)
  })
})
