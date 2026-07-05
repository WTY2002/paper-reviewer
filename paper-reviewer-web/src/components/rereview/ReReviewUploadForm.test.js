import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import ReReviewUploadForm from './ReReviewUploadForm.vue'

vi.mock('../../api/rereviewApi',()=>({rereviewApi:{create:vi.fn(),start:vi.fn()}}))

describe('ReReviewUploadForm',()=>{
  it('enables verification after both PDF files are selected',async()=>{
    const wrapper=mount(ReReviewUploadForm,{props:{reviewId:1}})
    const inputs=wrapper.findAll('input[type="file"]')
    Object.defineProperty(inputs[0].element,'files',{value:[new File(['%PDF-1.4'],'revised.pdf',{type:'application/pdf'})]})
    await inputs[0].trigger('change')
    expect(wrapper.get('button').attributes('disabled')).toBeDefined()
    Object.defineProperty(inputs[1].element,'files',{value:[new File(['%PDF-1.4'],'response.pdf',{type:'application/pdf'})]})
    await inputs[1].trigger('change')
    expect(wrapper.get('button').attributes('disabled')).toBeUndefined()
    expect(wrapper.text()).toContain('revised.pdf');expect(wrapper.text()).toContain('response.pdf')
  })
})
