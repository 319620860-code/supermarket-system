<template>
  <div class="sales-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><ShoppingCart /></el-icon>
        销售管理
      </h2>
      <div class="page-header-actions">
        <el-button type="primary" @click="showCreateOrderDialog">
          <el-icon><Plus /></el-icon> 创建销售订单
        </el-button>
      </div>
    </div>
    
    <!-- 搜索和筛选区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNumber" placeholder="请输入订单号" clearable></el-input>
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="收银员">
          <el-select
            v-model="searchForm.cashierId"
            placeholder="按收银员筛选"
            clearable
            filterable
            style="width: 200px"
          >
            <el-option
              v-for="u in cashierUserOptions"
              :key="u.id"
              :label="(u.realName || u.username) + ' (ID:' + u.id + ')'"
              :value="String(u.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.orderStatus" placeholder="订单流转状态" clearable style="width: 160px">
            <el-option label="已创建" value="CREATED"></el-option>
            <el-option label="已完成" value="COMPLETED"></el-option>
            <el-option label="已取消" value="CANCELLED"></el-option>
            <el-option label="已退款" value="REFUNDED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.paymentStatus" placeholder="是否已付款" clearable style="width: 140px">
            <el-option label="未支付" value="UNPAID"></el-option>
            <el-option label="已支付" value="PAID"></el-option>
            <el-option label="已退款" value="REFUNDED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="订单日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 订单列表 -->
    <el-card class="table-card">
      <el-table v-loading="loading" empty-text="暂无数据"
        :data="ordersList"
        style="width: 100%"
        stripe
        border
        @row-dblclick="handleOrderDetail"
      >
        <el-table-column prop="orderNumber" label="订单号" min-width="180"></el-table-column>
        <el-table-column label="客户名称" min-width="120">
          <template #default="scope">
            {{ scope.row.customerName !== undefined && scope.row.customerName !== null ? scope.row.customerName : (scope.row.customer_name !== undefined && scope.row.customer_name !== null ? scope.row.customer_name : '-') }}
          </template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="实付金额" width="120" :formatter="priceFormatter"></el-table-column>
        <el-table-column label="订单状态" width="110">
          <template #default="scope">
            <el-tag :type="orderStatusTagType(scope.row)">
              {{ orderStatusLabel(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付状态" width="100">
          <template #default="scope">
            <el-tag :type="paymentStatusTagType(scope.row)">
              {{ paymentStatusLabel(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="120">
          <template #default="scope">
            {{ paymentMethodLabel(scope.row.paymentMethod) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="订单日期" width="180"></el-table-column>
        <el-table-column prop="cashierName" label="收银员" width="120"></el-table-column>
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="scope">
            <div class="sales-table-actions">
              <el-button type="primary" size="small" @click="handleOrderDetail(scope.row)">
                <el-icon><Document /></el-icon> 详情
              </el-button>
              <el-button
                v-if="pickPaymentStatus(scope.row) === 'UNPAID' && pickOrderStatus(scope.row) === 'CREATED'"
                type="success"
                size="small"
                @click="handleOrderPay(scope.row)"
              >
                <el-icon><CircleCheck /></el-icon> 支付
              </el-button>
              <el-button
                v-if="pickOrderStatus(scope.row) === 'CREATED'"
                type="danger"
                size="small"
                @click="handleOrderCancel(scope.row)"
              >
                <el-icon><CircleClose /></el-icon> 取消
              </el-button>
              <el-button
                v-if="pickPaymentStatus(scope.row) === 'PAID' && pickOrderStatus(scope.row) === 'COMPLETED'"
                type="warning"
                size="small"
                @click="handleOrderRefund(scope.row)"
              >
                <el-icon><RefreshRight /></el-icon> 退款
              </el-button>
              <el-button
                v-if="pickPaymentStatus(scope.row) === 'PAID' && pickOrderStatus(scope.row) === 'COMPLETED'"
                type="info"
                size="small"
                @click="handlePrintReceipt(scope.row)"
              >
                <el-icon><Printer /></el-icon> 打印
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :total-text="'共'"
          :page-size-text="'条/页'"
          :jumper-text="'前往'"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 创建销售订单对话框 -->
    <el-dialog
      v-model="createOrderDialogVisible"
      title="创建销售订单"
      width="800px"
      :before-close="handleCreateOrderDialogClose"
    >
      <el-form
        ref="createOrderFormRef"
        :model="createOrderForm"
        label-position="top"
      >
        <el-form-item label="客户信息">
          <el-input
            v-model="createOrderForm.customerName"
            placeholder="请输入客户名称"
          ></el-input>
        </el-form-item>
        
        <!-- 订单商品列表 -->
        <el-form-item label="订单商品">
          <el-table
            :data="createOrderForm.items"
            style="width: 100%"
            stripe
            border
            empty-text="暂无数据"
          >
            <el-table-column prop="productId" label="商品编号" width="120"></el-table-column>
            <el-table-column prop="productName" label="商品名称" min-width="150"></el-table-column>
            <el-table-column prop="unitPrice" label="单价" width="100" :formatter="priceFormatter"></el-table-column>
            <el-table-column prop="quantity" label="数量" width="100">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.quantity"
                  :min="1"
                  :max="scope.row.maxStock"
                  @change="calculateTotal"
                ></el-input-number>
              </template>
            </el-table-column>
            <el-table-column prop="totalPrice" label="小计" width="120" :formatter="priceFormatter"></el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button
                  type="danger"
                  size="small"
                  @click="removeOrderItem(scope.$index)"
                >
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 添加商品按钮 -->
          <div class="add-product-button">
            <el-button type="primary" @click="showAddProductDialog">
              <el-icon><Plus /></el-icon> 添加商品
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item label="支付方式">
          <el-radio-group v-model="createOrderForm.paymentMethod">
            <el-radio label="cash">现金</el-radio>
            <el-radio label="card">银行卡</el-radio>
            <el-radio label="alipay">支付宝</el-radio>
            <el-radio label="wechat">微信支付</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="整单折扣（元）">
          <el-input-number
            v-model="createOrderForm.discountAmount"
            :min="0"
            :step="0.01"
            :precision="2"
            style="width: 200px"
            @change="calculateTotal"
          />
          <span class="form-hint">从商品小计中扣减，实付 = 小计 − 折扣</span>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="createOrderForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入订单备注"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="应收金额">
          <div class="total-amount">
            <div><span class="sub-label">商品小计：</span>¥{{ createOrderForm.subtotal.toFixed(2) }}</div>
            <div class="total-line">
              <span>实付金额：</span>
              <span class="amount-value">¥{{ createOrderForm.totalAmount.toFixed(2) }}</span>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer-row">
          <div class="footer-left">
            <el-button @click="savePendingOrder">挂单（暂存本机）</el-button>
            <el-button @click="restorePendingOrder">取回挂单</el-button>
          </div>
          <div>
            <el-button @click="handleCreateOrderDialogClose">取消</el-button>
            <el-button type="primary" @click="handleCreateOrder">创建订单</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
    
    <!-- 添加商品对话框 -->
    <el-dialog
      v-model="addProductDialogVisible"
      title="添加商品"
      width="800px"
      :before-close="handleAddProductDialogClose"
    >
      <div class="add-product-container">
        <el-input
          v-model="barcodeInput"
          placeholder="商品条码：扫描商品条码，回车加入购物车"
          clearable
          class="barcode-input"
          @keyup.enter="lookupProductByBarcode"
        >
          <template #prepend>商品条码</template>
        </el-input>
        <!-- 商品搜索 -->
        <el-input
          v-model="productSearchKeyword"
          placeholder="请输入商品名称或编号"
          clearable
          class="product-search-input"
          @input="handleProductSearch"
        >
          <template #append>
            <el-button type="primary" @click="handleProductSearch">
              <el-icon><Search /></el-icon> 搜索
            </el-button>
          </template>
        </el-input>
        
        <!-- 商品列表 -->
        <el-table
          v-loading="productSearchLoading"
          :data="productSearchResults"
          style="width: 100%"
          stripe
          border
          height="400"
          empty-text="暂无数据"
          @row-dblclick="handleSelectProduct"
          @selection-change="handleProductSelectionChange"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="id" label="商品编号" width="120"></el-table-column>
          <el-table-column prop="name" label="商品名称" min-width="200"></el-table-column>
          <el-table-column prop="categoryName" label="分类" width="120"></el-table-column>
          <el-table-column prop="sellingPrice" label="售价" width="100" :formatter="priceFormatter"></el-table-column>
          <el-table-column prop="quantity" label="库存" width="100"></el-table-column>
          <el-table-column prop="unit" label="单位" width="80"></el-table-column>
        </el-table>
        
        <!-- 选中商品列表 -->
        <div class="selected-products">
          <h4>已选商品</h4>
          <el-tag
            v-for="item in selectedProducts"
            :key="item.productId"
            closable
            @close="removeSelectedProduct(item.productId)"
          >
            {{ item.productName }} x {{ item.quantity }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="handleAddProductDialogClose">取消</el-button>
        <el-button type="primary" @click="confirmAddProducts">确定添加</el-button>
      </template>
    </el-dialog>
    
    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="orderDetailDialogVisible"
      title="订单详情"
      width="800px"
      :before-close="handleOrderDetailDialogClose"
    >
      <div class="order-detail">
        <div class="detail-header">
          <div class="order-info">
            <h3>订单号：{{ orderDetail.orderNumber }}</h3>
            <div class="order-meta">
              <span>订单状态：<el-tag :type="orderStatusTagType(orderDetail)">{{ orderStatusLabel(orderDetail) }}</el-tag></span>
              <span>支付状态：<el-tag :type="paymentStatusTagType(orderDetail)">{{ paymentStatusLabel(orderDetail) }}</el-tag></span>
              <span>创建时间：{{ orderDetail.createTime }}</span>
              <span>支付方式：{{ paymentMethodLabel(orderDetail.paymentMethod) }}</span>
            </div>
          </div>
          <div class="customer-info">
            <h4>客户信息</h4>
            <p>{{ orderDetail.customerName !== undefined && orderDetail.customerName !== null ? orderDetail.customerName : (orderDetail.customer_name !== undefined && orderDetail.customer_name !== null ? orderDetail.customer_name : '无') }}</p>
          </div>
        </div>
        
        <div class="detail-body">
          <h4>商品清单</h4>
          <el-table
            :data="orderDetail.orderItems"
            style="width: 100%"
            stripe
            border
            empty-text="暂无数据"
          >
            <el-table-column prop="productId" label="商品编号" width="120"></el-table-column>
            <el-table-column prop="productName" label="商品名称" min-width="180"></el-table-column>
            <el-table-column prop="price" label="单价" width="100" :formatter="priceFormatter"></el-table-column>
            <el-table-column prop="quantity" label="数量" width="100"></el-table-column>
            <el-table-column prop="amount" label="小计" width="120" :formatter="priceFormatter"></el-table-column>
          </el-table>
          
          <div class="order-summary">
            <div class="summary-item">
              <span>商品总价：</span>
              <span>¥{{ Number(orderDetail.totalAmount || 0).toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span>优惠金额：</span>
              <span>¥{{ orderDetail.discountAmount != null ? Number(orderDetail.discountAmount).toFixed(2) : '0.00' }}</span>
            </div>
            <div class="summary-item total">
              <span>实付金额：</span>
              <span>¥{{ Number(orderDetail.actualAmount || orderDetail.totalAmount || 0).toFixed(2) }}</span>
            </div>
          </div>
          
          <div class="order-remark">
            <h4>备注</h4>
            <p>{{ orderDetail.remark || '无' }}</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="handleOrderDetailDialogClose">关闭</el-button>
      </template>
    </el-dialog>
    
    <!-- 支付对话框 -->
    <el-dialog
      v-model="paymentDialogVisible"
      title="订单支付"
      width="500px"
      :before-close="handlePaymentDialogClose"
    >
      <el-form
        ref="paymentFormRef"
        :model="paymentForm"
        label-position="top"
      >
        <el-form-item label="订单信息">
          <div class="payment-order-info">
            <div>订单号：{{ paymentOrder.orderNumber }}</div>
            <div>客户名称：{{ paymentOrder.customerName !== undefined && paymentOrder.customerName !== null ? paymentOrder.customerName : (paymentOrder.customer_name !== undefined && paymentOrder.customer_name !== null ? paymentOrder.customer_name : '无') }}</div>
            <div>实付金额：<span class="payment-amount">¥{{ Number(paymentOrder.actualAmount || paymentOrder.totalAmount || 0).toFixed(2) }}</span></div>
          </div>
        </el-form-item>
        
        <el-form-item label="支付金额" prop="amount">
          <el-input-number
            v-model="paymentForm.amount"
            :min="0"
            :step="0.01"
            :precision="2"
            style="width: 100%"
            placeholder="请输入支付金额"
          />
        </el-form-item>
        
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-radio-group v-model="paymentForm.paymentMethod">
            <el-radio label="cash">现金</el-radio>
            <el-radio label="card">银行卡</el-radio>
            <el-radio label="alipay">支付宝</el-radio>
            <el-radio label="wechat">微信支付</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item v-if="paymentForm.paymentMethod === 'cash'" label="现金支付">
          <el-input
            v-model="paymentForm.cashAmount"
            type="number"
            placeholder="请输入收到的现金金额"
            step="0.01"
          />
          <div v-if="paymentForm.cashAmount > 0" class="change-amount">
            找零：¥{{ (paymentForm.cashAmount - paymentForm.amount).toFixed(2) }}
          </div>
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input
            v-model="paymentForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入支付备注"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handlePaymentDialogClose">取消</el-button>
        <el-button type="primary" @click="handlePaymentSubmit" :loading="paymentLoading">确认支付</el-button>
      </template>
    </el-dialog>

    <!-- 小票打印对话框 -->
    <el-dialog
      v-model="receiptDialogVisible"
      title="打印小票"
      width="400px"
    >
      <div class="receipt-preview">
        <div class="receipt-header">
          <h3>超市信息管理系统</h3>
          <p>销售小票</p>
          <p class="receipt-date">{{ receiptData.orderDate }}</p>
        </div>
        <div class="receipt-info">
          <p>订单号：{{ receiptData.orderNumber }}</p>
          <p>客户：{{ receiptData.customerName }}</p>
          <p>收银员：{{ receiptData.cashierName }}</p>
        </div>
        <div class="receipt-items">
          <table>
            <thead>
              <tr>
                <th>商品</th>
                <th>数量</th>
                <th>单价</th>
                <th>小计</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in receiptData.items" :key="item.productId">
                <td>{{ item.productName }}</td>
                <td>{{ item.quantity }}</td>
                <td>¥{{ Number(item.price).toFixed(2) }}</td>
                <td>¥{{ Number(item.amount).toFixed(2) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="receipt-total">
          <p>商品总计：¥{{ Number(receiptData.totalAmount).toFixed(2) }}</p>
          <p v-if="receiptData.discountAmount > 0">优惠：-¥{{ Number(receiptData.discountAmount).toFixed(2) }}</p>
          <p class="total-amount">实付：¥{{ Number(receiptData.actualAmount).toFixed(2) }}</p>
          <p>支付方式：{{ receiptData.paymentMethod }}</p>
        </div>
        <div class="receipt-footer">
          <p>感谢您的惠顾！</p>
          <p>欢迎下次光临</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="receiptDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doPrintReceipt">打印</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Document, CircleCheck, CircleClose, RefreshRight,
  Delete, Search, ShoppingCart, Printer
} from '@element-plus/icons-vue'
import { salesAPI, productAPI, userAPI, authAPI, unwrapPagePayload } from '../../services/api'

// 页面加载状态
const loading = ref(false)
const productSearchLoading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  orderNumber: '',
  customerName: '',
  cashierId: '',
  orderStatus: '',
  paymentStatus: '',
  dateRange: []
})

/** 收银员下拉（与订单 cashier_id 一致，为 sys_user.id 字符串）；无权限时列表为空可改用订单号筛选 */
const cashierUserOptions = ref([])

// 订单列表数据
const ordersList = ref([])

// 创建销售订单对话框
const createOrderDialogVisible = ref(false)
const createOrderFormRef = ref()
const PENDING_ORDER_KEY = 'supermarket_sales_pending_cart'

const createOrderForm = reactive({
  customerName: '',
  items: [],
  paymentMethod: 'cash',
  remark: '',
  discountAmount: 0,
  subtotal: 0,
  totalAmount: 0
})

const barcodeInput = ref('')

// 添加商品对话框
const addProductDialogVisible = ref(false)
const productSearchKeyword = ref('')
const productSearchResults = ref([])
const selectedProducts = ref([])
const productTableSelection = ref([])

// 订单详情对话框
const orderDetailDialogVisible = ref(false)
const orderDetail = reactive({ orderItems: [] })

// 支付对话框
const paymentDialogVisible = ref(false)
const paymentFormRef = ref()
const paymentForm = reactive({
  amount: 0,
  paymentMethod: 'cash',
  cashAmount: 0,
  remark: ''
})
const paymentOrder = reactive({})
const paymentLoading = ref(false)

// 当前登录用户信息
const currentUser = ref(null)

async function loadCashierUsers() {
  try {
    const res = await userAPI.getUsers({ page: 1, size: 500 })
    const page = unwrapPagePayload(res)
    cashierUserOptions.value = page.records || []
  } catch {
    cashierUserOptions.value = []
  }
}

// 获取当前登录用户信息
const loadCurrentUser = async () => {
  try {
    const user = await authAPI.getCurrentUser()
    console.log('获取到当前用户信息:', user)
    currentUser.value = user
  } catch (error) {
    console.error('获取当前用户信息失败:', error)
  }
}

onMounted(() => {
  loadCashierUsers()
  loadCurrentUser()
  getOrdersList()
})

// 获取订单列表
const getOrdersList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchForm.orderNumber?.trim() || undefined,
      customerName: searchForm.customerName?.trim() || undefined,
      cashierId: searchForm.cashierId || undefined,
      orderStatus: searchForm.orderStatus || undefined,
      paymentStatus: searchForm.paymentStatus || undefined,
      startDate: searchForm.dateRange?.[0],
      endDate: searchForm.dateRange?.[1]
    }
    const response = await salesAPI.getOrders(params)
    const page = unwrapPagePayload(response)
    ordersList.value = page.records
    total.value = page.total
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索订单
const handleSearch = () => {
  currentPage.value = 1
  getOrdersList()
}

// 重置搜索条件
const resetSearch = () => {
  Object.assign(searchForm, {
    orderNumber: '',
    customerName: '',
    cashierId: '',
    orderStatus: '',
    paymentStatus: '',
    dateRange: []
  })
  currentPage.value = 1
  getOrdersList()
}

// 页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  getOrdersList()
}

// 每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  getOrdersList()
}

// 显示创建订单对话框
const showCreateOrderDialog = () => {
  createOrderDialogVisible.value = true
}

// 关闭创建订单对话框
const handleCreateOrderDialogClose = () => {
  createOrderDialogVisible.value = false
  createOrderFormRef.value?.resetFields()
  Object.assign(createOrderForm, {
    customerName: '',
    items: [],
    paymentMethod: 'cash',
    remark: '',
    discountAmount: 0,
    subtotal: 0,
    totalAmount: 0
  })
}

const savePendingOrder = () => {
  try {
    const payload = {
      customerName: createOrderForm.customerName,
      items: JSON.parse(JSON.stringify(createOrderForm.items)),
      discountAmount: createOrderForm.discountAmount,
      paymentMethod: createOrderForm.paymentMethod,
      remark: createOrderForm.remark
    }
    localStorage.setItem(PENDING_ORDER_KEY, JSON.stringify(payload))
    ElMessage.success('挂单已保存到本机，可随时取回')
  } catch (e) {
    ElMessage.error('挂单失败')
  }
}

const restorePendingOrder = () => {
  const raw = localStorage.getItem(PENDING_ORDER_KEY)
  if (!raw) {
    ElMessage.info('暂无挂单')
    return
  }
  try {
    const d = JSON.parse(raw)
    createOrderForm.customerName = d.customerName || ''
    createOrderForm.items = Array.isArray(d.items) ? d.items : []
    createOrderForm.discountAmount = Number(d.discountAmount) || 0
    createOrderForm.paymentMethod = d.paymentMethod || 'cash'
    createOrderForm.remark = d.remark || ''
    calculateTotal()
    ElMessage.success('已取回挂单')
  } catch (e) {
    ElMessage.error('挂单数据无效')
  }
}

const lookupProductByBarcode = async () => {
  const code = barcodeInput.value?.trim()
  if (!code) return
  try {
    const p = await productAPI.getProductByBarcode(code, { sellableOnly: true })
    if (!p || !p.id) {
      ElMessage.warning('未找到可售商品：请确认商品条码正确，且商品已上架、所属分类未停用')
      return
    }
    handleSelectProduct(p)
    barcodeInput.value = ''
    ElMessage.success(`已添加：${p.name}`)
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '条码查询失败')
  }
}

// 搜索商品
const handleProductSearch = async () => {
  if (productSearchKeyword.value) {
    productSearchLoading.value = true
    try {
      const response = await productAPI.getProducts({
        keyword: productSearchKeyword.value,
        page: 1,
        pageSize: 50,
        forSale: true
      })
      productSearchResults.value = unwrapPagePayload(response).records
    } catch (error) {
      console.error('搜索商品失败:', error)
    } finally {
      productSearchLoading.value = false
    }
  } else {
    productSearchResults.value = []
  }
}

// 显示添加商品对话框
const showAddProductDialog = () => {
  addProductDialogVisible.value = true
  productSearchResults.value = []
  selectedProducts.value = []
  productTableSelection.value = []
}

// 关闭添加商品对话框
const handleAddProductDialogClose = () => {
  addProductDialogVisible.value = false
  productSearchKeyword.value = ''
  productSearchResults.value = []
  selectedProducts.value = []
  productTableSelection.value = []
}

// 移除已选商品
const removeSelectedProduct = (productId) => {
  const index = selectedProducts.value.findIndex(item => item.productId === productId)
  if (index > -1) {
    selectedProducts.value.splice(index, 1)
  }
}

const handleProductSelectionChange = (rows) => {
  productTableSelection.value = rows
}

/** 双击一行快速加入 1 件 */
const handleSelectProduct = (row) => {
  const unitPrice = Number(row.sellingPrice ?? 0)
  const existing = createOrderForm.items.find(
    (i) => i.productId === row.id
  )
  if (existing) {
    existing.quantity += 1
    existing.totalPrice = existing.unitPrice * existing.quantity
  } else {
    createOrderForm.items.push({
      productId: row.id,
      productName: row.name,
      unitPrice,
      quantity: 1,
      specification: row.specification || '',
      unit: row.unit || '',
      totalPrice: unitPrice,
      maxStock: row.quantity
    })
  }
  calculateTotal()
}

// 确认添加商品（表格多选）
const confirmAddProducts = () => {
  const rows = productTableSelection.value.length ? productTableSelection.value : selectedProducts.value
  if (!rows.length) {
    ElMessage.warning('请先勾选商品或使用双击添加')
    return
  }
  rows.forEach((row) => {
    const unitPrice = Number(row.sellingPrice ?? 0)
    const existingItemIndex = createOrderForm.items.findIndex(
      (item) => item.productId === row.id
    )
    if (existingItemIndex > -1) {
      createOrderForm.items[existingItemIndex].quantity += 1
      createOrderForm.items[existingItemIndex].totalPrice =
        createOrderForm.items[existingItemIndex].unitPrice * createOrderForm.items[existingItemIndex].quantity
    } else {
      createOrderForm.items.push({
        productId: row.id,
        productName: row.name,
        unitPrice,
        quantity: 1,
        specification: row.specification || '',
        unit: row.unit || '',
        totalPrice: unitPrice,
        maxStock: row.quantity
      })
    }
  })
  calculateTotal()
  handleAddProductDialogClose()
}

// 移除订单商品
const removeOrderItem = (index) => {
  createOrderForm.items.splice(index, 1)
  calculateTotal()
}

// 计算订单总额（小计 − 整单折扣）
const calculateTotal = () => {
  const sub = createOrderForm.items.reduce((sum, item) => {
    item.totalPrice = item.unitPrice * item.quantity
    return sum + item.totalPrice
  }, 0)
  createOrderForm.subtotal = sub
  const disc = Math.max(0, Number(createOrderForm.discountAmount) || 0)
  createOrderForm.totalAmount = Math.max(0, sub - disc)
}

// 创建订单
const handleCreateOrder = async () => {
  try {
    if (createOrderForm.items.length === 0) {
      ElMessage.warning('请添加商品到订单')
      return
    }
    if (!createOrderForm.customerName || createOrderForm.customerName.trim() === '') {
      ElMessage.warning('请输入客户名称')
      return
    }
    const orderItems = createOrderForm.items.map((line) => ({
      productId: line.productId,
      productName: line.productName,
      specification: line.specification || '',
      unit: line.unit || '',
      unitPrice: line.unitPrice,
      price: line.unitPrice,
      quantity: line.quantity
    }))
    // 确保传递cashierId，使用当前登录用户的ID
    console.log('当前用户信息:', currentUser.value)
    const payload = {
      customerName: createOrderForm.customerName,
      paymentMethod: createOrderForm.paymentMethod,
      remark: createOrderForm.remark,
      discountAmount: Math.max(0, Number(createOrderForm.discountAmount) || 0),
      cashierId: currentUser.value?.id ? String(currentUser.value.id) : null,
      orderItems
    }
    console.log('创建订单的payload:', payload)
    const response = await salesAPI.createOrder(payload)
    ElMessage.success('订单创建成功')
    handleCreateOrderDialogClose()
    getOrdersList()
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '订单创建失败')
    } else {
      ElMessage.error('订单创建失败，请检查网络连接')
    }
  }
}

// 处理订单支付
const handleOrderPay = (order) => {
  // 填充支付订单信息
  Object.assign(paymentOrder, order)
  // 设置默认支付金额为订单实付金额
  paymentForm.amount = Number(order.actualAmount || order.totalAmount || 0)
  // 重置其他支付表单数据
  paymentForm.paymentMethod = 'cash'
  paymentForm.cashAmount = 0
  paymentForm.remark = ''
  // 显示支付对话框
  paymentDialogVisible.value = true
}

// 关闭支付对话框
const handlePaymentDialogClose = () => {
  paymentDialogVisible.value = false
  // 重置支付表单
  paymentFormRef.value?.resetFields()
  Object.assign(paymentForm, {
    amount: 0,
    paymentMethod: 'cash',
    cashAmount: 0,
    remark: ''
  })
  // 清空支付订单信息
  Object.assign(paymentOrder, {})
}

// 提交支付
const handlePaymentSubmit = async () => {
  try {
    // 验证支付金额
    if (paymentForm.amount <= 0) {
      ElMessage.warning('请输入有效的支付金额')
      return
    }
    
    // 验证现金支付金额
    if (paymentForm.paymentMethod === 'cash' && paymentForm.cashAmount < paymentForm.amount) {
      ElMessage.warning('收到的现金金额不足')
      return
    }
    
    paymentLoading.value = true
    
    // 调用支付接口
    console.log('支付订单ID:', paymentOrder.id)
    console.log('支付参数:', {
      amount: paymentForm.amount,
      paymentMethod: paymentForm.paymentMethod,
      remark: paymentForm.remark
    })
    
    const response = await salesAPI.payOrder(paymentOrder.id, {
      amount: paymentForm.amount,
      paymentMethod: paymentForm.paymentMethod,
      remark: paymentForm.remark
    })
    
    ElMessage.success('订单支付成功')
    handlePaymentDialogClose()
    getOrdersList()
  } catch (error) {
    console.error('支付失败:', error)
    if (error.response) {
      ElMessage.error(error.response.data?.message || error.response.data || '订单支付失败')
    } else {
      ElMessage.error('订单支付失败，请检查网络连接')
    }
  } finally {
    paymentLoading.value = false
  }
}

// 处理订单取消
const handleOrderCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await salesAPI.cancelOrder(order.id)
    ElMessage.success('订单取消成功')
    getOrdersList()
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    if (error.response) {
      ElMessage.error(error.response.data.message || '订单取消失败')
    } else {
      ElMessage.error('订单取消失败，请检查网络连接')
    }
  }
}

// 处理订单退款
const handleOrderRefund = (order) => {
  try {
    ElMessageBox.confirm('确定要退款该订单吗？', '订单退款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await salesAPI.refundOrder(order.id)
        ElMessage.success('退款成功')
        getOrdersList()
      } catch (error) {
        if (error.response) {
          ElMessage.error(error.response.data.message || '退款失败')
        } else {
          ElMessage.error('退款失败，请检查网络连接')
        }
      }
    })
  } catch (error) {
    // 取消退款
  }
}

