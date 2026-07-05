<script setup>
import { reactive, watch } from 'vue'
const props = defineProps({ team: { type: Object, required: true }, saving: Boolean })
const emit = defineEmits(['save','confirm'])
const draft = reactive({ targetVenue:'', reviewers:[] })
watch(() => props.team, (team) => { draft.targetVenue=team?.targetVenue || ''; draft.reviewers=(team?.reviewers || []).map((r)=>({...r})) }, {immediate:true,deep:true})
</script>
<template><section class="team"><header><div><p class="eyebrow">REVIEWER PANEL</p><h2>Five independent lenses</h2></div><label>Target venue<input v-model="draft.targetVenue" placeholder="Journal or conference"></label></header><div class="reviewers"><article v-for="reviewer in draft.reviewers" :key="reviewer.role"><span>{{ reviewer.displayName || reviewer.role }}</span><label>Identity<textarea v-model="reviewer.identityDescription" rows="3"/></label><label>Review focus<textarea v-model="reviewer.reviewFocus" rows="3"/></label></article></div><div class="team-actions"><button class="secondary" :disabled="saving" @click="emit('save',draft)">Save adjustments</button><button class="primary" :disabled="saving" @click="emit('confirm',draft)">Confirm panel</button></div></section></template>
<style scoped>.team header{display:flex;justify-content:space-between;gap:24px;align-items:end}.team header label{width:min(340px,45%)}.reviewers{display:grid;grid-template-columns:repeat(5,minmax(220px,1fr));gap:12px;overflow:auto;margin:28px 0}.reviewers article{background:#f7f6f1;border:1px solid #d9d7ce;border-radius:10px;padding:16px;display:grid;gap:14px}.reviewers article>span{font:600 1.1rem 'Newsreader',serif}.reviewers textarea{resize:vertical;font-size:.82rem}.team-actions{display:flex;justify-content:flex-end;gap:10px}</style>
