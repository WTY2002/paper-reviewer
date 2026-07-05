import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import DashboardView from './DashboardView.vue'
import { dashboardApi } from '../api/dashboardApi'
vi.mock('../api/dashboardApi',()=>({dashboardApi:{get:vi.fn()}}))
describe('DashboardView',()=>{
  it('shows summary metrics and recent review records',async()=>{
    dashboardApi.get.mockResolvedValue({paperCount:2,reviewCount:4,activeReviewCount:1,completedReviewCount:3,recentReviews:[{reviewId:2,paperTitle:'Careful Science',reviewType:'FULL',status:'REVIEWING'}]})
    const wrapper=mount(DashboardView,{global:{stubs:{RouterLink:{template:'<a><slot/></a>'}}}});await flushPromises()
    expect(wrapper.text()).toContain('Careful Science');expect(wrapper.text()).toContain('In progress1');expect(wrapper.text()).not.toContain('Recent papers')
  })
})
