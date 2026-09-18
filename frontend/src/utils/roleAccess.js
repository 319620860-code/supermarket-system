/**
 * 登录身份与菜单/路由可见性（与 Login.vue 的 selectedRole、后端 JWT activeRole 一致）
 */
export const ROLE_ADMIN = 'admin'
export const ROLE_CASHIER = 'cashier'
export const ROLE_STOCK = 'stock'

/** 未选时用账号已有角色兜底 */
export function getEffectiveSelectedRole() {
  const s = localStorage.getItem('selectedRole')
  if (s && String(s).trim()) return String(s).trim()
  try {
    const roles = JSON.parse(localStorage.getItem('roles') || '[]')
    if (!Array.isArray(roles)) return ROLE_ADMIN
    if (roles.includes(ROLE_ADMIN)) return ROLE_ADMIN
    if (roles.includes(ROLE_STOCK)) return ROLE_STOCK
    if (roles.includes(ROLE_CASHIER)) return ROLE_CASHIER
  } catch {
    /* ignore */
  }
  return ROLE_ADMIN
}

/**
 * 当前身份是否可访问带 meta.allowedRoles 的路由
 * @param {import('vue-router').RouteLocationNormalized} to
 */
export function isRouteAllowedForSelectedRole(to) {
  const record = [...to.matched].reverse().find((r) => r.meta && r.meta.allowedRoles)
  if (!record) return true
  const allowed = record.meta.allowedRoles
  if (!Array.isArray(allowed) || !allowed.length) return true
  const role = getEffectiveSelectedRole()
  if (role === ROLE_ADMIN) return true
  return allowed.includes(role)
}

/** 侧栏：是否展示销售相关菜单 */
export function showSalesMenu() {
  const r = getEffectiveSelectedRole()
  return r === ROLE_ADMIN || r === ROLE_CASHIER
}

/** 侧栏：是否展示库存/仓库及商品主数据维护（属性、分类） */
export function showStockMenu() {
  const r = getEffectiveSelectedRole()
  return r === ROLE_ADMIN || r === ROLE_STOCK
}

/** 收银员：仅商品列表（查价） */
export function showCashierProductOnly() {
  return getEffectiveSelectedRole() === ROLE_CASHIER
}
