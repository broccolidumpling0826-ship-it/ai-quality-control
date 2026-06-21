// ─── 通用响应结构 ────────────────────────────────────────

export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T = unknown> {
  records: T[]
  total: number
  current: number
  size: number
}

// ─── 字典相关 ─────────────────────────────────────────────

export interface DictItem {
  value: string
  label: string
  colorTag: string
  sortNo: number
}

export type DictMap = Record<string, DictItem[]>

// ─── 用户 & 认证 ──────────────────────────────────────────

export interface UserInfo {
  token: string
  tokenName: string
  userNo: string
  username: string
  role: string
  department: string
  roles?: string[]
  permissions?: string[]
}

export interface LoginForm {
  /** 表单展示用工号；提交后端时映射为 userNo */
  username: string
  password: string
}

/** 登录请求体（与后端 LoginCmd 一致） */
export interface LoginCmd {
  userNo: string
  password: string
}

// ─── 分页查询参数 ─────────────────────────────────────────

export interface PageQuery {
  pageNum: number
  pageSize: number
  [key: string]: unknown
}

// ─── 检验相关 ─────────────────────────────────────────────

export interface InspectionRecord {
  id: string | number
  batchNo: string
  productName: string
  productCode: string
  standard: string
  inspectionDate: string
  inspector: string
  status: string
  department: string
  remark: string
  createTime: string
  updateTime: string
}

export interface JudgmentRecord {
  id: string | number
  batchNo: string
  productName: string
  judgmentResult: string
  judgeBy: string
  judgeTime: string
  remark: string
  status: string
}

export interface ReinspectionRecord {
  id: string | number
  originalBatchNo: string
  batchNo: string
  reason: string
  applyBy: string
  applyTime: string
  status: string
}

export interface ConcessionRecord {
  id: string | number
  batchNo: string
  productName: string
  defectDesc: string
  applyBy: string
  applyTime: string
  approveBy: string
  approveTime: string
  status: string
}

// ─── 仪表盘相关 ───────────────────────────────────────────

export interface DashboardSummary {
  pendingJudgment: number
  unqualifiedBatch: number
  reinspectionTask: number
  concessionApproval: number
  aiRiskWarning: number
  lowConfidenceReview: number
  pendingStandardConflict: number
}

export interface PendingItem {
  id: string | number
  type: string
  batchNo: string
  description: string
  priority: string
  createTime: string
  assignTo: string
}

export interface SystemMessage {
  id: string | number
  title: string
  content: string
  level: 'info' | 'warning' | 'error' | 'success'
  createTime: string
  isRead: boolean
}

// ─── 标准库 ───────────────────────────────────────────────

export interface StandardLib {
  id: string | number
  standardCode: string
  standardName: string
  version: string
  category: string
  status: string
  effectiveDate: string
  expiryDate: string
  description: string
}

export interface Indicator {
  id: string | number
  indicatorCode: string
  indicatorName: string
  standardId: string | number
  unit: string
  lowerLimit: number | null
  upperLimit: number | null
  targetValue: number | null
  testMethod: string
  status: string
}

// ─── 质保书数据 ───────────────────────────────────────────

export interface CertData {
  id: string | number
  certNo: string
  batchNo: string
  productName: string
  productCode: string
  customer: string
  issueDate: string
  status: string
  fileUrl: string
}

// ─── 权限审计 ─────────────────────────────────────────────

export interface AuditLog {
  id: string | number
  operatorNo: string
  operatorName: string
  action: string
  module: string
  targetId: string
  detail: string
  ip: string
  operateTime: string
}

// ─── 菜单 & RBAC ───────────────────────────────────────────

export interface MenuTreeNode {
  id: string
  parentId?: string
  menuType: 'DIR' | 'MENU' | 'HIDDEN' | 'LINK' | 'IFRAME'
  menuName: string
  path?: string
  component?: string
  routeName?: string
  icon?: string
  permCode?: string
  visible?: number
  sortOrder?: number
  metaJson?: string
  children?: MenuTreeNode[]
}

export interface RoleInfo {
  id: string
  roleCode: string
  roleName: string
  description?: string
  sortOrder?: number
  status?: number
}

export interface PermissionInfo {
  id: string
  permCode: string
  permName: string
  permType?: string
  description?: string
  status?: number
}

// ─── 路由 meta 扩展 ───────────────────────────────────────

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    requiresAuth?: boolean
    breadcrumb?: string[]
    permCode?: string
  }
}
