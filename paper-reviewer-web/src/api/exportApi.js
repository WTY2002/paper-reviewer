import { apiRequest } from './http'
export const exportApi={
 createReview(id,exportType){return apiRequest(`/api/reviews/${id}/exports`,{method:'POST',body:JSON.stringify({exportType})})},
 createRereview(id,exportType){return apiRequest(`/api/rereviews/${id}/exports`,{method:'POST',body:JSON.stringify({exportType})})},
 download(id){return apiRequest(`/api/exports/${id}`)},
}
