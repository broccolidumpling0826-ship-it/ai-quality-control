<template>
  <div class="login-page">
    <!-- 扫描线背景纹理 -->
    <div class="scanline-overlay"></div>

    <!-- 顶部 AQC 标识条 -->
    <div class="top-bar">
      <div class="top-bar-logo">
        <div class="logo-mark-sm"></div>
        <span class="top-bar-brand">AQC</span>
        <span class="top-bar-sep">/</span>
        <span class="top-bar-sub">ai-quality-control</span>
      </div>
      <div class="top-bar-time">{{ currentTime }}</div>
    </div>

    <!-- 登录卡片容器 -->
    <div class="login-card">
      <!-- 左侧品牌区 -->
      <div class="brand-panel">
        <!-- 角落装饰线 -->
        <div class="corner-tl"></div>
        <div class="corner-br"></div>

        <div class="brand-content">
          <!-- Logo -->
          <div class="brand-logo-area">
            <div class="brand-logo-mark"></div>
            <div class="brand-logo-text">
              <div class="brand-aqc">AQC</div>
              <div class="brand-full">ai-quality-control</div>
            </div>
          </div>

          <!-- 品牌标题 -->
          <h1 class="brand-title">质量判定解释<br/>与让步管理系统</h1>
          <p class="brand-subtitle">Industrial Precision Quality Control Platform</p>

          <!-- 分隔线 -->
          <div class="brand-divider">
            <span class="divider-line"></span>
            <span class="divider-text">SYSTEM FEATURES</span>
            <span class="divider-line"></span>
          </div>

          <!-- 功能特点列表 -->
          <div class="feature-list">
            <div class="feature-item">
              <span class="feature-icon green">◆</span>
              <div class="feature-text">
                <div class="feature-title">全流程质量追踪</div>
                <div class="feature-desc">检验录入 → 判定解释 → 改判 → 让步全链路</div>
              </div>
            </div>
            <div class="feature-item">
              <span class="feature-icon cyan">◆</span>
              <div class="feature-text">
                <div class="feature-title">AI 智能判定辅助</div>
                <div class="feature-desc">标准优先级匹配、偏差分析与合规建议</div>
              </div>
            </div>
            <div class="feature-item">
              <span class="feature-icon blue">◆</span>
              <div class="feature-text">
                <div class="feature-title">多维度数据分析</div>
                <div class="feature-desc">实时指标监控、质量趋势与统计报表</div>
              </div>
            </div>
            <div class="feature-item">
              <span class="feature-icon orange">◆</span>
              <div class="feature-text">
                <div class="feature-title">合规审计追踪</div>
                <div class="feature-desc">操作日志不可篡改，逆向改判升级审批</div>
              </div>
            </div>
          </div>

          <!-- 底部版本信息 -->
          <div class="brand-version">
            <span class="version-dot"></span>
            <span>SYSTEM ONLINE</span>
            <span class="version-sep">·</span>
            <span>v2.6.0</span>
            <span class="version-sep">·</span>
            <span>BUILD 20260514</span>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-panel">
        <!-- 顶部标题 -->
        <div class="form-header">
          <div class="form-header-label">OPERATOR LOGIN</div>
          <h2 class="form-title">操作员登录</h2>
          <p class="form-subtitle">请使用您的工号和密码登录系统</p>
        </div>

        <!-- 登录表单 -->
        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <div class="field-group">
            <div class="field-label">
              工号 / USERNAME
              <span class="field-required">*</span>
            </div>
            <el-form-item prop="username" class="clean-form-item">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入工号（如：ZJ001）"
                size="large"
                clearable
                class="aqc-input"
              >
                <template #prefix>
                  <span class="input-prefix-icon">◉</span>
                </template>
              </el-input>
            </el-form-item>
          </div>

          <div class="field-group">
            <div class="field-label">
              密码 / PASSWORD
              <span class="field-required">*</span>
            </div>
            <el-form-item prop="password" class="clean-form-item">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入登录密码"
                size="large"
                show-password
                clearable
                class="aqc-input"
              >
                <template #prefix>
                  <span class="input-prefix-icon">◈</span>
                </template>
              </el-input>
            </el-form-item>
          </div>

          <div class="login-options">
            <label class="remember-label">
              <input
                v-model="rememberMe"
                type="checkbox"
                class="remember-checkbox"
              />
              <span class="remember-text">记住工号</span>
            </label>
            <span class="forgot-link" @click="handleForgotPassword">忘记密码？</span>
          </div>

          <!-- 登录按钮 -->
          <button
            class="login-btn"
            :class="{ loading: loading, disabled: loading }"
            :disabled="loading"
            @click.prevent="handleLogin"
          >
            <span v-if="loading" class="btn-loading-spinner">⟳</span>
            <span v-else class="btn-icon">→</span>
            <span>{{ loading ? '身份验证中...' : '进入系统' }}</span>
          </button>
        </el-form>

        <!-- 底部信息 -->
        <div class="form-footer">
          <div class="footer-security">
            <span class="security-dot"></span>
            <span>安全连接已建立 · SSL/TLS 加密传输</span>
          </div>
          <div class="footer-copy">
            © 2026 ai-quality-control · 工业质量管理平台
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import type { LoginForm } from '@/types'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

