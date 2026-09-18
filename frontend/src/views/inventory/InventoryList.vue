<template>
  <div class="inventory-container">
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><Files /></el-icon>
        库存查询
      </h2>
      <div class="page-header-actions">
        <el-button type="primary" @click="showStockInDialog">
          <el-icon><Plus /></el-icon> 商品入库
        </el-button>
        <el-button type="warning" @click="showStockOutDialog">
          <el-icon><Minus /></el-icon> 商品出库
        </el-button>
        <!-- 库存盘点按钮已隐藏 -->
        <!-- <el-button type="info" @click="showInventoryCheckDialog">
          <el-icon><Document /></el-icon> 库存盘点
        </el-button> -->
      </div>
    </div>

    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="商品编号">
          <el-input v-model="searchForm.productId" placeholder="请输入商品编号" clearable></el-input>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable :loading="warehouseLoading" empty-text="暂无数据">
            <el-option v-for="warehouse in warehouseOptions" :key="warehouse.id" :label="warehouse.name" :value="warehouse.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="库存水平">
          <el-select v-model="searchForm.stockStatus" placeholder="相对最低库存" clearable style="width: 130px" empty-text="暂无数据">
            <el-option label="正常" value="normal"></el-option>
            <el-option label="低库存" value="low"></el-option>
            <el-option label="零库存" value="shortage"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.inventoryStatus" placeholder="启用/禁用" clearable style="width: 120px">
            <el-option label="启用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        empty-text="暂无数据"
        :data="inventoryList"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="productId" label="商品编号" width="100"></el-table-column>
        <el-table-column prop="productName" label="商品名称" min-width="150"></el-table-column>
        <el-table-column prop="warehouseName" label="仓库" width="120"></el-table-column>
        <el-table-column prop="warehouseId" label="仓库编号" width="100"></el-table-column>
        <el-table-column prop="quantity" label="库存数量" width="120"></el-table-column>
        <el-table-column prop="availableQuantity" label="可用数量" width="120"></el-table-column>
        <el-table-column prop="lockedQuantity" label="锁定数量" width="120"></el-table-column>
        <el-table-column prop="unitCost" label="单位成本" width="120" :formatter="priceFormatter"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="showStockInDialog(scope.row)">
              入库
            </el-button>
            <el-button type="warning" size="small" @click="showStockOutDialog(scope.row)">
              出库
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
          :total-text="'共'"
          :page-size-text="'条/页'"
          :jumper-text="'前往'"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <el-dialog
      v-model="stockInDialogVisible"
      title="商品入库"
      width="600px"
      :before-close="handleStockInDialogClose"
    >
      <el-form
        ref="stockInFormRef"
        :model="stockInForm"
        :rules="stockInRules"
        label-position="top"
      >
        <el-form-item label="商品信息">
          <el-select
            v-model="stockInForm.productId"
            placeholder="请选择商品"
            filterable
            remote
            :remote-method="remoteSearchProduct"
            :loading="productLoading"
            @change="handleProductChange"
            empty-text="暂无数据"
          >
            <el-option
              v-for="item in productOptions"
              :key="item.id"
              :label="`${item.name}（编号 ${item.id}）`"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select
            v-model="stockInForm.warehouseId"
            placeholder="请选择仓库"
            filterable
            remote
            :loading="warehouseLoading"
            empty-text="暂无数据"
          >
            <el-option
              v-for="item in warehouseOptions"
              :key="item.id"
              :label="`${item.name}（编号 ${item.id}）`"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="供应商">
          <el-select
            v-model="stockInForm.supplierId"
            placeholder="请选择供应商"
            filterable
            remote
            :remote-method="remoteSearchSupplier"
            :loading="supplierLoading"
            @change="handleSupplierChange"
            empty-text="暂无数据"
          >
            <el-option
              v-for="item in supplierOptions"
              :key="item.id"
              :label="`${item.name} (联系人: ${item.contactPerson})`"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="stockInForm.quantity" :min="1" placeholder="请输入入库数量"></el-input-number>
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="stockInForm.unitPrice" :min="0" :step="0.01" placeholder="请输入商品单价"></el-input-number>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="stockInForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleStockInDialogClose">取消</el-button>
        <el-button type="primary" @click="handleStockIn">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="stockOutDialogVisible"
      title="商品出库"
      width="600px"
      :before-close="handleStockOutDialogClose"
    >
      <el-form
        ref="stockOutFormRef"
        :model="stockOutForm"
        :rules="stockOutRules"
        label-position="top"
      >
        <el-form-item label="商品信息">
          <el-select
            v-model="stockOutForm.productId"
            placeholder="请选择商品"
            filterable
            remote
            :remote-method="remoteSearchProduct"
            :loading="productLoading"
            @change="handleStockOutProductChange"
            empty-text="暂无数据"
          >
            <el-option
              v-for="item in productOptions"
              :key="item.id"
              :label="`${item.name}（编号 ${item.id}）`"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select
            v-model="stockOutForm.warehouseId"
            placeholder="请选择仓库"
            filterable
            remote
            :loading="warehouseLoading"
            @change="handleStockOutWarehouseChange"
            empty-text="暂无数据"
          >
            <el-option
              v-for="item in warehouseOptions"
              :key="item.id"
              :label="`${item.name}（编号 ${item.id}）`"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number
            v-model="stockOutForm.quantity"
            :min="1"
            :max="stockOutForm.maxStock"
            placeholder="请输入出库数量"
          ></el-input-number>
          <span class="stock-info">当前库存: {{ stockOutForm.currentStock }}</span>
        </el-form-item>
        <el-form-item label="出库原因" prop="reason">
          <el-select v-model="stockOutForm.reason" placeholder="请选择出库原因" empty-text="暂无数据" @change="handleReasonChange">
            <el-option label="超市补货" value="超市补货"></el-option>
            <el-option label="退货" value="退货"></el-option>
            <el-option label="损耗" value="损耗"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="出库目标" prop="outTarget">
          <el-select v-model="stockOutForm.outTarget" placeholder="请选择出库目标" empty-text="暂无数据" @change="handleTargetChange">
            <el-option label="超市货架" value="超市货架"></el-option>
            <el-option label="供应商" value="供应商"></el-option>
            <el-option label="废品处理" value="废品处理"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="stockOutForm.unitPrice" :min="0" :step="0.01" placeholder="请输入商品单价"></el-input-number>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="stockOutForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleStockOutDialogClose">取消</el-button>
        <el-button type="primary" @click="handleStockOut">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="inventoryCheckDialogVisible"
      title="库存盘点"
      width="800px"
      :before-close="handleInventoryCheckDialogClose"
    >
      <el-form label-position="top">
        <el-form-item label="盘点描述">
          <el-input
            v-model="inventoryCheckForm.description"
            type="textarea"
            :rows="2"
            placeholder="请输入本次盘点的描述信息"
          ></el-input>
        </el-form-item>
      </el-form>
      <el-table
        :data="inventoryCheckList"
        style="width: 100%"
        stripe
        border
        max-height="400"
        empty-text="暂无数据"
      >
        <el-table-column prop="productId" label="商品编号" width="120"></el-table-column>
        <el-table-column prop="productName" label="商品名称" min-width="150"></el-table-column>
        <el-table-column prop="currentStock" label="系统库存" width="120"></el-table-column>
        <el-table-column prop="actualStock" label="实际库存" width="140">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.actualStock"
              :min="0"
              :controls="true"
              controls-position="right"
              style="width: 110px"
              @change="handleActualStockChange(scope.row)"
            ></el-input-number>
          </template>
        </el-table-column>
        <el-table-column prop="difference" label="差异" width="100">
          <template #default="scope">
            <span :class="scope.row.difference > 0 ? 'difference-plus' : 'difference-minus'">
              {{ scope.row.difference > 0 ? '+' : '' }}{{ scope.row.difference }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150">
          <template #default="scope">
            <el-input v-model="scope.row.remark" placeholder="请输入备注"></el-input>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="handleInventoryCheckDialogClose">取消</el-button>
        <el-button type="primary" @click="handleInventoryCheck">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Minus, Document, Files } from '@element-plus/icons-vue'
