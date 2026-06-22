<template>
  <div class="standard-conflict-detail-page">
    <div class="page-toolbar">
      <div>
        <div class="page-title">标准冲突详情</div>
        <div class="page-sub mono">{{ detail?.conflictNo || '—' }}</div>
      </div>
      <el-button @click="$router.back()">返回列表</el-button>
    </div>

    <StandardConflictDetailPanel
      :detail="detail"
      :loading="loading"
      @resolved="handleResolved"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import StandardConflictDetailPanel from '@/components/standard-conflicts/StandardConflictDetailPanel.vue'
import { getStandardConflict, type StandardConflict } from '@/api/standard-conflict'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<StandardConflict | null>(null)

async function loadDetail() {
  const id = String(route.query.id || '')
  if (!id) return
  loading.value = true
  try {
    detail.value = await getStandardConflict(id)
  } finally {
    loading.value = false
  }
}

function handleResolved(result: StandardConflict) {
  detail.value = result
  router.replace({ path: '/standard-conflicts', query: { id: result.id } })
}

onMounted(loadDetail)
</script>

<style scoped>
.standard-conflict-detail-page {
  padding: 16px;
}

.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.page-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.mono {
  font-family: var(--font-data);
}
</style>
