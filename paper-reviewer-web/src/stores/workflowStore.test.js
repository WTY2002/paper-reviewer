import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useReviewStore } from './reviewStore'
import { useWorkflowStore } from './workflowStore'

describe('review stream stores', () => {
  beforeEach(() => setActivePinia(createPinia()))
  it('orders and deduplicates workflow events', () => {
    const store = useWorkflowStore(); store.add({ sequence: 2, stage: 'REVIEWING' }); store.add({ sequence: 1, stage: 'ANALYZING' }); store.add({ sequence: 2, stage: 'REVIEWING' })
    expect(store.events.map((e) => e.sequence)).toEqual([1, 2]); expect(store.stage).toBe('REVIEWING')
  })
  it('merges incremental reviewer markdown', () => {
    const store = useReviewStore(); store.applyDelta('EIC', 'Hello '); store.applyDelta('EIC', 'world')
    expect(store.reports.EIC.contentMarkdown).toBe('Hello world')
  })
})
