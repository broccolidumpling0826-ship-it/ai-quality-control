<template>
  <div class="aqc-layout">
    <!-- ══ SIDEBAR ══════════════════════════════════════════════ -->
    <nav class="sidebar" :class="{ collapsed: isCollapsed }">
      <!-- 扫描线纹理伪元素由 CSS 实现 -->

      <!-- Logo 区域 -->
      <div class="sidebar-header">
        <div class="sidebar-logo" :class="{ collapsed: isCollapsed }">
          <div class="logo-mark"></div>
          <div v-if="!isCollapsed" class="logo-text-wrap">
            <div class="logo-text">AQC</div>
            <div class="logo-sub">ai-quality-control</div>
          </div>
        </div>
        <!-- 折叠按钮 -->
        <button class="collapse-btn" @click="toggleCollapse" :title="isCollapsed ? '展开侧栏' : '折叠侧栏'">
          <span class="collapse-icon">{{ isCollapsed ? '▶' : '◀' }}</span>
        </button>
      </div>

      <!-- 导航区域 -->
      <div class="nav-scroll">
        <!-- §4.1 质量工作台 -->
        <router-link to="/dashboard" custom v-slot="{ isActive, navigate }">
          <div
            class="nav-item"
            :class="{ active: isActive || isPathActive('/dashboard') }"
            @click="navigate"
            :title="isCollapsed ? '质量工作台' : ''"
          >
            <span class="nav-icon">⬡</span>
            <span v-if="!isCollapsed" class="nav-label">质量工作台</span>
            <span v-if="!isCollapsed && dashboardBadge" class="nav-badge red">{{ dashboardBadge }}</span>
          </div>
        </router-link>

        <!-- §4.2 标准库 -->
        <div v-if="!isCollapsed" class="nav-section-label">标准库</div>
        <div v-else class="nav-section-divider"></div>

        <router-link to="/standard-lib" custom v-slot="{ isActive, navigate }">
          <div
            class="nav-item"
            :class="{ active: isActive && route.path === '/standard-lib' }"
            @click="navigate"
            :title="isCollapsed ? '标准维护' : ''"
          >
            <span class="nav-icon">◉</span>
            <span v-if="!isCollapsed" class="nav-label">标准维护</span>
          </div>
        </router-link>

        <router-link to="/standard-lib/indicators" custom v-slot="{ isActive, navigate }">
          <div
            class="nav-item nav-item-sub"
            :class="{ active: isActive || isPathActive('/standard-lib/indicators') }"
            @click="navigate"
            :title="isCollapsed ? '指标项目' : ''"
          >
            <span class="nav-icon">◧</span>
            <span v-if="!isCollapsed" class="nav-label">指标项目</span>
          </div>
        </router-link>

        <router-link to="/standard-lib/gaps" custom v-slot="{ isActive, navigate }">
          <div
            class="nav-item nav-item-sub"
            :class="{ active: isActive || isPathActive('/standard-lib/gaps') }"
            @click="navigate"
            :title="isCollapsed ? '覆盖缺口' : ''"
          >
            <span class="nav-icon" style="color: var(--gold, #FFB400);">⚠</span>
            <span v-if="!isCollapsed" class="nav-label" style="color: var(--gold, #FFB400);">覆盖缺口</span>
          </div>
        </router-link>

        <!-- §4.3–4.4 检验与判定 -->
        <div v-if="!isCollapsed" class="nav-section-label">检验与判定</div>
        <div v-else class="nav-section-divider"></div>

        <router-link to="/inspection" custom v-slot="{ isActive, navigate }">
          <div
            class="nav-item"
            :class="{ active: isActive || isPathActive('/inspection') }"
            @click="navigate"
            :title="isCollapsed ? '检验录入' : ''"
          >
            <span class="nav-icon">✎</span>
            <span v-if="!isCollapsed" class="nav-label">检验录入</span>
          </div>
        </router-link>

        <router-link to="/judgment" custom v-slot="{ isActive, navigate }">
          <div
            class="nav-item"
            :class="{ active: isActive || isPathActive('/judgment') }"
            @click="navigate"
            :title="isCollapsed ? '判定解释' : ''"
          >
            <span class="nav-icon">◈</span>
            <span v-if="!isCollapsed" class="nav-label">判定解释</span>
          </div>
        </router-link>

        <!-- §4.5–4.7 质量流程 -->
        <div v-if="!isCollapsed" class="nav-section-label">质量流程</div>
        <div v-else class="nav-section-divider"></div>

        <router-link
          v-for="item in processNavItems"
          :key="item.path"
          :to="item.path"
          custom
          v-slot="{ isActive, navigate }"
        >
          <div
            class="nav-item"
            :class="{ active: isActive || isPathActive(item.path) }"
            @click="navigate"
            :title="isCollapsed ? item.label : ''"
          >
            <span class="nav-icon">{{ item.icon }}</span>
            <span v-if="!isCollapsed" class="nav-label">{{ item.label }}</span>
            <span
              v-if="!isCollapsed && item.badge"
              class="nav-badge"
              :class="item.badgeType"
            >{{ item.badge }}</span>
          </div>
        </router-link>

        <!-- §4.8–4.9 数据汇总 -->
        <div v-if="!isCollapsed" class="nav-section-label">数据汇总</div>
        <div v-else class="nav-section-divider"></div>

        <router-link
          v-for="item in dataNavItems"
          :key="item.path"
          :to="item.path"
          custom
          v-slot="{ isActive, navigate }"
        >
          <div
            class="nav-item"
            :class="{ active: isActive || isPathActive(item.path) }"
            @click="navigate"
            :title="isCollapsed ? item.label : ''"
          >
            <span class="nav-icon">{{ item.icon }}</span>
            <span v-if="!isCollapsed" class="nav-label">{{ item.label }}</span>
          </div>
        </router-link>

        <!-- §4.10 系统管理 -->
        <div v-if="!isCollapsed" class="nav-section-label">系统管理</div>
        <div v-else class="nav-section-divider"></div>

        <router-link to="/audit" custom v-slot="{ isActive, navigate }">
          <div
            class="nav-item"
            :class="{ active: isActive || isPathActive('/audit') }"
            @click="navigate"
            :title="isCollapsed ? '权限审计' : ''"
          >
            <span class="nav-icon">⊕</span>
            <span v-if="!isCollapsed" class="nav-label">权限审计</span>
          </div>
        </router-link>

        <!-- §4.11–4.12 管理员（ADMIN only） -->
        <template v-if="authStore.userInfo?.role === 'ADMIN'">
          <div v-if="!isCollapsed" class="nav-section-label">管理员</div>
          <div v-else class="nav-section-divider"></div>

          <router-link
            v-for="item in adminNavItems"
            :key="item.path"
            :to="item.path"
            custom
            v-slot="{ isActive, navigate }"
          >
            <div
              class="nav-item"
              :class="{ active: isActive || isPathActive(item.path) }"
              @click="navigate"
              :title="isCollapsed ? item.label : ''"
            >
              <span class="nav-icon">{{ item.icon }}</span>
              <span v-if="!isCollapsed" class="nav-label">{{ item.label }}</span>
            </div>
          </router-link>
        </template>
      </div>

      <!-- 底部用户信息 -->
      <div class="sidebar-footer" :class="{ collapsed: isCollapsed }">
        <el-dropdown @command="handleCommand" trigger="click" placement="top-start">
          <div class="user-info">
            <div class="user-avatar">{{ userInitial }}</div>
            <div v-if="!isCollapsed" class="user-meta">
              <div class="user-name">{{ authStore.userInfo?.username || '未登录' }}</div>
              <div class="user-role">{{ authStore.userInfo?.role || 'OPERATOR' }}</div>
            </div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon> 个人信息
              </el-dropdown-item>
              <el-dropdown-item command="password">
                <el-icon><Lock /></el-icon> 修改密码
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </nav>

    <!-- ══ MAIN ═════════════════════════════════════════════════ -->
    <div class="main-area">
      <!-- 顶栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <div class="topbar-breadcrumb">
            <span class="bc-root">ai-quality-control</span>
            <span class="bc-sep">›</span>
            <span class="bc-current">{{ currentTitle || '质量工作台' }}</span>
          </div>
        </div>
        <div class="topbar-right">
          <!-- 实时时钟 -->
          <span class="topbar-time">{{ currentTime }}</span>

          <!-- 通知铃铛 -->
          <div
            class="notif-btn"
            :class="{ 'has-notif': unreadCount > 0 }"
            @click="showNotifications = true"
            title="消息通知"
          >
            <span class="notif-icon">🔔</span>
            <span v-if="unreadCount > 0" class="notif-dot"></span>
          </div>

          <!-- 刷新 -->
          <div class="topbar-action-btn" @click="handleRefresh" title="刷新页面">
            <span style="font-size: 13px;">↻</span>
          </div>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="content-area">
        <router-view v-slot="{ Component, route }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </main>
    </div>

    <!-- 通知抽屉 -->
    <el-drawer
      v-model="showNotifications"
      title="NOTIFICATIONS / 站内消息"
      direction="rtl"
      size="360px"
    >
      <div v-if="notifications.length === 0" class="empty-notify">
        <el-empty description="暂无新消息" />
      </div>
      <div v-else class="notify-list-panel">
        <div class="notify-header-action">
          <span class="notify-mark-all" @click="markAllRead">全部标读</span>
        </div>
        <div
          v-for="item in notifications"
          :key="item.id"
          class="notify-item"
          :class="{ unread: !item.isRead }"
          @click="markRead(item)"
        >
          <div
            class="notify-level-dot"
            :style="{ background: getLevelColor(item.level) }"
          ></div>
          <div class="notify-body">
            <div class="notify-title-text">{{ item.title }}</div>
            <div class="notify-content-text">{{ item.content }}</div>
            <div class="notify-time-text">{{ item.createTime }}</div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import type { SystemMessage } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// ─── 侧边栏导航配置 ────────────────────────────────────────
