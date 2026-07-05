<script setup>
defineProps({ roles: { type: Array, default: () => [] }, modelValue: String })
defineEmits(['update:modelValue'])
const labels = { EIC:'Editor-in-Chief', METHODOLOGY:'Methods', DOMAIN:'Domain', PERSPECTIVE:'Perspective', DEVILS_ADVOCATE:"Devil’s Advocate" }
</script>
<template><div class="tabs" role="tablist"><button v-for="role in roles" :key="role" type="button" :class="{active:modelValue===role}" @click="$emit('update:modelValue',role)">{{ labels[role] || role }}</button></div></template>
<style scoped>.tabs{display:flex;gap:4px;overflow:auto;border-bottom:1px solid #d9d7ce}.tabs button{white-space:nowrap;border:0;background:none;padding:13px 15px;color:#687169;font:500 .85rem inherit;cursor:pointer}.tabs button.active{color:#174b38;border-bottom:2px solid #174b38}</style>
