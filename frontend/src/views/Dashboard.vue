<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><Document /></el-icon>
        数据概览
      </h2>
    </div>

    <el-card shadow="hover" class="welcome-card">
      <template #header>
        <div class="card-header">
          <h3>欢迎回来，{{ userInfo.username || '用户' }}</h3>
        </div>
      </template>
      <div class="welcome-content">
        <p>今天是 {{ currentDate }}，{{ currentDay }}</p>
      </div>
    </el-card>

    <div class="stats-container">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon bg-blue">
            <el-icon><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <h4 class="stat-title">商品种类</h4>
            <p class="stat-value">{{ productCount }}</p>
          </div>
        </div>
      </el-card>

      <el-card v-if="showInventoryMetrics" shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon bg-green">
            <el-icon><Box /></el-icon>
          </div>
          <div class="stat-info">
            <h4 class="stat-title">库存总件数</h4>
            <p class="stat-value">{{ inventoryQtyTotal }}</p>
          </div>
        </div>
      </el-card>

      <el-card v-if="showSalesMetrics" shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon bg-yellow">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <h4 class="stat-title">今日销售额</h4>
            <p class="stat-value">¥{{ formatMoney(todaySales) }}</p>
          </div>
        </div>
      </el-card>

      <el-card v-if="showSalesMetrics" shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon bg-red">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <h4 class="stat-title">本月销售额</h4>
            <p class="stat-value">¥{{ formatMoney(monthSales) }}</p>
          </div>
        </div>
      </el-card>
    </div>

    <el-alert
      v-if="dashboardHints.length"
      type="info"
      :closable="false"
      show-icon
      class="dashboard-hints-alert"
    >
      <ul class="dashboard-hints-list">
        <li v-for="(line, idx) in dashboardHints" :key="idx">{{ line }}</li>
      </ul>
    </el-alert>

    <div class="charts-container">
      <el-card v-if="showSalesMetrics" shadow="hover" class="chart-card chart-card--wide">
        <template #header>
          <div class="card-header">
            <h3>近7日销售趋势</h3>
          </div>
        </template>
        <div class="chart-content">
          <div ref="salesTrendChartRef" class="chart-container"></div>
        </div>
      </el-card>

      <div v-if="showInventoryMetrics" class="chart-row-inventory">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <h3>库存金额 · 分类占比</h3>
            </div>
          </template>
          <div class="chart-content">
            <div ref="categoryChartRef" class="chart-container"></div>
          </div>
        </el-card>
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <h3>分类库存件数 TOP</h3>
            </div>
          </template>
          <div class="chart-content">
            <div ref="qtyBarChartRef" class="chart-container"></div>
          </div>
        </el-card>
      </div>

      <el-card v-if="showInventoryMetrics" shadow="hover" class="low-stock-card chart-card--wide">
        <template #header>
          <div class="card-header">
            <h3>低库存预警</h3>
            <el-button type="primary" link @click="goToInventoryPage">去库存管理</el-button>
          </div>
        </template>
        <p v-if="!lowInventoryRows.length" class="low-stock-empty">当前无低于最低库存的商品</p>
        <el-table
          v-else
          :data="lowInventoryRows"
          stripe
          size="small"
          style="width: 100%"
          max-height="280"
        >
          <el-table-column prop="name" label="商品名称" min-width="120" show-overflow-tooltip />
          <el-table-column prop="barcode" label="条码" width="120" show-overflow-tooltip />
          <el-table-column label="分类" min-width="90" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.category_name || scope.row.categoryName || '—' }}</template>
          </el-table-column>
          <el-table-column label="当前库存" width="96" align="right">
            <template #default="scope">{{ scope.row.quantity ?? '—' }}</template>
          </el-table-column>
          <el-table-column label="最低库存" width="96" align="right">
            <template #default="scope">{{ scope.row.min_stock ?? scope.row.minStock ?? '—' }}</template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card v-if="showSalesMetrics" shadow="hover" class="chart-card chart-card--wide">
        <template #header>
          <div class="card-header">
            <h3>本月商品销售额 TOP</h3>
          </div>
        </template>
        <div class="chart-content chart-content--short">
          <div ref="rankChartRef" class="chart-container"></div>
        </div>
      </el-card>
    </div>

    <el-card v-if="showSalesMetrics" shadow="hover" class="recent-sales-card">
      <template #header>
        <div class="card-header">
          <h3>最近订单</h3>
          <el-button type="primary" link @click="goToSalesPage">查看全部</el-button>
        </div>
      </template>
      <el-table
        :data="recentOrders"
        class="recent-orders-table"
        stripe
        style="width: 100%"
      >
        <template #empty>
          <span class="table-empty-hint">暂无订单记录，可在「销售记录」中查看或创建订单。</span>
        </template>
        <el-table-column prop="orderNumber" label="订单号" show-overflow-tooltip />
        <el-table-column label="实收金额" align="right">
          <template #default="scope">¥{{ formatMoney(scope.row.actualAmount) }}</template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.paymentStatus === 'PAID' ? 'success' : 'info'" size="small">
              {{ paymentLabel(scope.row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="状态" align="center">
          <template #default="scope">
            <el-tag size="small">{{ orderStatusLabel(scope.row.orderStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" show-overflow-tooltip>
          <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="cashierName" label="收银员" align="center" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { Goods, Box, Money, TrendCharts, Document } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { productAPI, reportAPI, salesAPI } from '../services/api'
import { ROLE_ADMIN, ROLE_CASHIER, ROLE_STOCK, getEffectiveSelectedRole } from '../utils/roleAccess.js'

const router = useRouter()

const showSalesMetrics = computed(() => {
  const r = getEffectiveSelectedRole()
  return r === ROLE_ADMIN || r === ROLE_CASHIER
})

const showInventoryMetrics = computed(() => {
  const r = getEffectiveSelectedRole()
  return r === ROLE_ADMIN || r === ROLE_STOCK
})

const userStr = localStorage.getItem('user')
const userInfo = ref(JSON.parse(typeof userStr === 'string' && userStr && userStr !== 'undefined' ? userStr : '{}'))

const productCount = ref(0)
const inventoryQtyTotal = ref(0)
const todaySales = ref(0)
const monthSales = ref(0)
const recentOrders = ref([])
/** 低库存列表（quantity <= min_stock），最多展示 10 条 */
const lowInventoryRows = ref([])

/** 数据为空时的说明（加载完成后刷新） */
const dashboardHints = ref([])

const salesTrendChartRef = ref(null)
const categoryChartRef = ref(null)
const qtyBarChartRef = ref(null)
const rankChartRef = ref(null)
let salesChart = null
let categoryChart = null
let qtyBarChart = null
let rankChart = null

const currentDate = computed(() => {
  const date = new Date()
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
})

const currentDay = computed(() => {
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return weekDays[new Date().getDay()]
})

function formatMoney(n) {
  if (n == null || Number.isNaN(Number(n))) return '0.00'
  return Number(n).toFixed(2)
}

function formatDateTime(v) {
  if (!v) return ''
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function padDate(d) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function addDays(d, days) {
  const x = new Date(d)
  x.setDate(x.getDate() + days)
  return x
}

function paymentLabel(s) {
  const m = { UNPAID: '未付', PAID: '已付', REFUNDED: '已退' }
  return m[s] || s || '-'
}

function orderStatusLabel(s) {
  const m = { CREATED: '已创建', COMPLETED: '已完成', CANCELLED: '已取消', REFUNDED: '已退款' }
  return m[s] || s || '-'
}

function unwrapBody(res) {
  if (res == null) return {}
  return res.data !== undefined ? res.data : res
}

async function safeReq(label, fn) {
  try {
    return { ok: true, data: await fn() }
  } catch (e) {
    console.warn(`[仪表盘] ${label} 失败`, e)
    return { ok: false, error: e }
  }
}

function pickTotal(res) {
  const r = unwrapBody(res)
  const n = r.total ?? r.Total
  return Number(n) || 0
}

/** 后端 MyBatis Map 经 JSON 后 date 可能是字符串或数组 */
function normalizeTrendLabel(row) {
  const v = row.date ?? row.period ?? row.month
  if (v == null) return ''
  if (Array.isArray(v)) {
    if (v.length >= 3) return `${v[0]}-${String(v[1]).padStart(2, '0')}-${String(v[2]).padStart(2, '0')}`
    return v.join('-')
  }
  const s = String(v)
  return s.length > 10 ? s.slice(0, 10) : s
}

/** 固定展示最近 7 个自然日；无数据的日期补 0，避免 X 轴只有有单的一天 */
function buildLast7DaysFromTrend(trendData) {
  const today = new Date()
  const byDay = new Map()
  for (const row of trendData || []) {
    let label = normalizeTrendLabel(row)
    if (!label) continue
    if (label.length > 10) label = label.slice(0, 10)
    byDay.set(label, {
      salesAmount: Number(row.salesAmount ?? row.sales_amount) || 0,
      orderCount: Number(row.orderCount ?? row.order_count) || 0
    })
  }
  const dates = []
  for (let i = 6; i >= 0; i--) {
    dates.push(padDate(addDays(today, -i)))
  }
  const amounts = dates.map((d) => byDay.get(d)?.salesAmount ?? 0)
  const orderCounts = dates.map((d) => byDay.get(d)?.orderCount ?? 0)
  return { dates, amounts, orderCounts }
}

async function loadStats() {
  const today = new Date()
  const todayStr = padDate(today)
  const monthStart = new Date(today.getFullYear(), today.getMonth(), 1)
  const monthStartStr = padDate(monthStart)
  const trendStart = padDate(addDays(today, -6))

  const role = getEffectiveSelectedRole()
  const wantInv = role === ROLE_ADMIN || role === ROLE_STOCK
  const wantSales = role === ROLE_ADMIN || role === ROLE_CASHIER

  const [rProd, rInv, rLowInv, rToday, rMonth, rTrend, rRank, rOrd] = await Promise.all([
    safeReq('商品总数', () => productAPI.getProducts({ page: 1, pageSize: 1 })),
    wantInv ? safeReq('库存报表', () => reportAPI.getInventoryReport({})) : Promise.resolve({ ok: true, data: null, skipped: true }),
    wantInv ? safeReq('低库存', () => reportAPI.getLowInventoryReport({})) : Promise.resolve({ ok: true, skipped: true }),
    wantSales ? safeReq('今日销售', () => reportAPI.getSalesReport({ startDate: todayStr, endDate: todayStr })) : Promise.resolve({ ok: true, skipped: true }),
    wantSales ? safeReq('本月销售', () => reportAPI.getSalesReport({ startDate: monthStartStr, endDate: todayStr })) : Promise.resolve({ ok: true, skipped: true }),
    wantSales ? safeReq('销售趋势', () => reportAPI.getSalesReport({ startDate: trendStart, endDate: todayStr })) : Promise.resolve({ ok: true, skipped: true }),
    wantSales ? safeReq('销售排行', () => reportAPI.getProductSalesRank({ startDate: monthStartStr, endDate: todayStr })) : Promise.resolve({ ok: true, skipped: true }),
    wantSales ? safeReq('最近订单', () => salesAPI.getOrders({ page: 1, pageSize: 8 })) : Promise.resolve({ ok: true, skipped: true })
  ])

  const failed = [rProd, rInv, rLowInv, rToday, rMonth, rTrend, rRank, rOrd].filter(
    (r) => !r.skipped && !r.ok && r.error?.response?.status !== 401
  )
  if (failed.length) {
    ElMessage.warning('部分数据加载失败，请打开控制台查看详情')
  }

  productCount.value = rProd.ok ? pickTotal(rProd.data) : 0

  const invData = rInv.ok ? unwrapBody(rInv.data) : {}
  const invList = invData.inventoryList || invData.records || invData.list || []
  inventoryQtyTotal.value = invList.reduce((sum, row) => sum + (Number(row.quantity) || 0), 0)

  if (rLowInv.ok && !rLowInv.skipped) {
    const lowPayload = rLowInv.data
    const lowList = Array.isArray(lowPayload) ? lowPayload : []
    lowInventoryRows.value = lowList.slice(0, 10)
  } else {
    lowInventoryRows.value = []
  }

  const tData = rToday.ok ? unwrapBody(rToday.data) : {}
  todaySales.value = Number(tData.totalSales ?? 0)

  const mData = rMonth.ok ? unwrapBody(rMonth.data) : {}
  monthSales.value = Number(mData.totalSales ?? 0)

  const trData = rTrend.ok ? unwrapBody(rTrend.data) : {}
  const trendData = trData.trendData || []

  const catPie = buildCategoryPieData(invList)

  const rkData = rRank.ok ? unwrapBody(rRank.data) : {}
  const rankList = (rkData.rankList || []).slice(0, 8)

  const ordData = rOrd.ok ? unwrapBody(rOrd.data) : {}
  const orderList = ordData.records || ordData.list || []
  const orders = Array.isArray(orderList) ? orderList : []
  recentOrders.value = [...orders].sort((a, b) => {
    const ta = new Date(a.createTime).getTime()
    const tb = new Date(b.createTime).getTime()
    return (Number.isNaN(tb) ? 0 : tb) - (Number.isNaN(ta) ? 0 : ta)
  })

  const hints = []
  if (wantSales && rTrend.ok) {
    const { amounts } = buildLast7DaysFromTrend(trendData)
    if (amounts.reduce((a, b) => a + b, 0) === 0) {
      hints.push(
        '近7日暂无销售汇总（趋势图为平线）。若应有订单，请确认订单创建日期在近7日内，或到「报表分析」核对销售报表。'
      )
    }
  }
  if (wantSales && rRank.ok && rankList.length === 0) {
    hints.push('本月暂无商品销售额排行数据。')
  }
  if (wantInv && rInv.ok && catPie.length === 0) {
    hints.push('暂无分类库存数据，请先维护商品与库存。')
  }
  if (wantSales && rOrd.ok && recentOrders.value.length === 0) {
    hints.push('列表区暂无最近订单。')
  }
  dashboardHints.value = hints

  await nextTick()
  renderSalesTrendChart(trendData)
  renderCategoryChart(catPie)
  const qtyRows = buildCategoryQtyTop(invList)
  renderQtyBarChart(qtyRows)
  renderRankChart(rankList)
}

/** 按分类汇总库存件数，取 TOP 用于条形图 */
function buildCategoryQtyTop(invList) {
  const map = {}
  for (const row of invList || []) {
    const name = row.category_name || row.categoryName || '未分类'
    const q = Number(row.quantity) || 0
    map[name] = (map[name] || 0) + q
  }
  return Object.entries(map)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 8)
    .map(([name, value]) => ({ name, value }))
}

function buildCategoryPieData(invList) {
  const byMoney = {}
  const byQty = {}
  for (const row of invList) {
    const name = row.category_name || row.categoryName || '未分类'
    const iv = Number(row.inventory_value ?? row.inventoryValue) || 0
    let money = iv
    if (money <= 0) {
      const q = Number(row.quantity) || 0
      const sp = Number(row.selling_price ?? row.sellingPrice) || 0
      money = q * sp
    }
    const q = Number(row.quantity) || 0
    byMoney[name] = (byMoney[name] || 0) + money
    byQty[name] = (byQty[name] || 0) + q
  }
  const sumMoney = Object.values(byMoney).reduce((a, b) => a + b, 0)
  const src = sumMoney > 0 ? byMoney : byQty
  return Object.entries(src).map(([name, value]) => ({ name, value }))
}

function renderSalesTrendChart(trendData) {
  if (!salesTrendChartRef.value) return
  if (!salesChart) salesChart = echarts.init(salesTrendChartRef.value)

  const { dates, amounts, orderCounts } = buildLast7DaysFromTrend(trendData)

  salesChart.setOption({
    title: { show: false },
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售额', '订单数'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '48', top: '12%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { rotate: dates.some((d) => String(d).length > 10) ? 30 : 0 }
    },
    yAxis: [
      { type: 'value', name: '销售额(元)', axisLabel: { formatter: (v) => `¥${v}` } },
      {
        type: 'value',
        name: '订单数',
        minInterval: 1,
        splitLine: { show: false },
        axisLabel: {
          formatter: (v) => (Number.isFinite(v) ? String(Math.round(v)) : '')
        }
      }
    ],
    series: [
      {
        name: '销售额',
        type: 'bar',
        data: amounts,
        barMaxWidth: 36,
        itemStyle: { color: '#409EFF', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '订单数',
        type: 'line',
        yAxisIndex: 1,
        smooth: false,
        showSymbol: true,
        symbol: 'circle',
        symbolSize: 8,
        z: 10,
        lineStyle: {
          width: 3,
          color: '#67C23A',
          type: 'solid'
        },
        itemStyle: { color: '#67C23A', borderColor: '#fff', borderWidth: 1 },
        emphasis: {
          scale: true,
          itemStyle: { borderWidth: 2 }
        },
        data: orderCounts
      }
    ]
  })
}

function renderCategoryChart(pieData) {
  if (!categoryChartRef.value) return
  if (!categoryChart) categoryChart = echarts.init(categoryChartRef.value)

  if (!pieData.length) {
    categoryChart.setOption({
      title: { text: '暂无分类库存数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } }
    })
    return
  }

  categoryChart.setOption({
    title: { show: false },
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'middle' },
    series: [
      {
        name: '库存金额',
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['58%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{d}%' },
        data: pieData
      }
    ]
  })
}

function renderQtyBarChart(rows) {
  if (!qtyBarChartRef.value) return
  if (!qtyBarChart) qtyBarChart = echarts.init(qtyBarChartRef.value)

  const names = rows.map((r) => r.name)
  const vals = rows.map((r) => r.value)

  if (!names.length) {
    qtyBarChart.setOption({
      title: { text: '暂无分类库存数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } }
    })
    return
  }

  qtyBarChart.setOption({
    title: { show: false },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: { type: 'value', name: '件数' },
    yAxis: {
      type: 'category',
      data: names,
      inverse: true,
      axisLabel: { width: 100, overflow: 'truncate' }
    },
    series: [
      {
        name: '库存件数',
        type: 'bar',
        data: vals,
        barMaxWidth: 28,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#5eead4' },
            { offset: 1, color: '#0d9488' }
          ]),
          borderRadius: [0, 4, 4, 0]
        }
      }
    ]
  })
}

function renderRankChart(rankList) {
  if (!rankChartRef.value) return
  if (!rankChart) rankChart = echarts.init(rankChartRef.value)

  const names = rankList.map((r) => r.product_name || r.productName || '商品').slice(0, 8)
  const vals = rankList.map((r) => Number(r.salesAmount ?? r.sales_amount) || 0).slice(0, 8)

  if (!names.length) {
    rankChart.setOption({
      title: { text: '暂无排行数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } }
    })
    return
  }

  rankChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: { type: 'value', axisLabel: { formatter: (v) => `¥${v}` } },
    yAxis: { type: 'category', data: names, inverse: true, axisLabel: { width: 132, overflow: 'truncate' } },
    series: [
      {
        name: '销售额',
        type: 'bar',
        data: vals,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#5d9cec' },
            { offset: 1, color: '#409EFF' }
          ]),
          borderRadius: [0, 4, 4, 0]
        }
      }
    ]
  })
}