interface NavItem {
  path: string
  label: string
  icon: string
  badge?: string | number
  badgeType?: 'orange' | 'blue' | 'red'
}

// §4.1 工作台徽章（待判+不合格+复检+让步数量）
const dashboardBadge = ref<number | null>(null)

// §4.5–4.7 质量流程
const processNavItems: NavItem[] = [
  { path: '/reinspection',  label: '复检管理',   icon: '⟳', badge: 4,   badgeType: 'orange' },
  { path: '/re-judgment',   label: '改判管理',   icon: '↻', badge: 3,   badgeType: 'orange' },
  { path: '/concession',    label: '让步接收',   icon: '◎', badge: 2,   badgeType: 'blue' },
]

// §4.8 质保书数据（先）→ §4.9 质量统计（后）
const dataNavItems: NavItem[] = [
  { path: '/cert-data',     label: '质保书数据', icon: '⊞' },
  { path: '/statistics',    label: '质量统计',   icon: '▦' },
]

// §4.11–4.12 管理员（ADMIN only）
const adminNavItems: NavItem[] = [
  { path: '/admin/users',   label: '账号管理',   icon: '⊛' },
  { path: '/admin/dict',    label: '数据字典',   icon: '≡' },
]

// ─── 侧边栏折叠 ────────────────────────────────────────────
const isCollapsed = ref(false)
function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
}

