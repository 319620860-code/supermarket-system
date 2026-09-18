<template>
  <div class="product-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><List /></el-icon>
        商品列表
      </h2>
      <div v-if="!isReadOnlyCashier" class="page-header-actions">
        <el-button type="success" @click="showImportDialog">
          <el-icon><Upload /></el-icon> 导入商品
        </el-button>
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon> 新增商品
        </el-button>
      </div>
    </div>
    
    <!-- 搜索和筛选区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="商品名称、商品条码、规格文字"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-cascader
            v-model="searchForm.categoryId"
            class="category-cascader"
            :options="categoryTree"
            :props="cascaderPropsSearch"
            clearable
            filterable
            placeholder="请分级选择分类"
            :show-all-levels="true"
          />
        </el-form-item>
        <el-form-item label="商品状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable empty-text="暂无数据">
            <el-option label="上架" value="1"></el-option>
            <el-option label="下架" value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 商品列表 -->
    <el-card class="table-card">
      <el-table v-loading="loading" empty-text="暂无数据"
        :data="productList"
        style="width: 100%"
        stripe
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column v-if="!isReadOnlyCashier" type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="商品编号" width="100"></el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="120"></el-table-column>
        <el-table-column label="商品分类" width="140" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.categoryName || scope.row.category_name || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="specification" label="规格" width="100" show-overflow-tooltip />
        <el-table-column prop="unit" label="单位" width="80"></el-table-column>
        <el-table-column prop="sellingPrice" label="售价" width="100" :formatter="priceFormatter"></el-table-column>
        <el-table-column prop="quantity" label="数量" width="100"></el-table-column>
        <el-table-column prop="minStock" label="最低数量" width="100"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
        <el-table-column label="操作" :width="isReadOnlyCashier ? 120 : 280" fixed="right">
          <template #default="scope">
            <el-button type="success" size="small" @click="goDetail(scope.row.id)">
              详情
            </el-button>
            <template v-if="!isReadOnlyCashier">
              <el-button type="primary" size="small" @click="showEditDialog(scope.row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">
                删除
              </el-button>
              <el-button size="small" @click="handleToggleStatus(scope.row)">
                {{ scope.row.status === 1 ? '下架' : '上架' }}
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
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
    
    <!-- 新增商品对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增商品"
      width="820px"
      :before-close="handleAddDialogClose"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入商品名称"></el-input>
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-cascader
            v-model="addForm.categoryId"
            class="category-cascader category-cascader--block"
            :options="categoryTree"
            :props="cascaderPropsForm"
            clearable
            filterable
            placeholder="请分级选择分类"
            :show-all-levels="true"
          />
        </el-form-item>
        <el-form-item label="商品条码" prop="barcode">
          <el-input v-model="addForm.barcode" placeholder="留空则系统自动生成"></el-input>
        </el-form-item>
        <el-form-item label="商品单位" prop="unit">
          <el-input v-model="addForm.unit" placeholder="请输入商品单位（如：件、个、kg等）"></el-input>
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input
            v-model="addForm.specification"
            placeholder="选填"
          ></el-input>
        </el-form-item>
        <el-form-item label="进价" prop="purchasePrice">
          <el-input-number v-model="addForm.purchasePrice" :min="0" :step="0.01" placeholder="进价"></el-input-number>
        </el-form-item>
        <el-form-item label="售价" prop="sellingPrice">
          <el-input-number v-model="addForm.sellingPrice" :min="0" :step="0.01" placeholder="售价"></el-input-number>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="addForm.quantity" :min="0" placeholder="初始数量"></el-input-number>
        </el-form-item>
        <el-form-item label="最低数量" prop="minStock">
          <el-input-number v-model="addForm.minStock" :min="0" placeholder="数量预警线"></el-input-number>
        </el-form-item>
        <el-form-item label="商品状态" prop="status">
          <el-switch v-model="addForm.status" :active-value="1" :inactive-value="0">
            <template #active>上架</template>
            <template #inactive>下架</template>
          </el-switch>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="addForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          ></el-input>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleAddDialogClose">取消</el-button>
        <el-button type="primary" @click="handleCreateProduct">创建商品</el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑商品对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑商品"
      width="820px"
      :before-close="handleEditDialogClose"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入商品名称"></el-input>
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-cascader
            v-model="editForm.categoryId"
            class="category-cascader category-cascader--block"
            :options="categoryTree"
            :props="cascaderPropsForm"
            clearable
            filterable
            placeholder="请分级选择分类"
            :show-all-levels="true"
          />
        </el-form-item>
        <el-form-item label="商品条码" prop="barcode">
          <el-input v-model="editForm.barcode" placeholder="商品条码"></el-input>
        </el-form-item>
        <el-form-item label="商品单位" prop="unit">
          <el-input v-model="editForm.unit" placeholder="请输入商品单位（如：件、个、kg等）"></el-input>
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input
            v-model="editForm.specification"
            placeholder="选填"
          ></el-input>
        </el-form-item>
        <el-form-item label="进价" prop="purchasePrice">
          <el-input-number v-model="editForm.purchasePrice" :min="0" :step="0.01" placeholder="进价"></el-input-number>
        </el-form-item>
        <el-form-item label="售价" prop="sellingPrice">
          <el-input-number v-model="editForm.sellingPrice" :min="0" :step="0.01" placeholder="售价"></el-input-number>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="editForm.quantity" :min="0" placeholder="数量"></el-input-number>
        </el-form-item>
        <el-form-item label="最低数量" prop="minStock">
          <el-input-number v-model="editForm.minStock" :min="0" placeholder="数量预警线"></el-input-number>
        </el-form-item>
        <el-form-item label="商品状态" prop="status">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0">
            <template #active>上架</template>
            <template #inactive>下架</template>
          </el-switch>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          ></el-input>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleEditDialogClose">取消</el-button>
        <el-button type="primary" @click="handleEdit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入商品对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入商品"
      width="500px"
    >
      <div class="import-container">
        <div class="import-tips">
          <p>请上传Excel文件，格式要求：</p>
          <ul>
            <li>支持.xlsx格式</li>
            <li>必须包含：商品条码、商品名称、分类ID、进价、售价</li>
            <li>可选：规格、单位、库存数量、预警阈值、状态</li>
          </ul>
        </div>
        <el-upload
          class="upload-demo"
          drag
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          accept=".xlsx,.xls"
        >
          <el-icon class="el-icon--upload"><Upload /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传Excel文件
            </div>
          </template>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importLoading">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

