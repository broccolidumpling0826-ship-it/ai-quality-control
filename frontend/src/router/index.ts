import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Main',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '质量工作台', icon: 'DataBoard', breadcrumb: ['质量工作台'] }
      },
      {
        path: 'standard-lib',
        name: 'standard-lib',
        component: () => import('@/views/standard-lib/index.vue'),
        meta: { title: '标准库', icon: 'Document', breadcrumb: ['标准库'] }
      },
      {
        path: 'standard-lib/indicators',
        name: 'standard-lib-indicators',
        component: () => import('@/views/indicator/index.vue'),
        meta: { title: '指标项目', icon: 'TrendCharts', breadcrumb: ['标准库', '指标项目'] }
      },
      {
        path: 'standard-lib/gaps',
        name: 'standard-lib-gaps',
        component: () => import('@/views/standard-lib/gaps.vue'),
        meta: { title: '覆盖缺口', icon: 'Warning', breadcrumb: ['标准库', '覆盖缺口'] }
      },
      {
        path: 'inspection',
        name: 'inspection',
        component: () => import('@/views/inspection/index.vue'),
        meta: { title: '检验录入', icon: 'EditPen', breadcrumb: ['检验录入'] }
      },
      {
        path: 'inspection/form',
        name: 'inspection-form',
        component: () => import('@/views/inspection/form.vue'),
        meta: { title: '新建检验', icon: 'EditPen', breadcrumb: ['检验录入', '新建检验'] }
      },
      {
        path: 'judgment',
        name: 'judgment',
        component: () => import('@/views/judgment/index.vue'),
        meta: { title: '判定解释', icon: 'Stamp', breadcrumb: ['判定解释'] }
      },
      {
        path: 'judgment/explanation',
        name: 'judgment-explanation',
        component: () => import('@/views/judgment/explanation.vue'),
        meta: { title: '判定详情', icon: 'Stamp', breadcrumb: ['判定解释', '判定详情'] }
      },
      {
        path: 'reinspection',
        name: 'reinspection',
        component: () => import('@/views/reinspection/index.vue'),
        meta: { title: '复检管理', icon: 'RefreshRight', breadcrumb: ['复检管理'] }
      },
      {
        path: 're-judgment',
        name: 're-judgment',
        component: () => import('@/views/re-judgment/index.vue'),
        meta: { title: '改判申请', icon: 'Edit', breadcrumb: ['改判申请'] }
      },
      {
        path: 're-judgment/form',
        name: 're-judgment-form',
        component: () => import('@/views/re-judgment/form.vue'),
        meta: { title: '发起改判', icon: 'Edit', breadcrumb: ['改判申请', '发起改判'] }
      },
      {
        path: 're-judgment/detail',
        name: 're-judgment-detail',
        component: () => import('@/views/re-judgment/detail.vue'),
        meta: { title: '改判详情', icon: 'Edit', breadcrumb: ['改判申请', '改判详情'] }
      },
      {
        path: 'concession',
        name: 'concession',
        component: () => import('@/views/concession/index.vue'),
        meta: { title: '让步接收', icon: 'Check', breadcrumb: ['让步接收'] }
      },
      {
        path: 'concession/apply',
        name: 'concession-apply',
        component: () => import('@/views/concession/form.vue'),
        meta: { title: '发起让步申请', icon: 'Check', breadcrumb: ['让步接收', '发起让步申请'] }
      },
      {
        path: 'concession/detail',
        name: 'concession-detail',
        component: () => import('@/views/concession/detail.vue'),
        meta: { title: '让步详情', icon: 'Check', breadcrumb: ['让步接收', '让步详情'] }
      },
      {
        path: 'cert-data',
        name: 'cert-data',
        component: () => import('@/views/cert-data/index.vue'),
        meta: { title: '质保书数据', icon: 'Tickets', breadcrumb: ['质保书数据'] }
      },
      {
        path: 'statistics',
        name: 'statistics',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '质量统计', icon: 'PieChart', breadcrumb: ['质量统计'] }
      },
      {
        path: 'audit',
        name: 'audit',
        component: () => import('@/views/audit/index.vue'),
        meta: { title: '权限审计', icon: 'Lock', breadcrumb: ['权限审计'] }
      },
      {
        path: 'admin/users',
        name: 'admin-users',
        component: () => import('@/views/admin/users/index.vue'),
        meta: { title: '账号管理', icon: 'User', breadcrumb: ['管理员', '账号管理'], requiresAdmin: true }
      },
      {
        path: 'admin/dict',
        name: 'admin-dict',
        component: () => import('@/views/admin/dict/index.vue'),
        meta: { title: '数据字典', icon: 'List', breadcrumb: ['管理员', '数据字典'], requiresAdmin: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ left: 0, top: 0 })
})

// 全局前置守卫：无 token 强制跳转登录
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !authStore.token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  if (to.path === '/login' && authStore.token) {
    next('/dashboard')
    return
  }
  if (to.meta.requiresAdmin && authStore.userInfo?.role !== 'ADMIN') {
    next('/dashboard')
    return
  }
  next()
})

// 修改页面标题
router.afterEach((to) => {
  const title = to.meta.title as string | undefined
  document.title = title ? `${title} - 质量管理系统` : '质量管理系统'
})

export default router
