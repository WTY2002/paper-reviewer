<script setup>
defineProps({ events: { type: Array, default: () => [] }, connected: Boolean, error: String })
</script>
<template><footer class="workflow"><span class="pulse" :class="{live:connected}"/><strong>{{ connected ? 'Live review' : 'Workflow' }}</strong><div class="steps"><span v-for="event in events.slice(-5)" :key="event.sequence">{{ event.type?.replaceAll('_',' ') }}</span></div><span v-if="error" class="workflow-error">{{ error }}</span></footer></template>
<style scoped>.workflow{position:sticky;bottom:0;display:flex;align-items:center;gap:10px;padding:13px 18px;background:#17211b;color:#f7f4eb;border-radius:10px;font-size:.78rem;box-shadow:0 10px 30px #0003}.pulse{width:8px;height:8px;border-radius:50%;background:#77817a}.pulse.live{background:#7de0a4;box-shadow:0 0 0 5px #7de0a422}.steps{display:flex;gap:18px;margin-left:18px;overflow:hidden;color:#aeb8b0}.steps span{white-space:nowrap}.workflow-error{margin-left:auto;color:#ffb7ae}</style>
