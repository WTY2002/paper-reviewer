import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useWorkflowStore } from './workflowStore'

describe('review stream stores', () => {
  beforeEach(() => setActivePinia(createPinia()))
  it('orders and deduplicates workflow events', () => {
    const store = useWorkflowStore(); store.add({ sequence: 2, stage: 'REVIEWING' }); store.add({ sequence: 1, stage: 'ANALYZING' }); store.add({ sequence: 2, stage: 'REVIEWING' })
    expect(store.events.map((e) => e.sequence)).toEqual([1, 2])
  })
})