import api, { warehouseAPI, inventoryAPI, productAPI, supplierAPI, unwrapPagePayload } from '../../services/api'

const loading = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  productName: '',
  productId: '',
  warehouseId: '',
  stockStatus: '',
  inventoryStatus: ''
})

const inventoryList = ref([])

const stockInDialogVisible = ref(false)
const stockInFormRef = ref()
const stockInForm = reactive({
  productId: '',
  warehouseId: '',
  supplierId: '',
  quantity: 1,
  unitPrice: 0,
  remark: ''
})
const productOptions = ref([])
const productLoading = ref(false)
const supplierOptions = ref([])
const supplierLoading = ref(false)
const warehouseOptions = ref([])
const warehouseLoading = ref(false)

const stockOutDialogVisible = ref(false)
const stockOutFormRef = ref()
const stockOutForm = reactive({
  productId: '',
  warehouseId: '',
  quantity: 1,
  unitPrice: 0,
  reason: '',
  outTarget: '',
  remark: '',
  currentStock: 0,
  maxStock: 0
})

// 出库原因和出库目标的映射关系
const reasonTargetMap = {
  '超市补货': '超市货架',
  '退货': '供应商',
  '损耗': '废品处理',
  '其他': '其他'
}

// 出库目标和出库原因的映射关系
const targetReasonMap = {
  '超市货架': '超市补货',
  '供应商': '退货',
  '废品处理': '损耗',
  '其他': '其他'
}

