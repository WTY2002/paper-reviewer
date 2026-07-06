import { defineStore } from 'pinia'
import { authApi } from '../api/authApi'

const TOKEN_KEY = 'paper-reviewer-token'

export const useAuthStore = defineStore('auth', {
  state: () => ({ token: localStorage.getItem(TOKEN_KEY), user: null, ready: false }),
  getters: { isAuthenticated: (state) => Boolean(state.token) },
  actions: {
    setSession(session) {
      this.token = session.token
      this.user = { userId: session.userId, email: session.email, displayName: session.displayName }
      localStorage.setItem(TOKEN_KEY, session.token)
    },
    clearSession() {
      this.token = null
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
    },
    async restore() {
      if (!this.token) { this.ready = true; return }
      try { this.user = await authApi.me() } catch { this.clearSession() }
      finally { this.ready = true }
    },
    async login(credentials) { const result = await authApi.login(credentials); this.setSession(result) },
    async register(payload) { const result = await authApi.register(payload); this.setSession(result) },
    logout() { this.clearSession() },
  },
})