/** 去掉空 children，级联器不显示多余展开 */
function pruneCategoryTree(nodes) {
  if (!Array.isArray(nodes)) return []
  return nodes.map((n) => {
    const raw = n.children
    const children = pruneCategoryTree(raw || [])
    const out = { ...n }
    if (children.length) out.children = children
    else delete out.children
    return out
  })
}
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, List, Upload } from '@element-plus/icons-vue'
import {
  productAPI,
  categoryAPI,
  unwrapPagePayload
} from '../services/api'
import { ROLE_CASHIER, getEffectiveSelectedRole } from '../utils/roleAccess.js'

const route = useRoute()
const router = useRouter()

/** 收银员仅可浏览商品与价格，不可改主档 */
const isReadOnlyCashier = computed(() => getEffectiveSelectedRole() === ROLE_CASHIER)

// 页面加载状态
const loading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  categoryId: null,
  status: ''
})

// 商品列表数据
const productList = ref([])

// 分类树（分级下拉）
const categoryTree = ref([])

const cascaderPropsSearch = {
  value: 'id',
  label: 'name',
  children: 'children',
  emitPath: false,
  checkStrictly: true
}

const cascaderPropsForm = {
  value: 'id',
  label: 'name',
  children: 'children',
  emitPath: false,
  checkStrictly: true
}

// 新增商品对话框
const addDialogVisible = ref(false)
const addFormRef = ref()
const addForm = reactive({
  id: null,
  name: '',
  categoryId: null,
  barcode: '',
  unit: '',
  specification: '',
  purchasePrice: 0,
  sellingPrice: 0,
  quantity: 0,
  minStock: 10,
  status: 1,
  description: ''
})

