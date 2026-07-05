<script setup>
import { computed } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
const props = defineProps({ content: { type: String, default: '' } })
const html = computed(() => DOMPurify.sanitize(marked.parse(props.content || '_Waiting for this reviewer…_')))
</script>
<template><article class="markdown" v-html="html" /></template>
<style scoped>.markdown{line-height:1.72;color:#29332d}.markdown :deep(h1),.markdown :deep(h2),.markdown :deep(h3){font-family:'Newsreader',serif;line-height:1.12;margin:1.5em 0 .6em}.markdown :deep(h1){font-size:2.2rem}.markdown :deep(h2){font-size:1.6rem}.markdown :deep(table){width:100%;border-collapse:collapse;font-size:.9rem}.markdown :deep(td),.markdown :deep(th){padding:9px;border:1px solid #d9d7ce;text-align:left}.markdown :deep(blockquote){border-left:3px solid #174b38;margin-left:0;padding-left:18px;color:#5d665f}</style>
