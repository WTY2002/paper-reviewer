import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import HistoryView from './HistoryView.vue'
import { reviewApi } from '../api/reviewApi'
import { paperApi } from '../api/paperApi'

const router = vi.hoisted(() => ({ push: vi.fn() }))
vi.mock('vue-router', () => ({ useRouter: () => router }))
vi.mock('../api/reviewApi', () => ({ reviewApi: { list: vi.fn(), remove: vi.fn() } }))
vi.mock('../api/paperApi', () => ({ paperApi: { list: vi.fn() } }))

describe('HistoryView', () => {
  it('lists review records with paper titles and deletes after confirmation', async () => {
    reviewApi.list.mockResolvedValue([{ reviewId: 3, paperTitle: 'Mine', reviewType: 'FULL', status: 'COMPLETED', createdAt: '2026-07-05T08:00:00Z' }])
    reviewApi.remove.mockResolvedValue()
    const wrapper = mount(HistoryView, { attachTo: document.body, global: { stubs: { RouterLink: { template: '<a><slot/></a>' } } } })
    await flushPromises()
    expect(wrapper.text()).toContain('Mine')
    expect(wrapper.text()).toContain('FULL · COMPLETED')
    await wrapper.get('button.danger').trigger('click')
    document.body.querySelector('.danger-button').click(); await flushPromises()
    expect(reviewApi.remove).toHaveBeenCalledWith(3)
    wrapper.unmount()
  })

  it('opens the review detail when a record is clicked', async () => {
    reviewApi.list.mockResolvedValue([{ reviewId: 8, paperTitle: 'Paper', reviewType: 'QUICK', status: 'COMPLETED' }])
    const wrapper = mount(HistoryView, { global: { stubs: { RouterLink: { template: '<a><slot/></a>' } } } })
    await flushPromises(); await wrapper.get('article.history-row').trigger('click')
    expect(router.push).toHaveBeenCalledWith('/reviews/8')
  })

  it('fills missing titles from the related paper', async () => {
    reviewApi.list.mockResolvedValue([{ reviewId: 9, paperId: 4, reviewType: 'FULL', status: 'COMPLETED' }])
    paperApi.list.mockResolvedValue([{ paperId: 4, title: 'Recovered title' }])
    const wrapper = mount(HistoryView, { global: { stubs: { RouterLink: { template: '<a><slot/></a>' } } } })
    await flushPromises()
    expect(wrapper.text()).toContain('Recovered title')
  })
})
