<template>
  <div class="warehouse-container">
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><House /></el-icon>
        仓库列表
      </h2>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" label-width="80px" inline>
        <el-form-item label="仓库名称">
          <el-input v-model="searchForm.name" placeholder="请输入仓库名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="仓库类型">
          <el-select v-model="searchForm.type" placeholder="仓库类型" clearable style="width: 120px">
            <el-option label="总仓" :value="1" />
            <el-option label="分仓" :value="2" />
            <el-option label="退货仓" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="searchForm.address" placeholder="请输入仓库地址" clearable></el-input>
        </el-form-item>
        <el-form-item label="仓库状态">
          <el-select v-model="searchForm.status" placeholder="启用/停用" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button type="success" @click="showAddDialog">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        empty-text="暂无数据"
        :data="warehouseList"
        style="width: 100%"
        stripe
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="仓库名称" min-width="150"></el-table-column>
        <el-table-column prop="type" label="仓库类型" min-width="120">
          <template #default="scope">
            <el-tag :type="scope.row.type === 1 ? 'primary' : scope.row.type === 2 ? 'success' : 'warning'" size="small">
              {{ scope.row.type === 1 ? '总仓' : scope.row.type === 2 ? '分仓' : '退货仓' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200"></el-table-column>
        <el-table-column prop="contactPerson" label="联系人" min-width="120"></el-table-column>
        <el-table-column prop="phone" label="联系电话" min-width="150"></el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="scope">{{ scope.row.createTime || scope.row.createdAt || '—' }}</template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="scope">{{ scope.row.updateTime || scope.row.updatedAt || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
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

    <!-- 新增仓库对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增仓库"
      width="600px"
      :before-close="handleAddDialogClose"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="仓库名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入仓库名称"></el-input>
        </el-form-item>
        <el-form-item label="仓库类型" prop="type">
          <el-select v-model="addForm.type" placeholder="请选择仓库类型" empty-text="暂无数据">
            <el-option label="总仓" value="1"></el-option>
            <el-option label="分仓" value="2"></el-option>
            <el-option label="退货仓" value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="addForm.address" placeholder="请输入仓库地址"></el-input>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="addForm.contactPerson" placeholder="请输入联系人"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="addForm.phone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="addForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleAddDialogClose">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑仓库对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑仓库"
      width="600px"
      :before-close="handleEditDialogClose"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="仓库名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入仓库名称"></el-input>
        </el-form-item>
        <el-form-item label="仓库类型" prop="type">
          <el-select v-model="editForm.type" placeholder="请选择仓库类型" empty-text="暂无数据">
            <el-option label="总仓" value="1"></el-option>
            <el-option label="分仓" value="2"></el-option>
            <el-option label="退货仓" value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="editForm.address" placeholder="请输入仓库地址"></el-input>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="editForm.contactPerson" placeholder="请输入联系人"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="editForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleEditDialogClose">取消</el-button>
        <el-button type="primary" @click="handleEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { House } from '@element-plus/icons-vue'
import { warehouseAPI, unwrapPagePayload } from '../services/api'

// 页面加载状态
const loading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  name: '',
  type: '',
  address: '',
  status: ''
})

// 仓库列表数据
const warehouseList = ref([])

// 多选数据
const multipleSelection = ref([])

// 新增仓库对话框
const addDialogVisible = ref(false)
const addFormRef = ref()
const addForm = reactive({
  name: '',
  type: '',
  address: '',
  contactPerson: '',
  phone: '',
  remark: ''
})

// 编辑仓库对话框
const editDialogVisible = ref(false)
const editFormRef = ref()
const editForm = reactive({
  id: '',
  name: '',
  type: '',
  address: '',
  contactPerson: '',
  phone: '',
  remark: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入仓库名称', trigger: 'blur' },
    { min: 1, max: 50, message: '仓库名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择仓库类型', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入仓库地址', trigger: 'blur' },
    { min: 1, max: 200, message: '仓库地址长度在 1 到 200 个字符', trigger: 'blur' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人', trigger: 'blur' },
    { min: 1, max: 50, message: '联系人长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 页面加载时获取仓库列表
onMounted(() => {
  getWarehouseList()
})

// 获取仓库列表
const getWarehouseList = async () => {
  try {
    loading.value = true
    const params = {
      name: searchForm.name?.trim() || undefined,
      type: searchForm.type === '' || searchForm.type === null ? undefined : searchForm.type,
      address: searchForm.address?.trim() || undefined,
      status: searchForm.status === '' || searchForm.status === null ? undefined : searchForm.status,
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    const response = await warehouseAPI.getWarehouses(params)
    const page = unwrapPagePayload(response)
    warehouseList.value = page.records
    total.value = page.total
  } catch (error) {
    console.error('获取仓库列表失败:', error)
    ElMessage.error('获取仓库列表失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  getWarehouseList()
}

// 处理当前页变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  getWarehouseList()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  getWarehouseList()
}

// 重置搜索表单
const resetForm = () => {
  Object.assign(searchForm, {
    name: '',
    type: '',
    address: '',
    status: ''
  })
  currentPage.value = 1
  getWarehouseList()
}

// 处理多选
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection
}

// 显示新增仓库对话框
const showAddDialog = () => {
  addDialogVisible.value = true
}

// 关闭新增仓库对话框
const handleAddDialogClose = () => {
  addDialogVisible.value = false
  addFormRef.value?.resetFields()
  Object.assign(addForm, {
    name: '',
    type: '',
    address: '',
    contactPerson: '',
    phone: '',
    remark: ''
  })
}

// 处理新增仓库
const handleAdd = async () => {
  try {
    await addFormRef.value.validate()
    const response = await warehouseAPI.createWarehouse(addForm)
    // 后端直接返回仓库对象，没有 success 字段
    ElMessage.success('新增仓库成功')
    handleAddDialogClose()
    getWarehouseList()
  } catch (error) {
    console.error('新增仓库失败:', error)
    if (error.response) {
      ElMessage.error(error.response.data.message || '新增仓库失败')
    } else {
      ElMessage.error('新增仓库失败，请检查网络连接')
    }
  }
}

// 显示编辑仓库对话框
const showEditDialog = (row) => {
  Object.assign(editForm, row)
  editDialogVisible.value = true
}

// 关闭编辑仓库对话框
const handleEditDialogClose = () => {
  editDialogVisible.value = false
  editFormRef.value?.resetFields()
  Object.assign(editForm, {
    id: '',
    name: '',
    type: '',
    address: '',
    contactPerson: '',
    phone: '',
    remark: ''
  })
}

// 处理编辑仓库
const handleEdit = async () => {
  try {
    await editFormRef.value.validate()
    const response = await warehouseAPI.updateWarehouse(editForm.id, editForm)
    // 后端直接返回仓库对象，没有 success 字段
    ElMessage.success('编辑仓库成功')
    handleEditDialogClose()
    getWarehouseList()
  } catch (error) {
    console.error('编辑仓库失败:', error)
    if (error.response) {
      ElMessage.error(error.response.data.message || '编辑仓库失败')
    } else {
      ElMessage.error('编辑仓库失败，请检查网络连接')
    }
  }
}

// 处理删除仓库
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该仓库吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await warehouseAPI.deleteWarehouse(id)
    // 后端返回 204 No Content，没有 success 字段
    ElMessage.success('删除仓库成功')
    getWarehouseList()
  } catch (error) {
    console.error('删除仓库失败:', error)
    if (error.response) {
      ElMessage.error(error.response.data.message || '删除仓库失败')
    } else if (error.message !== 'cancel') {
      ElMessage.error('删除仓库失败，请检查网络连接')
    }
  }
}
</script>

<style scoped>
.warehouse-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>