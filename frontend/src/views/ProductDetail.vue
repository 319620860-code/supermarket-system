<template>
  <div class="product-detail">
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><View /></el-icon>
        商品详情
      </h2>
      <div class="page-header-actions">
        <el-button type="primary" link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="8" animated />

    <template v-else-if="product">
      <el-card class="detail-card" shadow="never">
        <template #header>
          <span>基本信息</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品编号">{{ product.id }}</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ product.name }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ product.categoryName || product.category_name || '—' }}</el-descriptions-item>
          <el-descriptions-item label="条码">{{ product.barcode || '—' }}</el-descriptions-item>
          <el-descriptions-item label="规格">{{ product.specification || '—' }}</el-descriptions-item>
          <el-descriptions-item label="单位">{{ product.unit || '—' }}</el-descriptions-item>
          <el-descriptions-item label="进价">{{ formatMoney(product.purchasePrice) }}</el-descriptions-item>
          <el-descriptions-item label="售价">{{ formatMoney(product.sellingPrice) }}</el-descriptions-item>
          <el-descriptions-item label="库存数量">{{ product.quantity ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="最低库存">{{ product.minStock ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="product.status === 1 ? 'success' : 'danger'" size="small">
              {{ product.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ product.createTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">{{ product.updateTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ product.description || '—' }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="!isReadOnlyCashier" class="detail-actions">
          <el-button type="primary" @click="goEdit">编辑商品</el-button>
        </div>
      </el-card>
    </template>

    <el-empty v-else description="商品不存在或已删除" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, View } from '@element-plus/icons-vue'
import { productAPI } from '../services/api'
import { ROLE_CASHIER, getEffectiveSelectedRole } from '../utils/roleAccess.js'

const route = useRoute()
const router = useRouter()

const isReadOnlyCashier = computed(() => getEffectiveSelectedRole() === ROLE_CASHIER)

const loading = ref(true)
const product = ref(null)

function formatMoney(v) {
  const n = Number(v)
  return Number.isFinite(n) ? `¥${n.toFixed(2)}` : '—'
}

async function loadData() {
  const id = route.params.id
  if (!id) {
    product.value = null
    loading.value = false
    return
  }
  loading.value = true
  product.value = null
  try {
    const res = await productAPI.getProductDetail(id)
    const outer = res?.data !== undefined ? res.data : res
    product.value = outer?.product ?? outer
    if (!product.value || !product.value.id) {
      ElMessage.warning('未找到该商品')
    }
  } catch (e) {
    product.value = null
    ElMessage.error(e.response?.data?.message || e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push({ name: 'Product' })
}

function goEdit() {
  if (!product.value?.id) return
  router.push({ name: 'Product', query: { editId: String(product.value.id) } })
}

onMounted(loadData)
watch(() => route.params.id, loadData)
</script>

<style scoped>
.product-detail {
  padding: 20px;
}
.detail-card {
  margin-bottom: 20px;
}
.detail-actions {
  margin-top: 16px;
}
</style>
