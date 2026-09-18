<template>
  <div class="user-container">
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><User /></el-icon>
        员工管理
      </h2>
    </div>
    <el-card>
      <div class="card-header">
        <div class="toolbar-left toolbar-filters">
          <el-input
            v-model="searchQuery"
            placeholder="用户名/姓名/手机/邮箱"
            prefix-icon="Search"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
          <el-select v-model="filters.status" placeholder="账号状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
          <el-select v-model="filters.gender" placeholder="性别" clearable style="width: 100px">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="0" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        </div>
        <el-button type="primary" @click="openCreateDialog">新增员工</el-button>
      </div>

      <el-table :data="userList" stripe style="width: 100%" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="employeeNo" label="员工编号" width="120" />
        <el-table-column prop="username" label="姓名" />
        <el-table-column prop="department" label="部门" width="100" />
        <el-table-column prop="position" label="职位" width="100" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="scope">
            {{ scope.row.gender === 1 ? '男' : scope.row.gender === 0 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="在职状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              @change="handleStatusChange(scope.row)"
              :active-value="1"
              :inactive-value="0"
            />
          </template>
        </el-table-column>
        <el-table-column prop="hireDate" label="入职日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" min-width="200" fixed="right" align="center">
          <template #default="scope">
            <div class="table-ops-inline">
              <el-button type="primary" link size="small" @click="openEditDialog(scope.row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="deleteUser(scope.row.id)">删除</el-button>
              <el-button type="primary" link size="small" @click="openAssignRolesDialog(scope.row)">分配角色</el-button>
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
          :total="pagination.total"
          :total-text="'共'"
          :page-size-text="'条/页'"
          :jumper-text="'前往'"
        />
      </div>
    </el-card>

    <!-- 创建员工对话框 -->
    <el-dialog v-model="createDialogVisible" title="新增员工" width="700px">
      <el-form :model="createForm" :rules="rules" ref="createFormRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="员工编号" prop="employeeNo">
              <el-input v-model="createForm.employeeNo" placeholder="请输入员工编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="username">
              <el-input v-model="createForm.username" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门" prop="department">
              <el-select v-model="createForm.department" placeholder="请选择部门" empty-text="暂无数据">
                <el-option label="管理层" value="管理层" />
                <el-option label="财务部" value="财务部" />
                <el-option label="采购部" value="采购部" />
                <el-option label="销售部" value="销售部" />
                <el-option label="仓储部" value="仓储部" />
                <el-option label="收银台" value="收银台" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位" prop="position">
              <el-select v-model="createForm.position" placeholder="请选择职位" empty-text="暂无数据">
                <el-option label="管理员" value="管理员" />
                <el-option label="财务人员" value="财务人员" />
                <el-option label="采购员" value="采购员" />
                <el-option label="销售员" value="销售员" />
                <el-option label="仓管员" value="仓管员" />
                <el-option label="收银员" value="收银员" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="createForm.password" type="password" placeholder="请输入密码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期">
              <el-date-picker v-model="createForm.hireDate" type="date" placeholder="选择入职日期" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="createForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="createForm.phone" placeholder="请输入电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="createForm.gender" placeholder="请选择性别" empty-text="暂无数据">
                <el-option label="男" value="1" />
                <el-option label="女" value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="在职状态">
              <el-switch v-model="createForm.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createUser">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑员工对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑员工" width="700px">
      <el-form :model="editForm" :rules="rules" ref="editFormRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="员工编号" prop="employeeNo">
              <el-input v-model="editForm.employeeNo" placeholder="请输入员工编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="username">
              <el-input v-model="editForm.username" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门" prop="department">
              <el-select v-model="editForm.department" placeholder="请选择部门" empty-text="暂无数据">
                <el-option label="管理层" value="管理层" />
                <el-option label="财务部" value="财务部" />
                <el-option label="采购部" value="采购部" />
                <el-option label="销售部" value="销售部" />
                <el-option label="仓储部" value="仓储部" />
                <el-option label="收银台" value="收银台" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位" prop="position">
              <el-select v-model="editForm.position" placeholder="请选择职位" empty-text="暂无数据">
                <el-option label="管理员" value="管理员" />
                <el-option label="财务人员" value="财务人员" />
                <el-option label="采购员" value="采购员" />
                <el-option label="销售员" value="销售员" />
                <el-option label="仓管员" value="仓管员" />
                <el-option label="收银员" value="收银员" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="入职日期">
              <el-date-picker v-model="editForm.hireDate" type="date" placeholder="选择入职日期" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="editForm.gender" placeholder="请选择性别" empty-text="暂无数据">
                <el-option label="男" value="1" />
                <el-option label="女" value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="editForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="editForm.phone" placeholder="请输入电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="在职状态">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateUser">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog v-model="assignRolesDialogVisible" title="分配角色" width="500px">
      <div v-if="selectedUser" class="mb-4">
        <strong>当前员工：</strong>{{ selectedUser.username }}
      </div>
      <el-select
        v-model="selectedRoleIds"
        multiple
        placeholder="请选择角色"
        style="width: 100%"
      >
        <el-option
          v-for="role in roleList"
          :key="role.id"
          :label="role.name"
          :value="role.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="assignRolesDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="assignRoles">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, User } from '@element-plus/icons-vue'
import { userAPI, userRoleAPI, roleAPI, unwrapPagePayload, unwrapListPayload } from '../../services/api.js'

function apiErrorMessage(error, fallback) {
  const st = error?.response?.status
  const d = error?.response?.data
  if (st === 403) return '需要管理员权限，请使用管理员身份登录'
  if (typeof d === 'string' && d) return d
  if (d && typeof d === 'object' && d.message) return d.message
  return fallback
}

const userList = ref([])
const searchQuery = ref('')
const filters = reactive({
  status: '',
  gender: ''
})
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const assignRolesDialogVisible = ref(false)
const selectedUser = ref(null)

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 创建表单
const createForm = reactive({
  employeeNo: '',
  username: '',
  password: '',
  email: '',
  phone: '',
  gender: '1',
  status: 1,
  department: '',
  position: '',
  hireDate: ''
})

// 编辑表单
const editForm = reactive({
  id: '',
  employeeNo: '',
  username: '',
  email: '',
  phone: '',
  gender: '1',
  status: 1,
  department: '',
  position: '',
  hireDate: ''
})

// 分配角色
const selectedRoleIds = ref([])
const roleList = ref([])

// 表单验证规则
const rules = reactive({
  employeeNo: [{ required: true, message: '请输入员工编号', trigger: 'blur' }],
  username: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  department: [{ required: true, message: '请选择部门', trigger: 'change' }],
  position: [{ required: true, message: '请选择职位', trigger: 'change' }]
})

// 创建表单引用
const createFormRef = ref(null)
const editFormRef = ref(null)

// 初始化数据
onMounted(() => {
  fetchUsers()
  fetchRoles()
})

// 按关键字搜索（回到第 1 页）
const handleSearch = () => {
  pagination.currentPage = 1
  fetchUsers()
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      keyword: searchQuery.value || undefined,
      status: filters.status === '' || filters.status === null ? undefined : filters.status,
      gender: filters.gender === '' || filters.gender === null ? undefined : filters.gender
    }
    const response = await userAPI.getUsers(params)
    const page = unwrapPagePayload(response)
    userList.value = page.records
    pagination.total = page.total
  } catch (error) {
    ElMessage.error(apiErrorMessage(error, '获取用户列表失败'))
  }
}

