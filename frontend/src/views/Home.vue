<template>
  <div class="home-container">
    <!-- 侧边栏 -->
    <el-aside :width="collapsed ? '64px' : asideWidth" class="aside-container aside-layout">
      <div class="logo-container">
        <div class="logo-mark" aria-hidden="true">
          <el-icon class="logo-mark-icon"><ShoppingCart /></el-icon>
        </div>
        <h3 v-show="!collapsed" class="logo-text">超市信息管理系统</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="aside-menu sm-home-menu"
        background-color="transparent"
        text-color="#e2e8f0"
        active-text-color="#5eead4"
        router
      >
        <el-menu-item index="/home/dashboard">
            <el-icon><Document /></el-icon>
            <span v-show="!collapsed">仪表盘</span>
          </el-menu-item>
          <!-- 管理员/库存管理员：完整商品主数据 -->
          <el-sub-menu v-if="showStockProductMenu" index="1">
            <template #title>
              <el-icon><Goods /></el-icon>
              <span v-show="!collapsed">商品管理</span>
            </template>
            <el-menu-item index="/home/product">
              <el-icon><List /></el-icon>
              <span v-show="!collapsed">商品列表</span>
            </el-menu-item>
            <el-menu-item index="/home/category">
              <el-icon><FolderOpened /></el-icon>
              <span v-show="!collapsed">商品分类</span>
            </el-menu-item>
          </el-sub-menu>
          <!-- 收银员：仅可查价、选品 -->
          <el-menu-item v-else-if="showCashierProductEntry" index="/home/product">
            <el-icon><Goods /></el-icon>
            <span v-show="!collapsed">商品列表</span>
          </el-menu-item>
          <el-sub-menu v-if="showStockAreaMenu" index="sub-inventory">
            <template #title>
              <el-icon><Box /></el-icon>
              <span v-show="!collapsed">库存管理</span>
            </template>
            <el-menu-item index="/home/inventory/alert">
              <el-icon><Warning /></el-icon>
              <span v-show="!collapsed">库存预警</span>
            </el-menu-item>
            <el-menu-item index="/home/inventory/list">
              <el-icon><Files /></el-icon>
              <span v-show="!collapsed">库存查询</span>
            </el-menu-item>
            <el-menu-item index="/home/inventory/records">
              <el-icon><Document /></el-icon>
              <span v-show="!collapsed">库存变动</span>
            </el-menu-item>
            <!-- 盘点记录菜单已隐藏 -->
            <!-- <el-menu-item index="/home/inventory/check-records">
              <el-icon><Document /></el-icon>
              <span v-show="!collapsed">盘点记录</span>
            </el-menu-item> -->
          </el-sub-menu>
          <el-menu-item v-if="showSalesAreaMenu" index="/home/sales">
            <el-icon><Money /></el-icon>
            <span v-show="!collapsed">销售管理</span>
          </el-menu-item>
          <el-sub-menu v-if="showSalesAreaMenu" index="sub-reports">
            <template #title>
              <el-icon><DataAnalysis /></el-icon>
              <span v-show="!collapsed">报表分析</span>
            </template>
            <el-menu-item index="/home/reports/sales">
              <el-icon><TrendCharts /></el-icon>
              <span v-show="!collapsed">销售报表</span>
            </el-menu-item>
            <el-menu-item index="/home/reports/inventory">
              <el-icon><PieChart /></el-icon>
              <span v-show="!collapsed">库存报表</span>
            </el-menu-item>
            <el-menu-item index="/home/reports/product-rank">
              <el-icon><Histogram /></el-icon>
              <span v-show="!collapsed">商品销售排行</span>
            </el-menu-item>
            <el-menu-item index="/home/reports/profit">
              <el-icon><Coin /></el-icon>
              <span v-show="!collapsed">毛利分析</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-if="showStockAreaMenu" index="/home/supplier">
            <el-icon><GoodsFilled /></el-icon>
            <span v-show="!collapsed">供应商管理</span>
          </el-menu-item>

          <!-- 系统管理：仅管理员账号可见（与路由 requiresAdmin 一致） -->
          <el-sub-menu v-if="isAdmin" index="6">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span v-show="!collapsed">系统管理</span>
            </template>
            <el-menu-item index="/home/users">
              <el-icon><User /></el-icon>
              <span v-show="!collapsed">员工管理</span>
            </el-menu-item>
            <el-menu-item index="/home/roles">
              <el-icon><Key /></el-icon>
              <span v-show="!collapsed">角色管理</span>
            </el-menu-item>
            <el-menu-item index="/home/permissions">
              <el-icon><Lock /></el-icon>
              <span v-show="!collapsed">权限管理</span>
            </el-menu-item>
            <el-menu-item index="/home/audit-logs">
              <el-icon><Notebook /></el-icon>
              <span v-show="!collapsed">操作日志</span>
            </el-menu-item>
          </el-sub-menu>

      </el-menu>
    </el-aside>
    <!-- 主内容区域 -->
    <el-container class="home-main">
      <!-- 顶部导航栏 -->
      <el-header height="56px" class="header-container sm-home-header">
        <div class="header-left">
          <el-button
            type="text"
            class="menu-toggle-btn"
            @click="toggleMenu"
            v-if="!collapsed"
          >
            <el-icon><Menu /></el-icon>
          </el-button>
          <el-button
            type="text"
            class="menu-toggle-btn"
            @click="toggleMenu"
            v-else
          >
            <el-icon><CaretRight /></el-icon>
          </el-button>
          <div class="header-title-wrap">
            <span class="current-page">{{ currentPageTitle }}</span>
          </div>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" placement="bottom-end">
            <span class="user-info">
              <span class="user-chip">
                <el-icon class="user-avatar"><User /></el-icon>
                <span class="user-name">{{ userInfo.username || '用户' }}</span>
              </span>
              <el-icon class="user-caret"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleProfile">
                  <el-icon><UserFilled /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <!-- 内容区域 -->
      <el-main class="main-container">
        <div class="main-inner">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Document,
  Goods,
  Warning,
  Files,
  Box,
  Money,
  DataAnalysis,
  Setting,
  Menu,
  CaretRight,
  User,
  UserFilled,
  ArrowDown,
  SwitchButton,
  OfficeBuilding,
  List,
  Notebook,
  FolderOpened,
  Key,
  Lock,
  ShoppingCart,
  TrendCharts,
  PieChart,
  Histogram,
  Coin,
  GoodsFilled
} from '@element-plus/icons-vue'
import {
  ROLE_ADMIN,
  ROLE_CASHIER,
  ROLE_STOCK,
  getEffectiveSelectedRole
} from '../utils/roleAccess.js'