// 实时时钟
const currentTime = ref('')
let clockTimer: ReturnType<typeof setInterval>

function updateClock() {
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  currentTime.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

const loginForm = reactive<LoginForm>({
  username: '',
  password: ''
})

const rules: FormRules<LoginForm> = {
  username: [
    { required: true, message: '请输入工号', trigger: 'blur' },
    { min: 2, max: 50, message: '工号长度 2~50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, max: 32, message: '密码长度 4~32 个字符', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.login(loginForm)

    if (rememberMe.value) {
      localStorage.setItem('qc_remember_username', loginForm.username)
    } else {
      localStorage.removeItem('qc_remember_username')
    }

    ElMessage({
      message: '身份验证通过，正在进入系统...',
      type: 'success',
      duration: 1500
    })

    const redirect = (route.query.redirect as string) || '/dashboard'
    setTimeout(() => router.push(redirect), 600)
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : '登录失败，请检查工号和密码'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

function handleForgotPassword() {
  ElMessage.info('请联系系统管理员重置密码')
}

onMounted(() => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)

  const saved = localStorage.getItem('qc_remember_username')
  if (saved) {
    loginForm.username = saved
    rememberMe.value = true
  }
})

onUnmounted(() => {
  clearInterval(clockTimer)
})
</script>

<!-- 非 scoped：全局强制覆盖登录页的 Element Plus 输入框 -->
<style>
.login-page .el-input__wrapper {
  --el-input-bg-color: #060E1C !important;
  --el-fill-color-blank: #060E1C !important;
  background-color: #060E1C !important;
  box-shadow: 0 0 0 1px #234569 inset !important;
  border-radius: 3px !important;
}
.login-page .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #3A5B7A inset !important;
}
.login-page .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px #00D4FF inset, 0 0 8px rgba(0,212,255,0.15) !important;
}
/* 强制覆盖原生 <input> 元素背景 */
.login-page .el-input__inner,
.login-page input[type="text"],
.login-page input[type="password"],
.login-page input[type="search"],
.login-page input:not([type="checkbox"]):not([type="submit"]) {
  background-color: transparent !important;
  background: transparent !important;
  color: #E8F0FA !important;
  font-family: 'IBM Plex Mono', 'Courier New', monospace !important;
  font-size: 13px !important;
  caret-color: #00D4FF !important;
}
.login-page .el-input__inner::placeholder {
  color: #3A5B7A !important;
  font-family: 'IBM Plex Sans', sans-serif !important;
}
.login-page .el-input__prefix-inner,
.login-page .el-input__suffix-inner .el-icon {
  color: #3A5B7A !important;
}
.login-page .el-input__suffix-inner .el-icon:hover {
  color: #00D4FF !important;
}

