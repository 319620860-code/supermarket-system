<template>
  <div class="permission-container">
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><Lock /></el-icon>
        权限管理
      </h2>
    </div>
    <el-card>
      <div class="card-header">
        <div class="toolbar-left perm-toolbar">
          <el-input
            v-model="searchQuery"
            placeholder="权限名称/编码"
            prefix-icon="Search"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
          <el-select v-model="filterType" placeholder="权限类型" clearable style="width: 120px">
            <el-option label="菜单" value="1" />
            <el-option label="按钮" value="2" />
            <el-option label="接口" value="3" />
          </el-select>
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 110px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        </div>
        <el-button type="primary" @click="openCreateDialog">新增权限</el-button>
      </div>

      <div class="permission-tree-container">
        <el-tree
          :data="displayPermissionTree"
          show-checkbox
          node-key="id"
          :props="{
            label: 'name',
            children: 'children'
          }"
          ref="permissionTreeRef"
          @node-click="handleNodeClick"
          @node-contextmenu="handleNodeContextMenu"
          empty-text="暂无数据"
        >
          <template #default="{ node, data }">
            <div class="node-content">
              <span>{{ node.label }}</span>
              <div class="node-actions" v-if="data.id">
                <el-button type="primary" link size="small" @click.stop="openEditDialog(data)">编辑</el-button>
                <el-button type="danger" link size="small" @click.stop="deletePermission(data.id)">删除</el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </el-card>

    <!-- 创建权限对话框 -->
    <el-dialog v-model="createDialogVisible" title="新增权限" width="500px">
      <el-form :model="createForm" :rules="rules" ref="createFormRef" label-width="100px">
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="createForm.code" placeholder="请输入权限编码" />
        </el-form-item>
        <el-form-item label="权限类型" prop="type">
          <el-select v-model="createForm.type" placeholder="请选择权限类型" empty-text="暂无数据">
            <el-option label="菜单" value="1" />
            <el-option label="按钮" value="2" />
            <el-option label="接口" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="父级权限">
          <el-select v-model="createForm.parentId" placeholder="请选择父级权限" empty-text="暂无数据">
            <el-option label="无" value="null" />
            <el-option
              v-for="permission in permissionOptions"
              :key="permission.id"
              :label="permission.name"
              :value="permission.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="createForm.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="createForm.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="createForm.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="orderNum">
          <el-input-number v-model="createForm.orderNum" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="createForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createPermission">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑权限对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑权限" width="500px">
      <el-form :model="editForm" :rules="rules" ref="editFormRef" label-width="100px">
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="editForm.code" placeholder="请输入权限编码" />
        </el-form-item>
        <el-form-item label="权限类型" prop="type">
          <el-select v-model="editForm.type" placeholder="请选择权限类型" empty-text="暂无数据">
            <el-option label="菜单" value="1" />
            <el-option label="按钮" value="2" />
            <el-option label="接口" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="父级权限">
          <el-select v-model="editForm.parentId" placeholder="请选择父级权限" empty-text="暂无数据">
            <el-option label="无" value="null" />
            <el-option
              v-for="permission in permissionOptions"
              :key="permission.id"
              :label="permission.name"
              :value="permission.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="editForm.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="editForm.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="editForm.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="orderNum">
          <el-input-number v-model="editForm.orderNum" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updatePermission">确定</el-button>
      </template>
    </el-dialog>

    <!-- 右键菜单 -->
    <el-dropdown-menu v-if="contextMenuVisible" ref="contextMenu" :style="{
      left: contextMenuPosition.x + 'px',
      top: contextMenuPosition.y + 'px',
      position: 'fixed',
      zIndex: 10000
    }">
      <el-dropdown-item @click="openCreateDialogWithParent(contextMenuData)">添加子权限</el-dropdown-item>
      <el-dropdown-item @click="openEditDialog(contextMenuData)">编辑</el-dropdown-item>
      <el-dropdown-item @click="deletePermission(contextMenuData.id)" divided>
        <span style="color: #f56c6c">删除</span>
      </el-dropdown-item>
    </el-dropdown-menu>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Lock } from '@element-plus/icons-vue'
import { permissionAPI } from '../../services/api.js'

const permissionTree = ref([])
const permissionTreeRef = ref(null)
const searchQuery = ref('')
const appliedKeyword = ref('')
const filterType = ref('')
const filterStatus = ref('')

/** 按 type / status 筛选（与关键字筛选组合） */
function filterTreeByMeta(nodes, type, status) {
  if ((!type || type === '') && (status === '' || status === null || status === undefined)) {
    return nodes
  }
  const walk = (items) => {
    const out = []
    for (const node of items || []) {
      const rawChildren = node.children && node.children.length ? node.children : []
      const children = walk(rawChildren)
      const okT = !type || String(node.type) === String(type)
      const okS = status === '' || status === null || status === undefined || Number(node.status) === Number(status)
      const selfOk = okT && okS
      if (selfOk || children.length) {
        const o = { ...node }
        if (children.length) o.children = children
        else delete o.children
        out.push(o)
      }
    }
    return out
  }
  return walk(nodes)
}

