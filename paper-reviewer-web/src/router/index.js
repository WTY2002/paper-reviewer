import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'

const routes = [
  { path: '/login', component: LoginView, meta: { public: true } },
  { path: '/register', component: RegisterView, meta: { public: true } },
  { path: '/', component: () => import('../views/DashboardView.vue') },
  { path: '/upload', component: () => import('../views/UploadView.vue') },
  { path: '/reviews/:reviewId', component: () => import('../views/ReviewWorkspaceView.vue') },
  { path: '/rereviews/:rereviewId', component: () => import('../views/ReviewWorkspaceView.vue') },
  { path: '/history', component: () => import('../views/HistoryView.vue') },
  { path: '/settings', component: () => import('../views/SettingsView.vue') },
]

export const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (!auth.ready) await auth.restore()
  if (!to.meta.public && !auth.isAuthenticated) return { path: '/login', query: { redirect: to.fullPath } }
  if (to.meta.public && auth.isAuthenticated) return '/'
})
