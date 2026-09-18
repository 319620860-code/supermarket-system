import { createRouter, createWebHistory } from 'vue-router'
import { isRouteAllowedForSelectedRole } from '../utils/roleAccess.js'

// 登录/壳层同步加载；业务页懒加载以减小首包（vue-expert：代码分割）
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: '注册' }
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { title: '首页', requiresAuth: true },
    children: [
      {
        path: '',
        name: 'HomeDefault',
        redirect: { name: 'Dashboard' }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '仪表盘', requiresAuth: true, allowedRoles: ['admin', 'cashier', 'stock'] }
      },
      // 商品管理
      {
        path: 'product/detail/:id',
        name: 'ProductDetail',
        component: () => import('../views/ProductDetail.vue'),
        meta: { title: '商品详情', requiresAuth: true, allowedRoles: ['admin', 'cashier', 'stock'] }
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('../views/Product.vue'),
        meta: { title: '商品列表', requiresAuth: true, allowedRoles: ['admin', 'cashier', 'stock'] }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('../views/Category.vue'),
        meta: { title: '商品分类', requiresAuth: true, allowedRoles: ['admin', 'stock'] }
      },
      // 供应商管理
      {
        path: 'supplier',
        name: 'Supplier',
        component: () => import('../views/Supplier.vue'),
        meta: { title: '供应商管理', requiresAuth: true, allowedRoles: ['admin', 'stock'] }
      },
      // 库存管理（二级：预警 / 查询 / 变动记录）
      {
        path: 'inventory',
        component: () => import('../views/inventory/InventoryLayout.vue'),
        meta: { requiresAuth: true, allowedRoles: ['admin', 'stock'] },
        redirect: { name: 'InventoryList' },
        children: [
          {
            path: 'alert',
            name: 'InventoryAlert',
            component: () => import('../views/inventory/InventoryAlert.vue'),
            meta: { title: '库存预警', requiresAuth: true, allowedRoles: ['admin', 'stock'] }
          },
          {
            path: 'list',
            name: 'InventoryList',
            component: () => import('../views/inventory/InventoryList.vue'),
            meta: { title: '库存查询', requiresAuth: true, allowedRoles: ['admin', 'stock'] }
          },
          {
            path: 'records',
            name: 'InventoryRecords',
            component: () => import('../views/inventory/InventoryRecords.vue'),
            meta: { title: '库存变动', requiresAuth: true, allowedRoles: ['admin', 'stock'] }
          },
          {
            path: 'check-records',
            name: 'InventoryCheckRecords',
            component: () => import('../views/inventory/InventoryCheckRecords.vue'),
            meta: { title: '盘点记录', requiresAuth: true, allowedRoles: ['admin', 'stock'] }
          }
        ]
      },
      // 销售管理
      {
        path: 'sales',
        name: 'Sales',
        component: () => import('../views/sales/SalesOrders.vue'),
        meta: { title: '销售管理', requiresAuth: true, allowedRoles: ['admin', 'cashier'] }
      },
      // 报表分析（二级：销售 / 库存 / 排行 / 毛利）
      {
        path: 'reports',
        component: () => import('../views/reports/ReportsLayout.vue'),
        meta: { requiresAuth: true, allowedRoles: ['admin', 'cashier'] },
        redirect: { name: 'ReportsSales' },
        children: [
          {
            path: 'sales',
            name: 'ReportsSales',
            component: () => import('../views/Reports.vue'),
            meta: {
              title: '销售报表',
              reportTab: 'sales',
              requiresAuth: true,
              allowedRoles: ['admin', 'cashier']
            }
          },
          {
            path: 'inventory',
            name: 'ReportsInventory',
            component: () => import('../views/Reports.vue'),
            meta: {
              title: '库存报表',
              reportTab: 'inventory',
              requiresAuth: true,
              allowedRoles: ['admin', 'cashier']
            }
          },
          {
            path: 'product-rank',
            name: 'ReportsProductRank',
            component: () => import('../views/Reports.vue'),
            meta: {
              title: '商品销售排行',
              reportTab: 'product_rank',
              requiresAuth: true,
              allowedRoles: ['admin', 'cashier']
            }
          },
          {
            path: 'profit',
            name: 'ReportsProfit',
            component: () => import('../views/Reports.vue'),
            meta: {
              title: '毛利分析',
              reportTab: 'profit',
              requiresAuth: true,
              allowedRoles: ['admin', 'cashier']
            }
          }
        ]
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true, allowedRoles: ['admin', 'cashier', 'stock'] }
      },
      // 系统管理
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/system/User.vue'),
        meta: { title: '员工管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('../views/system/Role.vue'),
        meta: { title: '角色管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'permissions',
        name: 'Permissions',
        component: () => import('../views/system/Permission.vue'),
        meta: { title: '权限管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'audit-logs',
        name: 'AuditLogs',
        component: () => import('../views/system/AuditLog.vue'),
        meta: { title: '操作日志', requiresAuth: true, requiresAdmin: true }
      }
    ]
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题：取最深层子路由的 title，避免仍显示父级「首页」
  const deepest = to.matched[to.matched.length - 1]
  const t = deepest?.meta.title ?? to.meta.title
  document.title = (t ? `${t} - ` : '') + '超市信息管理系统'
  
  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    const userInfo = localStorage.getItem('user')
    const selectedRole = localStorage.getItem('selectedRole')
    
    if (token && userInfo) {
      // 已登录，检查权限
      const user = JSON.parse(userInfo)
      let roles = []
      try {
        const raw = JSON.parse(localStorage.getItem('roles') || '[]')
        roles = Array.isArray(raw) ? raw : []
      } catch {
        roles = []
      }
      
      // 检查是否需要管理员权限
      if (to.meta.requiresAdmin) {
        if (roles.includes('admin')) {
          // 有管理员权限，允许访问
          next()
        } else {
          // 无管理员权限，重定向到首页
          next({ name: 'Dashboard' })
        }
      } else if (!isRouteAllowedForSelectedRole(to)) {
        next({ name: 'Dashboard' })
      } else {
        next()
      }
    } else {
      // 未登录，跳转到登录页
      next({ name: 'Login' })
    }
  } else {
    // 不需要登录，直接访问
    next()
  }
})

export default router