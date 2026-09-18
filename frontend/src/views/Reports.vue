<template>
  <div class="reports-container">
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><TrendCharts /></el-icon>
        {{ reportPageSubtitle }}
      </h2>
    </div>

    <!-- 数据筛选区域 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-col>
        <el-col :span="12" class="filter-actions-col">
          <el-button type="primary" @click="refreshData">刷新数据</el-button>
        </el-col>
      </el-row>
      <div v-if="reportType !== 'inventory'" class="filter-quick-row">
        <span class="filter-quick-label">快捷范围</span>
        <el-button size="small" @click="applyQuickDateRange('last7')">近7日</el-button>
        <el-button size="small" @click="applyQuickDateRange('thisMonth')">本月</el-button>
        <el-button size="small" @click="applyQuickDateRange('prevMonth')">上月</el-button>
        <span v-if="dateRangeLabel" class="filter-range-text">当前：{{ dateRangeLabel }}</span>
      </div>
      <p v-else class="filter-inventory-note">库存报表为当前全量快照，日期与快捷范围不影响本表；切换销售/排行/毛利时可按日期筛选。</p>
    </div>

    <el-alert
      v-if="reportEmptyHint"
      type="info"
      :closable="false"
      show-icon
      class="report-hint-alert"
    >
      {{ reportEmptyHint }}
    </el-alert>
    
    <!-- 数据统计卡片 -->
    <div class="stats-section" v-if="reportType === 'sales'">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-label">总销售额</div>
              <div class="stats-value">{{ Number(totalSales).toFixed(2) }}</div>
              <div class="stats-change" :class="{ 'positive': salesGrowth > 0, 'negative': salesGrowth < 0 }">
                {{ salesGrowth > 0 ? '+' : '' }}{{ salesGrowth }}%
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-label">订单数</div>
              <div class="stats-value">{{ totalOrders }}</div>
              <div class="stats-change" :class="{ 'positive': orderGrowth > 0, 'negative': orderGrowth < 0 }">
                {{ orderGrowth > 0 ? '+' : '' }}{{ orderGrowth }}%
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-label">客单价</div>
              <div class="stats-value">{{ Number(averageOrderValue).toFixed(2) }}</div>
              <div class="stats-change" :class="{ 'positive': avgGrowth > 0, 'negative': avgGrowth < 0 }">
                {{ avgGrowth > 0 ? '+' : '' }}{{ avgGrowth }}%
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-label">销售增长率</div>
              <div class="stats-value">{{ salesGrowthRate }}%</div>
              <div class="stats-change" :class="{ 'positive': salesGrowthRate > 0, 'negative': salesGrowthRate < 0 }">
                {{ salesGrowthRate > 0 ? '增长' : '下降' }}
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 库存统计卡片 -->
    <div class="stats-section" v-if="reportType === 'inventory'">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-label">库存总额</div>
              <div class="stats-value">{{ Number(totalInventoryValue).toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-label">库存商品数</div>
              <div class="stats-value">{{ totalInventoryCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-label">低库存商品数</div>
              <div class="stats-value">{{ lowInventoryCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-label">库存周转率</div>
              <div class="stats-value">{{ inventoryTurnover }}次</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 毛利分析汇总 -->
    <div class="stats-section" v-if="reportType === 'profit'">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-label">销售收入（已完成已付）</div>
              <div class="stats-value">{{ profitSummary.sales.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-label">成本（进价×销量）</div>
              <div class="stats-value">{{ profitSummary.cost.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-label">毛利</div>
              <div class="stats-value" :style="{ color: profitSummary.profit >= 0 ? '#67c23a' : '#f56c6c' }">{{ profitSummary.profit.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 商品销售排行统计卡片 -->
    <div class="stats-section" v-if="reportType === 'product_rank'">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-label">热销商品数</div>
              <div class="stats-value">{{ topProducts.length }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-label">热销商品销售额</div>
              <div class="stats-value">{{ topProductsSales }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-label">占总销售额比例</div>
              <div class="stats-value">{{ topProductsRatio }}%</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 图表展示区域 -->
    <div class="charts-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>{{ reportTitle }}</span>
          </div>
        </template>
        <div ref="chartRef" :class="['chart-container', reportType === 'profit' ? 'chart-container--profit' : '']"></div>
      </el-card>
    </div>
    
    <!-- 数据表格 -->
    <div class="table-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>{{ tableTitle }}</span>
            <el-button type="primary" size="small" @click="exportData">导出数据</el-button>
          </div>
        </template>
        <el-table :data="tableData" stripe style="width: 100%" empty-text="暂无数据">
          <el-table-column prop="date" label="日期" v-if="reportType === 'sales'" />
          <el-table-column prop="productName" label="商品名称" v-if="reportType === 'product_rank' || reportType === 'profit'" />
          <el-table-column v-if="reportType === 'inventory'" label="商品编号" width="100">
            <template #default="scope">{{ scope.row.id ?? scope.row.productId ?? '—' }}</template>
          </el-table-column>
          <el-table-column v-if="reportType === 'inventory'" label="商品名称" min-width="140" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.name ?? scope.row.productName ?? '—' }}</template>
          </el-table-column>
          <el-table-column v-if="reportType === 'inventory'" label="分类" min-width="100" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.category_name ?? scope.row.categoryName ?? '—' }}</template>
          </el-table-column>
          <el-table-column prop="salesAmount" label="销售额" v-if="reportType === 'sales' || reportType === 'product_rank'" />
          <el-table-column prop="orderCount" label="订单数" v-if="reportType === 'sales'" />
          <el-table-column v-if="reportType === 'inventory'" label="库存数量" width="110" align="right">
            <template #default="scope">{{ scope.row.quantity ?? scope.row.stockQuantity ?? '—' }}</template>
          </el-table-column>
          <el-table-column v-if="reportType === 'inventory'" label="库存价值" width="120" align="right">
            <template #default="scope">{{ Number(scope.row.inventory_value ?? scope.row.inventoryValue ?? scope.row.stockValue ?? 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="salesVolume" label="销售量" v-if="reportType === 'product_rank' || reportType === 'profit'" />
          <el-table-column label="成本" v-if="reportType === 'profit'" align="right">
            <template #default="scope">{{ Number(scope.row.totalCost ?? 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="毛利" v-if="reportType === 'profit'" align="right">
            <template #default="scope">{{ Number(scope.row.profit ?? 0).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { reportAPI } from '../services/api'
import { ElMessage } from 'element-plus'
import { TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

function padDate(d) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function addDays(d, days) {
  const x = new Date(d)
  x.setDate(x.getDate() + days)
  return x
}

/** 最近一周：含今天共 7 个自然日（与仪表盘「近 7 日」一致），用本地日期避免 toISOString 时区错位 */
function defaultLastWeekRange() {
  const today = new Date()
  return [padDate(addDays(today, -6)), padDate(today)]
}

/** 后端 MyBatis Map 经 JSON 后 date 可能为字符串或数组 */
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

function enumerateDaysInclusive(startStr, endStr) {
  const dates = []
  const start = new Date(`${startStr}T12:00:00`)
  const end = new Date(`${endStr}T12:00:00`)
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) return dates
  for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
    dates.push(padDate(new Date(d)))
  }
  return dates
}

/** 按日期范围补全：无订单的日期销售额、订单数为 0，图表 X 轴连续 */
function buildFilledSalesTrendRows(rawRows, startDate, endDate) {
  if (!startDate || !endDate) {
    return Array.isArray(rawRows) ? [...rawRows] : []
  }
  const byDay = new Map()
  for (const row of rawRows || []) {
    let label = normalizeTrendLabel(row)
    if (!label) continue
    if (label.length > 10) label = label.slice(0, 10)
    byDay.set(label, {
      salesAmount: Number(row.salesAmount ?? row.sales_amount) || 0,
      orderCount: Number(row.orderCount ?? row.order_count) || 0
    })
  }
  const days = enumerateDaysInclusive(startDate, endDate)
  return days.map((date) => ({
    date,
    period: date,
    salesAmount: byDay.get(date)?.salesAmount ?? 0,
    orderCount: byDay.get(date)?.orderCount ?? 0
  }))
}

const route = useRoute()
const ALLOWED_TABS = ['sales', 'inventory', 'product_rank', 'profit']

// 响应式数据：默认最近一周（本地日期）；具体 tab 由路由 meta.reportTab 同步
const dateRange = ref(defaultLastWeekRange())
const reportType = ref('sales')
const chartRef = ref(null)
let chartInstance = null

// 销售报表数据
const totalSales = ref(0)
const totalOrders = ref(0)
const averageOrderValue = ref(0)
const salesGrowth = ref(0)
const orderGrowth = ref(0)
const avgGrowth = ref(0)
const salesGrowthRate = ref(0)

// 库存报表数据
const totalInventoryValue = ref(0)
const totalInventoryCount = ref(0)
const lowInventoryCount = ref(0)
const inventoryTurnover = ref(0)

// 商品销售排行数据
const topProducts = ref([])
const topProductsSales = ref(0)
const topProductsRatio = ref(0)

// 表格数据
const tableData = ref([])

/** 毛利报表：按表格行汇总（与后端 order 已完成+已付一致） */
const profitSummary = computed(() => {
  if (reportType.value !== 'profit') return { sales: 0, cost: 0, profit: 0 }
  let sales = 0
  let cost = 0
  let profit = 0
  for (const r of tableData.value || []) {
    sales += Number(r.salesAmount) || 0
    cost += Number(r.totalCost) || 0
    profit += Number(r.profit) || 0
  }
  return { sales, cost, profit }
})

// 计算属性
const reportTitle = computed(() => {
  const titles = {
    sales: '销售趋势图',
    inventory: '库存分布',
    product_rank: '商品销售排行',
    profit: '各商品毛利贡献（条形图）'
  }
  return titles[reportType.value] || '报表分析'
})

/** 页头副标题：与所选报表类型一致 */
const reportPageSubtitle = computed(() => {
  const m = {
    sales: '销售报表',
    inventory: '库存报表',
    product_rank: '商品销售排行',
    profit: '毛利分析'
  }
  return m[reportType.value] || '数据分析'
})

const tableTitle = computed(() => {
  const titles = {
    sales: '销售明细',
    inventory: '库存明细',
    product_rank: '商品销售排行明细',
    profit: '毛利明细（按商品）'
  }
  return titles[reportType.value] || '数据明细'
})

const dateRangeLabel = computed(() => {
  const r = dateRange.value
  if (!Array.isArray(r) || r.length < 2 || !r[0] || !r[1]) return ''
  return `${r[0]} 至 ${r[1]}`
})

/** 无数据时的说明（销售/毛利/排行依赖日期与订单状态） */
const reportEmptyHint = computed(() => {
  const t = reportType.value
  const rangeText = dateRangeLabel.value || '当前所选日期'
  if (t === 'profit' || t === 'product_rank') {
    if (!tableData.value.length) {
      return `${rangeText} 内无数据。毛利与排行仅统计「已完成且已付款」的订单，可尝试扩大范围或到「销售记录」核对订单状态。`
    }
  }
  if (t === 'sales') {
    if (totalSales.value === 0 && totalOrders.value === 0) {
      return `${rangeText} 内无销售汇总。请确认该时段是否有订单，或调整日期范围。`
    }
  }
  if (t === 'inventory' && !tableData.value.length) {
    return '暂无库存数据。请确认是否已维护商品与库存。'
  }
  return null
})

function applyQuickDateRange(mode) {
  const today = new Date()
  if (mode === 'last7') {
    dateRange.value = defaultLastWeekRange()
  } else if (mode === 'thisMonth') {
    const start = new Date(today.getFullYear(), today.getMonth(), 1)
    dateRange.value = [padDate(start), padDate(today)]
  } else if (mode === 'prevMonth') {
    const firstThis = new Date(today.getFullYear(), today.getMonth(), 1)
    const lastPrev = new Date(firstThis.getTime() - 86400000)
    const firstPrev = new Date(lastPrev.getFullYear(), lastPrev.getMonth(), 1)
    dateRange.value = [padDate(firstPrev), padDate(lastPrev)]
  }
  refreshData()
}

/** 库存报表行：统一后端 snake_case 与表格字段 */
function normalizeInventoryReportRow(row) {
  if (!row || typeof row !== 'object') return row
  const invVal = row.inventory_value ?? row.inventoryValue
  return {
    ...row,
    categoryName: row.category_name ?? row.categoryName,
    stockValue: invVal != null ? Number(invVal) : undefined,
    productName: row.name ?? row.productName,
    stockQuantity: row.quantity ?? row.stockQuantity
  }
}

/** 按分类汇总库存金额，供饼图使用 */
function aggregateInventoryByCategory(rows) {
  const map = new Map()
  for (const row of rows || []) {
    const name = row.category_name ?? row.categoryName ?? '未分类'
    const val = Number(row.inventory_value ?? row.inventoryValue ?? row.stockValue) || 0
    map.set(name, (map.get(name) || 0) + val)
  }
  return Array.from(map.entries()).map(([categoryName, stockValue]) => ({ categoryName, stockValue }))
}

const handleChartResize = () => {
  chartInstance?.resize()
}

// 初始化图表
const initChart = () => {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)
    window.addEventListener('resize', handleChartResize)
  }
}

// 渲染图表
const renderChart = () => {
  if (!chartInstance) {
    console.warn('图表实例尚未初始化，跳过渲染');
    return;
  }
  
  let option = {};
  const safeTableData = tableData.value || [];
  
  switch (reportType.value) {
    case 'sales':
      // 销售趋势图
      option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            crossStyle: {
              color: '#999'
            }
          }
        },
        legend: {
          data: ['销售额', '订单数']
        },
        xAxis: [
          {
            type: 'category',
            data:
              safeTableData.length > 0
                ? safeTableData.map((item) => item.date || item.month || item.period || '')
                : ['暂无数据'],
            axisPointer: {
              type: 'shadow'
            },
            axisLabel: {
              rotate: safeTableData.length > 5 ? 30 : 0
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            name: '销售额',
            min: 0,
            axisLabel: {
              formatter: '¥{value}'
            }
          },
          {
            type: 'value',
            name: '订单数',
            min: 0,
            axisLabel: {
              formatter: '{value}单'
            }
          }
        ],
        series: [
          {
            name: '销售额',
            type: 'bar',
            data: safeTableData.length > 0 ? safeTableData.map(item => item.salesAmount || 0) : [0],
            itemStyle: {
              color: '#409EFF'
            }
          },
          {
            name: '订单数',
            type: 'line',
            yAxisIndex: 1,
            data: safeTableData.length > 0 ? safeTableData.map(item => item.orderCount || 0) : [0],
            itemStyle: {
              color: '#67C23A'
            }
          }
        ]
      }
      break;
    case 'inventory': {
      // 按分类汇总库存金额（多商品同属一类时合并）
      const validInventoryData = aggregateInventoryByCategory(safeTableData)
      
      option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 10,
          data: validInventoryData.length > 0 ? validInventoryData.map(item => item.categoryName) : ['暂无数据']
        },
        series: [
          {
            name: '库存分布',
            type: 'pie',
            radius: '50%',
            center: ['50%', '60%'],
            data: validInventoryData.length > 0 ? validInventoryData.map(item => ({
              value: item.stockValue,
              name: item.categoryName
            })) : [{value: 1, name: '暂无数据'}],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      break
    }
    case 'product_rank':
      // 商品销售排行柱状图（兼容 product_name / productName）
      const validProductData = safeTableData.filter((item) => {
        const n = item.product_name ?? item.productName
        return n && item.salesAmount !== undefined
      })
      const rankLabel = (r) => r.product_name ?? r.productName ?? ''

      option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          data: ['销售额']
        },
        xAxis: {
          type: 'category',
          data: validProductData.length > 0 ? validProductData.map((item) => rankLabel(item)) : ['暂无数据'],
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value',
          name: '销售额',
          axisLabel: {
            formatter: '¥{value}'
          }
        },
        series: [
          {
            name: '销售额',
            type: 'bar',
            data: validProductData.length > 0 ? validProductData.map(item => item.salesAmount) : [0],
            itemStyle: {
              color: function(params) {
                // 前3名使用不同颜色
                const colors = ['#F56C6C', '#E6A23C', '#67C23A'];
                return params.dataIndex < 3 ? colors[params.dataIndex] : '#409EFF';
              }
            }
          }
        ]
      }
      break
    case 'profit': {
      const rows = safeTableData
        .map((item) => normalizeProfitRow(item))
        .filter((item) => item.productName)
      const labels = rows.map((r) => r.productName)
      const profits = rows.map((r) => Number(r.profit) || 0)
      option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: (params) => {
            const p = Array.isArray(params) ? params[0] : params
            const idx = p?.dataIndex ?? 0
            const row = rows[idx]
            if (!row) return ''
            return `${row.productName}<br/>毛利: ¥${Number(row.profit).toFixed(2)}<br/>销售收入: ¥${Number(row.salesAmount).toFixed(2)}<br/>成本: ¥${Number(row.totalCost).toFixed(2)}`
          }
        },
        legend: {
          data: ['毛利']
        },
        grid: { left: '3%', right: '8%', bottom: '3%', top: '3%', containLabel: true },
        xAxis: {
          type: 'value',
          name: '毛利(元)',
          axisLabel: { formatter: (v) => `¥${v}` }
        },
        yAxis: {
          type: 'category',
          data: labels.length ? labels : ['暂无数据'],
          inverse: true,
          axisLabel: { width: 120, overflow: 'truncate' }
        },
        series: [
          {
            name: '毛利',
            type: 'bar',
            data: labels.length ? profits : [0],
            itemStyle: {
              color: (params) => {
                const v = params.value
                return v >= 0 ? '#67C23A' : '#F56C6C'
              },
              borderRadius: [0, 4, 4, 0]
            }
          }
        ]
      }
      break
    }
  }

  // 确保在DOM更新后调用setOption
  setTimeout(() => {
    if (chartInstance && chartInstance.setOption) {
      chartInstance.setOption(option);
    }
  }, 0);
}

// 获取销售报表数据
const getSalesReport = async () => {
  try {
    const [startDate, endDate] = Array.isArray(dateRange.value) ? dateRange.value : []
    const response = await reportAPI.getSalesReport({
      startDate,
      endDate
    })
    const data = response.data || response
    totalSales.value = data.totalSales || 0
    totalOrders.value = data.totalOrders || 0
    averageOrderValue.value = data.averageOrderValue || 0
    salesGrowth.value = data.salesGrowth || 0
    orderGrowth.value = data.orderGrowth || 0
    avgGrowth.value = data.avgGrowth || 0
    salesGrowthRate.value = data.salesGrowthRate || 0
    const rawTrend = data.trendData || data.list || []
    const [rangeStart, rangeEnd] = Array.isArray(dateRange.value) ? dateRange.value : []
    tableData.value = buildFilledSalesTrendRows(rawTrend, rangeStart, rangeEnd)
    renderChart()
  } catch (error) {
    console.error('获取销售报表失败:', error)
    console.error('错误详情:', error.response)
    ElMessage.error('获取销售报表失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

// 获取库存报表数据
const getInventoryReport = async () => {
  try {
    const response = await reportAPI.getInventoryReport()
    const data = response.data || response
    totalInventoryValue.value = data.totalInventoryValue || 0
    totalInventoryCount.value = data.totalInventoryCount || 0
    lowInventoryCount.value = data.lowInventoryCount || 0
    inventoryTurnover.value = data.inventoryTurnover || 0
    const raw = data.inventoryList || data.list || []
    tableData.value = raw.map(normalizeInventoryReportRow)
    renderChart()
  } catch (error) {
    console.error('获取库存报表失败:', error)
    console.error('错误详情:', error.response)
    ElMessage.error('获取库存报表失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

/** 后端 Map 多为 snake_case：product_name、total_quantity；统一为图表/表格用的字段 */
function normalizeProductRankRow(row) {
  if (!row || typeof row !== 'object') return row
  const name = row.product_name ?? row.productName ?? ''
  const vol = row.total_quantity ?? row.salesVolume ?? row.totalQuantity
  return {
    ...row,
    productName: name,
    salesVolume: vol != null ? Number(vol) : undefined
  }
}

/** 毛利报表：兼容 product_name / salesVolume 等 */
function normalizeProfitRow(row) {
  if (!row || typeof row !== 'object') return {}
  const name = row.product_name ?? row.productName ?? ''
  return {
    ...row,
    productName: name,
    salesVolume: Number(row.salesVolume ?? row.sales_volume) || 0,
    salesAmount: Number(row.salesAmount ?? row.sales_amount) || 0,
    totalCost: Number(row.totalCost ?? row.total_cost) || 0,
    profit: Number(row.profit) || 0
  }
}

// 获取商品销售排行数据
const getProductRankReport = async () => {
  try {
    const [startDate, endDate] = Array.isArray(dateRange.value) ? dateRange.value : []
    const response = await reportAPI.getProductSalesRank({ startDate, endDate })
    const data = response.data || response
    const raw = data.rankList || data.list || []
    const list = raw.map(normalizeProductRankRow)
    topProducts.value = list
    topProductsSales.value = data.totalSales || data.salesAmount || 0
    topProductsRatio.value = data.salesRatio || 0
    tableData.value = [...list]
    renderChart()
  } catch (error) {
    console.error('获取商品销售排行失败:', error)
    console.error('错误详情:', error.response)
    ElMessage.error('获取商品销售排行失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

// 获取毛利报表（已完成且已付款订单，按商品汇总）
const getProfitReport = async () => {
  try {
    const [startDate, endDate] = Array.isArray(dateRange.value) ? dateRange.value : []
    const response = await reportAPI.getProfitReport({ startDate, endDate })
    const raw = Array.isArray(response) ? response : response?.data ?? response?.list ?? []
    const list = Array.isArray(raw) ? raw : []
    tableData.value = list.map(normalizeProfitRow)
    renderChart()
  } catch (error) {
    console.error('获取毛利报表失败:', error)
    ElMessage.error('获取毛利报表失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

// 刷新数据
const refreshData = () => {
  switch (reportType.value) {
    case 'sales':
      getSalesReport()
      break
    case 'inventory':
      getInventoryReport()
      break
    case 'product_rank':
      getProductRankReport()
      break
    case 'profit':
      getProfitReport()
      break
  }
}

// 日期范围变化处理
const handleDateChange = () => {
  refreshData()
}

function applyReportTabFromRoute() {
  const tab = [...route.matched]
    .reverse()
    .find((r) => r.meta?.reportTab)?.meta?.reportTab
  if (typeof tab === 'string' && ALLOWED_TABS.includes(tab)) {
    reportType.value = tab
  }
}

function escapeCsvCell(val) {
  const s = String(val ?? '')
  if (/[",\r\n]/.test(s)) return `"${s.replace(/"/g, '""')}"`
  return s
}

function downloadCsv(filename, content) {
  const blob = new Blob(['\uFEFF' + content], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = filename
  a.click()
  URL.revokeObjectURL(a.href)
}

// 导出当前表格为 CSV（UTF-8 BOM，Excel 可正确打开中文）
const exportData = () => {
  const t = reportType.value
  const suffix = Array.isArray(dateRange.value) && dateRange.value[0] ? `_${dateRange.value[0]}_${dateRange.value[1]}` : ''
  let lines = []
  if (t === 'sales') {
    lines.push(['日期', '销售额', '订单数'].map(escapeCsvCell).join(','))
    for (const row of tableData.value) {
      lines.push([row.date, row.salesAmount, row.orderCount].map(escapeCsvCell).join(','))
    }
  } else if (t === 'inventory') {
    lines.push(['商品编号', '商品名称', '分类', '库存数量', '库存价值'].map(escapeCsvCell).join(','))
    for (const row of tableData.value) {
      const id = row.id ?? row.productId ?? ''
      const name = row.name ?? row.productName ?? ''
      const cat = row.category_name ?? row.categoryName ?? ''
      const qty = row.quantity ?? row.stockQuantity ?? ''
      const val = row.inventory_value ?? row.inventoryValue ?? row.stockValue ?? ''
      lines.push([id, name, cat, qty, val].map(escapeCsvCell).join(','))
    }
  } else if (t === 'product_rank') {
    lines.push(['商品名称', '销售额', '销售量'].map(escapeCsvCell).join(','))
    for (const row of tableData.value) {
      const name = row.product_name ?? row.productName ?? ''
      lines.push([name, row.salesAmount, row.salesVolume ?? row.total_quantity].map(escapeCsvCell).join(','))
    }
  } else if (t === 'profit') {
    lines.push(['商品名称', '销售量', '销售收入', '成本', '毛利'].map(escapeCsvCell).join(','))
    for (const row of tableData.value) {
      const r = normalizeProfitRow(row)
      lines.push(
        [r.productName, r.salesVolume, r.salesAmount, r.totalCost, r.profit].map(escapeCsvCell).join(',')
      )
    }
  }
  if (!lines.length) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  downloadCsv(`报表_${t}${suffix}.csv`, lines.join('\r\n'))
  ElMessage.success('已导出 CSV')
}

onMounted(() => {
  initChart()
  applyReportTabFromRoute()
  refreshData()
})

watch(
  () => route.fullPath,
  () => {
    applyReportTabFromRoute()
    refreshData()
  }
)

onUnmounted(() => {
  window.removeEventListener('resize', handleChartResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.reports-container {
  padding: 20px;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-quick-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
}

.filter-quick-label {
  font-size: 13px;
  color: #606266;
  margin-right: 4px;
}

.filter-range-text {
  font-size: 13px;
  color: #909399;
  margin-left: 8px;
}

.filter-actions-col {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.filter-inventory-note {
  margin: 12px 0 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

.report-hint-alert {
  margin-bottom: 16px;
}

.stats-section {
  margin-bottom: 20px;
}

.stats-card {
  height: 100%;
}

.stats-content {
  text-align: center;
}

.stats-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stats-change {
  font-size: 12px;
}

.stats-change.positive {
  color: #67C23A;
}

.stats-change.negative {
  color: #F56C6C;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 400px;
}

.table-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container--profit {
  height: 480px;
}
</style>