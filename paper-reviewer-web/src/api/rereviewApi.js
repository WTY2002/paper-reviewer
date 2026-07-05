import { apiRequest } from './http'
export const rereviewApi={
  create(reviewId,revisedFile,responseFile,outputLanguage='AUTO'){const body=new FormData();body.append('revisedFile',revisedFile);body.append('responseFile',responseFile);body.append('outputLanguage',outputLanguage);return apiRequest(`/api/reviews/${reviewId}/rereviews`,{method:'POST',body})},
  start(id){return apiRequest(`/api/rereviews/${id}/start`,{method:'POST'})},
  get(id){return apiRequest(`/api/rereviews/${id}`)},
  remove(id){return apiRequest(`/api/rereviews/${id}`,{method:'DELETE'})},
}
