<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import UploadDropzone from '../components/paper/UploadDropzone.vue'
import { paperApi } from '../api/paperApi'
import { reviewApi } from '../api/reviewApi'
import ReReviewUploadForm from '../components/rereview/ReReviewUploadForm.vue'

const router = useRouter(); const route=useRoute(); const file = ref(); const reviewType = ref('FULL'); const loading = ref(false); const error = ref('')
function selected(value) { file.value = value; error.value = '' }
async function upload() {
  if (!file.value) return
  loading.value = true; error.value = ''
  try {
    const paper = await paperApi.upload(file.value)
    const paperId = paper.paperId || paper.id
    const review = await reviewApi.analyze({ paperId, reviewType: reviewType.value, outputLanguage: 'AUTO' })
    router.push(`/reviews/${review.reviewId || review.id}`)
  }
  catch (e) { error.value = e.message }
  finally { loading.value = false }
}
function openRereview(result){router.push(`/rereviews/${result.rereviewId||result.id}`)}
</script>

<template><section><template v-if="route.query.sourceReview"><p class="eyebrow">RE-REVIEW</p><h1>Verify the revision.</h1><ReReviewUploadForm :review-id="route.query.sourceReview" @created="openRereview"/></template><template v-else><p class="eyebrow">NEW REVIEW</p><h1>Bring the manuscript.</h1><p class="intro muted">We’ll extract the text, identify the field, and assemble a five-person reviewer panel.</p><div class="upload-grid"><UploadDropzone @selected="selected"/><aside class="panel options"><h2>Review setup</h2><label>Review mode<select v-model="reviewType"><option value="FULL">Full review</option><option value="QUICK">Quick assessment</option></select></label><div v-if="file" class="file-pill"><strong>{{ file.name }}</strong><span>{{ (file.size/1024/1024).toFixed(1) }} MB</span></div><p v-if="error" class="error">{{ error }}</p><button class="primary" :disabled="!file || loading" @click="upload">{{ loading ? 'Securely uploading…' : 'Analyze paper' }}</button></aside></div></template></section></template>

<style scoped>.intro{max-width:620px;font-size:1.08rem}.upload-grid{display:grid;grid-template-columns:minmax(0,1.5fr) minmax(280px,.7fr);gap:24px;margin-top:44px}.options{margin:0;display:flex;flex-direction:column;gap:24px}.options .primary{margin-top:auto}.file-pill{display:flex;justify-content:space-between;gap:12px;padding:14px;background:#e9eee8;border-radius:8px}.file-pill strong{overflow:hidden;text-overflow:ellipsis}.file-pill span{white-space:nowrap;color:#6e756f}@media(max-width:900px){.upload-grid{grid-template-columns:1fr}}</style>
