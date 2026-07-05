import { flushPromises, mount } from '@vue/test-utils'
import { vi } from 'vitest'
import ExportActions from './ExportActions.vue'

const mocks=vi.hoisted(()=>({createReview:vi.fn(),download:vi.fn()}))
vi.mock('../../api/exportApi',()=>({exportApi:{createReview:mocks.createReview,createRereview:vi.fn(),download:mocks.download}}))

describe('ExportActions',()=>{
  it('creates and downloads a markdown export',async()=>{
    mocks.createReview.mockResolvedValue({exportId:7});mocks.download.mockResolvedValue(new Blob(['# Review'],{type:'text/markdown'}))
    URL.createObjectURL=vi.fn(()=> 'blob:review');URL.revokeObjectURL=vi.fn()
    const click=vi.spyOn(HTMLAnchorElement.prototype,'click').mockImplementation(()=>{})
    const wrapper=mount(ExportActions,{props:{reviewId:3}})
    await wrapper.findAll('button')[0].trigger('click');await flushPromises()
    expect(mocks.createReview).toHaveBeenCalledWith(3,'MARKDOWN');expect(click).toHaveBeenCalled()
  })
})
