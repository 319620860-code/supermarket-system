/**
 * 权限控制工具函数
 */

/**
 * 检查用户是否拥有指定角色
 * @param {string} role 要检查的角色
 * @returns {boolean} 是否拥有该角色
 */
export function hasRole(role) {
  const roles = JSON.parse(localStorage.getItem('roles') || '[]')
  return roles.includes(role)
}

/**
 * 检查用户是否拥有指定角色之一
 * @param {Array<string>} roles 要检查的角色列表
 * @returns {boolean} 是否拥有任一角色
 */
export function hasAnyRole(roles) {
  const userRoles = JSON.parse(localStorage.getItem('roles') || '[]')
  return roles.some(role => userRoles.includes(role))
}

/**
 * 检查用户是否拥有所有指定角色
 * @param {Array<string>} roles 要检查的角色列表
 * @returns {boolean} 是否拥有所有角色
 */
export function hasAllRoles(roles) {
  const userRoles = JSON.parse(localStorage.getItem('roles') || '[]')
  return roles.every(role => userRoles.includes(role))
}

/**
 * 获取用户选择的登录角色
 * @returns {string} 用户选择的角色
 */
export function getSelectedRole() {
  return localStorage.getItem('selectedRole')
}

/**
 * 获取用户的所有角色
 * @returns {Array<string>} 用户角色列表
 */
export function getUserRoles() {
  return JSON.parse(localStorage.getItem('roles') || '[]')
}

/**
 * 检查用户是否是管理员
 * @returns {boolean} 是否是管理员
 */
export function isAdmin() {
  return hasRole('admin')
}

/**
 * 检查用户是否是收银员
 * @returns {boolean} 是否是收银员
 */
export function isCashier() {
  return hasRole('cashier')
}

/**
 * 检查用户是否是库存管理员
 * @returns {boolean} 是否是库存管理员
 */
export function isStockManager() {
  return hasRole('stock')
}

/**
 * 清除所有登录信息
 */
export function clearAuthInfo() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('roles')
  localStorage.removeItem('selectedRole')
}