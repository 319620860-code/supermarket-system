<template>
  <div class="supplier-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><ShoppingBag /></el-icon>
        供应商列表
      </h2>
      <div class="page-header-actions">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon> 新增供应商
        </el-button>
      </div>
    </div>
    
    <!-- 搜索和筛选区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.name" placeholder="请输入供应商名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="searchForm.contactPerson" placeholder="请输入联系人姓名" clearable></el-input>
        </el-form-item>
        <el-form-item label="合作状态">
          <el-select v-model="searchForm.status" placeholder="启用/停用" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 供应商列表 -->
    <el-card class="table-card">
      <el-table v-loading="loading" empty-text="暂无数据"
        :data="supplierList"
        style="width: 100%"
        stripe
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="供应商ID" width="100"></el-table-column>
        <el-table-column prop="name" label="供应商名称" min-width="120"></el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="120"></el-table-column>
        <el-table-column prop="phone" label="联系电话" width="150"></el-table-column>
        <el-table-column prop="email" label="邮箱" width="200"></el-table-column>
        <el-table-column prop="address" label="地址" min-width="200"></el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="showEditDialog(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">
              删除
            </el-button>
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
    
    <!-- 新增供应商对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增供应商"
      width="500px"
      :before-close="handleAddDialogClose"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入供应商名称"></el-input>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="addForm.contactPerson" placeholder="请输入联系人姓名"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="addForm.phone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="addForm.email" placeholder="请输入邮箱地址"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input
            v-model="addForm.address"
            type="textarea"
            :rows="3"
            placeholder="请输入供应商地址"
          ></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
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
    
    <!-- 编辑供应商对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑供应商"
      width="500px"
      :before-close="handleEditDialogClose"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入供应商名称"></el-input>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="editForm.contactPerson" placeholder="请输入联系人姓名"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱地址"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input
            v-model="editForm.address"
            type="textarea"
            :rows="3"
            placeholder="请输入供应商地址"
          ></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
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
import { Plus, ShoppingBag } from '@element-plus/icons-vue'
import { supplierAPI, unwrapPagePayload } from '../services/api'

// 页面加载状态
const loading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  name: '',
  contactPerson: '',
  status: ''
})

// 供应商列表数据
const supplierList = ref([])

// 新增供应商对话框
const addDialogVisible = ref(false)
const addFormRef = ref()
const addForm = reactive({
  name: '',
  contactPerson: '',
  phone: '',
  email: '',
  address: '',
  remark: ''
})

// 编辑供应商对话框
const editDialogVisible = ref(false)
const editFormRef = ref()
const editForm = reactive({
  id: '',
  name: '',
  contactPerson: '',
  phone: '',
  email: '',
  address: '',
  remark: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' },
    { min: 1, max: 100, message: '供应商名称长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' },
    { min: 1, max: 50, message: '联系人姓名长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号码格式', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
    { required: true, message: '请输入邮箱地址', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入供应商地址', trigger: 'blur' },
    { min: 1, max: 200, message: '供应商地址长度在 1 到 200 个字符', trigger: 'blur' }
  ]
}

// 页面加载时获取供应商列表
onMounted(() => {
  getSupplierList()
})

// 获取供应商列表
const getSupplierList = async () => {
  try {
    loading.value = true
    const response = await supplierAPI.getSuppliers({
      name: searchForm.name?.trim() || undefined,
      contactPerson: searchForm.contactPerson?.trim() || undefined,
      status: searchForm.status === '' || searchForm.status === null ? undefined : searchForm.status,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    const page = unwrapPagePayload(response)
    supplierList.value = page.records
    total.value = page.total
  } catch (error) {
    console.error('获取供应商列表失败:', error)
    ElMessage.error('获取供应商列表失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 搜索供应商
const handleSearch = () => {
  currentPage.value = 1
  getSupplierList()
}

// 重置搜索条件
const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    contactPerson: '',
    status: ''
  })
  currentPage.value = 1
  getSupplierList()
}

// 页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  getSupplierList()
}

// 每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  getSupplierList()
}

// 显示新增供应商对话框
const showAddDialog = () => {
  addDialogVisible.value = true
}

// 关闭新增供应商对话框
const handleAddDialogClose = () => {
  addDialogVisible.value = false
  addFormRef.value?.resetFields()
}

// 处理新增供应商
const handleAdd = async () => {
  try {
    await addFormRef.value.validate()
    const response = await supplierAPI.createSupplier(addForm)
    ElMessage.success('新增供应商成功')
    addDialogVisible.value = false
    addFormRef.value.resetFields()
    getSupplierList()
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '新增供应商失败')
    } else {
      ElMessage.error('新增供应商失败，请检查网络连接')
    }
  }
}

// 显示编辑供应商对话框
const showEditDialog = (row) => {
  Object.assign(editForm, row)
  editDialogVisible.value = true
}

// 关闭编辑供应商对话框
const handleEditDialogClose = () => {
  editDialogVisible.value = false
  editFormRef.value?.resetFields()
}

// 处理编辑供应商
const handleEdit = async () => {
  try {
    await editFormRef.value.validate()
    const response = await supplierAPI.updateSupplier(editForm.id, editForm)
    ElMessage.success('编辑供应商成功')
    editDialogVisible.value = false
    editFormRef.value.resetFields()
    getSupplierList()
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '编辑供应商失败')
    } else {
      ElMessage.error('编辑供应商失败，请检查网络连接')
    }
  }
}

// 处理删除供应商
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该供应商吗？删除后不可恢复！', '删除供应商', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await supplierAPI.deleteSupplier(id)
      ElMessage.success('删除供应商成功')
    getSupplierList()
    } catch (error) {
      if (error.response) {
        ElMessage.error(error.response.data.message || '删除供应商失败')
      } else {
        ElMessage.error('删除供应商失败，请检查网络连接')
      }
    }
  }).catch(() => {
    // 用户取消删除操作
  })
}

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  // 这里可以处理多选操作，比如批量删除等
  console.log('Selected rows:', selection)
}
</script>

<style scoped>
.supplier-container {
  padding: 20px;
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
</style>