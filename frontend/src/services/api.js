import axios from 'axios'

// 创建axios实例
// 开发环境使用相对路径，生产环境使用绝对路径
const baseURL = import.meta.env.VITE_API_URL || '/api'

const api = axios.create({
  baseURL: baseURL,
  timeout: 30000,
  withCredentials: true
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    // 统一处理API响应格式
    return response.data ? response.data : response;
  },
  error => {
    if (error.response && error.response.status === 401) {
      const url = error.config?.url || ''
      // 登录/注册失败时后端可能仍返回 401：不要整页跳转，交给页面显示 message
      if (url.includes('/auth/login') || url.includes('/auth/register')) {
        return Promise.reject(error)
      }
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('roles')
      localStorage.removeItem('selectedRole')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

/**
 * 解包统一分页结构（与后端 PagePayload 一致）：
 * 扁平 { records, total, current, pageSize, pages }，
 * 或嵌套 { data: { ... } }（如用户列表），
 * 并兼容历史字段 list / page / size。
 */
export function unwrapPagePayload(res) {
  const empty = { records: [], total: 0, current: 1, pageSize: 10, pages: 0 }
  if (res == null) return { ...empty }
  const inner = res.data !== undefined ? res.data : res
  if (inner && typeof inner === 'object' && !Array.isArray(inner)) {
    const records = inner.records ?? inner.list ?? inner.items ?? []
    const total = Number(inner.total ?? 0)
    const current = Number(inner.current ?? inner.page ?? 1)
    const pageSize = Number(inner.pageSize ?? inner.size ?? 10)
    let pages = inner.pages
    if (pages == null || Number.isNaN(Number(pages))) {
      pages = pageSize > 0 ? Math.ceil(total / pageSize) : 0
    } else {
      pages = Number(pages)
    }
    return { records, total, current, pageSize, pages }
  }
  return { ...empty }
}

export function unwrapListPayload(res) {
  if (res == null) return []
  const payload = res.data !== undefined ? res.data : res
  if (Array.isArray(payload)) return payload
  if (payload && Array.isArray(payload.data)) return payload.data
  return []
}

// 认证相关API
export const authAPI = {
  // 登录
  async login(data) { return await api.post('/auth/login', data) },
  // 注册
  async register(data) { return await api.post('/auth/register', data) },
  // 获取当前用户信息
  async getCurrentUser() { return await api.get('/auth/me') },
  // 修改密码
  async changePassword(data) { return await api.post('/auth/change-password', data) }
}

// 商品相关API
export const productAPI = {
  // 获取商品列表
  async getProducts(params) { return await api.get('/products', { params }) },
  // 获取商品详情
  async getProduct(id) { return await api.get(`/products/${id}`) },
  async getProductDetail(id) { return await api.get(`/products/${id}/detail`) },
  async getProductById(id) { return await api.get(`/products/${id}`) },
  // 根据条码获取商品
  /** @param {{ sellableOnly?: boolean }} opts 收银建议 sellableOnly=true（仅可售：上架+分类启用） */
  async getProductByBarcode(barcode, opts = {}) {
    const params = {}
    if (opts.sellableOnly) params.sellableOnly = true
    return await api.get(`/products/barcode/${encodeURIComponent(barcode)}`, { params })
  },
  // 搜索商品（与分页列表同一接口）
  async searchProducts(params) {
    const { size, ...rest } = params || {}
    return await api.get('/products', { params: { ...rest, pageSize: size ?? rest.pageSize } })
  },
  // 创建商品
  async createProduct(data) { return await api.post('/products', data) },
  // 更新商品
  async updateProduct(id, data) { return await api.put(`/products/${id}`, data) },
  // 更新商品库存
  async updateProductStock(id, quantity) { return await api.put(`/products/${id}/stock`, null, { params: { quantity } }) },
  // 删除商品
  async deleteProduct(id) { return await api.delete(`/products/${id}`) },
  // 批量删除商品
  async batchDeleteProducts(ids) { return await api.delete('/products/batch', { data: ids }) },
  // 导入商品
  async importProducts(formData) { return await api.post('/products/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }) }
}

// 供应商相关API
export const supplierAPI = {
  // 获取供应商列表
  async getSuppliers(params) { return await api.get('/suppliers', { params }) },
  // 获取供应商详情
  async getSupplier(id) { return await api.get(`/suppliers/${id}`) },
  // 搜索供应商
  async searchSuppliers(params) {
    const { size, ...rest } = params || {}
    return await api.get('/suppliers', { params: { ...rest, pageSize: size ?? rest.pageSize } })
  },
  // 创建供应商
  async createSupplier(data) { return await api.post('/suppliers', data) },
  // 更新供应商
  async updateSupplier(id, data) { return await api.put(`/suppliers/${id}`, data) },
  // 删除供应商
  async deleteSupplier(id) { return await api.delete(`/suppliers/${id}`) },
  // 批量删除供应商
  async batchDeleteSuppliers(ids) { return await api.delete('/suppliers/batch', { data: ids }) }
}

// 库存相关API
export const inventoryAPI = {
  // 获取库存列表
  async getInventories(params) { return await api.get('/inventories', { params }) },
  async getInventoryList(params) { return await api.get('/inventories', { params }) },
  // 获取库存详情
  async getInventory(id) { return await api.get(`/inventories/${id}`) },
  async getInventoryById(id) { return await api.get(`/inventories/${id}`) },
  // 根据商品ID和仓库ID获取库存
  async getInventoryByProductIdAndWarehouseId(productId, warehouseId) { return await api.get(`/inventories/product/${productId}/warehouse/${warehouseId}`) },
  // 入库操作
  async stockIn(data) { return await api.post('/stock-in', data) },
  // 出库操作
  async stockOut(data) { return await api.post('/stock-out', data) },
  // 获取入库记录
  async getStockInRecords(params) { return await api.get('/stock-in/records', { params }) },
  // 获取出库记录
  async getStockOutRecords(params) { return await api.get('/stock-out/records', { params }) },
  // 获取库存盘点列表（分仓库存行，与库存列表同源）
  async getInventoryCheckList() { return await api.get('/inventories', { params: { page: 1, pageSize: 5000 } }) },
  // 库存盘点
  async inventoryCheck(data) { return await api.post('/stock-check', data) }
}

// 销售相关API
export const salesAPI = {
  // 获取销售订单列表
  async getOrders(params) { return await api.get('/orders', { params }) },
  // 获取销售订单详情
  async getOrder(id) { return await api.get(`/orders/${id}`) },
  async getOrderDetail(id) { return await api.get(`/orders/${id}`) }, // 别名
  // 创建销售订单
  async createOrder(data) { return await api.post('/orders', data) },
  // 订单明细行
  async getOrderItems(orderId) { return await api.get(`/orders/${orderId}/items`) },
  // 更新销售订单
  async updateOrder(id, data) { return await api.put(`/orders/${id}`, data) },
  // 取消销售订单
  async cancelOrder(id) { return await api.post(`/orders/${id}/cancel`) },
  // 删除销售订单
  async deleteOrder(id) { return await api.delete(`/orders/${id}`) },
  // 订单支付
  async payOrder(id, data) { return await api.post(`/orders/${id}/pay`, data) },
  // 订单退款
  async refundOrder(id) { return await api.post(`/orders/${id}/refund`) },
  // 获取销售统计数据
  async getSalesStats(params) { return await api.get('/sales/stats', { params }) },
  // 获取销售排行
  async getSalesRank(params) { return await api.get('/sales/ranking', { params }) },
  // 获取销售图表数据
  async getSalesChart(params) { return await api.get('/sales/chart', { params }) }
}

// 分类相关API
export const categoryAPI = {
  // 获取所有分类
  async getCategories() { return await api.get('/categories') },
  // 获取分类树
  async getCategoryTree() { return await api.get('/categories/tree') },
  // 根据父分类ID获取子分类
  async getCategoriesByParentId(parentId) { return await api.get(`/categories/parent/${parentId}`) },
  // 获取分类详情
  async getCategory(id) { return await api.get(`/categories/${id}`) },
  async createCategory(data) { return await api.post('/categories', data) },
  async updateCategory(id, data) { return await api.put(`/categories/${id}`, data) },
  async deleteCategory(id) { return await api.delete(`/categories/${id}`) }
}

// 仓库相关API
export const warehouseAPI = {
  // 获取仓库列表
  async getWarehouses(params) { return await api.get('/warehouses', { params }) },
  // 获取所有仓库
  async getAllWarehouses() { return await api.get('/warehouses/all') },
  // 获取仓库详情
  async getWarehouse(id) { return await api.get(`/warehouses/${id}`) },
  async getWarehouseById(id) { return await api.get(`/warehouses/${id}`) },
  // 创建仓库
  async createWarehouse(data) { return await api.post('/warehouses', data) },
  // 更新仓库
  async updateWarehouse(id, data) { return await api.put(`/warehouses/${id}`, data) },
  // 删除仓库
  async deleteWarehouse(id) { return await api.delete(`/warehouses/${id}`) }
}

// 报表相关API
export const reportAPI = {
  // 获取销售报表
  async getSalesReport(params) { return await api.post('/reports/sales-statistics', params) },
  // 获取库存报表
  async getInventoryReport(params) { return await api.post('/reports/inventory', params || {}) },
  // 低库存预警列表（quantity <= min_stock）
  async getLowInventoryReport(params) { return await api.post('/reports/low-inventory', params || {}) },
  // 获取商品销售排行
  async getProductSalesRank(params) { return await api.post('/reports/sales-ranking', params || {}) },
  /** 毛利分析（已完成已支付订单明细，按商品汇总） */
  async getProfitReport(params) { return await api.post('/reports/profit', params || {}) }
}

// 角色相关API
export const roleAPI = {
  // 获取所有角色
  async getAllRoles() { return await api.get('/roles') },
  // 获取角色详情
  async getRoleById(id) { return await api.get(`/roles/${id}`) },
  // 创建角色
  async createRole(data) { return await api.post('/roles', data) },
  // 更新角色
  async updateRole(id, data) { return await api.put(`/roles/${id}`, data) },
  // 删除角色
  async deleteRole(id) { return await api.delete(`/roles/${id}`) },
  // 获取角色权限
  async getRolePermissions(roleId) { return await api.get(`/roles/${roleId}/permissions`) },
  // 设置角色权限
  async setRolePermissions(roleId, permissionIds) { return await api.post(`/roles/${roleId}/permissions`, permissionIds) }
}

// 权限相关API（与后端 SysPermissionController 路径一致）
export const permissionAPI = {
  async getAllPermissions() { return await api.get('/permissions/list') },
  async getPermissionTree() { return await api.get('/permissions/tree') },
  async getUserMenuTree() { return await api.get('/permissions/user-menu-tree') },
  async getPermissionById(id) { return await api.get(`/permissions/get/${id}`) },
  async createPermission(data) { return await api.post('/permissions/create', data) },
  async updatePermission(id, data) { return await api.put(`/permissions/update/${id}`, data) },
  async deletePermission(id) { return await api.delete(`/permissions/delete/${id}`) }
}

// 用户相关API
export const userAPI = {
  // 获取用户列表
  async getUsers(params) { return await api.get('/users', { params }) },
  // 获取用户详情
  async getUser(id) { return await api.get(`/users/${id}`) },
  // 创建用户
  async createUser(data) { return await api.post('/users', data) },
  // 更新用户
  async updateUser(id, data) { return await api.put(`/users/${id}`, data) },
  // 删除用户
  async deleteUser(id) { return await api.delete(`/users/${id}`) },
  // 更新用户状态
  async updateUserStatus(id, status) { return await api.put(`/users/${id}`, { status }) }
}

// 用户角色相关API（请求体字段 roleIds 与后端一致）
export const userRoleAPI = {
  async getUserRoles(userId) { return await api.get(`/users/${userId}/roles`) },
  async setUserRoles(userId, roleIds) { return await api.put(`/users/${userId}/roles`, { roleIds }) }
}

/** 关键操作审计记录查询（仅管理员，需 ROLE_ADMIN） */
export const auditLogAPI = {
  async getPage(params) { return await api.get('/audit-logs', { params }) }
}

export default api