const inventoryCheckDialogVisible = ref(false)
const inventoryCheckForm = reactive({
  description: ''
})
const inventoryCheckList = ref([])

const stockInRules = {
  productId: [
    { required: true, message: '请选择商品', trigger: 'change' }
  ],
  warehouseId: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  supplierId: [
    { required: true, message: '请选择供应商', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入入库数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '入库数量不能小于1', trigger: 'blur' }
  ],
  unitPrice: [
    { required: true, message: '请输入商品单价', trigger: 'blur' },
    { type: 'number', min: 0, message: '商品单价不能为负数', trigger: 'blur' }
  ]
}

const stockOutRules = {
  productId: [
    { required: true, message: '请选择商品', trigger: 'change' }
  ],
  warehouseId: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入出库数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '出库数量不能小于1', trigger: 'blur' }
  ],
  unitPrice: [
    { required: true, message: '请输入商品单价', trigger: 'blur' },
    { type: 'number', min: 0, message: '商品单价不能为负数', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请选择出库原因', trigger: 'change' }
  ],
  outTarget: [
    { required: true, message: '请选择出库目标', trigger: 'change' }
  ]
}

onMounted(async () => {
  getInventoryList()
  await getWarehouseList()
})

const getInventoryList = async () => {
  try {
    loading.value = true
    const params = {
      keyword: searchForm.productName || undefined,
      productId: searchForm.productId ? Number(searchForm.productId) : undefined,
      warehouseId: searchForm.warehouseId ? Number(searchForm.warehouseId) : undefined,
      status: searchForm.inventoryStatus === '' || searchForm.inventoryStatus === null ? undefined : Number(searchForm.inventoryStatus),
      stockLevel: searchForm.stockStatus || undefined,
      page: currentPage.value,
      pageSize: pageSize.value
    }

    const response = await inventoryAPI.getInventoryList(params)
    const page = unwrapPagePayload(response)
    inventoryList.value = page.records
    total.value = page.total
  } catch (error) {
    console.error('获取库存列表失败:', error)
    ElMessage.error('获取库存列表失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const getWarehouseList = async () => {
  try {
    warehouseLoading.value = true
    const response = await warehouseAPI.getAllWarehouses()
    warehouseOptions.value = response.data || response || []
  } catch (error) {
    console.error('获取仓库列表失败:', error)
    ElMessage.error('获取仓库列表失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    warehouseLoading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  getInventoryList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    productName: '',
    productId: '',
    warehouseId: '',
    stockStatus: '',
    inventoryStatus: ''
  })
  currentPage.value = 1
  getInventoryList()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  getInventoryList()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  getInventoryList()
}

const remoteSearchProduct = async (query) => {
  if (query) {
    productLoading.value = true
    try {
      const response = await productAPI.searchProducts({
        keyword: query, page: 1, pageSize: 20
      })
      productOptions.value = unwrapPagePayload(response).records
    } catch (error) {
      console.error('搜索商品失败:', error)
    } finally {
      productLoading.value = false
    }
  } else {
    productOptions.value = []
  }
}

const remoteSearchSupplier = async (query) => {
  supplierLoading.value = true
  try {
    const response = await supplierAPI.searchSuppliers({
      keyword: query, page: 1, pageSize: 20
    })
    supplierOptions.value = unwrapPagePayload(response).records
  } catch (error) {
    console.error('搜索供应商失败:', error)
  } finally {
    supplierLoading.value = false
  }
}

const showStockInDialog = (row) => {
  if (row) {
    stockInForm.productId = row.productId
    handleProductChange(row.productId)
  }
  stockInDialogVisible.value = true
}

const handleStockInDialogClose = () => {
  stockInDialogVisible.value = false
  stockInFormRef.value?.resetFields()
  Object.assign(stockInForm, {
    productId: '',
    warehouseId: '',
    supplierId: '',
    quantity: 1,
    unitPrice: 0,
    remark: ''
  })
}

const handleProductChange = async (productId) => {
  if (productId) {
    try {
      const response = await productAPI.getProductById(productId)
      stockInForm.unitPrice = response.data.data.price
    } catch (error) {
      console.error('获取商品信息失败:', error)
    }
  }
}

const handleSupplierChange = async () => {}

const handleStockIn = async () => {
  try {
    await stockInFormRef.value.validate()

    await inventoryAPI.stockIn(stockInForm)
    ElMessage.success('商品入库成功')
    handleStockInDialogClose()
    await getInventoryList()
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '商品入库失败')
    } else if (error !== false) {
      ElMessage.error('商品入库失败，请检查网络连接')
    }
  }
}