// 编辑商品对话框
const editDialogVisible = ref(false)
const editFormRef = ref()
const editForm = reactive({
  id: '',
  name: '',
  categoryId: null,
  barcode: '',
  unit: '',
  specification: '',
  purchasePrice: 0,
  sellingPrice: 0,
  quantity: 0,
  minStock: 10,
  status: 1,
  description: ''
})

// 导入商品对话框
const importDialogVisible = ref(false)
const importLoading = ref(false)
const importFile = ref(null)

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 1, max: 50, message: '商品名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  unit: [
    { required: true, message: '请输入商品单位', trigger: 'blur' },
    { min: 1, max: 10, message: '商品单位长度在 1 到 10 个字符', trigger: 'blur' }
  ],
  sellingPrice: [
    { required: true, message: '请输入售价', trigger: 'blur' },
    { type: 'number', min: 0, message: '售价不能为负数', trigger: 'blur' }
  ],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '数量不能为负数', trigger: 'blur' }
  ]
}

// 页面加载时获取商品列表和分类列表
/** 从详情页「编辑商品」带回 ?editId= 时自动打开编辑弹窗 */
const tryOpenEditFromQuery = async () => {
  const q = route.query.editId
  if (!q) return
  let row = productList.value.find((p) => String(p.id) === String(q))
  if (!row) {
    try {
      const res = await productAPI.getProduct(q)
      const p = res?.data !== undefined ? res.data : res
      if (p?.id) row = p
    } catch {
      return
    }
  }
  if (row) {
    showEditDialog(row)
    router.replace({ path: route.path, query: {} })
  }
}

const goDetail = (id) => {
  router.push({ name: 'ProductDetail', params: { id: String(id) } })
}

onMounted(() => {
  getCategories()
  getProductList()
})

watch(
  () => route.query.editId,
  (q) => {
    if (q) tryOpenEditFromQuery()
  }
)

// 获取分类树（分级）
const getCategories = async () => {
  try {
    const response = await categoryAPI.getCategoryTree()
    const raw = Array.isArray(response) ? response : (response?.data || [])
    categoryTree.value = pruneCategoryTree(raw)
  } catch (error) {
    console.error('获取分类树失败:', error)
    ElMessage.error('获取分类失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

// 获取商品列表
const getProductList = async () => {
  try {
    loading.value = true
    // 构建参数，仅传递有值的参数
    const params = {
      keyword: searchForm.keyword || undefined,
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 仅当分类ID有值时才传递
    if (searchForm.categoryId !== '' && searchForm.categoryId != null) {
      params.categoryId = Number(searchForm.categoryId)
    }
    
    // 仅当状态有值时才传递
    if (searchForm.status !== '') {
      params.status = searchForm.status
    }
    
    const response = await productAPI.getProducts(params)
    const page = unwrapPagePayload(response)
    productList.value = page.records
    total.value = page.total
  } catch (error) {
    console.error('获取商品列表失败:', error)
    console.error('错误详情:', error.response?.data)
    ElMessage.error('获取商品列表失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    loading.value = false
  }
  await tryOpenEditFromQuery()
}

// 搜索商品
const handleSearch = () => {
  currentPage.value = 1
  getProductList()
}

// 重置搜索条件
const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    categoryId: null,
    status: ''
  })
  currentPage.value = 1
  getProductList()
}

// 页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  getProductList()
}

// 每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  getProductList()
}

const resetAddForm = () => {
  Object.assign(addForm, {
    id: null,
    name: '',
    categoryId: null,
    barcode: '',
    unit: '',
    specification: '',
    purchasePrice: 0,
    sellingPrice: 0,
    quantity: 0,
    minStock: 10,
    status: 1,
    description: ''
  })
  addFormRef.value?.resetFields()
}

// 显示新增商品对话框
const showAddDialog = () => {
  resetAddForm()
  addDialogVisible.value = true
}

// 显示导入商品对话框
const showImportDialog = () => {
  importFile.value = null
  importDialogVisible.value = true
}

// 处理文件选择
const handleFileChange = (file) => {
  importFile.value = file.raw
}

