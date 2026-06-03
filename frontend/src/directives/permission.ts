import type { Directive, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/store/auth'

function checkPermission(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
  const authStore = useAuthStore()
  const permissions = authStore.userInfo?.permissions || []

  if (permissions.includes('*')) {
    return
  }

  const required = Array.isArray(binding.value) ? binding.value : [binding.value]
  const hasPerm = required.some((p) => permissions.includes(p))

  if (!hasPerm) {
    el.parentNode?.removeChild(el)
  }
}

export const permissionDirective: Directive = {
  mounted(el, binding) {
    checkPermission(el as HTMLElement, binding)
  },
  updated(el, binding) {
    checkPermission(el as HTMLElement, binding)
  }
}

export function hasPermission(code: string): boolean {
  const authStore = useAuthStore()
  const permissions = authStore.userInfo?.permissions || []
  return permissions.includes('*') || permissions.includes(code)
}