const route = useRoute()
const router = useRouter()

/** 侧栏宽度（与 theme.css --sm-aside-width 保持一致观感） */
const asideWidth = '220px'

const collapsed = ref(false)
const userStr = localStorage.getItem('user');
// 确保只有当userStr是有效字符串且不是"undefined"字符串时才解析
const userInfo = ref(JSON.parse((typeof userStr === 'string' && userStr && userStr !== 'undefined') ? userStr : '{}'))

const activeMenu = computed(() => {
  return route.path
})

/** 与 router beforeEach 中 requiresAdmin 判断一致：localStorage.roles 含 admin */
const isAdmin = computed(() => {
  try {
    const roles = JSON.parse(localStorage.getItem('roles') || '[]')
    return Array.isArray(roles) && roles.includes('admin')
  } catch {
    return false
  }
})

/** 当前登录身份（与侧栏、路由 meta.allowedRoles 一致） */
const effectiveRole = computed(() => {
  route.path
  return getEffectiveSelectedRole()
})

const showStockProductMenu = computed(() => {
  const r = effectiveRole.value
  return r === ROLE_ADMIN || r === ROLE_STOCK
})

const showCashierProductEntry = computed(() => effectiveRole.value === ROLE_CASHIER)

const showStockAreaMenu = computed(() => {
  const r = effectiveRole.value
  return r === ROLE_ADMIN || r === ROLE_STOCK
})

const showSalesAreaMenu = computed(() => {
  const r = effectiveRole.value
  return r === ROLE_ADMIN || r === ROLE_CASHIER
})

/**
 * 顶栏标题：与各侧栏一级分组一致（与「商品管理」下各子页统一显示父级名称相同）。
 * 正文区 h2 仍由各页面自行区分。
 */
const currentPageTitle = computed(() => {
  const p = route.path
  if (p === '/home/dashboard') {
    return '仪表盘'
  }
  if (p.startsWith('/home/product') || p === '/home/category') {
    return '商品管理'
  }
  if (p.startsWith('/home/inventory')) {
    return '库存管理'
  }
  if (p === '/home/sales') {
    return '销售管理'
  }
  if (p.startsWith('/home/reports')) {
    return '报表分析'
  }
  if (p === '/home/supplier') {
    return '供应商管理'
  }
  if (p === '/home/users') {
    return '员工管理'
  }
  if (p === '/home/roles' || p === '/home/permissions' || p === '/home/audit-logs') {
    return '系统管理'
  }
  return route.meta.title || '首页'
})

const toggleMenu = () => {
  collapsed.value = !collapsed.value
}

const handleProfile = () => {
  // 导航到个人中心页面
  router.push('/home/profile')
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('roles')
  localStorage.removeItem('selectedRole')
  ElMessage.success('退出登录成功')
  router.push('/login')
}

