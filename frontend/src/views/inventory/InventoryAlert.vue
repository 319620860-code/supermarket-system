<template>
  <div class="inventory-container">
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><Warning /></el-icon>
        库存预警
      </h2>
      <div class="page-header-actions">
        <el-button type="primary" @click="loadLowInventoryAlert">刷新</el-button>
      </div>
    </div>

    <el-card class="alert-card" shadow="hover">
      <template #header>
        <div class="alert-card-header">
          <span class="alert-title">
            <el-icon><Warning /></el-icon>
            当前库存 ≤ 最低库存的商品
          </span>
        </div>
      </template>
      <el-table
        v-loading="lowInvLoading"
        :data="lowInventoryList"
        stripe
        border
        max-height="520"
        empty-text="暂无低库存商品"
        size="small"
      >
        <el-table-column prop="name" label="商品名称" min-width="160" />
        <el-table-column prop="barcode" label="条码" width="140" />
        <el-table-column prop="category_name" label="分类" width="120">
          <template #default="scope">{{ scope.row.category_name || scope.row.categoryName || '-' }}</template>
        </el-table-column>
        <el-table-column label="当前库存" width="100" align="center">
          <template #default="scope">{{ scope.row.quantity ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="最低库存" width="100" align="center">
          <template #default="scope">{{ scope.row.min_stock ?? scope.row.minStock ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="selling_price" label="售价" width="100" align="right">
          <template #default="scope">¥{{ Number(scope.row.selling_price ?? scope.row.sellingPrice ?? 0).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { reportAPI } from '../../services/api'

const lowInventoryList = ref([])
const lowInvLoading = ref(false)

const loadLowInventoryAlert = async () => {
  lowInvLoading.value = true
  try {
    const res = await reportAPI.getLowInventoryReport({})
    const list = Array.isArray(res) ? res : res?.data ?? []
    lowInventoryList.value = list
  } catch (e) {
    console.error(e)
    ElMessage.error('加载库存预警失败')
  } finally {
    lowInvLoading.value = false
  }
}

onMounted(() => {
  loadLowInventoryAlert()
})
</script>

<style scoped>
.inventory-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.page-subtitle {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 1.25rem;
  font-weight: 600;
}

.page-header-actions {
  display: flex;
  gap: 8px;
}

.alert-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
</style>
