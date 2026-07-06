import { fetchEventSource } from '@microsoft/fetch-event-source'
import { useAuthStore } from '../stores/authStore'
import { useWorkflowStore } from '../stores/workflowStore'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

export function subscribeToReview(reviewId) {
  const auth = useAuthStore(); const workflow = useWorkflowStore(); const controller = new AbortController()
  fetchEventSource(`${API_BASE_URL}/api/reviews/${reviewId}/stream`, {
    signal: controller.signal,
    headers: { Authorization: `Bearer ${auth.token}`, Accept: 'text/event-stream' },
    openWhenHidden: true,
    async onopen(response) { if (!response.ok) throw new Error(`Stream rejected (${response.status})`); workflow.connected = true },
    onmessage(message) {
      if (!message.data) return
      const event = JSON.parse(message.data); workflow.add(event)
    },
    onclose() { workflow.connected = false },
    onerror(error) { workflow.connected = false; workflow.error = error.message; throw error },
  }).catch((error) => { if (!controller.signal.aborted) workflow.error = error.message })
  return () => controller.abort()
}
