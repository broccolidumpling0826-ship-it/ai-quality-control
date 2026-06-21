<template>
  <div class="prompt-versions-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">Prompt 版本管理</h2>
        <p class="page-desc">查看与切换 AI Prompt 激活版本（管理员）</p>
      </div>
      <el-button type="primary" plain :icon="Refresh" :loading="loading" @click="loadVersions">
        刷新
      </el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form inline>
        <el-form-item label="Prompt Key">
          <el-select v-model="selectedKey" placeholder="全部" clearable style="width:200px" @change="loadVersions">
            <el-option label="RAG 问答" value="rag-qa" />
            <el-option label="判定解释" value="judgment-explain" />
            <el-option label="让步评估" value="concession-agent" />
            <el-option label="质保书摘要" value="cert-summary" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" v-loading="loading">
      <el-col
        v-for="group in groupedVersions"
        :key="group.key"
        :xs="24"
        :lg="12"
        style="margin-bottom:16px"
      >
        <el-card shadow="never" class="prompt-card">
          <template #header>
            <div class="prompt-header">
              <span style="font-weight:600">{{ promptKeyLabel(group.key) }}</span>
              <el-tag type="info" size="small">{{ group.key }}</el-tag>
            </div>
          </template>

          <el-table :data="group.versions" border size="small">
            <el-table-column prop="versionNo" label="版本号" width="100" />
            <el-table-column prop="description" label="说明" show-overflow-tooltip />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.isActive" type="success" size="small">激活</el-tag>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="!row.isActive"
                  link
                  type="primary"
                  size="small"
                  :loading="activating === `${group.key}:${row.versionNo}`"
                  @click="handleActivate(group.key, row.versionNo)"
                >激活</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && groupedVersions.length === 0" description="暂无 Prompt 版本记录" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { listPromptVersions, activatePromptVersion, type AiPromptVersion } from '@/api/ai-audit'

const loading = ref(false)
const activating = ref('')
const selectedKey = ref('')
const versions = ref<AiPromptVersion[]>([])

const groupedVersions = computed(() => {
  const map = new Map<string, AiPromptVersion[]>()
  for (const v of versions.value) {
    const list = map.get(v.promptKey) || []
    list.push(v)
    map.set(v.promptKey, list)
  }
  return Array.from(map.entries()).map(([key, vers]) => ({
    key,
    versions: vers.sort((a, b) => (b.isActive ? 1 : 0) - (a.isActive ? 1 : 0))
  }))
})

function promptKeyLabel(key: string) {
  const map: Record<string, string> = {
    'rag-qa': 'RAG 问答',
    'judgment-explain': '判定解释',
    'concession-agent': '让步评估',
    'cert-summary': '质保书摘要'
  }
  return map[key] || key
}

async function loadVersions() {
  loading.value = true
  try {
    versions.value = await listPromptVersions(selectedKey.value || undefined)
  } finally {
    loading.value = false
  }
}

async function handleActivate(promptKey: string, versionNo: string) {
  await ElMessageBox.confirm(
    `确认将 ${promptKeyLabel(promptKey)} 切换至 ${versionNo}？`,
    '切换 Prompt 版本',
    { type: 'warning' }
  )
  activating.value = `${promptKey}:${versionNo}`
  try {
    await activatePromptVersion(promptKey, versionNo)
    ElMessage.success('Prompt 版本已激活')
    await loadVersions()
  } finally {
    activating.value = ''
  }
}

onMounted(loadVersions)
</script>

<style scoped>
.prompt-versions-page {
  padding: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-heading);
  margin-bottom: 4px;
}

.page-desc {
  font-size: 14px;
  color: var(--text-secondary);
}

.filter-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.filter-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}

.prompt-card {
  border-radius: 12px;
  height: 100%;
}

.prompt-header {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
