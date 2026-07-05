<script setup>
import { ref } from 'vue'
const emit = defineEmits(['selected'])
const input = ref(); const dragging = ref(false); const error = ref('')
const MAX_SIZE = 20 * 1024 * 1024
function choose(files) {
  const file = files?.[0]; error.value = ''
  if (!file) return
  if (file.type !== 'application/pdf' || !file.name.toLowerCase().endsWith('.pdf')) { error.value = 'Choose a PDF file.'; return }
  if (file.size > MAX_SIZE) { error.value = 'The PDF must be 20 MB or smaller.'; return }
  emit('selected', file)
}
</script>

<template><div><button class="dropzone" :class="{dragging}" type="button" @click="input.click()" @dragover.prevent="dragging=true" @dragleave="dragging=false" @drop.prevent="dragging=false;choose($event.dataTransfer.files)"><span class="upload-mark">↑</span><strong>Drop your PDF here</strong><span>or choose a file · max 20 MB · 300 pages</span></button><input ref="input" class="sr-only" type="file" accept="application/pdf,.pdf" @change="choose($event.target.files)"><p v-if="error" class="error">{{ error }}</p></div></template>

<style scoped>.dropzone{width:100%;min-height:280px;border:1.5px dashed #9eaaa2;border-radius:14px;background:#f7f8f3;display:grid;place-content:center;justify-items:center;gap:10px;color:#657068;font:inherit;cursor:pointer}.dropzone strong{font:600 1.5rem 'Newsreader',serif;color:#17211b}.dropzone.dragging{background:#e4eee5;border-color:#174b38}.upload-mark{display:grid;place-items:center;width:52px;height:52px;border-radius:50%;background:#174b38;color:#fff;font-size:1.7rem;margin-bottom:8px}.sr-only{position:absolute;width:1px;height:1px;opacity:0}</style>
