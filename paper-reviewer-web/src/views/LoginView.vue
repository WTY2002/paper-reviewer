<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const auth = useAuthStore(); const router = useRouter(); const route = useRoute()
const form = reactive({ email: '', password: '' }); const error = ref(''); const loading = ref(false)
async function submit() {
  loading.value = true; error.value = ''
  try { await auth.login(form); router.push(route.query.redirect || '/') }
  catch (e) { error.value = e.message }
  finally { loading.value = false }
}
</script>

<template><section class="auth-card"><p class="eyebrow">ACADEMIC REVIEW, REIMAGINED</p><h1>Welcome back.</h1><p class="muted">Continue where your manuscript left off.</p><form @submit.prevent="submit"><label>Email<input v-model="form.email" type="email" required autocomplete="email"></label><label>Password<input v-model="form.password" type="password" required autocomplete="current-password"></label><p v-if="error" class="error">{{ error }}</p><button class="primary" :disabled="loading">{{ loading ? 'Signing in…' : 'Sign in' }}</button></form><p class="auth-switch">New here? <router-link to="/register">Create an account</router-link></p></section></template>