// 查看订单详情
const handleOrderDetail = async (order) => {
  try {
    const detail = await salesAPI.getOrderDetail(order.id)
    const items = await salesAPI.getOrderItems(order.id)
    Object.assign(orderDetail, detail, { orderItems: items || [] })
    orderDetailDialogVisible.value = true
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败，请检查网络连接')
  }
}

// 关闭订单详情对话框
const handleOrderDetailDialogClose = () => {
  orderDetailDialogVisible.value = false
  Object.assign(orderDetail, { orderItems: [] })
}

/** 兼容后端 camelCase 与可能的 snake_case */
const pickOrderStatus = (row) => (row ? row.orderStatus ?? row.order_status : undefined)
const pickPaymentStatus = (row) => (row ? row.paymentStatus ?? row.payment_status : undefined)

/** 订单流转状态（与库字段 order_status 一致，单独一列展示） */
const orderStatusLabel = (row) => {
  if (!row) return '-'
  const os = pickOrderStatus(row)
  const map = { CREATED: '已创建', COMPLETED: '已完成', CANCELLED: '已取消', REFUNDED: '已退款' }
  return map[os] || os || '-'
}

/** 支付状态（与库字段 payment_status 一致，单独一列展示） */
const paymentStatusLabel = (row) => {
  if (!row) return '-'
  const ps = pickPaymentStatus(row)
  const map = { UNPAID: '未支付', PAID: '已支付', REFUNDED: '已退款' }
  return map[ps] || ps || '-'
}

