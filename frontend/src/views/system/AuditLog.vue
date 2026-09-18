<template>
  <div class="audit-log-container">
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><Notebook /></el-icon>
        操作日志
      </h2>
      <p class="page-desc">记录入库、出库、订单支付/取消/退款等关键操作（仅管理员可查看）</p>
    </div>

    <el-card shadow="hover">
      <div class="audit-toolbar">
        <el-select
          v-model="filterAction"
          clearable
          placeholder="操作类型"
          style="width: 170px"
          empty-text="暂无数据"
        >
          <el-option
            v-for="opt in actionOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <el-input
          v-model="filterUsername"
          clearable
          placeholder="用户名（模糊）"
          style="width: 180px"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <el-button @click="exportCurrentPageCsv">导出本页</el-button>
        <el-button @click="exportFilteredCsv">导出筛选（最多500条）</el-button>
      </div>

      <el-table v-loading="loading" :data="records" stripe style="width: 100%" empty-text="暂无记录">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="时间" width="180">
          <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="username" label="用户" width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="140">
          <template #default="scope">
            <el-tag size="small" type="info">{{ actionLabel(scope.row.action) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="详情" min-width="260" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="130" show-overflow-tooltip />
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          :total-text="'共'"
          :page-size-text="'条/页'"
          :jumper-text="'前往'"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Notebook } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { auditLogAPI } from '../../services/api'

const loading = ref(false)
const records = ref([])
const filterAction = ref('')
const filterUsername = ref('')

const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

const ACTION_LABELS = {
  STOCK_IN_CREATE: '创建入库单',
  STOCK_OUT_CREATE: '创建出库单',
  ORDER_PAY: '订单支付',
  ORDER_CANCEL: '订单取消',
  ORDER_REFUND: '订单退款'
}

const actionOptions = Object.entries(ACTION_LABELS).map(([value, label]) => ({ value, label }))

function actionLabel(code) {
  return ACTION_LABELS[code] || code || '—'
}

function formatDateTime(v) {
  if (v == null) return '—'
  if (Array.isArray(v) && v.length >= 3) {
    const [y, m, d, h = 0, mi = 0, s = 0] = v
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(mi).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

function buildQueryParams() {
  const p = {
    page: pagination.value.page,
    pageSize: pagination.value.pageSize
  }
  if (filterAction.value) p.action = filterAction.value
  if (filterUsername.value && String(filterUsername.value).trim()) {
    p.username = String(filterUsername.value).trim()
  }
  return p
}

async function loadData() {
  loading.value = true
  try {
    const res = await auditLogAPI.getPage(buildQueryParams())
    const data = res?.data !== undefined ? res.data : res
    records.value = data?.records ?? data?.list ?? []
    pagination.value.total = Number(data?.total ?? 0)
  } catch (e) {
    console.error(e)
    ElMessage.error('加载操作日志失败：' + (e.response?.data?.message || e.message || '未知错误'))
    records.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.page = 1
  loadData()
}

function resetFilters() {
  filterAction.value = ''
  filterUsername.value = ''
  pagination.value.page = 1
  loadData()
}

function escapeCsvCell(val) {
  const s = String(val ?? '')
  if (/[",\r\n]/.test(s)) return `"${s.replace(/"/g, '""')}"`
  return s
}

function downloadCsv(filename, lines) {
  const blob = new Blob(['\uFEFF' + lines.join('\r\n')], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = filename
  a.click()
  URL.revokeObjectURL(a.href)
}

function rowsToCsvLines(rows) {
  const header = ['ID', '时间', '用户', '操作', '详情', 'IP'].map(escapeCsvCell).join(',')
  const body = rows.map((row) =>
    [
      row.id,
      formatDateTime(row.createTime),
      row.username,
      actionLabel(row.action),
      row.detail,
      row.ip
    ]
      .map(escapeCsvCell)
      .join(',')
  )
  return [header, ...body]
}

function exportCurrentPageCsv() {
  if (!records.value.length) {
    ElMessage.warning('当前无数据可导出')
    return
  }
  downloadCsv(`操作日志_本页_${pagination.value.page}.csv`, rowsToCsvLines(records.value))
  ElMessage.success('已导出当前页')
}

async function exportFilteredCsv() {
  loading.value = true
  try {
    const res = await auditLogAPI.getPage({
      page: 1,
      pageSize: 500,
      ...(filterAction.value ? { action: filterAction.value } : {}),
      ...(filterUsername.value && String(filterUsername.value).trim()
        ? { username: String(filterUsername.value).trim() }
        : {})
    })
    const data = res?.data !== undefined ? res.data : res
    const list = data?.records ?? data?.list ?? []
    const total = Number(data?.total ?? 0)
    if (!list.length) {
      ElMessage.warning('无数据可导出')
      return
    }
    downloadCsv('操作日志_筛选.csv', rowsToCsvLines(list))
    if (total > 500) {
      ElMessage.success(`已导出前 500 条（共 ${total} 条匹配，可缩小筛选条件后再导出）`)
    } else {
      ElMessage.success(`已导出 ${list.length} 条`)
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败：' + (e.response?.data?.message || e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.audit-log-container {
  padding: 20px;
}

.page-desc {
  margin: 8px 0 0;
  font-size: 13px;
  color: #909399;
}

.audit-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
