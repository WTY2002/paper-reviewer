import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ConfirmDialog from './ConfirmDialog.vue'

describe('ConfirmDialog',()=>{
  it('emits explicit confirm and cancel actions',async()=>{
    const wrapper=mount(ConfirmDialog,{props:{open:true,title:'Delete?',message:'Permanent action.'},attachTo:document.body})
    const buttons=document.body.querySelectorAll('.dialog-actions button')
    await buttons[0].click();expect(wrapper.emitted('cancel')).toHaveLength(1)
    await buttons[1].click();expect(wrapper.emitted('confirm')).toHaveLength(1)
    wrapper.unmount()
  })
})
