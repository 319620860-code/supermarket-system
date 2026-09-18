<template>
  <div class="role-container">
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><Key /></el-icon>
        角色管理
      </h2>
    </div>
    <el-card>
      <div class="card-header">
        <div class="toolbar-left toolbar-filters">
          <el-input
            v-model="searchQuery"
            placeholder="请输入角色名称/编码"
            prefix-icon="Search"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
          <el-select
            v-model="filterStatus"
            placeholder="角色状态"
            clearable
            style="width: 120px"
            @change="() => { pagination.currentPage = 1 }"
          >
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        </div>
        <el-button type="primary" @click="openCreateDialog">新增角色</el-button>
      </div>

      <el-table :data="roleList" stripe style="width: 100%" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="code" label="角色编码" />
        <el-table-column prop="description" label="角色描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              @change="handleStatusChange(scope.row)"
              :active-value="1"
              :inactive-value="0"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" min-width="200" fixed="right" align="center">
          <template #default="scope">
            <div class="table-ops-inline">
              <el-button type="primary" link size="small" @click="openEditDialog(scope.row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="deleteRole(scope.row.id)">删除</el-button>
              <el-button type="primary" link size="small" @click="openPermissionDialog(scope.row)">分配权限</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredTotal"
          :total-text="'共'"
          :page-size-text="'条/页'"
          :jumper-text="'前往'"
        />
      </div>
    </el-card>

    <!-- 创建角色对话框 -->
    <el-dialog v-model="createDialogVisible" title="新增角色" width="500px">
      <el-form :model="createForm" :rules="rules" ref="createFormRef" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="createForm.code" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="createForm.description" placeholder="请输入角色描述" type="textarea" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="createForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createRole">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑角色对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑角色" width="500px">
      <el-form :model="editForm" :rules="rules" ref="editFormRef" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="editForm.code" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="editForm.description" placeholder="请输入角色描述" type="textarea" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateRole">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog v-model="permissionDialogVisible" title="分配权限" width="600px">
      <div v-if="selectedRole" class="mb-4">
        <strong>当前角色：</strong>{{ selectedRole.name }}
      </div>
      <el-tree
        v-model="checkedPermissionIds"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        check-strictly
        :default-checked-keys="checkedPermissionIds"
        :props="{
          label: 'name',
          children: 'children'
        }"
        style="max-height: 500px; overflow-y: auto"
        empty-text="暂无数据"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="assignPermission">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Key } from '@element-plus/icons-vue'
import { roleAPI, permissionAPI, unwrapListPayload } from '../../services/api.js'

function apiErrorMessage(error, fallback) {
  const st = error?.response?.status
  const d = error?.response?.data
  if (st === 403) return '需要管理员权限，请使用管理员身份登录'
  if (typeof d === 'string' && d) return d
  if (d && typeof d === 'object' && d.message) return d.message
  return fallback
}

const allRoles = ref([])
const searchQuery = ref('')
const appliedKeyword = ref('')
const filterStatus = ref('')

const filteredRoles = computed(() => {
  const q = appliedKeyword.value.trim().toLowerCase()
  let list = allRoles.value
  if (filterStatus.value !== '' && filterStatus.value !== null && filterStatus.value !== undefined) {
    list = list.filter((r) => Number(r.status) === Number(filterStatus.value))
  }
  if (!q) return list
  return list.filter((r) => {
    const name = r.name != null ? String(r.name).toLowerCase() : ''
    const code = r.code != null ? String(r.code).toLowerCase() : ''
    const desc = r.description != null ? String(r.description).toLowerCase() : ''
    return name.includes(q) || code.includes(q) || desc.includes(q)
  })
})

const filteredTotal = computed(() => filteredRoles.value.length)

const roleList = computed(() => {
  const list = filteredRoles.value
  const start = (pagination.currentPage - 1) * pagination.pageSize
  return list.slice(start, start + pagination.pageSize)
})
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const selectedRole = ref(null)

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

// 创建表单
const createForm = reactive({
  name: '',
  code: '',
  description: '',
  status: 1
})

// 编辑表单
const editForm = reactive({
  id: '',
  name: '',
  code: '',
  description: '',
  status: 1
})

// 权限树
const permissionTree = ref([])
const checkedPermissionIds = ref([])

// 表单验证规则
const rules = reactive({
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
})

// 表单引用
const createFormRef = ref(null)
const editFormRef = ref(null)

// 初始化数据
onMounted(() => {
  fetchRoles()
  fetchPermissionTree()
})

watch(filteredTotal, (total) => {
  const maxPage = Math.max(1, Math.ceil(total / pagination.pageSize) || 1)
  if (pagination.currentPage > maxPage) pagination.currentPage = maxPage
})

const handleSearch = () => {
  appliedKeyword.value = searchQuery.value.trim()
  pagination.currentPage = 1
}

// 获取角色列表
const fetchRoles = async () => {
  try {
    const response = await roleAPI.getAllRoles()
    const list = unwrapListPayload(response)
    allRoles.value = list
  } catch (error) {
    ElMessage.error(apiErrorMessage(error, '获取角色列表失败'))
  }
}

// 获取权限树
const fetchPermissionTree = async () => {
  try {
    const response = await permissionAPI.getPermissionTree()
    permissionTree.value = response.data
  } catch (error) {
    ElMessage.error('获取权限树失败')
  }
}

// 打开创建对话框
const openCreateDialog = () => {
  createFormRef.value?.resetFields()
  Object.assign(createForm, {
    name: '',
    code: '',
    description: '',
    status: '1'
  })
  createDialogVisible.value = true
}

// 创建角色
const createRole = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await roleAPI.createRole(createForm)
        ElMessage.success('角色创建成功')
        createDialogVisible.value = false
        fetchRoles()
      } catch (error) {
        ElMessage.error('角色创建失败: ' + error.response.data.message)
      }
    }
  })
}

// 打开编辑对话框
const openEditDialog = (row) => {
  Object.assign(editForm, row)
  editDialogVisible.value = true
}

// 更新角色
const updateRole = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await roleAPI.updateRole(editForm.id, editForm)
        ElMessage.success('角色更新成功')
        editDialogVisible.value = false
        fetchRoles()
      } catch (error) {
        ElMessage.error('角色更新失败: ' + error.response.data.message)
      }
    }
  })
}

// 删除角色
const deleteRole = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await roleAPI.deleteRole(id)
    ElMessage.success('角色删除成功')
    fetchRoles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('角色删除失败')
    }
  }
}

// 处理状态变更
const handleStatusChange = async (row) => {
  try {
    await roleAPI.updateRole(row.id, { status: row.status })
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 打开分配权限对话框
const openPermissionDialog = async (row) => {
  selectedRole.value = row
  try {
    const response = await roleAPI.getRolePermissions(row.id)
    checkedPermissionIds.value = response.data.map(permission => permission.id)
    permissionDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取角色权限失败')
  }
}

// 分配权限
const assignPermission = async () => {
  try {
    await roleAPI.setRolePermissions(selectedRole.value.id, checkedPermissionIds.value)
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch (error) {
    ElMessage.error('权限分配失败')
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
}

const handleCurrentChange = (current) => {
  pagination.currentPage = current
}
</script>

<style scoped>
.role-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.mb-4 {
  margin-bottom: 16px;
}

.table-ops-inline {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  justify-content: center;
  gap: 2px;
  white-space: nowrap;
}

.table-ops-inline :deep(.el-button) {
  padding: 0 4px;
  margin: 0;
}
</style>