// 获取角色列表
const fetchRoles = async () => {
  try {
    const response = await roleAPI.getAllRoles()
    roleList.value = unwrapListPayload(response)
  } catch (error) {
    ElMessage.error(apiErrorMessage(error, '获取角色列表失败'))
  }
}

// 打开创建对话框
const openCreateDialog = () => {
  createFormRef.value?.resetFields()
  Object.assign(createForm, {
    employeeNo: '',
    username: '',
    password: '',
    email: '',
    phone: '',
    gender: '1',
    status: 1,
    department: '',
    position: '',
    hireDate: ''
  })
  createDialogVisible.value = true
}

// 创建员工
const createUser = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await userAPI.createUser(createForm)
        ElMessage.success('员工创建成功')
        createDialogVisible.value = false
        fetchUsers()
      } catch (error) {
        ElMessage.error('用户创建失败: ' + error.response.data.message)
      }
    }
  })
}

// 打开编辑对话框
const openEditDialog = (row) => {
  Object.assign(editForm, row)
  editDialogVisible.value = true
}

// 更新用户
const updateUser = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await userAPI.updateUser(editForm.id, editForm)
        ElMessage.success('用户更新成功')
        editDialogVisible.value = false
        fetchUsers()
      } catch (error) {
        ElMessage.error('用户更新失败: ' + error.response.data.message)
      }
    }
  })
}

// 删除用户
const deleteUser = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userAPI.deleteUser(id)
    ElMessage.success('用户删除成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('用户删除失败')
    }
  }
}

// 处理状态变更
const handleStatusChange = async (row) => {
  const nextStatus = row.status
  try {
    await userAPI.updateUser(row.id, { status: nextStatus })
    ElMessage.success(nextStatus === 1 ? '账号启用成功' : '账号禁止成功')
  } catch (error) {
    ElMessage.error(nextStatus === 1 ? '账号启用失败' : '账号禁止失败')
    // 恢复原状态
    row.status = nextStatus === 1 ? 0 : 1
  }
}

// 打开分配角色对话框
const openAssignRolesDialog = async (row) => {
  selectedUser.value = row
  try {
    const response = await userRoleAPI.getUserRoles(row.id)
    selectedRoleIds.value = response.data.map(role => role.id)
    assignRolesDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取用户角色失败')
  }
}

// 分配角色
const assignRoles = async () => {
  try {
    await userRoleAPI.setUserRoles(selectedUser.value.id, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    assignRolesDialogVisible.value = false
  } catch (error) {
    ElMessage.error('角色分配失败')
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchUsers()
}

const handleCurrentChange = (current) => {
  pagination.currentPage = current
  fetchUsers()
}
</script>

<style scoped>
.user-container {
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