// ─── 路径激活判断 ──────────────────────────────────────────
function isPathActive(path: string): boolean {
  return route.path === path || route.path.startsWith(path + '/')
}

// ─── 当前页面标题 ──────────────────────────────────────────
const currentTitle = computed(() => route.meta.title as string | undefined)

// ─── 实时时钟 ──────────────────────────────────────────────
const currentTime = ref('')
let clockTimer: ReturnType<typeof setInterval>

function updateClock() {
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  currentTime.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

// ─── 通知 ──────────────────────────────────────────────────
const showNotifications = ref(false)
const notifications = ref<SystemMessage[]>([])
const unreadCount = computed(() => notifications.value.filter((n) => !n.isRead).length)

function getLevelColor(level: string): string {
  const map: Record<string, string> = {
    info:    'var(--text-muted)',
    success: '#16C974',
    warning: '#FF8C00',
    error:   '#FF3B5C'
  }
  return map[level] ?? 'var(--text-muted)'
}

function markRead(item: SystemMessage) {
  item.isRead = true
}

function markAllRead() {
  notifications.value.forEach((n) => (n.isRead = true))
}

// ─── 用户 ──────────────────────────────────────────────────
const userInitial = computed(() => {
  const name = authStore.userInfo?.username || ''
  return name.slice(-2) || '?'
})

async function handleCommand(command: string) {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await authStore.logout()
    router.push('/login')
    ElMessage.success('已安全退出')
  } else if (command === 'profile') {
    ElMessage.info('个人信息功能开发中')
  } else if (command === 'password') {
    ElMessage.info('修改密码功能开发中')
  }
}

function handleRefresh() {
  window.location.reload()
}

// ─── 生命周期 ──────────────────────────────────────────────
onMounted(() => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)
})

