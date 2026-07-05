import { defineStore } from 'pinia'

export const useReviewStore = defineStore('review', {
  state: () => ({ review: null, reports: {}, loading: false, error: null }),
  actions: {
    setReview(review) {
      this.review = review
      this.reports = Object.fromEntries((review.reports || []).map((report) => [report.reviewerRole, report]))
    },
    applyDelta(role, text) {
      const report = this.reports[role] || { reviewerRole: role, contentMarkdown: '', status: 'GENERATING' }
      report.contentMarkdown = `${report.contentMarkdown || ''}${text || ''}`
      this.reports[role] = report
    },
  },
})
