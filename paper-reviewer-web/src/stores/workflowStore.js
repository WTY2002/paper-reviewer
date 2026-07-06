import { defineStore } from 'pinia'

export const useWorkflowStore = defineStore('workflow', {
  state: () => ({ events: [], connected: false, error: null }),
  actions: {
    add(event) {
      if (this.events.some((item) => item.sequence === event.sequence)) return
      this.events.push(event); this.events.sort((a, b) => a.sequence - b.sequence)
      if (event.type === 'REVIEW_FAILED') this.error = event.payload?.message || 'Review failed'
    },
    reset() { this.events = []; this.connected = false; this.error = null },
  },
})