/* ── Chrome/Safari autofill 白色背景破解 ─── */
.login-page .el-input__inner:-webkit-autofill,
.login-page .el-input__inner:-webkit-autofill:hover,
.login-page .el-input__inner:-webkit-autofill:focus,
.login-page .el-input__inner:-webkit-autofill:active,
.login-page input:-webkit-autofill,
.login-page input:-webkit-autofill:hover,
.login-page input:-webkit-autofill:focus,
.login-page input:-webkit-autofill:active {
  -webkit-box-shadow: 0 0 0 1000px #060E1C inset !important;
  box-shadow: 0 0 0 1000px #060E1C inset !important;
  -webkit-text-fill-color: #E8F0FA !important;
  background-color: #060E1C !important;
  transition: background-color 9999s ease-in-out 0s !important;
}
</style>

<style scoped>
/* ══════════════════════════════════════════════════════════════
   AQC 登录页 — 工业精密控制室风格
══════════════════════════════════════════════════════════════ */

/* ── 整体页面 ─────────────────────────────────────────────── */
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-base);
  background-image:
    radial-gradient(ellipse 80% 50% at 20% 30%, rgba(0, 212, 255, 0.04) 0%, transparent 60%),
    radial-gradient(ellipse 60% 40% at 80% 70%, rgba(61, 158, 255, 0.03) 0%, transparent 60%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

/* 扫描线纹理 */
.scanline-overlay {
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 3px,
    rgba(0, 212, 255, 0.012) 3px,
    rgba(0, 212, 255, 0.012) 4px
  );
  pointer-events: none;
  z-index: 0;
}

/* ── 顶部标识条 ───────────────────────────────────────────── */
.top-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 48px;
  background: var(--bg-panel);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  z-index: 10;
}

.top-bar-logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 小型六边形 logo */
.logo-mark-sm {
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, var(--cyan), var(--blue));
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  flex-shrink: 0;
}

.top-bar-brand {
  font-family: var(--font-data);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.12em;
  color: var(--cyan);
  text-transform: uppercase;
}

.top-bar-sep {
  color: var(--border-bright);
  font-size: 10px;
}

.top-bar-sub {
  font-family: var(--font-data);
  font-size: 9px;
  color: var(--text-muted);
  letter-spacing: 0.05em;
}

.top-bar-time {
  font-family: var(--font-data);
  font-size: 11px;
  color: var(--text-secondary);
  letter-spacing: 0.03em;
}

/* ── 登录卡片 ─────────────────────────────────────────────── */
.login-card {
  display: flex;
  width: 900px;
  max-width: 100%;
  min-height: 560px;
  border: 1px solid var(--border);
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.7), 0 0 0 1px rgba(0, 212, 255, 0.05);
  position: relative;
  z-index: 1;
}

/* ── 左侧品牌面板 ─────────────────────────────────────────── */
.brand-panel {
  flex: 1;
  background: var(--bg-panel);
  background-image:
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 3px,
      rgba(0, 212, 255, 0.01) 3px,
      rgba(0, 212, 255, 0.01) 4px
    ),
    linear-gradient(160deg, rgba(0, 212, 255, 0.03) 0%, transparent 60%);
  border-right: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 52px 44px;
  position: relative;
  overflow: hidden;
}

/* 角落装饰线 */
.corner-tl,
.corner-br {
  position: absolute;
  width: 32px;
  height: 32px;
}

.corner-tl {
  top: 20px;
  left: 20px;
  border-top: 1px solid var(--cyan);
  border-left: 1px solid var(--cyan);
  opacity: 0.4;
}

.corner-br {
  bottom: 20px;
  right: 20px;
  border-bottom: 1px solid var(--cyan);
  border-right: 1px solid var(--cyan);
  opacity: 0.4;
}

.brand-content {
  position: relative;
  z-index: 1;
  width: 100%;
}

/* Logo 区 */
.brand-logo-area {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
}

/* 大型六边形 Logo */
.brand-logo-mark {
  width: 48px;
  height: 48px;
  min-width: 48px;
  background: linear-gradient(135deg, var(--cyan), var(--blue));
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
}

.brand-logo-text {
  line-height: 1;
}

.brand-aqc {
  font-family: var(--font-data);
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.15em;
  color: var(--cyan);
  text-transform: uppercase;
}

.brand-full {
  font-family: var(--font-data);
  font-size: 10px;
  color: var(--text-muted);
  letter-spacing: 0.06em;
  margin-top: 4px;
}

