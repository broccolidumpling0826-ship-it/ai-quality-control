import type { Router } from 'vue-router'

let routerInstance: Router | null = null

export function setRouter(router: Router): void {
  routerInstance = router
}

export function getRouter(): Router {
  if (!routerInstance) {
    throw new Error('[Router] 实例尚未初始化')
  }
  return routerInstance
}