const showStockOutDialog = (row) => {
  if (row) {
    stockOutForm.productId = row.productId
    stockOutForm.warehouseId = row.warehouseId
    const qty = Number(row.quantity ?? row.currentStock ?? 0)
    stockOutForm.currentStock = qty
    stockOutForm.maxStock = qty > 0 ? qty : 100 // 确保maxStock至少为100，避免输入框被禁用
    handleStockOutProductChange(row.productId)
  } else {
    // 重置表单时，确保maxStock不为0
    stockOutForm.maxStock = 100
  }
  stockOutDialogVisible.value = true
  
  // 如果出库目标是其他仓库，加载其他仓库列表
  if (stockOutForm.outTarget === '其他仓库') {
    loadOtherWarehouses()
  }
}

const handleStockOutDialogClose = () => {
  stockOutDialogVisible.value = false
  stockOutFormRef.value?.resetFields()
  Object.assign(stockOutForm, {
    productId: '',
    warehouseId: '',
    quantity: 1,
    unitPrice: 0,
    reason: '',
    outTarget: '',
    remark: '',
    currentStock: 0,
    maxStock: 100 // 确保maxStock不为0，避免输入框被禁用
  })
}

// 处理出库原因变化
const handleReasonChange = (reason) => {
  stockOutForm.outTarget = reasonTargetMap[reason] || ''
}

// 处理出库目标变化
const handleTargetChange = (target) => {
  stockOutForm.reason = targetReasonMap[target] || ''
}

const handleStockOutProductChange = async (productId) => {
  if (productId) {
    try {
      const productResponse = await productAPI.getProductById(productId)
      if (productResponse) {
        const productData = productResponse.data || productResponse
        stockOutForm.unitPrice = productData.sellingPrice || 0
      }

      if (stockOutForm.warehouseId) {
        try {
          const inventoryResponse = await inventoryAPI.getInventoryByProductIdAndWarehouseId(productId, stockOutForm.warehouseId)
          if (inventoryResponse) {
            const inventoryData = inventoryResponse.data || inventoryResponse
            const quantity = inventoryData.quantity || 0
            stockOutForm.currentStock = quantity
            stockOutForm.maxStock = quantity
          }
        } catch (inventoryError) {
          console.error('获取库存信息失败:', inventoryError)
          // 即使获取库存失败，也不要将 maxStock 设置为 0，保持之前的值
        }
      }
    } catch (error) {
      console.error('获取信息失败:', error)
    }
  }
}

const handleStockOutWarehouseChange = async (warehouseId) => {
  if (stockOutForm.productId && warehouseId) {
    try {
      const response = await inventoryAPI.getInventoryByProductIdAndWarehouseId(stockOutForm.productId, warehouseId)
      if (response) {
        const inventoryData = response.data || response
        const quantity = inventoryData.quantity || 0
        stockOutForm.currentStock = quantity
        stockOutForm.maxStock = quantity
      }
    } catch (error) {
      console.error('获取库存信息失败:', error)
      // 即使获取库存失败，也不要将 maxStock 设置为 0，保持之前的值
    }
  }
}