/** 主内容区可滚动；切换子路由时保留滚动条位置会导致新页面仍停在底部（如仪表盘「查看全部」进销售页） */
watch(
  () => route.path,
  () => {
    nextTick(() => {
      const main = document.querySelector('.home-main .main-container')
      if (main) main.scrollTop = 0
    })
  }
)
</script>

<style scoped>
.home-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--sm-bg-app);
}

/* 右侧：顶栏 + 主区纵向铺满，避免宽屏下主区高度异常 */
.home-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.aside-container {
  background: var(--sm-sidebar-bg, linear-gradient(180deg, #115e59 0%, #0f172a 72%));
  height: 100%;
  transition: width 0.3s;
  border-right: 1px solid var(--sm-sidebar-border, rgba(45, 212, 191, 0.22));
}

.aside-layout {
  display: flex;
  flex-direction: column;
}

.logo-container {
  padding: 18px 14px 16px;
  text-align: center;
  border-bottom: 1px solid var(--sm-sidebar-border, rgba(45, 212, 191, 0.22));
}

.logo-mark {
  width: 44px;
  height: 44px;
  margin: 0 auto 10px;
  border-radius: 12px;
  background: rgba(45, 212, 191, 0.15);
  border: 1px solid rgba(45, 212, 191, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-mark-icon {
  font-size: 22px;
  color: #5eead4;
}

.logo-text {
  color: #f0fdfa;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.4px;
  line-height: 1.4;
}

.aside-menu {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  border-right: none;
  padding: 8px 0 16px;
  /* Firefox：细滚动条 + 半透明 */
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.18) transparent;
}

/* WebKit：侧栏滚动条变细、默认近乎隐藏，悬停侧栏时略明显 */
.aside-menu::-webkit-scrollbar {
  width: 5px;
}
.aside-menu::-webkit-scrollbar-track {
  background: transparent;
}
.aside-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.12);
  border-radius: 6px;
}
.aside-container:hover .aside-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.22);
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  height: var(--sm-header-height, 56px);
  box-sizing: border-box;
  background: linear-gradient(180deg, #ffffff 0%, #f8fffc 100%);
  box-shadow: var(--sm-header-shadow, 0 1px 0 rgba(13, 148, 136, 0.12), 0 4px 24px rgba(15, 118, 110, 0.08));
  padding: 0 20px 0 16px;
  border-bottom: 1px solid rgba(13, 148, 136, 0.1);
}

.sm-home-header .current-page {
  color: var(--sm-primary-dark, #0f766e);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.header-title-wrap {
  min-width: 0;
  margin-left: 4px;
  padding-left: 12px;
  border-left: 3px solid var(--sm-primary-light, #14b8a6);
}

.menu-toggle-btn {
  margin-right: 0;
  color: var(--sm-primary-dark, #0f766e);
  padding: 8px;
  border-radius: 8px;
}

.menu-toggle-btn:hover {
  background: rgba(13, 148, 136, 0.08);
  color: var(--sm-primary, #0d9488);
}

.current-page {
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 0.2px;
}

.header-right {
  flex-shrink: 0;
}

.user-info {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 6px 4px 6px 10px;
  border-radius: 999px;
  border: 1px solid rgba(13, 148, 136, 0.18);
  background: rgba(255, 255, 255, 0.9);
  transition: background 0.2s ease, box-shadow 0.2s ease;
}

.user-info:hover {
  background: #fff;
  box-shadow: 0 2px 12px rgba(13, 148, 136, 0.12);
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 160px;
}

.user-avatar {
  font-size: 18px;
  color: var(--sm-primary, #0d9488);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-caret {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.main-container {
  flex: 1;
  min-height: 0;
  background-color: var(--sm-bg-main, #f0fdfa);
  padding: var(--sm-main-padding, 20px 24px 28px);
  overflow: auto;
  box-sizing: border-box;
}

.main-inner {
  max-width: var(--sm-content-max, 1440px);
  margin: 0 auto;
  width: 100%;
  min-height: 100%;
}

/* 侧栏菜单：悬停与激活与主题一致 */
:deep(.sm-home-menu.el-menu) {
  --el-menu-hover-bg-color: rgba(45, 212, 191, 0.12);
  --el-menu-active-color: var(--sm-menu-active, #5eead4);
}
:deep(.sm-home-menu .el-sub-menu__title:hover),
:deep(.sm-home-menu .el-menu-item:hover) {
  background-color: rgba(45, 212, 191, 0.1) !important;
}
:deep(.sm-home-menu .el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(45, 212, 191, 0.2) 0%, transparent 100%) !important;
  border-right: 3px solid var(--sm-primary-light, #14b8a6);
}

:deep(.sm-home-menu.el-menu--vertical .el-menu-item),
:deep(.sm-home-menu.el-menu--vertical .el-sub-menu__title) {
  margin: 2px 8px;
  border-radius: 8px;
}
</style>
