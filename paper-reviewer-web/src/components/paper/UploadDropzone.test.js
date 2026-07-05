import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import UploadDropzone from './UploadDropzone.vue'

describe('UploadDropzone', () => {
  it('rejects non-PDF files', async () => {
    const wrapper = mount(UploadDropzone)
    const input = wrapper.get('input')
    Object.defineProperty(input.element, 'files', { value: [new File(['x'], 'notes.txt', { type: 'text/plain' })] })
    await input.trigger('change')
    expect(wrapper.text()).toContain('Choose a PDF file')
    expect(wrapper.emitted('selected')).toBeUndefined()
  })

  it('accepts a PDF under 20 MB', async () => {
    const wrapper = mount(UploadDropzone)
    const input = wrapper.get('input')
    const file = new File(['%PDF-1.4'], 'paper.pdf', { type: 'application/pdf' })
    Object.defineProperty(input.element, 'files', { value: [file] })
    await input.trigger('change')
    expect(wrapper.emitted('selected')[0][0]).toBe(file)
  })
})
