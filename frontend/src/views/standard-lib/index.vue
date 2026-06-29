<template>
  <div class="standard-lib-page">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="标准类型">
          <el-select v-model="searchForm.standardType" placeholder="请选择" clearable style="width:140px">
            <el-option
              v-for="item in dictStore.getItems('STANDARD_TYPE')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="品种">
          <el-select v-model="searchForm.productVariety" placeholder="请选择" clearable style="width:140px">
            <el-option
              v-for="item in dictStore.getItems('PRODUCT_VARIETY')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="牌号">
          <el-select v-model="searchForm.productGrade" placeholder="请选择" clearable filterable style="width:140px">
            <el-option
              v-for="item in dictStore.getItems('PRODUCT_GRADE')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width:120px">
            <el-option
              v-for="item in dictStore.getItems('STANDARD_STATUS')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增标准</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表区域 -->
    <el-card shadow="never" style="margin-top:12px">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width:100%"
      >
        <el-table-column
          prop="standardCode"
          label="标准编号"
          width="150"
          :filters="getFilters('standardCode')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="standardType"
          label="标准类型"
          width="120"
          :filters="getFilters('standardType')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <el-tag :type="dictStore.getColorTag('STANDARD_TYPE', row.standardType) as any">
              {{ dictStore.getLabel('STANDARD_TYPE', row.standardType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="customerId"
          label="关联客户"
          min-width="140"
          show-overflow-tooltip
          :filters="getFilters('customerId')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            {{ formatCustomerLabel(row.customerId) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="standardName"
          label="标准名称"
          min-width="160"
          :filters="getFilters('standardName')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="specRange"
          label="规格范围"
          min-width="160"
          show-overflow-tooltip
          :filters="getFilters('specRange')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="productVariety"
          label="品种"
          width="100"
          :filters="getFilters('productVariety')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            {{ dictStore.getLabel('PRODUCT_VARIETY', row.productVariety) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="productGrade"
          label="牌号"
          width="120"
          :filters="getFilters('productGrade')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="version"
          label="版本号"
          width="80"
          :filters="getFilters('version')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="effectiveDate"
          label="生效日期"
          width="110"
          :filters="getFilters('effectiveDate')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        />
        <el-table-column
          prop="expiryDate"
          label="失效日期"
          width="110"
          :filters="getFilters('expiryDate')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <span :class="{ 'text-warning': isExpiringSoon(row.expiryDate) }">
              {{ row.expiryDate || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          label="状态"
          width="108"
          class-name="status-col"
          :filters="getFilters('status')"
          :filter-method="filterMethod"
          filter-placement="bottom-start"
        >
          <template #default="{ row }">
            <el-tag
              class="status-tag"
              size="small"
              :type="dictStore.getColorTag('STANDARD_STATUS', row.status) as any"
            >
              {{ dictStore.getLabel('STANDARD_STATUS', row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" class-name="op-col">
          <template #default="{ row }">
            <div class="op-cell">
              <template v-for="(group, gi) in [getRowActionGroups(row)]" :key="gi">
                <el-button
                  v-for="action in group.visible"
                  :key="action.key"
                  link
                  :type="action.type"
                  @click="action.handler"
                >{{ action.label }}</el-button>
                <el-dropdown
                  v-if="group.more.length"
                  trigger="click"
                  @command="(key: string) => runRowAction(row, key)"
                >
                  <el-button link type="primary" class="op-more-btn">...</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item
                        v-for="action in group.more"
                        :key="action.key"
                        :command="action.key"
                      >
                      <span :class="`op-more-item op-more-item--${action.type}`">{{ action.label }}</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top:16px;justify-content:flex-end"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 新增/编辑 Drawer -->
    <el-drawer
      v-model="drawerVisible"
      :title="drawerMode === 'add' ? '新增标准' : (drawerMode === 'edit' ? '编辑标准' : '查看标准')"
      size="960px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-alert
          v-if="drawerMode === 'edit' && formData.status === 'PUBLISHED'"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom:12px"
          title="已发布标准修改后仅影响后续检验判定，历史判定结论仍保留当时的标准快照。"
        />
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="标准编号" prop="standardCode">
              <el-input v-model="formData.standardCode" placeholder="请输入标准编号" :disabled="drawerMode === 'view'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标准名称" prop="standardName">
              <el-input v-model="formData.standardName" placeholder="请输入标准名称" :disabled="drawerMode === 'view'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标准类型" prop="standardType">
              <el-select
                v-model="formData.standardType"
                placeholder="请选择"
                style="width:100%"
                :disabled="drawerMode === 'view'"
                @change="onStandardTypeChange"
              >
                <el-option
                  v-for="item in dictStore.getItems('STANDARD_TYPE')"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联客户" prop="customerId">
              <el-select
                v-model="formData.customerId"
                :disabled="!isCustomerStandard || drawerMode === 'view'"
                :placeholder="isCustomerStandard ? '请选择关联客户' : '仅客户协议标准需选择'"
                filterable
                clearable
                style="width:100%"
              >
                <el-option
                  v-for="item in customerOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品种" prop="productVariety">
              <el-select v-model="formData.productVariety" placeholder="请选择" style="width:100%" :disabled="drawerMode === 'view'">
                <el-option
                  v-for="item in dictStore.getItems('PRODUCT_VARIETY')"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="牌号" prop="productGrade">
              <el-select
                v-model="formData.productGrade"
                placeholder="请选择牌号"
                filterable
                style="width:100%"
                :disabled="drawerMode === 'view'"
              >
                <el-option
                  v-for="item in dictStore.getItems('PRODUCT_GRADE')"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格范围" prop="specRange">
              <el-input v-model="formData.specRange" placeholder="如：Φ10~Φ20mm（必填，用于检验规格下拉）" :disabled="drawerMode === 'view'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本号" prop="version">
              <el-input v-model="formData.version" placeholder="如：V1.0" :disabled="drawerMode === 'view'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生效日期" prop="effectiveDate">
              <el-date-picker
                v-model="formData.effectiveDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width:100%"
                :disabled="drawerMode === 'view'"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="失效日期" prop="expiryDate">
              <el-date-picker
                v-model="formData.expiryDate"
                type="date"
                placeholder="选择日期（可为空）"
                value-format="YYYY-MM-DD"
                style="width:100%"
                :disabled="drawerMode === 'view'"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述">
              <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="标准描述（选填）" :disabled="drawerMode === 'view'" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">标准源文件</el-divider>
        <div class="source-file-panel">
          <el-table
            v-if="sourceDocuments.length"
            :data="sourceDocuments"
            border
            size="small"
            style="width:100%"
          >
            <el-table-column label="文件名" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">
                <span>{{ row.sourceFileName || '-' }}</span>
                <el-tooltip v-if="row.parseErrorMessage" :content="row.parseErrorMessage" placement="top">
                  <el-icon class="source-error-icon"><WarningFilled /></el-icon>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column label="类型" width="80">
              <template #default="{ row }">{{ sourceFileTypeLabel(row.sourceFileName) }}</template>
            </el-table-column>
            <el-table-column label="解析" width="90">
              <template #default="{ row }">{{ row.parseStatus || '-' }}</template>
            </el-table-column>
            <el-table-column label="索引" width="90">
              <template #default="{ row }">{{ row.indexStatus || '-' }}</template>
            </el-table-column>
            <el-table-column label="条款数" width="72" align="center">
              <template #default="{ row }">{{ row.chunkCount ?? 0 }}</template>
            </el-table-column>
            <el-table-column label="操作" :width="drawerMode === 'view' ? 220 : 320" align="center" class-name="source-op-col">
              <template #default="{ row }">
                <div class="source-file-op-cell">
                  <el-button
                    v-if="canViewSourceClauses(row)"
                    type="primary"
                    plain
                    size="small"
                    class="source-file-op-btn"
                    @click="openClauseDrawer(row)"
                  >
                    查看拆分结果
                  </el-button>
                  <el-button
                    type="primary"
                    plain
                    size="small"
                    class="source-file-op-btn"
                    @click="handleSourceDownload(row)"
                  >
                    下载
                  </el-button>
                  <template v-if="drawerMode !== 'view'">
                    <el-button
                      v-if="formData.status === 'PUBLISHED'"
                      type="warning"
                      plain
                      size="small"
                      class="source-file-op-btn"
                      :loading="sourceReindexDocumentId === row.documentId"
                      @click="handleSourceReindex(row)"
                    >
                      重索引
                    </el-button>
                    <el-button
                      type="danger"
                      plain
                      size="small"
                      class="source-file-op-btn"
                      @click="handleSourceDelete(row)"
                    >
                      删除
                    </el-button>
                  </template>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="尚未上传标准源文件" :image-size="64" />
          <div class="source-file-actions" style="margin-top:12px;display:flex;gap:8px;flex-wrap:wrap;align-items:center">
            <el-upload
              v-if="drawerMode !== 'view' && formData.id"
              :show-file-list="false"
              accept=".pdf,.xlsx,.xls,.png,.jpg,.jpeg,application/pdf,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel,image/png,image/jpeg"
              :http-request="handleSourceUpload"
            >
              <el-button type="primary" :loading="sourceUploadLoading">新增源文件</el-button>
            </el-upload>
            <el-button
              v-if="formData.status === 'PUBLISHED' && sourceDocuments.length && drawerMode !== 'view'"
              type="warning"
              :loading="sourceReindexAllLoading"
              @click="handleSourceReindexAll"
            >
              全部重索引
            </el-button>
            <span v-if="sourceDocuments.length" class="text-meta">已上传 {{ sourceDocuments.length }} / 10</span>
          </div>
          <el-alert
            v-if="drawerMode === 'add'"
            type="info"
            :closable="false"
            show-icon
            style="margin-top:8px"
            title="请先保存标准后再上传源文件。每个标准最多 10 个文件，支持 PDF、Excel（xlsx/xls）、图片（png/jpg/jpeg）。"
          />
        </div>

        <!-- 指标列表 -->
        <div style="margin-top:16px">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
            <span style="font-weight:600;font-size:14px">指标列表</span>
            <span class="text-meta" style="margin-left:8px">合格限必填；让步上下限选填（未配置则超出合格限判定为需复检）</span>
            <el-button
              v-if="drawerMode !== 'view'"
              type="primary"
              size="small"
              @click="addIndicatorRow"
            >添加指标</el-button>
          </div>
          <el-table :data="formData.indicators" border size="small">
            <el-table-column label="指标项目" min-width="200">
              <template #default="{ row }">
                <el-select
                  v-if="drawerMode !== 'view'"
                  v-model="row.indicatorId"
                  filterable
                  clearable
                  placeholder="请选择指标项目"
                  style="width:100%"
                  size="small"
                  @change="(val: string) => handleIndicatorSelect(row, val)"
                >
                  <el-option
                    v-for="opt in getIndicatorOptions(row)"
                    :key="opt.id"
                    :label="formatIndicatorOption(opt)"
                    :value="opt.id"
                  />
                </el-select>
                <span v-else>{{ formatIndicatorLabel(row) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="指标代码" width="100">
              <template #default="{ row }">
                <span class="text-meta">{{ row.indicatorCode || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="类别" width="110">
              <template #default="{ row }">
                <el-tag v-if="row.category" :type="dictStore.getColorTag('INDICATOR_CATEGORY', row.category) as any" size="small">
                  {{ dictStore.getLabel('INDICATOR_CATEGORY', row.category) }}
                </el-tag>
                <span v-else class="text-meta">-</span>
              </template>
            </el-table-column>
            <el-table-column label="单位" width="70">
              <template #default="{ row }">
                <span class="text-meta">{{ row.unit || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="合格下限" width="88">
              <template #default="{ row }">
                <el-input v-if="drawerMode !== 'view'" v-model="row.lowerLimit" size="small" placeholder="-" />
                <span v-else>{{ row.lowerLimit ?? '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="合格上限" width="88">
              <template #default="{ row }">
                <el-input v-if="drawerMode !== 'view'" v-model="row.upperLimit" size="small" placeholder="-" />
                <span v-else>{{ row.upperLimit ?? '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="让步下限" width="88">
              <template #default="{ row }">
                <el-input v-if="drawerMode !== 'view'" v-model="row.concessionLower" size="small" placeholder="选填" />
                <span v-else>{{ row.concessionLower ?? '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="让步上限" width="88">
              <template #default="{ row }">
                <el-input v-if="drawerMode !== 'view'" v-model="row.concessionUpper" size="small" placeholder="选填" />
                <span v-else>{{ row.concessionUpper ?? '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="drawerMode !== 'view'" label="操作" width="70" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" size="small" @click="removeIndicatorRow($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form>

      <template #footer>
        <div v-if="drawerMode !== 'view'" style="display:flex;justify-content:flex-end;gap:8px">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
        </div>
        <div v-else style="display:flex;justify-content:flex-end">
          <el-button @click="drawerVisible = false">关闭</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 条款拆分查看抽屉 -->
    <el-drawer
      v-model="clauseDrawerVisible"
      title="条款拆分结果"
      size="800px"
      destroy-on-close
    >
      <div v-if="clauseDrawerDocument" class="clause-drawer">
        <div class="clause-drawer-summary">
          <div class="clause-drawer-file">{{ clauseDrawerDocument.sourceFileName || '-' }}</div>
          <div class="clause-drawer-meta">
            <span>解析：{{ clauseDrawerDocument.parseStatus || '-' }}</span>
            <span>索引：{{ clauseDrawerDocument.indexStatus || '-' }}</span>
            <span>条款：{{ clauseDrawerDocument.chunkCount ?? 0 }}</span>
          </div>
          <el-alert
            v-if="clauseDrawerDocument.parseErrorMessage"
            type="warning"
            :closable="false"
            show-icon
            :title="clauseDrawerDocument.parseErrorMessage"
            style="margin-top:8px"
          />
          <div class="clause-drawer-hint">只读验收视图，不可编辑切片内容</div>
        </div>

        <div class="clause-drawer-toolbar">
          <el-input
            v-model="clauseKeyword"
            clearable
            placeholder="搜索条款号或原文"
            style="flex:1"
            @keyup.enter="searchClauseList"
          />
          <el-button type="primary" :loading="clauseLoading" @click="searchClauseList">搜索</el-button>
        </div>

        <el-table
          v-loading="clauseLoading"
          :data="clauseList"
          border
          size="small"
          style="width:100%"
          class="clause-list-table"
          @row-click="openClauseDetail"
        >
          <el-table-column label="#" width="52" align="center">
            <template #default="{ $index }">
              {{ (clausePageNum - 1) * clausePageSize + $index + 1 }}
            </template>
          </el-table-column>
          <el-table-column prop="clauseNo" label="条款" width="100" show-overflow-tooltip />
          <el-table-column label="页码" width="64" align="center">
            <template #default="{ row }">{{ row.pageNo ?? '-' }}</template>
          </el-table-column>
          <el-table-column label="字符" width="64" align="center">
            <template #default="{ row }">{{ clauseTextLength(row.paragraphText) }}</template>
          </el-table-column>
          <el-table-column label="向量" width="88" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="embeddingStatusTagType(row.embeddingStatus)">
                {{ row.embeddingStatus || '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="原文预览" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">{{ previewClauseText(row.paragraphText) }}</template>
          </el-table-column>
        </el-table>

        <div class="clause-drawer-pagination">
          <el-pagination
            v-model:current-page="clausePageNum"
            v-model:page-size="clausePageSize"
            :total="clauseTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @current-change="loadClauseList"
            @size-change="handleClausePageSizeChange"
          />
        </div>
      </div>
    </el-drawer>

    <!-- 条款详情 -->
    <el-dialog v-model="clauseDetailVisible" title="条款详情" width="720px" destroy-on-close>
      <template v-if="clauseDetail">
        <el-descriptions :column="2" border size="small" style="margin-bottom:12px">
          <el-descriptions-item label="条款号">{{ clauseDetail.clauseNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="页码">{{ clauseDetail.pageNo ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="向量状态">{{ clauseDetail.embeddingStatus || '-' }}</el-descriptions-item>
          <el-descriptions-item label="字符数">{{ clauseTextLength(clauseDetail.paragraphText) }}</el-descriptions-item>
          <el-descriptions-item label="clauseKey" :span="2">{{ clauseDetail.clauseKey || '-' }}</el-descriptions-item>
          <el-descriptions-item label="esDocumentKey" :span="2">{{ clauseDetail.esDocumentKey || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="clause-detail-text">{{ clauseDetail.paragraphText || '-' }}</div>
      </template>
    </el-dialog>

    <!-- 发布确认对话框 -->
    <el-dialog v-model="publishDialogVisible" title="发布确认" width="440px">
      <p>确认发布标准 <strong>{{ publishTarget?.standardName }}</strong>？</p>
      <el-form style="margin-top:12px">
        <el-form-item label="为旧版本设置失效日期">
          <el-date-picker
            v-model="publishExpiryDate"
            type="date"
            placeholder="可选：选择旧版本失效日期"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="publishLoading" @click="confirmPublish">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import type { FormInstance, UploadRequestOptions } from 'element-plus'
import { useDictStore } from '@/store/dict'
import { useTableFilter } from '@/composables/use-table-filter'
import {
  pageStandards,
  addStandard,
  updateStandard,
  publishStandard,
  deleteStandard,
  getStandardById,
  uploadStandardSourceFile,
  downloadStandardSourceFile,
  deleteStandardSourceFile,
  reindexStandardSourceFile,
  reindexAllStandardSourceFiles,
  pageStandardSourceClauses,
  getStandardSourceClause,
  type StandardSourceDocumentSummary,
  type StandardClauseSummary
} from '@/api/standard'
import { listActiveIndicators } from '@/api/indicator'
import type { PageResult } from '@/types'

interface IndicatorOption {
  id: string
  indicatorName: string
  indicatorCode: string
  category?: string
  indicatorCategory?: string
  unit?: string
}

interface StandardIndicatorRow {
  indicatorId: string
  indicatorName: string
  indicatorCode: string
  category: string
  unit: string
  lowerLimit: string | number
  upperLimit: string | number
  concessionLower?: string | number
  concessionUpper?: string | number
  isRequired?: number
}

const dictStore = useDictStore()

/** 字典未同步时的演示客户兜底（与 init-dict-data.sql 一致） */
const FALLBACK_CUSTOMERS = [
  { value: 'CUST-001', label: '华东汽车配件有限公司', colorTag: '', sortNo: 1 },
  { value: 'CUST-002', label: '西南建材集团', colorTag: '', sortNo: 2 }
]

// 搜索
const searchForm = reactive({
  standardType: '',
  productVariety: '',
  productGrade: '',
  status: ''
})

// 分页
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)
const tableData = ref<any[]>([])
const { getFilters, filterMethod } = useTableFilter(tableData)

// 抽屉
const drawerVisible = ref(false)
const drawerMode = ref<'add' | 'edit' | 'view'>('add')
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const defaultForm = () => ({
  id: '',
  status: '',
  standardCode: '',
  standardName: '',
  standardType: '',
  productVariety: '',
  productGrade: '',
  specRange: '',
  version: '',
  effectiveDate: '',
  expiryDate: '9999-12-31',
  customerId: '',
  description: '',
  indicators: [] as any[]
})

const formData = reactive(defaultForm())
const indicatorOptions = ref<IndicatorOption[]>([])
const indicatorOptionsLoading = ref(false)

const sourceDocuments = ref<StandardSourceDocumentSummary[]>([])
const sourceUploadLoading = ref(false)
const sourceReindexDocumentId = ref('')
const sourceReindexAllLoading = ref(false)

const clauseDrawerVisible = ref(false)
const clauseDrawerDocument = ref<StandardSourceDocumentSummary | null>(null)
const clauseKeyword = ref('')
const clauseList = ref<StandardClauseSummary[]>([])
const clauseLoading = ref(false)
const clausePageNum = ref(1)
const clausePageSize = ref(20)
const clauseTotal = ref(0)
const clauseDetailVisible = ref(false)
const clauseDetail = ref<StandardClauseSummary | null>(null)

function applySourceDocuments(list?: StandardSourceDocumentSummary[]) {
  sourceDocuments.value = (list || []).filter((item) => item.hasSourceFile !== false)
}

async function reloadSourceDocuments(standardId: string) {
  try {
    const detail = await getStandardById(standardId) as any
    applySourceDocuments(detail.sourceDocuments)
  } catch {
    // handled by request interceptor
  }
}

function sourceFileTypeLabel(fileName?: string) {
  if (!fileName) return '-'
  const ext = fileName.includes('.') ? fileName.split('.').pop()?.toLowerCase() : ''
  if (ext === 'pdf') return 'PDF'
  if (ext === 'xlsx' || ext === 'xls') return 'Excel'
  if (ext === 'png' || ext === 'jpg' || ext === 'jpeg') return '图片'
  return ext ? ext.toUpperCase() : '-'
}

function canViewSourceClauses(row: StandardSourceDocumentSummary) {
  return (row.chunkCount ?? 0) > 0 && !!row.documentId
}

function previewClauseText(text?: string) {
  if (!text) return '-'
  return text.length > 80 ? `${text.slice(0, 80)}…` : text
}

function clauseTextLength(text?: string) {
  return text ? text.length : 0
}

function embeddingStatusTagType(status?: string) {
  if (status === 'INDEXED') return 'success'
  if (status === 'FAILED') return 'danger'
  if (status === 'PENDING') return 'warning'
  return 'info'
}

async function openClauseDrawer(row: StandardSourceDocumentSummary) {
  if (!formData.id || !row.documentId) return
  clauseDrawerDocument.value = row
  clauseKeyword.value = ''
  clausePageNum.value = 1
  clausePageSize.value = 20
  clauseDrawerVisible.value = true
  await loadClauseList()
}

async function loadClauseList() {
  if (!formData.id || !clauseDrawerDocument.value?.documentId) return
  clauseLoading.value = true
  try {
    const res = await pageStandardSourceClauses(formData.id, clauseDrawerDocument.value.documentId, {
      keyword: clauseKeyword.value || undefined,
      pageNum: clausePageNum.value,
      pageSize: clausePageSize.value
    }) as PageResult<StandardClauseSummary>
    clauseList.value = res.records || []
    clauseTotal.value = res.total ?? 0
  } finally {
    clauseLoading.value = false
  }
}

function searchClauseList() {
  clausePageNum.value = 1
  loadClauseList()
}

function handleClausePageSizeChange() {
  clausePageNum.value = 1
  loadClauseList()
}

async function openClauseDetail(row: StandardClauseSummary) {
  if (!formData.id || !clauseDrawerDocument.value?.documentId || !row.id) return
  try {
    clauseDetail.value = await getStandardSourceClause(
      formData.id,
      clauseDrawerDocument.value.documentId,
      row.id
    ) as StandardClauseSummary
    clauseDetailVisible.value = true
  } catch {
    // handled by request interceptor
  }
}

const isCustomerStandard = computed(() => formData.standardType === 'CUSTOMER')

const customerOptions = ref<typeof FALLBACK_CUSTOMERS>([...FALLBACK_CUSTOMERS])

const formRules = {
  standardCode: [{ required: true, message: '请输入标准编号', trigger: 'blur' }],
  standardName: [{ required: true, message: '请输入标准名称', trigger: 'blur' }],
  standardType: [{ required: true, message: '请选择标准类型', trigger: 'change' }],
  specRange: [{ required: true, message: '请输入规格范围', trigger: 'blur' }],
  productVariety: [{ required: true, message: '请选择品种', trigger: 'change' }],
  productGrade: [{ required: true, message: '请选择牌号', trigger: 'change' }],
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }],
  customerId: [{
    validator: (_rule: unknown, value: string, callback: (err?: Error) => void) => {
      if (formData.standardType === 'CUSTOMER' && !value) {
        callback(new Error('客户协议标准请选择关联客户'))
      } else {
        callback()
      }
    },
    trigger: 'change'
  }]
}

function formatCustomerLabel(customerId?: string) {
  if (!customerId) return '-'
  const found = customerOptions.value.find((item) => item.value === customerId)
  return found?.label ?? customerId
}

function onStandardTypeChange(type: string) {
  if (type !== 'CUSTOMER') {
    formData.customerId = ''
  }
}

// 发布
const publishDialogVisible = ref(false)
const publishLoading = ref(false)
const publishTarget = ref<any>(null)
const publishExpiryDate = ref('')

function isExpiringSoon(date: string) {
  if (!date) return false
  const diff = new Date(date).getTime() - Date.now()
  return diff > 0 && diff < 3 * 24 * 3600 * 1000
}

function mapStandardRow(row: Record<string, unknown>) {
  return {
    ...row,
    productVariety: row.productVariety ?? row.variety,
    productGrade: row.productGrade ?? row.grade,
    version: row.version ?? row.versionNo
  }
}

function mapStandardForm(detail: Record<string, unknown>) {
  // 不把 API 别名字段写入 formData，避免提交时 mapStandardPayload 读到旧值
  const {
    variety: _variety,
    grade: _grade,
    versionNo: _versionNo,
    version: _version,
    remark: _remark,
    description: _description,
    productVariety: _pv,
    productGrade: _pg,
    indicators: _indicators,
    ...rest
  } = detail
  return {
    ...defaultForm(),
    ...rest,
    productVariety: detail.variety ?? detail.productVariety ?? '',
    productGrade: detail.grade ?? detail.productGrade ?? '',
    version: detail.versionNo ?? detail.version ?? '',
    description: detail.description ?? detail.remark ?? '',
    expiryDate: detail.expiryDate ?? '9999-12-31'
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await pageStandards({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm
    }) as PageResult<any>
    tableData.value = (res.records || []).map((row) => mapStandardRow(row))
    total.value = res.total || 0
  } catch {
    // handled by request interceptor
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  Object.assign(searchForm, { standardType: '', productVariety: '', productGrade: '', status: '' })
  pageNum.value = 1
  loadData()
}

async function loadIndicatorOptions() {
  indicatorOptionsLoading.value = true
  try {
    const res = await listActiveIndicators() as IndicatorOption[]
    indicatorOptions.value = Array.isArray(res) ? res : []
  } finally {
    indicatorOptionsLoading.value = false
  }
}

function formatIndicatorOption(opt: IndicatorOption) {
  return `${opt.indicatorName}（${opt.indicatorCode}）`
}

function formatIndicatorLabel(row: StandardIndicatorRow) {
  if (row.indicatorName) {
    return row.indicatorCode ? `${row.indicatorName}（${row.indicatorCode}）` : row.indicatorName
  }
  return row.indicatorId || '-'
}

function getIndicatorOptions(currentRow: StandardIndicatorRow) {
  const usedIds = new Set(
    formData.indicators
      .filter((r) => r !== currentRow && r.indicatorId)
      .map((r) => r.indicatorId)
  )
  return indicatorOptions.value.filter((opt) => !usedIds.has(opt.id))
}

function handleIndicatorSelect(row: StandardIndicatorRow, indicatorId: string) {
  if (!indicatorId) {
    row.indicatorId = ''
    row.indicatorName = ''
    row.indicatorCode = ''
    row.category = ''
    row.unit = ''
    return
  }
  const item = indicatorOptions.value.find((i) => i.id === indicatorId)
  if (!item) return
  row.indicatorId = item.id
  row.indicatorName = item.indicatorName
  row.indicatorCode = item.indicatorCode
  row.category = item.category ?? item.indicatorCategory ?? ''
  row.unit = item.unit ?? ''
}

async function enrichIndicators(indicators: any[]): Promise<StandardIndicatorRow[]> {
  if (!indicatorOptions.value.length) {
    await loadIndicatorOptions()
  }
  return (indicators || []).map((ind) => {
    const indicatorId = ind.indicatorId ?? ind.id ?? ''
    const meta = indicatorOptions.value.find((o) => o.id === indicatorId)
    return {
      indicatorId,
      indicatorName: ind.indicatorName ?? meta?.indicatorName ?? '',
      indicatorCode: ind.indicatorCode ?? meta?.indicatorCode ?? '',
      category: ind.category ?? ind.indicatorCategory ?? meta?.category ?? meta?.indicatorCategory ?? '',
      unit: ind.unit ?? meta?.unit ?? '',
      lowerLimit: ind.lowerLimit ?? '',
      upperLimit: ind.upperLimit ?? '',
      concessionLower: ind.concessionLower,
      concessionUpper: ind.concessionUpper,
      isRequired: ind.isRequired
    }
  })
}

/** 关联客户下拉：绕过登录时加载的 Redis 旧缓存，强制从 DB 刷新 */
async function loadCustomerOptions() {
  try {
    const items = await dictStore.refreshItems('QC_CUSTOMER')
    customerOptions.value = items.length ? items : [...FALLBACK_CUSTOMERS]
  } catch {
    customerOptions.value = [...FALLBACK_CUSTOMERS]
  }
}

const FORM_DICT_CODES = ['STANDARD_TYPE', 'PRODUCT_VARIETY', 'PRODUCT_GRADE', 'STANDARD_STATUS'] as const

/** 标准维护表单依赖的字典：登录前 loadAll 可能失败，此处按需补拉 */
async function ensureFormDictOptions() {
  if (!dictStore.loaded) {
    await dictStore.loadAll().catch(() => {})
  }
  await Promise.all(
    FORM_DICT_CODES.map(async (code) => {
      if (dictStore.getItems(code).length > 0) return
      await dictStore.refreshItems(code).catch(() => {})
    })
  )
}

async function openDrawer(mode: 'add' | 'edit' | 'view', row?: any) {
  drawerMode.value = mode
  await Promise.all([loadIndicatorOptions(), loadCustomerOptions(), ensureFormDictOptions()])
  if (mode === 'add') {
    Object.assign(formData, defaultForm())
    applySourceDocuments()
  } else if (row) {
    try {
      const detail = await getStandardById(row.id) as any
      const indicators = await enrichIndicators(detail.indicators || [])
      Object.assign(formData, mapStandardForm(detail), { indicators })
      applySourceDocuments(detail.sourceDocuments)
    } catch {
      const indicators = await enrichIndicators(row.indicators || [])
      Object.assign(formData, mapStandardForm(row), { indicators })
      applySourceDocuments()
    }
  }
  drawerVisible.value = true
}

function handleAdd() {
  openDrawer('add')
}

function handleEdit(row: any) {
  openDrawer('edit', row)
}

function handleView(row: any) {
  openDrawer('view', row)
}

function handlePublish(row: any) {
  publishTarget.value = row
  publishExpiryDate.value = ''
  publishDialogVisible.value = true
}

type RowActionType = 'primary' | 'warning' | 'success' | 'danger'

interface RowAction {
  key: string
  label: string
  type: RowActionType
  handler: () => void
}

const VISIBLE_ACTION_LIMIT = 3

function getRowActions(row: any): RowAction[] {
  const actions: RowAction[] = [
    { key: 'view', label: '查看', type: 'primary', handler: () => handleView(row) }
  ]
  if (row.status === 'DRAFT' || row.status === 'PUBLISHED') {
    actions.push({ key: 'edit', label: '编辑', type: 'warning', handler: () => handleEdit(row) })
  }
  if (row.status === 'DRAFT') {
    actions.push({ key: 'publish', label: '发布', type: 'success', handler: () => handlePublish(row) })
  }
  actions.push({ key: 'delete', label: '删除', type: 'danger', handler: () => handleDelete(row) })
  return actions
}

function splitRowActions(actions: RowAction[]) {
  if (actions.length <= VISIBLE_ACTION_LIMIT) {
    return { visible: actions, more: [] as RowAction[] }
  }
  return {
    visible: actions.slice(0, VISIBLE_ACTION_LIMIT),
    more: actions.slice(VISIBLE_ACTION_LIMIT)
  }
}

function getRowActionGroups(row: any) {
  return splitRowActions(getRowActions(row))
}

function runRowAction(row: any, key: string) {
  getRowActions(row).find((a) => a.key === key)?.handler()
}

async function handleDelete(row: any) {
  const name = row.standardName || row.standardCode || row.id
  const isPublished = row.status === 'PUBLISHED'
  const tip = isPublished
    ? `标准「${name}」已发布，删除后历史判定依据快照仍保留，但该标准将不再参与后续判定。确定删除？`
    : `确定删除草稿标准「${name}」？删除后不可恢复。`
  try {
    await ElMessageBox.confirm(tip, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteStandard(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // handled by request interceptor
  }
}

async function confirmPublish() {
  if (!publishTarget.value) return
  publishLoading.value = true
  try {
    const result = await publishStandard(publishTarget.value.id) as any
    const ingestError = result?.sourceIngestError
    const ingests = (result?.sourceIngests || []) as Array<{ documentId?: string; chunkCount?: number; indexStatus?: string }>
    let msg = ingestError ? `标准发布成功，但部分源文件索引失败：${ingestError}` : '标准发布成功'
    if (ingests.length) {
      const summary = ingests.map((item) => `${item.documentId || '-'}:${item.indexStatus || '-'}(${item.chunkCount ?? 0})`).join('；')
      msg += `；源文件索引：${summary}`
    }
    ElMessage.success(msg)
    publishDialogVisible.value = false
    loadData()
  } finally {
    publishLoading.value = false
  }
}

async function handleSourceUpload(options: UploadRequestOptions) {
  if (!formData.id) {
    ElMessage.warning('请先保存标准后再上传源文件')
    return
  }
  sourceUploadLoading.value = true
  try {
    await uploadStandardSourceFile(formData.id, options.file as File)
    await reloadSourceDocuments(formData.id)
    ElMessage.success('源文件上传成功')
  } finally {
    sourceUploadLoading.value = false
  }
}

async function handleSourceDownload(row: StandardSourceDocumentSummary) {
  if (!formData.id || !row.documentId) return
  try {
    const blob = await downloadStandardSourceFile(formData.id, row.documentId)
    const fileName = row.sourceFileName || 'standard-source.bin'
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = fileName
    link.click()
    URL.revokeObjectURL(link.href)
  } catch {
    // handled by fetch error
  }
}

async function handleSourceDelete(row: StandardSourceDocumentSummary) {
  if (!formData.id || !row.documentId) return
  try {
    await ElMessageBox.confirm(`确定删除源文件「${row.sourceFileName || row.documentId}」？`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteStandardSourceFile(formData.id, row.documentId)
    await reloadSourceDocuments(formData.id)
    ElMessage.success('源文件已删除')
  } catch {
    // handled by request interceptor
  }
}

async function handleSourceReindex(row: StandardSourceDocumentSummary) {
  if (!formData.id || !row.documentId) return
  sourceReindexDocumentId.value = row.documentId
  try {
    await reindexStandardSourceFile(formData.id, row.documentId)
    await reloadSourceDocuments(formData.id)
    ElMessage.success('重新索引完成')
  } finally {
    sourceReindexDocumentId.value = ''
  }
}

async function handleSourceReindexAll() {
  if (!formData.id) return
  sourceReindexAllLoading.value = true
  try {
    await reindexAllStandardSourceFiles(formData.id)
    await reloadSourceDocuments(formData.id)
    ElMessage.success('全部源文件重新索引完成')
  } finally {
    sourceReindexAllLoading.value = false
  }
}

function addIndicatorRow() {
  if (!indicatorOptions.value.length && !indicatorOptionsLoading.value) {
    loadIndicatorOptions()
  }
  formData.indicators.push({
    indicatorId: '',
    indicatorName: '',
    indicatorCode: '',
    category: '',
    unit: '',
    lowerLimit: '',
    upperLimit: '',
    concessionLower: '',
    concessionUpper: ''
  } as StandardIndicatorRow)
}

function removeIndicatorRow(index: number) {
  formData.indicators.splice(index, 1)
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请完善必填项后再保存')
    return
  }
  if (!formData.expiryDate) {
    formData.expiryDate = '9999-12-31'
  }
  if (!formData.indicators.length) {
    ElMessage.warning('请至少添加一条指标配置')
    return
  }
  if (formData.indicators.some((row) => !row.indicatorId)) {
    ElMessage.warning('请为每条指标选择指标项目')
    return
  }
  submitLoading.value = true
  try {
    if (drawerMode.value === 'add') {
      await addStandard(formData)
      ElMessage.success('新增成功')
    } else {
      await updateStandard(formData)
      ElMessage.success('保存成功')
    }
    drawerVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadCustomerOptions()
  ensureFormDictOptions()
})
</script>

<style scoped>
.standard-lib-page {
  padding: 16px;
}
.search-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}
.text-warning {
  color: #e6a23c;
}
.text-meta {
  color: var(--text-muted);
  font-size: 12px;
}

.standard-lib-page :deep(.status-col .cell) {
  overflow: visible;
}
.standard-lib-page :deep(.status-col) {
  overflow: visible;
}
.status-tag {
  max-width: 100%;
  white-space: nowrap;
}

.standard-lib-page :deep(.op-col .cell) {
  overflow: visible;
}
.op-cell {
  display: inline-flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
}
.op-more-btn {
  padding: 0 4px;
  font-weight: 600;
  letter-spacing: 1px;
}
.op-more-item--danger {
  color: var(--el-color-danger);
}
.op-more-item--warning {
  color: var(--el-color-warning);
}
.op-more-item--success {
  color: var(--el-color-success);
}
.op-more-item--primary {
  color: var(--el-color-primary);
}

.source-error-icon {
  margin-left: 4px;
  color: var(--el-color-warning);
  vertical-align: middle;
}

.standard-lib-page :deep(.source-op-col .cell) {
  overflow: visible;
}

.source-file-op-cell {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.source-file-panel :deep(.source-file-op-btn.el-button--primary.is-plain) {
  background: rgba(0, 212, 255, 0.12) !important;
  border-color: rgba(0, 212, 255, 0.55) !important;
  color: #00d4ff !important;
}

.source-file-panel :deep(.source-file-op-btn.el-button--primary.is-plain:hover) {
  background: rgba(0, 212, 255, 0.22) !important;
  border-color: #00d4ff !important;
  color: #ffffff !important;
}

.source-file-panel :deep(.source-file-op-btn.el-button--warning.is-plain) {
  background: rgba(255, 140, 0, 0.12) !important;
  border-color: rgba(255, 140, 0, 0.55) !important;
  color: #ff8c00 !important;
}

.source-file-panel :deep(.source-file-op-btn.el-button--warning.is-plain:hover) {
  background: rgba(255, 140, 0, 0.22) !important;
  border-color: #ff8c00 !important;
  color: #ffffff !important;
}

.source-file-panel :deep(.source-file-op-btn.el-button--danger.is-plain) {
  background: rgba(255, 59, 92, 0.12) !important;
  border-color: rgba(255, 59, 92, 0.55) !important;
  color: #ff3b5c !important;
}

.source-file-panel :deep(.source-file-op-btn.el-button--danger.is-plain:hover) {
  background: rgba(255, 59, 92, 0.22) !important;
  border-color: #ff3b5c !important;
  color: #ffffff !important;
}

.clause-drawer-summary {
  margin-bottom: 12px;
}

.clause-drawer-file {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 6px;
}

.clause-drawer-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.clause-drawer-hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.clause-drawer-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.clause-list-table :deep(.el-table__row) {
  cursor: pointer;
}

.clause-drawer-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.clause-detail-text {
  white-space: pre-wrap;
  line-height: 1.6;
  max-height: 420px;
  overflow: auto;
  padding: 12px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  font-size: 13px;
}
</style>