.brand-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  margin: 0 0 8px;
  letter-spacing: 0.02em;
}

.brand-subtitle {
  font-family: var(--font-data);
  font-size: 9px;
  color: var(--text-muted);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin-bottom: 28px;
}

/* 分隔线 */
.brand-divider {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: var(--border);
}

.divider-text {
  font-family: var(--font-data);
  font-size: 8px;
  letter-spacing: 0.15em;
  color: var(--text-muted);
  text-transform: uppercase;
  white-space: nowrap;
}

/* 功能列表 */
.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 32px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.feature-icon {
  font-size: 8px;
  margin-top: 3px;
  flex-shrink: 0;
}

.feature-icon.green  { color: var(--green); }
.feature-icon.cyan   { color: var(--cyan); }
.feature-icon.blue   { color: var(--blue); }
.feature-icon.orange { color: var(--orange); }

.feature-text {}

.feature-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.feature-desc {
  font-family: var(--font-data);
  font-size: 10px;
  color: var(--text-muted);
  line-height: 1.5;
}

/* 版本标识 */
.brand-version {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-data);
  font-size: 9px;
  color: var(--text-muted);
  letter-spacing: 0.05em;
}

.version-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--green);
  animation: pulse-green 2s ease-in-out infinite;
}

@keyframes pulse-green {
  0%, 100% { box-shadow: 0 0 0 0 rgba(22, 201, 116, 0.5); }
  50%       { box-shadow: 0 0 0 3px rgba(22, 201, 116, 0); }
}

.version-sep {
  color: var(--border-bright);
}

/* ── 右侧表单区 ───────────────────────────────────────────── */
.form-panel {
  width: 400px;
  min-width: 360px;
  background: var(--bg-card);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 40px;
}

/* 表单顶部标题 */
.form-header {
  margin-bottom: 36px;
}

.form-header-label {
  font-family: var(--font-data);
  font-size: 9px;
  letter-spacing: 0.2em;
  color: var(--text-muted);
  text-transform: uppercase;
  margin-bottom: 8px;
}

.form-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 6px;
  letter-spacing: 0.02em;
}

.form-subtitle {
  font-family: var(--font-data);
  font-size: 10px;
  color: var(--text-muted);
  letter-spacing: 0.04em;
}

/* 表单主体 */
.login-form {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 字段组 */
.field-group {
  margin-bottom: 20px;
}

.field-label {
  font-family: var(--font-data);
  font-size: 9px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--text-muted);
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.field-required {
  color: var(--red);
}

/* 清除 el-form-item 默认 margin */
.clean-form-item {
  margin-bottom: 0 !important;
}

/* 输入框深色样式（强制覆盖 Element Plus） */
.aqc-input :deep(.el-input__wrapper) {
  --el-input-bg-color: #060E1C;
  --el-fill-color-blank: #060E1C;
  background-color: #060E1C !important;
  box-shadow: 0 0 0 1px #234569 inset !important;
  border-radius: 3px !important;
  height: 44px !important;
  padding: 0 12px !important;
  transition: all 0.15s !important;
}

.aqc-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #3A5B7A inset !important;
}

.aqc-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #00D4FF inset, 0 0 8px rgba(0, 212, 255, 0.15) !important;
}

.aqc-input :deep(.el-input__inner) {
  color: #E8F0FA !important;
  font-family: 'IBM Plex Mono', 'Courier New', monospace !important;
  font-size: 13px !important;
  background: transparent !important;
  height: 44px !important;
  line-height: 44px !important;
  caret-color: #00D4FF;
}

.aqc-input :deep(.el-input__inner::placeholder) {
  color: #3A5B7A !important;
  font-family: 'IBM Plex Sans', sans-serif !important;
  font-size: 12px !important;
}

.aqc-input :deep(.el-input__prefix-inner),
.aqc-input :deep(.el-input__suffix-inner) {
  color: #3A5B7A !important;
}

.aqc-input :deep(.el-input__suffix-inner .el-icon:hover) {
  color: #7A9BBE !important;
}

/* 清空按钮深色 */
.aqc-input :deep(.el-input__clear) {
  color: #3A5B7A !important;
}
.aqc-input :deep(.el-input__clear:hover) {
  color: #7A9BBE !important;
}