// 处理导入
const handleImport = async () => {
  if (!importFile.value) {
    ElMessage.error('请先选择文件')
    return
  }

  try {
    importLoading.value = true
    const formData = new FormData()
    formData.append('file', importFile.value)
    await productAPI.importProducts(formData)
    ElMessage.success('导入成功')
    importDialogVisible.value = false
    getProductList()
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error(error.response?.data?.message || '导入失败')
  } finally {
    importLoading.value = false
  }
}

// 关闭新增商品对话框
const handleAddDialogClose = () => {
  addDialogVisible.value = false
  resetAddForm()
}

const handleCreateProduct = async () => {
  try {
    await addFormRef.value.validate()
    const payload = {
      name: addForm.name,
      categoryId: addForm.categoryId,
      barcode: addForm.barcode || undefined,
      unit: addForm.unit,
      specification: addForm.specification || undefined,
      purchasePrice: addForm.purchasePrice,
      sellingPrice: addForm.sellingPrice,
      quantity: addForm.quantity,
      minStock: addForm.minStock,
      description: addForm.description,
      status: addForm.status
    }
    const response = await productAPI.createProduct(payload)
    ElMessage.success('商品已创建')
    addDialogVisible.value = false
    resetAddForm()
    getProductList()
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data?.message || '新增商品失败')
    } else {
      ElMessage.error('新增商品失败，请检查网络连接')
    }
  }
}

// 显示编辑商品对话框
const showEditDialog = (row) => {
  Object.assign(editForm, {
    id: row.id,
    name: row.name,
    categoryId: row.categoryId,
    barcode: row.barcode,
    unit: row.unit,
    specification: row.specification || '',
    purchasePrice: row.purchasePrice != null ? Number(row.purchasePrice) : 0,
    sellingPrice: row.sellingPrice != null ? Number(row.sellingPrice) : 0,
    quantity: row.quantity,
    minStock: row.minStock != null ? row.minStock : 10,
    status: row.status,
    description: row.description || ''
  })
  editDialogVisible.value = true
}

// 关闭编辑商品对话框
const handleEditDialogClose = () => {
  editDialogVisible.value = false
  editFormRef.value?.resetFields()
}

// 处理编辑商品
const handleEdit = async () => {
  try {
    await editFormRef.value.validate()
    const payload = {
      name: editForm.name,
      categoryId: editForm.categoryId,
      barcode: editForm.barcode,
      unit: editForm.unit,
      specification: editForm.specification || undefined,
      purchasePrice: editForm.purchasePrice,
      sellingPrice: editForm.sellingPrice,
      quantity: editForm.quantity,
      minStock: editForm.minStock,
      description: editForm.description,
      status: editForm.status
    }
    await productAPI.updateProduct(editForm.id, payload)
    ElMessage.success('编辑商品成功')
    editDialogVisible.value = false
    editFormRef.value?.resetFields()
    getProductList()
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '编辑商品失败')
    } else {
      ElMessage.error('编辑商品失败，请检查网络连接')
    }
  }
}

// 处理删除商品
const handleDelete = (id) => {
  ElMessageBox.confirm(
    '确定要删除该商品吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await productAPI.deleteProduct(id)
      ElMessage.success('删除商品成功')
      getProductList()
    } catch (error) {
      if (error.response) {
        ElMessage.error(error.response.data.message || '删除商品失败')
      } else {
        ElMessage.error('删除商品失败，请检查网络连接')
      }
    }
  }).catch(() => {})
}

// 处理切换商品状态
const handleToggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await productAPI.updateProduct(row.id, { status: newStatus })
    ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
    getProductList()
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '操作失败')
    } else {
      ElMessage.error('操作失败，请检查网络连接')
    }
  }
}

// 处理选择商品
const handleSelectionChange = (selection) => {
  console.log('选中的商品:', selection)
}

// 价格格式化
const priceFormatter = (row) => {
  if (row.sellingPrice == null) return '—'
  return '¥' + Number(row.sellingPrice).toFixed(2)
}
</script>

<style scoped>
.product-container {
  padding: 20px;
  background: #f0fdfa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-subtitle {
  font-size: 20px;
  font-weight: 600;
  color: #0f766e;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