function handleResize() {
  salesChart?.resize()
  categoryChart?.resize()
  qtyBarChart?.resize()
  rankChart?.resize()
}

const goToSalesPage = () => {
  router.push('/home/sales')
}

const goToInventoryPage = () => {
  router.push('/home/inventory/list')
}

onMounted(async () => {
  try {
    await loadStats()
  } catch (e) {
    console.error(e)
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  salesChart?.dispose()
  categoryChart?.dispose()
  qtyBarChart?.dispose()
  rankChart?.dispose()
  salesChart = null
  categoryChart = null
  qtyBarChart = null
  rankChart = null
})
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

.welcome-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
}

.welcome-content {
  padding: 8px 0 0;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 16px;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}

.bg-blue {
  background-color: #409eff;
}
.bg-green {
  background-color: #67c23a;
}
.bg-yellow {
  background-color: #e6a23c;
}
.bg-red {
  background-color: #f56c6c;
}

.stat-title {
  margin: 0 0 6px 0;
  font-size: 13px;
  color: #909399;
  font-weight: normal;
}

.stat-value {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.chart-card--wide {
  grid-column: span 2;
}

/* 库存两行图：与「近7日」同宽占满一行，两列并排 */
.chart-row-inventory {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 960px) {
  .charts-container {
    grid-template-columns: 1fr;
  }
  .chart-card--wide {
    grid-column: span 1;
  }
  .chart-row-inventory {
    grid-template-columns: 1fr;
  }
}

.chart-content {
  height: 320px;
}

.chart-content--short {
  height: 280px;
}

.chart-container {
  width: 100%;
  height: 100%;
  min-height: 220px;
}

.recent-sales-card {
  margin-bottom: 20px;
}

.dashboard-hints-alert {
  margin-bottom: 16px;
}

.dashboard-hints-list {
  margin: 0;
  padding-left: 18px;
  line-height: 1.6;
}

.table-empty-hint {
  color: #909399;
  font-size: 14px;
}

.low-stock-card {
  margin-bottom: 20px;
}

.low-stock-card .card-header h3 {
  margin: 0;
  font-size: 16px;
}

.low-stock-empty {
  margin: 0;
  padding: 12px 0;
  color: #909399;
  font-size: 14px;
}

/* 最近订单：6 列均匀分配宽度 */
.recent-orders-table {
  width: 100%;
}

.recent-orders-table :deep(.el-table__header-wrapper table),
.recent-orders-table :deep(.el-table__body-wrapper table) {
  table-layout: fixed;
  width: 100%;
}

.recent-orders-table :deep(.el-table__header-wrapper colgroup col),
.recent-orders-table :deep(.el-table__body-wrapper colgroup col) {
  width: 16.666% !important;
}
</style>
