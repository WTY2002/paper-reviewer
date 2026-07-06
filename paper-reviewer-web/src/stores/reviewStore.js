import { defineStore } from 'pinia'

export const useReviewStore = defineStore('review', {
  state: () => ({ review: null, reports: {} }),
  actions: {
    setReview(review) {
      this.review = review
      this.reports = Object.fromEntries((review.reports || []).map((report) => [report.reviewerRole, report]))
    },
  },
})
