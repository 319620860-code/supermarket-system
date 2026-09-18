<template>
  <div class="inventory-check-records">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>库存盘点记录</span>
          <div class="card-header-actions">
            <el-input
              v-model="searchForm.keyword"
              placeholder="输入商品名称或盘点单号"
              style="width: 240px"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="checkRecords"
        style="width: 100%"
        stripe
        border
        :default-sort="{ prop: 'createTime', order: 'descending' }"
      >
        <el-table-column prop="checkNumber" label="盘点单号" width="180" />
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="systemQuantity" label="系统数量" width="120" />
        <el-table-column prop="actualQuantity" label="实际数量" width="120" />
        <el-table-column prop="difference" label="差异" width="100" />
        <el-table-column prop="unitPrice" label="单价" width="100" :formatter="priceFormatter" />
        <el-table-column prop="differenceAmount" label="差异金额" width="120" :formatter="priceFormatter" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 || row.status === '1' ? 'success' : 'warning'">
              {{ row.status === 1 || row.status === '1' ? '已确认' : '待确认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="createTime" label="盘点时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0 || row.status === '0'"
              type="primary"
              size="small"
              @click="handleConfirm(row.id)"
            >
              确认
            </el-button>
            <el-button type="info" size="small" @click="handleViewDetails(row.id)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="盘点记录详情"
      width="600px"
    >
      <el-form :model="detailForm" label-width="100px">
        <el-form-item label="盘点单号">
          <el-input v-model="detailForm.checkNumber" disabled />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="detailForm.productName" disabled />
        </el-form-item>
        <el-form-item label="系统数量">
          <el-input v-model="detailForm.systemQuantity" disabled />
        </el-form-item>
        <el-form-item label="实际数量">
          <el-input v-model="detailForm.actualQuantity" disabled />
        </el-form-item>
        <el-form-item label="差异">
          <el-input v-model="detailForm.difference" disabled />
        </el-form-item>
        <el-form-item label="单价">
          <el-input v-model="detailForm.unitPrice" disabled />
        </el-form-item>
        <el-form-item label="差异金额">
          <el-input v-model="detailForm.differenceAmount" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-input v-model="detailForm.statusLabel" disabled />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="detailForm.operatorName" disabled />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="detailForm.remark" type="textarea" disabled />
        </el-form-item>
        <el-form-item label="盘点时间">
          <el-input v-model="detailForm.createTime" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            v-if="detailForm.status === 0 || detailForm.status === '0'"
            type="primary"
            @click="handleConfirmInDialog"
          >
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import api, { unwrapPagePayload } from '../../services/api'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const checkRecords = ref([])

const searchForm = reactive({
  keyword: ''
})

const detailDialogVisible = ref(false)
const detailForm = reactive({})
const currentCheckId = ref(null)

// 价格格式化
const priceFormatter = (row, column, cellValue) => {
  return `¥${cellValue.toFixed(2)}`
}

// 获取盘点记录列表
const getCheckRecords = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    const response = await api.get('/stock-check/records', { params })
    const data = unwrapPagePayload(response)
    checkRecords.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('获取盘点记录失败')
    console.error('获取盘点记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  getCheckRecords()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  getCheckRecords()
}

// 页码变化
const handleCurrentChange = (current) => {
  currentPage.value = current
  getCheckRecords()
}

// 确认盘点
const handleConfirm = async (id) => {
  try {
    await api.post(`/stock-check/${id}/confirm`)
    ElMessage.success('确认盘点成功')
    getCheckRecords()
  } catch (error) {
    ElMessage.error('确认盘点失败')
    console.error('确认盘点失败:', error)
  }
}

// 查看详情
const handleViewDetails = async (id) => {
  try {
    const data = await api.get(`/stock-check/${id}`)
    currentCheckId.value = id
    detailForm.checkNumber = data.checkNumber || ''
    detailForm.productName = data.productName || '未知商品'
    detailForm.systemQuantity = data.systemQuantity || 0
    detailForm.actualQuantity = data.actualQuantity || 0
    detailForm.difference = data.difference || 0
    detailForm.unitPrice = data.unitPrice ? `¥${data.unitPrice.toFixed(2)}` : '¥0.00'
    detailForm.differenceAmount = data.differenceAmount ? `¥${data.differenceAmount.toFixed(2)}` : '¥0.00'
    detailForm.status = data.status || 0
    detailForm.statusLabel = data.status === 1 || data.status === '1' ? '已确认' : '待确认'
    detailForm.operatorName = data.operatorName || '未知用户'
    detailForm.remark = data.remark || ''
    detailForm.createTime = data.createTime || ''
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取盘点记录详情失败')
    console.error('获取盘点记录详情失败:', error)
  }
}

// 在对话框中确认盘点
const handleConfirmInDialog = async () => {
  if (!currentCheckId.value) {
    ElMessage.error('缺少盘点记录ID')
    return
  }
  try {
    await api.post(`/stock-check/${currentCheckId.value}/confirm`)
    ElMessage.success('确认盘点成功')
    detailDialogVisible.value = false
    getCheckRecords()
  } catch (error) {
    ElMessage.error('确认盘点失败')
    console.error('确认盘点失败:', error)
  }
}

// 初始化
onMounted(() => {
  getCheckRecords()
})
</script>

<style scoped>
.inventory-check-records {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>