const orderStatusTagType = (row) => {
  const os = pickOrderStatus(row)
  if (os === 'CREATED') return 'warning'
  if (os === 'COMPLETED') return 'success'
  if (os === 'CANCELLED') return 'info'
  if (os === 'REFUNDED') return 'danger'
  return ''
}

const paymentStatusTagType = (row) => {
  const ps = pickPaymentStatus(row)
  if (ps === 'UNPAID') return 'warning'
  if (ps === 'PAID') return 'success'
  if (ps === 'REFUNDED') return 'danger'
  return ''
}

// 支付方式中文映射
const paymentMethodLabel = (paymentMethod) => {
  if (!paymentMethod || paymentMethod === '???') return '-'  
  const map = {
    'cash': '现金',
    'card': '银行卡',
    'alipay': '支付宝',
    'wechat': '微信支付',
    'WeChat': '微信支付',
    'wechatpay': '微信支付',
    '微信支付': '微信支付',
    '支付宝': '支付宝',
    '现金': '现金'
  }
  return map[paymentMethod] || '-'  
}

// 价格格式化
const priceFormatter = (row, column, cellValue) => {
  const v = Number(cellValue)
  return `¥${(Number.isFinite(v) ? v : 0).toFixed(2)}`
}

// 小票打印相关
const receiptDialogVisible = ref(false)
const receiptData = reactive({
  orderNumber: '',
  customerName: '',
  cashierName: '',
  orderDate: '',
  items: [],
  totalAmount: 0,
  discountAmount: 0,
  actualAmount: 0,
  paymentMethod: ''
})

