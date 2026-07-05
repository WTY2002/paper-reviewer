import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import SettingsView from './SettingsView.vue'
import { settingsApi } from '../api/settingsApi'

vi.mock('../api/settingsApi', () => ({ settingsApi: { get: vi.fn(), update: vi.fn() } }))

describe('SettingsView', () => {
  it('shows model status without an API key field', async () => {
    settingsApi.get.mockResolvedValue({ email:'ada@example.com', displayName:'Ada', defaultOutputLanguage:'AUTO', model:'qwen-plus' })
    const wrapper = mount(SettingsView); await flushPromises()
    expect(wrapper.text()).toContain('Qwen')
    expect(wrapper.find('input[type="password"]').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('API Key')
  })
})