onUnmounted(() => {
  clearInterval(clockTimer)
})
</script>

<style scoped>
/* ══════════════════════════════════════════════════════════════
   AQC 主布局 — 工业精密控制室风格
══════════════════════════════════════════════════════════════ */

/* ── 整体布局容器 ─────────────────────────────────────────── */
.aqc-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--bg-base);
  color: var(--text-primary);
  font-family: var(--font-ui);
  font-size: 13px;
}

/* ══ SIDEBAR ══════════════════════════════════════════════════ */
.sidebar {
  width: 220px;
  min-width: 220px;
  background: var(--bg-panel);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  transition: width 0.25s ease, min-width 0.25s ease;
  flex-shrink: 0;
  z-index: 20;
}

/* 扫描线纹理 */
.sidebar::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 3px,
    rgba(0, 212, 255, 0.015) 3px,
    rgba(0, 212, 255, 0.015) 4px
  );
  pointer-events: none;
  z-index: 0;
}

.sidebar.collapsed {
  width: 60px;
  min-width: 60px;
}

/* ── 侧栏顶部（Logo + 折叠按钮） ─────────────────────────── */
.sidebar-header {
  padding: 20px 16px 16px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  overflow: hidden;
}

.sidebar-logo.collapsed {
  justify-content: center;
}

/* 六边形 Logo-mark */
.logo-mark {
  width: 28px;
  height: 28px;
  min-width: 28px;
  background: linear-gradient(135deg, var(--cyan), var(--blue));
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  flex-shrink: 0;
}

.logo-text-wrap {
  overflow: hidden;
  white-space: nowrap;
}

.logo-text {
  font-family: var(--font-data);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.12em;
  color: var(--cyan);
  text-transform: uppercase;
  line-height: 1.3;
}

.logo-sub {
  font-family: var(--font-data);
  font-size: 9px;
  color: var(--text-muted);
  letter-spacing: 0.05em;
  margin-top: 2px;
}

/* 折叠按钮 */
.collapse-btn {
  background: transparent;
  border: 1px solid var(--border);
  color: var(--text-muted);
  width: 22px;
  height: 22px;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s;
  flex-shrink: 0;
  padding: 0;
}

.collapse-btn:hover {
  border-color: var(--cyan);
  color: var(--cyan);
}

.collapse-icon {
  font-size: 9px;
}

/* ── 导航滚动区 ───────────────────────────────────────────── */
.nav-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px 0;
  position: relative;
  z-index: 1;
}

.nav-scroll::-webkit-scrollbar {
  width: 3px;
}
.nav-scroll::-webkit-scrollbar-thumb {
  background: var(--border);
}

/* 分区标签 */
.nav-section-label {
  font-family: var(--font-data);
  font-size: 9px;
  letter-spacing: 0.15em;
  color: var(--text-muted);
  text-transform: uppercase;
  padding: 14px 20px 6px;
  user-select: none;
}

.nav-section-divider {
  height: 1px;
  background: var(--border);
  margin: 10px 12px;
}

/* 导航项 */
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  cursor: pointer;
  transition: all 0.15s ease;
  border-left: 2px solid transparent;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 400;
  position: relative;
  user-select: none;
  white-space: nowrap;
  overflow: hidden;
}

.nav-item:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.nav-item.active {
  background: linear-gradient(90deg, rgba(0, 212, 255, 0.12) 0%, transparent 100%);
  border-left-color: var(--cyan);
  color: var(--text-primary);
}

.sidebar.collapsed .nav-item {
  padding: 10px 0;
  justify-content: center;
  border-left: 2px solid transparent;
}

.sidebar.collapsed .nav-item.active {
  border-left-color: var(--cyan);
}

/* 子级菜单项（如 指标项目 在 标准库 下） */
.nav-item-sub {
  padding-left: 36px;
  font-size: 12px;
  color: var(--text-muted);
  border-left-color: transparent;
}

.nav-item-sub:hover {
  color: var(--text-secondary);
}

.nav-item-sub.active {
  background: linear-gradient(90deg, rgba(0, 212, 255, 0.08) 0%, transparent 100%);
  border-left-color: var(--cyan);
  color: var(--text-primary);
}

.sidebar.collapsed .nav-item-sub {
  padding-left: 0;
  justify-content: center;
}

.nav-icon {
  font-size: 14px;
  width: 20px;
  text-align: center;
  flex-shrink: 0;
  font-style: normal;
}