/* 密码显示切换按钮 */
.aqc-input :deep(.el-input__password) {
  color: #3A5B7A !important;
}
.aqc-input :deep(.el-input__password:hover) {
  color: #00D4FF !important;
}

.input-prefix-icon {
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1;
}

/* 选项行 */
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  margin-top: 4px;
}

/* 自定义复选框 */
.remember-label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.remember-checkbox {
  /* 隐藏原生 checkbox，完全自定义外观 */
  appearance: none;
  -webkit-appearance: none;
  width: 13px;
  height: 13px;
  border: 1px solid #234569;
  background: #060E1C;
  border-radius: 2px;
  cursor: pointer;
  position: relative;
  flex-shrink: 0;
  transition: border-color 0.15s, background 0.15s;
}

.remember-checkbox:hover {
  border-color: #3A5B7A;
}

.remember-checkbox:checked {
  background: #00D4FF;
  border-color: #00D4FF;
}

.remember-checkbox:checked::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 4px;
  width: 4px;
  height: 7px;
  border: 1.5px solid #060E1C;
  border-top: none;
  border-left: none;
  transform: rotate(45deg);
}

.remember-text {
  font-family: 'IBM Plex Mono', monospace;
  font-size: 10px;
  color: #7A9BBE;
  letter-spacing: 0.05em;
  user-select: none;
}

.forgot-link {
  font-family: var(--font-data);
  font-size: 10px;
  color: var(--text-muted);
  cursor: pointer;
  letter-spacing: 0.05em;
  transition: color 0.15s;
}

.forgot-link:hover {
  color: var(--cyan);
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 46px;
  background: var(--cyan);
  color: var(--bg-base);
  border: none;
  border-radius: 3px;
  font-family: var(--font-ui);
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.06em;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.15s ease;
  position: relative;
  overflow: hidden;
}

.login-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.1) 50%, transparent 100%);
  transform: translateX(-100%);
  transition: transform 0.4s ease;
}

.login-btn:hover::before {
  transform: translateX(100%);
}

.login-btn:hover {
  background: #00F0FF;
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.4);
  transform: translateY(-1px);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn.loading {
  opacity: 0.8;
  cursor: not-allowed;
  transform: none;
}

.login-btn.loading:hover {
  box-shadow: none;
  transform: none;
}

.btn-icon {
  font-size: 16px;
  font-weight: 400;
}

.btn-loading-spinner {
  font-size: 16px;
  animation: spin 1s linear infinite;
  display: inline-block;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

/* 底部信息 */
.form-footer {
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid var(--border);
}

.footer-security {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-data);
  font-size: 9px;
  color: var(--green);
  letter-spacing: 0.04em;
  margin-bottom: 8px;
}

.security-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--green);
  flex-shrink: 0;
}

.footer-copy {
  font-family: var(--font-data);
  font-size: 9px;
  color: var(--text-muted);
  letter-spacing: 0.04em;
}

/* ── 覆盖 el-form-item error 位置 ─────────────────────────── */
.login-form :deep(.el-form-item__error) {
  padding-top: 4px !important;
  font-family: var(--font-data) !important;
  font-size: 9px !important;
  color: var(--red) !important;
  letter-spacing: 0.04em !important;
}

/* ── 响应式 ───────────────────────────────────────────────── */
@media (max-width: 820px) {
  .login-card {
    flex-direction: column;
    width: 100%;
    min-height: auto;
    border-radius: 4px;
  }

  .brand-panel {
    padding: 36px 28px;
    border-right: none;
    border-bottom: 1px solid var(--border);
  }

  .feature-list {
    display: none;
  }

  .brand-version {
    display: none;
  }

  .form-panel {
    width: 100%;
    min-width: 0;
    padding: 36px 28px;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 0;
    justify-content: flex-start;
  }

  .login-card {
    border: none;
    border-radius: 0;
    min-height: 100vh;
  }

  .form-panel {
    padding: 28px 20px;
  }

  .brand-panel {
    padding: 28px 20px;
  }

  .top-bar-sub {
    display: none;
  }
}
</style>
