<template>
  <div class="category-container">
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><FolderOpened /></el-icon>
        商品分类
      </h2>
      <div class="page-header-actions">
        <el-button type="primary" @click="openAddRoot">
          <el-icon><Plus /></el-icon>
          新增顶级分类
        </el-button>
      </div>
    </div>

    <el-card class="table-card" shadow="never">
      <div class="category-toolbar">
        <span class="toolbar-label">状态筛选</span>
        <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 140px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </div>
      <el-table
        v-loading="loading"
        class="category-tree-table"
        :data="displayTree"
        row-key="id"
        border
        stripe
        default-expand-all
        empty-text="暂无分类数据"
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="name" label="分类名称" class-name="col-cat-name" show-overflow-tooltip />
        <el-table-column prop="level" label="层级" align="center" class-name="col-cat-num" />
        <el-table-column prop="sort" label="排序" align="center" class-name="col-cat-num" />
        <el-table-column label="状态" align="center" class-name="col-cat-status">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="col-cat-ops">
          <template #default="{ row }">
            <div class="table-ops-inline">
              <el-button type="primary" link @click="openAddChild(row)">添加子分类</el-button>
              <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增 -->
    <el-dialog
      v-model="addVisible"
      :title="addTitle"
      width="480px"
      destroy-on-close
      @closed="resetAddForm"
    >
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-position="top">
        <el-form-item v-if="addForm.lockParent" label="上级分类">
          <el-input :model-value="addForm.parentLabel" disabled />
        </el-form-item>
        <el-form-item v-else label="上级分类" prop="parentId">
          <el-cascader
            v-model="addForm.parentId"
            class="w-full"
            :options="parentCascaderOptions"
            :props="parentCascaderProps"
            clearable
            placeholder="请分级选择上级（选「顶级分类」则为一级）"
            :show-all-levels="true"
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入分类名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="addForm.sort" :min="0" :max="9999" controls-position="right" class="w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitAdd">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑 -->
    <el-dialog
      v-model="editVisible"
      title="编辑分类"
      width="480px"
      destroy-on-close
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-position="top">
        <el-form-item label="上级分类">
          <el-input :model-value="editParentLabel" disabled />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入分类名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="editForm.sort" :min="0" :max="9999" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, FolderOpened } from '@element-plus/icons-vue'
import { categoryAPI } from '../services/api'

function pruneCategoryTree(nodes) {
  if (!Array.isArray(nodes)) return []
  return nodes.map((n) => {
    const children = pruneCategoryTree(n.children || [])
    const out = { ...n }
    if (children.length) out.children = children
    else delete out.children
    return out
  })
}

const loading = ref(false)
const submitLoading = ref(false)
const treeData = ref([])
const filterStatus = ref('')

/** 按分类 status（0/1）筛选树，保留命中节点及其父链由树结构展示 */
const displayTree = computed(() => {
  const st = filterStatus.value
  if (st === '' || st === null || st === undefined) return treeData.value
  function walk(nodes) {
    if (!Array.isArray(nodes)) return []
    const out = []
    for (const n of nodes) {
      const children = walk(n.children || [])
      const selfOk = Number(n.status) === Number(st)
      if (selfOk || children.length) {
        const row = { ...n }
        if (children.length) row.children = children
        else delete row.children
        out.push(row)
      }
    }
    return out
  }
  return walk(treeData.value)
})

const addVisible = ref(false)
const addTitle = ref('新增分类')
const addFormRef = ref()
const addForm = reactive({
  name: '',
  parentId: 0,
  sort: 0,
  lockParent: false,
  parentLabel: ''
})

const parentCascaderProps = {
  value: 'id',
  label: 'name',
  children: 'children',
  emitPath: false,
  checkStrictly: true
}

const parentCascaderOptions = computed(() => [
  { id: 0, name: '顶级分类', children: pruneCategoryTree(treeData.value) }
])

const addRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const editVisible = ref(false)
const editFormRef = ref()
const editForm = reactive({
  id: null,
  name: '',
  parentId: 0,
  level: 1,
  sort: 0,
  status: 1
})

const editRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const editParentLabel = computed(() => {
  if (editForm.parentId === 0 || editForm.parentId == null) return '顶级分类'
  const name = findNameById(treeData.value, editForm.parentId)
  return name || `ID:${editForm.parentId}`
})

function findNameById(nodes, id) {
  if (!nodes || !id) return ''
  for (const n of nodes) {
    if (n.id === id) return n.name
    const sub = findNameById(n.children, id)
    if (sub) return sub
  }
  return ''
}

async function loadTree() {
  loading.value = true
  try {
    const res = await categoryAPI.getCategoryTree()
    const raw = Array.isArray(res) ? res : (res?.data || [])
    treeData.value = pruneCategoryTree(raw)
  } catch (e) {
    console.error(e)
    ElMessage.error(e.response?.data?.message || e.message || '加载分类失败')
  } finally {
    loading.value = false
  }
}

function resetAddForm() {
  addForm.name = ''
  addForm.parentId = 0
  addForm.sort = 0
  addForm.lockParent = false
  addForm.parentLabel = ''
}

function openAddRoot() {
  addTitle.value = '新增顶级分类'
  resetAddForm()
  addForm.parentId = 0
  addVisible.value = true
}

function openAddChild(row) {
  addTitle.value = '新增子分类'
  resetAddForm()
  addForm.lockParent = true
  addForm.parentId = row.id
  addForm.parentLabel = row.name
  addVisible.value = true
}

async function submitAdd() {
  try {
    await addFormRef.value?.validate()
  } catch {
    return
  }
  const pid = addForm.lockParent ? addForm.parentId : (addForm.parentId ?? 0)
  submitLoading.value = true
  try {
    await categoryAPI.createCategory({
      name: addForm.name.trim(),
      parentId: pid,
      sort: addForm.sort ?? 0,
      status: 1
    })
    ElMessage.success('新增成功')
    addVisible.value = false
    await loadTree()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '新增失败')
  } finally {
    submitLoading.value = false
  }
}

function openEdit(row) {
  editForm.id = row.id
  editForm.name = row.name
  editForm.parentId = row.parentId ?? 0
  editForm.level = row.level ?? 1
  editForm.sort = row.sort ?? 0
  editForm.status = row.status ?? 1
  editVisible.value = true
}

async function submitEdit() {
  try {
    await editFormRef.value?.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    await categoryAPI.updateCategory(editForm.id, {
      name: editForm.name.trim(),
      parentId: editForm.parentId,
      level: editForm.level,
      sort: editForm.sort,
      status: editForm.status
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    await loadTree()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(
    `确定删除分类「${row.name}」吗？若其下还有子分类将无法删除。`,
    '删除分类',
    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
  )
    .then(async () => {
      try {
        await categoryAPI.deleteCategory(row.id)
        ElMessage.success('已删除')
        await loadTree()
      } catch (e) {
        ElMessage.error(e.response?.data?.message || e.message || '删除失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadTree()
})
</script>

<style scoped>
.category-container {
  /* 与商品列表页 .product-container 一致 */
  padding: 20px;
}

.table-card {
  margin-bottom: 16px;
}

.category-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.category-toolbar .toolbar-label {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

/* 五列按表格宽度均匀分配（每列约 20%） */
.category-tree-table {
  width: 100%;
}

.category-tree-table :deep(.el-table__header-wrapper table),
.category-tree-table :deep(.el-table__body-wrapper table) {
  table-layout: fixed;
  width: 100%;
}

.category-tree-table :deep(.el-table__header-wrapper colgroup col),
.category-tree-table :deep(.el-table__body-wrapper colgroup col) {
  width: 20% !important;
}

.category-tree-table :deep(.col-cat-name .cell) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.table-ops-inline {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 4px;
  row-gap: 2px;
}

.table-ops-inline :deep(.el-button) {
  padding: 0 2px;
  margin: 0;
}

.w-full {
  width: 100%;
}
</style>