const handleStockOut = async () => {
  try {
    await stockOutFormRef.value.validate()

    if (stockOutForm.quantity > stockOutForm.maxStock) {
      ElMessage.error('出库数量超过当前库存')
      return
    }

    await inventoryAPI.stockOut({
      productId: stockOutForm.productId,
      warehouseId: stockOutForm.warehouseId,
      quantity: stockOutForm.quantity,
      unitPrice: stockOutForm.unitPrice,
      outType: stockOutForm.reason,
      outTarget: stockOutForm.outTarget,
      remark: stockOutForm.remark
    })

    ElMessage.success('商品出库成功')
    handleStockOutDialogClose()
    await getInventoryList()
  } catch (error) {
    console.error('出库失败:', error)
    if (error.response) {
      ElMessage.error(error.response.data.message || '商品出库失败')
    } else if (error !== false) {
      ElMessage.error('商品出库失败，请检查网络连接')
    }
  }
}

const showInventoryCheckDialog = () => {
  getInventoryCheckList()
  inventoryCheckDialogVisible.value = true
}

const getInventoryCheckList = async () => {
  try {
    const response = await inventoryAPI.getInventoryList({ page: 1, pageSize: 5000 })
    const list = unwrapPagePayload(response).records
    inventoryCheckList.value = list.map((item) => ({
      ...item,
      productId: item.productId,
      productName: item.productName || '',
      currentStock: item.quantity ?? 0,
      actualStock: item.quantity ?? 0,
      difference: 0,
      unitPrice: item.unitCost != null ? Number(item.unitCost) : 0,
      remark: ''
    }))
  } catch (error) {
    console.error('获取库存盘点列表失败:', error)
    ElMessage.error('获取库存盘点列表失败')
  }
}

const handleInventoryCheckDialogClose = () => {
  inventoryCheckDialogVisible.value = false
  Object.assign(inventoryCheckForm, {
    description: ''
  })
  inventoryCheckList.value = []
}

const handleActualStockChange = (row) => {
  row.difference = row.actualStock - row.currentStock
}

const handleInventoryCheck = async () => {
  try {
    const formattedItems = inventoryCheckList.value.map(item => ({
      productId: item.productId,
      actualQuantity: item.actualStock,
      systemQuantity: item.currentStock,
      difference: item.difference,
      unitPrice: item.unitPrice,
      remark: item.remark
    }))

    // 创建盘点记录
    const createResponse = await inventoryAPI.inventoryCheck({
      description: inventoryCheckForm.description,
      items: formattedItems
    })

    // 等待一小段时间确保后端处理完成
    await new Promise(resolve => setTimeout(resolve, 500))

    // 确认盘点结果（更新库存）
    const response = await api.get('/stock-check/records', { params: { page: 1, pageSize: 100 } })
    const records = unwrapPagePayload(response).records
    
    // 确认最近创建的、状态为0的盘点记录（按创建时间排序后取前N条）
    let confirmCount = 0
    const pendingRecords = records
      .filter(r => r.status === 0)
      .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      .slice(0, formattedItems.length)
    
    for (const record of pendingRecords) {
      try {
        await api.post(`/stock-check/${record.id}/confirm`)
        confirmCount++
      } catch (confirmError) {
        console.error(`确认盘点记录 ${record.id} 失败:`, confirmError)
      }
    }

    if (confirmCount > 0) {
      ElMessage.success(`库存盘点成功，已确认 ${confirmCount} 条盘点记录`)
    } else {
      ElMessage.warning('盘点记录已创建，但未能自动确认，请手动确认')
    }
    
    handleInventoryCheckDialogClose()
    getInventoryList()
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '库存盘点失败')
    } else {
      ElMessage.error('库存盘点失败，请检查网络连接')
    }
  }
}

const priceFormatter = (row, column, cellValue) => {
  return `¥${(cellValue || 0).toFixed(2)}`
}
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
  flex-wrap: wrap;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  width: 100%;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.stock-info {
  margin-left: 10px;
  color: #606266;
}

.difference-plus {
  color: #67c23a;
}

.difference-minus {
  color: #f56c6c;
}
</style>
