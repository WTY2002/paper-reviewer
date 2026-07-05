<script setup>
import { nextTick, ref, watch } from 'vue'

const props=defineProps({open:Boolean,title:{type:String,default:'Confirm action'},message:{type:String,default:''},confirmLabel:{type:String,default:'Delete'},busy:Boolean})
const emit=defineEmits(['confirm','cancel']);const cancelButton=ref()
watch(()=>props.open,async(value)=>{if(value){await nextTick();cancelButton.value?.focus()}})
function cancel(){if(!props.busy)emit('cancel')}
</script>

<template><Teleport to="body"><div v-if="open" class="dialog-backdrop" role="presentation" @mousedown.self="cancel" @keydown.esc="cancel"><section class="confirm-dialog" role="alertdialog" aria-modal="true" aria-labelledby="confirm-title" aria-describedby="confirm-message"><div class="dialog-mark" aria-hidden="true">!</div><div><p class="eyebrow">CONFIRMATION</p><h2 id="confirm-title">{{ title }}</h2><p id="confirm-message" class="dialog-message">{{ message }}</p></div><div class="dialog-actions"><button ref="cancelButton" class="secondary" :disabled="busy" @click="cancel">Cancel</button><button class="danger-button" :disabled="busy" @click="emit('confirm')">{{ busy?'Deleting…':confirmLabel }}</button></div></section></div></Teleport></template>

<style scoped>.dialog-backdrop{position:fixed;inset:0;z-index:1000;display:grid;place-items:center;padding:24px;background:rgba(19,29,24,.52);backdrop-filter:blur(3px)}.confirm-dialog{width:min(100%,480px);display:grid;grid-template-columns:44px 1fr;gap:18px;padding:28px;background:var(--paper);border:1px solid var(--line);border-radius:16px;box-shadow:0 30px 90px rgba(12,24,17,.28)}.dialog-mark{width:44px;height:44px;display:grid;place-items:center;border-radius:50%;background:#f8e7e2;color:#a2372c;font-size:1.3rem;font-weight:700}.confirm-dialog h2{font-size:2rem}.confirm-dialog .eyebrow{margin-bottom:10px;color:#a2372c}.dialog-message{margin:14px 0 0;color:var(--muted)}.dialog-actions{grid-column:1/-1;display:flex;justify-content:flex-end;gap:10px;margin-top:8px}.danger-button{border:0;border-radius:8px;padding:13px 20px;background:#a2372c;color:white;font:600 1rem inherit;cursor:pointer}@media(max-width:520px){.confirm-dialog{grid-template-columns:1fr}.dialog-actions{grid-column:1;flex-direction:column-reverse}.dialog-actions button{width:100%}}</style>
