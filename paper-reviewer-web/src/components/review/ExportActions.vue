<script setup>
import { ref } from 'vue';import { exportApi } from '../../api/exportApi'
const props=defineProps({reviewId:[String,Number],rereviewId:[String,Number]});const loading=ref('');const error=ref('')
async function run(type){
  loading.value=type;error.value=''
  try{
    const made=props.rereviewId?await exportApi.createRereview(props.rereviewId,type):await exportApi.createReview(props.reviewId,type)
    const blob=await exportApi.download(made.exportId)
    if(!(blob instanceof Blob)||blob.size===0)throw new Error('The exported file is empty.')
    const url=URL.createObjectURL(blob);const link=document.createElement('a')
    link.href=url;link.download=`peer-review.${type==='PDF'?'pdf':'md'}`;link.style.display='none'
    document.body.appendChild(link);link.click();link.remove()
    setTimeout(()=>URL.revokeObjectURL(url),1000)
  }catch(e){error.value=e.message}finally{loading.value=''}
}
</script>
<template><div class="exports"><span>Export complete report</span><button class="secondary" :disabled="loading!==''" @click="run('MARKDOWN')">{{ loading==='MARKDOWN'?'Preparing…':'Markdown' }}</button><button class="primary" :disabled="loading!==''" @click="run('PDF')">{{ loading==='PDF'?'Preparing…':'PDF' }}</button><small v-if="error">{{ error }}</small></div></template>
<style scoped>.exports{display:flex;align-items:center;justify-content:flex-end;gap:10px;margin:14px 0}.exports>span{margin-right:auto;color:#667069;font-size:.83rem}.exports button{padding:9px 14px}.exports small{color:#a2372c}</style>
