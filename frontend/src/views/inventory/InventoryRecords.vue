<template>
  <div class="inventory-container">
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><Document /></el-icon>
        库存变动记录
      </h2>
    </div>

    <el-card class="table-card">
      <el-tabs v-model="activeTab" class="record-tabs">
        <el-tab-pane label="入库记录" name="stockIn">
          <el-table
            v-loading="stockInLoading"
            :data="stockInRecords"
            style="width: 100%"
            stripe
            border
            empty-text="暂无数据"
          >
            <el-table-column prop="recordNo" label="入库单号" min-width="150" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.recordNo || '—' }}</template>
            </el-table-column>
            <el-table-column prop="productName" label="商品名称" min-width="140" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.productName || '—' }}</template>
            </el-table-column>
            <el-table-column prop="specification" label="规格" width="100" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.specification || '—' }}</template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="70">
              <template #default="scope">{{ scope.row.unit || '—' }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" align="right">
              <template #default="scope">{{ scope.row.quantity || 0 }}</template>
            </el-table-column>
            <el-table-column prop="unitPrice" label="进价" width="100" align="right" :formatter="priceFormatter" />
            <el-table-column prop="lineAmount" label="小计" width="100" align="right" :formatter="priceFormatter" />
            <el-table-column prop="supplierName" label="供应商" min-width="120" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.supplierName || ('#' + (scope.row.supplierId ?? '')) }}</template>
            </el-table-column>
            <el-table-column label="操作人" width="110" show-overflow-tooltip>
              <template #default="scope">{{ formatOperator(scope.row) }}</template>
            </el-table-column>
            <el-table-column prop="remark" label="单据备注" min-width="120" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.remark || '—' }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="入库时间" width="170">
              <template #default="scope">{{ scope.row.createTime || '—' }}</template>
            </el-table-column>
          </el-table>
          <div class="pagination-container" style="margin-top: 12px">
            <el-pagination
              v-model:current-page="stockInCurrentPage"
              v-model:page-size="stockInPageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next"
              :total="stockInTotal"
              @size-change="handleStockInSizeChange"
              @current-change="handleStockInPageChange"
            />
          </div>
        </el-tab-pane>
        <el-tab-pane label="出库记录" name="stockOut">
          <el-table
            v-loading="stockOutLoading"
            :data="stockOutRecords"
            style="width: 100%"
            stripe
            border
            empty-text="暂无数据"
          >
            <el-table-column prop="recordNo" label="出库单号" min-width="150" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.recordNo || '—' }}</template>
            </el-table-column>
            <el-table-column prop="outType" label="出库原因" min-width="120" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.outType || '—' }}</template>
            </el-table-column>
            <el-table-column prop="productName" label="商品名称" min-width="140" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.productName || '—' }}</template>
            </el-table-column>
            <el-table-column prop="specification" label="规格" width="100" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.specification || '—' }}</template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="70">
              <template #default="scope">{{ scope.row.unit || '—' }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" align="right">
              <template #default="scope">{{ scope.row.quantity || 0 }}</template>
            </el-table-column>
            <el-table-column prop="unitPrice" label="单价" width="100" align="right" :formatter="priceFormatter" />
            <el-table-column prop="lineAmount" label="小计" width="100" align="right" :formatter="priceFormatter" />
            <el-table-column label="操作人" width="110" show-overflow-tooltip>
              <template #default="scope">{{ formatOperator(scope.row) }}</template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="100" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.remark || '—' }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="出库时间" width="170">
              <template #default="scope">{{ scope.row.createTime || '—' }}</template>
            </el-table-column>
          </el-table>
          <div class="pagination-container" style="margin-top: 12px">
            <el-pagination
              v-model:current-page="stockOutCurrentPage"
              v-model:page-size="stockOutPageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next"
              :total="stockOutTotal"
              @size-change="handleStockOutSizeChange"
              @current-change="handleStockOutPageChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { inventoryAPI, unwrapPagePayload } from '../../services/api'

const stockInLoading = ref(false)
const stockOutLoading = ref(false)
const stockInRecords = ref([])
const stockOutRecords = ref([])
const stockInCurrentPage = ref(1)
const stockInPageSize = ref(10)
const stockInTotal = ref(0)
const stockOutCurrentPage = ref(1)
const stockOutPageSize = ref(10)
const stockOutTotal = ref(0)
const activeTab = ref('stockIn')

const priceFormatter = (row, column, cellValue) => {
  return `¥${(cellValue || 0).toFixed(2)}`
}

function formatOperator(row) {
  const name = row?.operatorName ?? row?.operator_name
  if (name) return name
  const id = row?.operatorId ?? row?.operator_id
  if (id != null && id !== '') return `用户#${id}`
  return '—'
}

const getStockInRecords = async () => {
  try {
    stockInLoading.value = true
    const params = {
      page: stockInCurrentPage.value,
      pageSize: stockInPageSize.value
    }
    const response = await inventoryAPI.getStockInRecords(params)
    const page = unwrapPagePayload(response)
    stockInRecords.value = page.records
    stockInTotal.value = page.total
  } catch (error) {
    console.error('获取入库记录失败:', error)
    ElMessage.error('获取入库记录失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    stockInLoading.value = false
  }
}

const handleStockInPageChange = (p) => {
  stockInCurrentPage.value = p
  getStockInRecords()
}

const handleStockInSizeChange = (size) => {
  stockInPageSize.value = size
  stockInCurrentPage.value = 1
  getStockInRecords()
}

const getStockOutRecords = async () => {
  try {
    stockOutLoading.value = true
    const params = {
      page: stockOutCurrentPage.value,
      pageSize: stockOutPageSize.value
    }
    const response = await inventoryAPI.getStockOutRecords(params)
    const page = unwrapPagePayload(response)
    stockOutRecords.value = page.records
    stockOutTotal.value = page.total
  } catch (error) {
    console.error('获取出库记录失败:', error)
    ElMessage.error('获取出库记录失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    stockOutLoading.value = false
  }
}

const handleStockOutPageChange = (p) => {
  stockOutCurrentPage.value = p
  getStockOutRecords()
}

const handleStockOutSizeChange = (size) => {
  stockOutPageSize.value = size
  stockOutCurrentPage.value = 1
  getStockOutRecords()
}

onMounted(() => {
  getStockInRecords()
  getStockOutRecords()
})
</script>

<style scoped>
.inventory-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-subtitle {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 1.25rem;
  font-weight: 600;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.record-tabs {
  margin-top: 10px;
}
</style>