// 处理打印小票
const handlePrintReceipt = async (order) => {
  try {
    const detail = await salesAPI.getOrderDetail(order.id)
    const items = await salesAPI.getOrderItems(order.id)
    
    receiptData.orderNumber = detail.orderNumber
    receiptData.customerName = detail.customerName !== undefined && detail.customerName !== null ? detail.customerName : (detail.customer_name !== undefined && detail.customer_name !== null ? detail.customer_name : '无')
    receiptData.cashierName = detail.cashierName || '-'
    receiptData.orderDate = detail.createTime || new Date().toLocaleString()
    receiptData.items = items || []
    receiptData.totalAmount = detail.totalAmount || 0
    receiptData.discountAmount = detail.discountAmount || 0
    receiptData.actualAmount = detail.actualAmount || detail.totalAmount || 0
    receiptData.paymentMethod = paymentMethodLabel(detail.paymentMethod)
    
    receiptDialogVisible.value = true
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  }
}

// 执行打印
const doPrintReceipt = () => {
  const printContent = document.querySelector('.receipt-preview')
  const originalContent = document.body.innerHTML
  
  const printWindow = window.open('', '_blank')
  printWindow.document.write(`
    <html>
      <head>
        <title>销售小票</title>
        <style>
          body {
            font-family: Arial, sans-serif;
            width: 300px;
            margin: 0 auto;
            padding: 20px;
          }
          .receipt-header {
            text-align: center;
            border-bottom: 1px dashed #000;
            padding-bottom: 10px;
            margin-bottom: 10px;
          }
          .receipt-header h3 {
            margin: 0 0 5px 0;
            font-size: 18px;
          }
          .receipt-date {
            font-size: 12px;
            color: #666;
          }
          .receipt-info {
            font-size: 12px;
            margin-bottom: 10px;
          }
          .receipt-info p {
            margin: 3px 0;
          }
          .receipt-items table {
            width: 100%;
            border-collapse: collapse;
            font-size: 12px;
          }
          .receipt-items th, .receipt-items td {
            padding: 3px;
            text-align: left;
          }
          .receipt-items th {
            border-bottom: 1px dashed #000;
          }
          .receipt-total {
            margin-top: 10px;
            padding-top: 10px;
            border-top: 1px dashed #000;
            font-size: 12px;
          }
          .receipt-total p {
            margin: 5px 0;
            display: flex;
            justify-content: space-between;
          }
          .receipt-total .total-amount {
            font-size: 14px;
            font-weight: bold;
          }
          .receipt-footer {
            text-align: center;
            margin-top: 15px;
            padding-top: 10px;
            border-top: 1px dashed #000;
            font-size: 12px;
          }
        </style>
      </head>
      <body>
        ${printContent.innerHTML}
      </body>
    </html>
  `)
  printWindow.document.close()
  printWindow.print()
  receiptDialogVisible.value = false
}
</script>