/** 按名称/编码筛选树：命中节点保留其完整子树；否则仅保留命中子分支 */
function filterPermissionTree(nodes, keyword) {
  if (!keyword?.trim()) return nodes
  const k = keyword.trim().toLowerCase()
  const walk = (items) => {
    const out = []
    for (const node of items) {
      const name = node.name != null ? String(node.name).toLowerCase() : ''
      const code = node.code != null ? String(node.code).toLowerCase() : ''
      const selfMatch = name.includes(k) || code.includes(k)
      const children = node.children && node.children.length ? node.children : []
      if (selfMatch) {
        out.push({ ...node, children })
        continue
      }
      const filteredChildren = walk(children)
      if (filteredChildren.length) {
        out.push({ ...node, children: filteredChildren })
      }
    }
    return out
  }
  return walk(nodes)
}

const displayPermissionTree = computed(() => {
  const byMeta = filterTreeByMeta(permissionTree.value, filterType.value, filterStatus.value)
  return filterPermissionTree(byMeta, appliedKeyword.value)
})
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const contextMenuData = ref(null)

// 创建表单
const createForm = reactive({
  name: '',
  code: '',
  type: 1,
  parentId: null,
  path: '',
  component: '',
  icon: '',
  orderNum: 0,
  status: 1
})

// 编辑表单
const editForm = reactive({
  id: '',
  name: '',
  code: '',
  type: 1,
  parentId: null,
  path: '',
  component: '',
  icon: '',
  orderNum: 0,
  status: 1
})

// 表单验证规则
const rules = reactive({
  name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择权限类型', trigger: 'change' }]
})

// 表单引用
const createFormRef = ref(null)
const editFormRef = ref(null)

// 权限选项（用于选择父级权限）
const permissionOptions = ref([])

// 初始化数据
onMounted(() => {
  fetchPermissionTree()
  document.addEventListener('click', handleDocumentClick)
})

const handleSearch = () => {
  appliedKeyword.value = searchQuery.value.trim()
}

// 获取权限树
const fetchPermissionTree = async () => {
  try {
    const response = await permissionAPI.getPermissionTree()
    permissionTree.value = response.data
    // 转换为选项格式
    permissionOptions.value = flattenTree(response.data)
  } catch (error) {
    ElMessage.error('获取权限树失败')
  }
}

// 扁平化树结构
const flattenTree = (tree) => {
  let result = []
  const traverse = (node, level = 0) => {
    // 添加缩进
    const label = ''.padStart(level * 2, ' ') + node.name
    result.push({ ...node, label })
    if (node.children && node.children.length > 0) {
      node.children.forEach(child => traverse(child, level + 1))
    }
  }
  tree.forEach(node => traverse(node))
  return result
}

// 节点点击事件
const handleNodeClick = (data) => {
  console.log('点击节点:', data)
}

// 节点右键菜单
const handleNodeContextMenu = (event, data) => {
  event.preventDefault()
  event.stopPropagation()
  contextMenuData.value = data
  contextMenuPosition.value = {
    x: event.clientX,
    y: event.clientY
  }
  contextMenuVisible.value = true
}

// 点击文档关闭右键菜单
const handleDocumentClick = () => {
  contextMenuVisible.value = false
}

// 打开创建对话框
const openCreateDialog = () => {
  createForm.parentId = null
  openCreateDialogInternal()
}

// 打开创建子权限对话框
const openCreateDialogWithParent = (parentData) => {
  createForm.parentId = parentData.id
  openCreateDialogInternal()
  contextMenuVisible.value = false
}

// 内部创建对话框
const openCreateDialogInternal = () => {
  createFormRef.value?.resetFields()
  Object.assign(createForm, {
    name: '',
    code: '',
    type: 1,
    parentId: createForm.parentId,
    path: '',
    component: '',
    icon: '',
    orderNum: 0,
    status: 1
  })
  createDialogVisible.value = true
}

// 创建权限
const createPermission = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 处理parentId为null的情况
        const formData = {
          ...createForm,
          parentId: createForm.parentId === 'null' ? null : createForm.parentId
        }
        await permissionAPI.createPermission(formData)
        ElMessage.success('权限创建成功')
        createDialogVisible.value = false
        fetchPermissionTree()
      } catch (error) {
        ElMessage.error('权限创建失败: ' + error.response.data.message)
      }
    }
  })
}

// 打开编辑对话框
const openEditDialog = (data) => {
  Object.assign(editForm, data)
  editDialogVisible.value = true
  contextMenuVisible.value = false
}

// 更新权限
const updatePermission = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 处理parentId为null的情况
        const formData = {
          ...editForm,
          parentId: editForm.parentId === 'null' ? null : editForm.parentId
        }
        await permissionAPI.updatePermission(editForm.id, formData)
        ElMessage.success('权限更新成功')
        editDialogVisible.value = false
        fetchPermissionTree()
      } catch (error) {
        ElMessage.error('权限更新失败: ' + error.response.data.message)
      }
    }
  })
}

// 删除权限
const deletePermission = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该权限及其子权限吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await permissionAPI.deletePermission(id)
    ElMessage.success('权限删除成功')
    fetchPermissionTree()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('权限删除失败')
    }
  }
}
</script>

<style scoped>
.permission-container {
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

.permission-tree-container {
  max-height: 600px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
}

.node-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.node-actions {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.3s;
}

.node-actions :deep(.el-button) {
  padding: 0 4px;
  margin: 0;
}

.node-content:hover .node-actions {
  opacity: 1;
}

.el-dropdown-menu {
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 5px 0;
  min-width: 100px;
}
</style>
