<script setup>
import { ref } from 'vue'
import VuePdfEmbed from 'vue-pdf-embed'
defineProps({ source: [String, Uint8Array, ArrayBuffer, Object] })
const page = ref(1); const pages = ref(0)
</script>
<template><div class="pdf"><div class="pdf-tools"><button @click="page=Math.max(1,page-1)">←</button><span>{{ page }} / {{ pages || '…' }}</span><button @click="page=Math.min(pages || page+1,page+1)">→</button></div><VuePdfEmbed v-if="source" :source="source" :page="page" @loaded="doc=>pages=doc.numPages"/><p v-else>Loading protected PDF…</p></div></template>
<style scoped>.pdf{height:calc(100vh - 120px);overflow:auto;background:#d8d8d2;border-radius:10px}.pdf-tools{position:sticky;top:0;z-index:2;display:flex;justify-content:center;gap:16px;padding:9px;background:#222c26;color:white}.pdf-tools button{border:0;background:none;color:white;cursor:pointer}.pdf :deep(canvas){max-width:100%;height:auto!important;margin:12px auto;display:block;box-shadow:0 5px 25px #0003}</style>