.nav-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 角标 */
.nav-badge {
  margin-left: auto;
  background: var(--red);
  color: #fff;
  font-family: var(--font-data);
  font-size: 9px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 8px;
  min-width: 18px;
  text-align: center;
  flex-shrink: 0;
}

.nav-badge.blue   { background: var(--blue); }
.nav-badge.orange { background: var(--orange); }
.nav-badge.red    { background: var(--red); }

/* ── 底部用户信息 ─────────────────────────────────────────── */
.sidebar-footer {
  padding: 14px 20px;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.sidebar-footer.collapsed {
  padding: 14px 0;
  display: flex;
  justify-content: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  border-radius: 4px;
  padding: 4px;
  transition: background 0.15s;
}

.user-info:hover {
  background: var(--bg-hover);
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--blue-dim), var(--cyan-dim));
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-data);
  font-size: 11px;
  font-weight: 600;
  color: var(--cyan);
  flex-shrink: 0;
  min-width: 30px;
}

.user-meta {
  overflow: hidden;
  flex: 1;
}

.user-name {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-role {
  font-family: var(--font-data);
  font-size: 9px;
  color: var(--text-muted);
  letter-spacing: 0.05em;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ══ MAIN AREA ════════════════════════════════════════════════ */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

/* ── 顶栏 ────────────────────────────────────────────────── */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  height: 52px;
  border-bottom: 1px solid var(--border);
  background: var(--bg-panel);
  flex-shrink: 0;
  z-index: 10;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.topbar-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-data);
  font-size: 11px;
  color: var(--text-muted);
}

.bc-root {
  color: var(--text-muted);
}

.bc-sep {
  color: var(--border-bright);
  font-size: 12px;
}

.bc-current {
  color: var(--text-primary);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 实时时钟 */
.topbar-time {
  font-family: var(--font-data);
  font-size: 11px;
  color: var(--text-secondary);
  letter-spacing: 0.03em;
  user-select: none;
}

/* 通知按钮 */
.notif-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  transition: all 0.15s;
}

.notif-btn:hover {
  border-color: var(--cyan);
}

.notif-icon {
  font-size: 14px;
  line-height: 1;
}

/* 红点动效 */
.notif-dot {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--red);
  border: 1px solid var(--bg-panel);
  animation: pulse-red 2s ease-in-out infinite;
}

@keyframes pulse-red {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 59, 92, 0.4); }
  50%       { box-shadow: 0 0 0 4px rgba(255, 59, 92, 0); }
}

/* 顶栏图标按钮通用 */
.topbar-action-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.15s;
  font-size: 14px;
}

.topbar-action-btn:hover {
  border-color: var(--cyan);
  color: var(--cyan);
}

/* ── 内容区 ──────────────────────────────────────────────── */
.content-area {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  background: var(--bg-base);
  padding: 24px 28px;
}

.content-area::-webkit-scrollbar {
  width: 4px;
}

.content-area::-webkit-scrollbar-track {
  background: transparent;
}

.content-area::-webkit-scrollbar-thumb {
  background: var(--border-bright);
  border-radius: 2px;
}

/* ── 通知抽屉内容 ─────────────────────────────────────────── */
.notify-header-action {
  display: flex;
  justify-content: flex-end;
  padding: 0 0 12px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 8px;
}

.notify-mark-all {
  font-family: var(--font-data);
  font-size: 9px;
  color: var(--cyan);
  cursor: pointer;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.notify-mark-all:hover {
  color: #00F0FF;
}

.notify-list-panel {
  padding: 0;
}

.notify-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
  transition: background 0.15s;
}

.notify-item:hover {
  background: var(--bg-hover);
  margin: 0 -16px;
  padding: 12px 16px;
}

.notify-item:last-child {
  border-bottom: none;
}

.notify-level-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-top: 5px;
  flex-shrink: 0;
}

.notify-body {
  flex: 1;
  min-width: 0;
}

.notify-title-text {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.5;
  margin-bottom: 3px;
}

.notify-content-text {
  font-size: 11px;
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 4px;
}

.notify-time-text {
  font-family: var(--font-data);
  font-size: 9px;
  color: var(--text-muted);
}

.notify-item.unread .notify-title-text {
  color: var(--text-primary);
}

.empty-notify {
  padding: 40px 0;
  text-align: center;
}

/* ── 路由过渡动画 ─────────────────────────────────────────── */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}
</style>
