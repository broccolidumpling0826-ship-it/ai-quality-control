<template>
  <div class="notification-bell">
    <el-badge :value="unreadCount > 0 ? unreadCount : ''" :max="99" class="badge">
      <el-button circle plain @click="toggleDrawer">
        <el-icon :size="20"><Bell /></el-icon>
      </el-button>
    </el-badge>

    <el-drawer
      v-model="drawerVisible"
      title="消息通知"
      direction="rtl"
      size="360px"
      :modal="false"
    >
      <template #header>
        <div style="display:flex;align-items:center;justify-content:space-between;width:100%">
          <span style="font-size:16px;font-weight:600">消息通知</span>
          <el-button
            link
            type="primary"
            size="small"
            @click="handleMarkAllRead"
            :disabled="unreadCount === 0"
          >全部已读</el-button>
        </div>
      </template>

      <div v-loading="listLoading">
        <template v-if="notifications.length > 0">
          <div
            v-for="item in notifications"
            :key="item.id"
            :class="['notification-item', { 'unread': !item.isRead }]"
            @click="handleItemClick(item)"
          >
            <div class="notification-header">
              <span class="notification-title">{{ item.title }}</span>
              <el-tag
                v-if="!item.isRead"
                type="danger"
                size="small"
                effect="plain"
                style="flex-shrink:0"
              >未读</el-tag>
            </div>
            <p class="notification-content">{{ item.contentSummary }}</p>
            <div class="notification-time">{{ item.createTime }}</div>
          </div>

          <div style="text-align:center;padding:12px 0">
            <el-button
              v-if="hasMore"
              link
              type="primary"
              @click="loadMore"
              :loading="loadingMore"
            >加载更多</el-button>
            <span v-else style="color:#c0c4cc;font-size:12px">已加载全部</span>
          </div>
        </template>
        <el-empty v-else description="暂无通知消息" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { getUnreadCount, pageNotifications, markRead, markAllRead } from '@/api/notification'

const unreadCount = ref(0)
const drawerVisible = ref(false)
const listLoading = ref(false)
const loadingMore = ref(false)
const notifications = ref<any[]>([])
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)

const hasMore = ref(false)

let pollTimer: ReturnType<typeof setInterval> | null = null

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount() as any
    unreadCount.value = typeof res === 'number' ? res : (res?.count ?? 0)
  } catch {}
}

async function loadNotifications(reset = false) {
  if (reset) {
    pageNum.value = 1
    notifications.value = []
  }
  listLoading.value = reset
  loadingMore.value = !reset
  try {
    const res = await pageNotifications({
      pageNum: pageNum.value,
      pageSize
    }) as any
    const records = res.records || []
    if (reset) {
      notifications.value = records
    } else {
      notifications.value.push(...records)
    }
    total.value = res.total || 0
    hasMore.value = notifications.value.length < total.value
  } finally {
    listLoading.value = false
    loadingMore.value = false
  }
}

async function loadMore() {
  pageNum.value++
  await loadNotifications(false)
}

function toggleDrawer() {
  drawerVisible.value = !drawerVisible.value
  if (drawerVisible.value) {
    loadNotifications(true)
  }
}

async function handleItemClick(item: any) {
  if (!item.isRead) {
    try {
      await markRead(item.id)
      item.isRead = true
      if (unreadCount.value > 0) unreadCount.value--
    } catch {}
  }
}

async function handleMarkAllRead() {
  try {
    await markAllRead()
    notifications.value.forEach(n => (n.isRead = true))
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch {}
}

function startPolling() {
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 60000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(startPolling)
onUnmounted(stopPolling)
</script>

<style scoped>
.notification-bell {
  display: inline-flex;
  align-items: center;
}
.badge :deep(.el-badge__content) {
  top: 4px;
  right: 4px;
}
.notification-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}
.notification-item:hover {
  background: #f5f7fa;
}
.notification-item.unread {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}
.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
}
.notification-title {
  font-weight: 500;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.notification-content {
  font-size: 12px;
  color: #666;
  margin: 0 0 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.notification-time {
  font-size: 11px;
  color: #c0c4cc;
}
</style>
