<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
const auth = useAuthStore(); const router = useRouter(); const form = reactive({ email: '', password: '', displayName: '' }); const error = ref(''); const loading = ref(false)
async function submit() { loading.value = true; error.value = ''; try { await auth.register(form); router.push('/') } catch (e) { error.value = e.message } finally { loading.value = false } }
</script>

<template><section class="auth-card"><p class="eyebrow">YOUR PRIVATE REVIEW ROOM</p><h1>Sharpen your next paper.</h1><p class="muted">Five expert lenses. One focused revision plan.</p><form @submit.prevent="submit"><label>Name<input v-model="form.displayName" required autocomplete="name"></label><label>Email<input v-model="form.email" type="email" required autocomplete="email"></label><label>Password<input v-model="form.password" type="password" minlength="8" required autocomplete="new-password"></label><p v-if="error" class="error">{{ error }}</p><button class="primary" :disabled="loading">{{ loading ? 'Creating…' : 'Create account' }}</button></form><p class="auth-switch">Already registered? <router-link to="/login">Sign in</router-link></p></section></template>
