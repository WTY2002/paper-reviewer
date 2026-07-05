<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/authStore'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
async function logout() { await auth.logout(); router.push('/login') }
</script>

<template>
  <div v-if="route.meta.public" class="public-shell"><router-view /></div>
  <div v-else class="app-shell">
    <aside class="sidebar">
      <router-link class="brand" to="/">PeerLens</router-link>
      <nav>
        <router-link to="/">Dashboard</router-link>
        <router-link to="/upload">Upload paper</router-link>
        <router-link to="/history">History</router-link>
        <router-link to="/settings">Settings</router-link>
      </nav>
      <button class="link-button" @click="logout">Sign out</button>
    </aside>
    <main class="page"><router-view /></main>
  </div>
</template>
