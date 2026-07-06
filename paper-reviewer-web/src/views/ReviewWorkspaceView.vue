<script setup>
import { computed, defineAsyncComponent, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { paperApi } from '../api/paperApi'
import { reviewApi } from '../api/reviewApi'
import { rereviewApi } from '../api/rereviewApi'
import { useReviewStore } from '../stores/reviewStore'
import { useWorkflowStore } from '../stores/workflowStore'
import { subscribeToReview } from '../stream/reviewEventSource'
import ReviewerTeamEditor from '../components/review/ReviewerTeamEditor.vue'
import ReviewerTabs from '../components/review/ReviewerTabs.vue'
import ReviewMarkdownPanel from '../components/review/ReviewMarkdownPanel.vue'
import ScoreTable from '../components/review/ScoreTable.vue'
import WorkflowBar from '../components/review/WorkflowBar.vue'
import VerificationChecklist from '../components/rereview/VerificationChecklist.vue'
import ExportActions from '../components/review/ExportActions.vue'

const PdfViewer=defineAsyncComponent(()=>import('../components/paper/PdfViewer.vue'))

const route = useRoute(); const reviewStore = useReviewStore(); const workflow = useWorkflowStore()
const pdfSource = ref(); const team = ref(); const selectedRole = ref('EIC'); const activePane = ref('REVIEW'); const loading = ref(true); const saving = ref(false); const error = ref(''); let stopStream; let blobUrl
const review = computed(() => reviewStore.review)
const reviewerRoles = computed(() => Object.keys(reviewStore.reports).length ? Object.keys(reviewStore.reports) : ['EIC','METHODOLOGY','DOMAIN','PERSPECTIVE','DEVILS_ADVOCATE'])
const roles = computed(() => review.value?.reviewType === 'FULL' && review.value?.status === 'COMPLETED'
  ? [...reviewerRoles.value, 'DECISION', 'ROADMAP', 'QUESTIONS'] : reviewerRoles.value)
const report = computed(() => {
  if(selectedRole.value==='DECISION')return {contentMarkdown:review.value?.editorialDecisionMarkdown}
  if(selectedRole.value==='ROADMAP')return {contentMarkdown:review.value?.revisionRoadmapMarkdown}
  if(selectedRole.value==='QUESTIONS')return {contentMarkdown:review.value?.authorQuestionsMarkdown}
  return reviewStore.reports[selectedRole.value] || {}
})
const isReviewerReport = computed(() => reviewerRoles.value.includes(selectedRole.value))
const isRereview=computed(()=>Boolean(route.params.rereviewId))
const waitingForTeam = computed(() => ['TEAM_PENDING','ANALYZING'].includes(review.value?.status) && team.value)
const readyToStart=computed(()=>review.value?.status==='TEAM_CONFIRMED')

async function load() {
  stopStream?.(); stopStream=undefined
  if(blobUrl){URL.revokeObjectURL(blobUrl);blobUrl=undefined;pdfSource.value=undefined}
  loading.value = true; error.value = ''; workflow.reset()
  try {
    const data = isRereview.value ? await rereviewApi.get(route.params.rereviewId) : await reviewApi.get(route.params.reviewId)
    if(isRereview.value){reviewStore.setReview({...data,reviewType:'REREVIEW',paperId:data.revisedPaperId,reports:[{reviewerRole:'EIC',contentMarkdown:data.resultMarkdown}]})}else{reviewStore.setReview(data)}
    const paperId = data.paperId || data.paper?.id
    if (paperId) { const blob = await paperApi.pdf(paperId); blobUrl = URL.createObjectURL(blob); pdfSource.value = blobUrl }
    if (!isRereview.value && data.reviewType === 'FULL' && ['TEAM_PENDING','TEAM_CONFIRMED','ANALYZING'].includes(data.status)) {
      try { const teamData = await reviewApi.team(data.id || route.params.reviewId); team.value = teamData.team || teamData } catch { /* generation may still be running */ }
    }
    stopStream = subscribeToReview(isRereview.value ? data.originalReviewId : route.params.reviewId)
  } catch (e) { error.value = e.message }
  finally { loading.value = false }
}
async function saveTeam(draft) { saving.value=true; try { team.value=await reviewApi.updateTeam(route.params.reviewId,draft) } catch(e){error.value=e.message} finally{saving.value=false} }
async function confirmTeam(draft) { saving.value=true; try { await reviewApi.updateTeam(route.params.reviewId,draft); await reviewApi.confirmTeam(route.params.reviewId); await load() } catch(e){error.value=e.message} finally{saving.value=false} }
async function startReview(){saving.value=true;try{await reviewApi.start(route.params.reviewId);await load()}catch(e){error.value=e.message}finally{saving.value=false}}
onMounted(load); onBeforeUnmount(()=>{ stopStream?.(); if(blobUrl) URL.revokeObjectURL(blobUrl) })
</script>

<template><section class="workspace-page"><div v-if="loading" class="panel">Loading the review room…</div><div v-else-if="error" class="error">{{ error }}</div><template v-else><header class="workspace-head"><div><p class="eyebrow">{{ review.reviewType }} REVIEW</p><h1>{{ review.paperTitle || review.paper?.title || (isRereview?'Revision verification':'Manuscript review') }}</h1></div><span class="status">{{ review.status }}</span></header><ExportActions v-if="review.status==='COMPLETED'" :review-id="route.params.reviewId" :rereview-id="route.params.rereviewId"/><div v-if="waitingForTeam" class="panel"><ReviewerTeamEditor :team="team" :saving="saving" @save="saveTeam" @confirm="confirmTeam"/></div><div v-else-if="readyToStart" class="panel start-panel"><div><h2>Reviewer panel confirmed.</h2><p class="muted">The five reviewers are ready to begin their independent reports.</p></div><button class="primary" :disabled="saving" @click="startReview">{{saving?'Starting…':'Start full review'}}</button></div><template v-else><nav class="pane-switch" aria-label="Review workspace view"><button :class="{active:activePane==='PDF'}" @click="activePane='PDF'">Paper PDF</button><button :class="{active:activePane==='REVIEW'}" @click="activePane='REVIEW'">Review Markdown</button></nav><div class="workspace"><PdfViewer v-if="activePane==='PDF'" :source="pdfSource"/><section v-show="activePane==='REVIEW'" class="report"><VerificationChecklist v-if="isRereview" :checklist="review.checklist||{}"/><template v-else><ReviewerTabs v-model="selectedRole" :roles="roles"/><ReviewMarkdownPanel :content="report.contentMarkdown"/><ScoreTable v-if="isReviewerReport" :scores="report.scores || report.dimensionScores"/></template></section></div></template><WorkflowBar :events="workflow.events" :connected="workflow.connected" :error="workflow.error"/></template></section></template>

<style scoped>.workspace-page{max-width:1600px}.workspace-head{display:flex;justify-content:space-between;align-items:end;gap:20px;margin-bottom:24px}.workspace-head h1{font-size:clamp(2.1rem,4vw,4rem)}.status{border:1px solid #aeb7af;border-radius:999px;padding:7px 12px;font-size:.75rem}.pane-switch{display:flex;width:max-content;padding:4px;margin:12px 0;background:#e8e7e0;border-radius:9px}.pane-switch button{border:0;background:transparent;padding:9px 18px;border-radius:7px;cursor:pointer}.pane-switch button.active{background:#174b38;color:white}.workspace{display:block;min-height:0}.workspace>.pdf,.report{height:calc(100vh - 190px);width:100%}.report{overflow:auto;background:#fbfaf6;border:1px solid #d9d7ce;border-radius:10px;padding:0 26px 30px}.report>:first-child{position:sticky;top:0;background:#fbfaf6;z-index:2}.workflow{margin-top:14px}.start-panel{display:flex;justify-content:space-between;align-items:center}.start-panel p{margin-bottom:0}@media(max-width:700px){.workspace>.pdf,.report{height:auto;min-height:600px}.exports{flex-wrap:wrap}}</style>