<style scoped>
.sales-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.page-subtitle {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 1.25rem;
  font-weight: 600;
}

.page-header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  width: 100%;
}

.table-card {
  margin-bottom: 20px;
}

/* 销售记录列表：操作列按钮同一行展示 */
.sales-table-actions {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 6px;
}

.sales-table-actions :deep(.el-button) {
  margin-left: 0;
  flex-shrink: 0;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.add-product-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.barcode-input {
  max-width: 520px;
}

.product-search-input {
  max-width: 100%;
}

.form-hint {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}

.dialog-footer-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  flex-wrap: wrap;
  gap: 12px;
}

.footer-left {
  display: flex;
  gap: 8px;
}

.sub-label {
  color: #606266;
}

.total-line {
  margin-top: 8px;
  font-size: 16px;
}

.selected-products {
  margin-top: 10px;
}

.selected-products h4 {
  margin-bottom: 10px;
}

.selected-products .el-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.order-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 20px;
}

.order-info h3 {
  margin-bottom: 10px;
}

.order-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.order-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.order-summary {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.summary-item {
  display: flex;
  gap: 10px;
  font-size: 16px;
}

.summary-item.total {
  font-weight: bold;
  font-size: 18px;
}

.order-remark {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.order-remark h4 {
  margin-bottom: 10px;
}

.total-amount {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: bold;
}

.amount-value {
  color: #f56c6c;
}

/* 支付对话框样式 */
.payment-order-info {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.payment-order-info div {
  margin-bottom: 8px;
}

.payment-amount {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.change-amount {
  margin-top: 10px;
  font-size: 14px;
  color: #67c23a;
  font-weight: 500;
}

/* 小票预览样式 */
.receipt-preview {
  width: 100%;
}

.receipt-preview .receipt-header {
  text-align: center;
  border-bottom: 1px dashed #ccc;
  padding-bottom: 10px;
  margin-bottom: 10px;
}

.receipt-preview .receipt-header h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
}

.receipt-preview .receipt-date {
  font-size: 12px;
  color: #666;
}

.receipt-preview .receipt-info {
  font-size: 12px;
  margin-bottom: 10px;
}

.receipt-preview .receipt-info p {
  margin: 3px 0;
}

.receipt-preview .receipt-items table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}

.receipt-preview .receipt-items th,
.receipt-preview .receipt-items td {
  padding: 3px;
  text-align: left;
}

.receipt-preview .receipt-items th {
  border-bottom: 1px dashed #ccc;
}

.receipt-preview .receipt-total {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #ccc;
  font-size: 12px;
}

.receipt-preview .receipt-total p {
  margin: 5px 0;
  display: flex;
  justify-content: space-between;
}

.receipt-preview .receipt-total .total-amount {
  font-size: 14px;
  font-weight: bold;
}

.receipt-preview .receipt-footer {
  text-align: center;
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px dashed #ccc;
  font-size: 12px;
}
</style>