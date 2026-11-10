<template>
  <div class="order-list">
    <el-card shadow="never">
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="订单号/收货人" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="待付款" value="PENDING_PAY" />
            <el-option label="待发货" value="PENDING_SHIP" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="退款中" value="REFUNDING" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker v-model="dateRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="handleCreate">新建订单</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border stripe row-key="id" @expand-change="handleExpand">
        <el-table-column type="expand">
          <template #default="{ row }">
            <el-table :data="row.items || []" border>
              <el-table-column prop="productName" label="商品" />
              <el-table-column prop="quantity" label="数量" width="100" />
              <el-table-column label="单价" width="120">
                <template #default="{ row: item }">{{ item.unitPrice?.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="小计" width="120">
                <template #default="{ row: item }">{{ (item.quantity * item.unitPrice)?.toFixed(2) }}</template>
              </el-table-column>
            </el-table>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="170" />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">{{ row.totalAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiver" label="收货人" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="address" label="地址" min-width="200" />
        <el-table-column prop="trackingNo" label="物流单号" width="150" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING_PAY'" link type="success" @click="handlePay(row)">付款</el-button>
            <el-button v-if="row.status === 'PENDING_SHIP'" link type="warning" @click="openShipDialog(row)">发货</el-button>
            <el-button v-if="row.status === 'SHIPPED'" link type="primary" @click="handleConfirm(row)">确认收货</el-button>
            <el-button v-if="row.status === 'PENDING_PAY'" link type="info" @click="handleCancel(row)">取消</el-button>
            <el-button v-if="row.status === 'PENDING_SHIP' || row.status === 'SHIPPED'" link type="danger" @click="handleRefund(row)">退款</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="shipVisible" title="发货" width="400px">
      <el-form :model="shipForm" label-width="100px">
        <el-form-item label="物流单号">
          <el-input v-model="shipForm.trackingNo" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" @click="submitShip">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="createVisible" title="新建订单" width="700px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="收货人" required>
          <el-input v-model="createForm.receiver" />
        </el-form-item>
        <el-form-item label="电话" required>
          <el-input v-model="createForm.phone" />
        </el-form-item>
        <el-form-item label="地址" required>
          <el-input v-model="createForm.address" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="商品明细">
          <div style="margin-bottom: 10px">
            <el-button type="primary" size="small" @click="addOrderItem">添加商品</el-button>
          </div>
          <el-table :data="createForm.items" border>
            <el-table-column label="商品" width="300">
              <template #default="{ row }">
                <el-select v-model="row.productId" filterable placeholder="选择商品" style="width: 100%" @change="onOrderProductChange(row)">
                  <el-option v-for="p in products" :key="p.id" :label="p.productName" :value="p.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="130">
              <template #default="{ row }">{{ row.unitPrice?.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="数量" width="130">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="小计" width="120">
              <template #default="{ row }">{{ ((row.unitPrice || 0) * (row.quantity || 0)).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="70">
              <template #default="{ $index }">
                <el-button link type="danger" @click="removeOrderItem($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="总金额">
          <span style="font-size: 20px; font-weight: bold; color: #F56C6C">
            ¥ {{ orderTotalAmount.toFixed(2) }}
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">提交订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageOrders, pay, shipOrder, confirm, cancel, refund, createOrder, getOrder } from '@/api/order'
import { pageProducts } from '@/api/product'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const products = ref([])
const dateRange = ref([])
const queryParams = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '', startTime: '', endTime: '' })

const shipVisible = ref(false)
const shipForm = reactive({ orderId: null, trackingNo: '' })

const createVisible = ref(false)
const createForm = reactive({ receiver: '', phone: '', address: '', items: [] })

const statusMap = {
  PENDING_PAY: { label: '待付款', type: 'warning' },
  PENDING_SHIP: { label: '待发货', type: 'primary' },
  SHIPPED: { label: '已发货', type: 'info' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'info' },
  REFUNDING: { label: '退款中', type: 'danger' },
  REFUNDED: { label: '已退款', type: 'danger' }
}

const orderTotalAmount = computed(() => {
  return createForm.items.reduce((sum, item) => sum + (item.unitPrice || 0) * (item.quantity || 0), 0)
})

const loadData = async () => {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.startTime = dateRange.value[0].toISOString()
      queryParams.endTime = dateRange.value[1].toISOString()
    } else {
      queryParams.startTime = ''
      queryParams.endTime = ''
    }
    const data = await pageOrders(queryParams)
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (e) {}
  loading.value = false
}

const loadProducts = async () => {
  const data = await pageProducts({ pageNum: 1, pageSize: 1000 })
  products.value = data.list || []
}

const resetQuery = () => {
  Object.assign(queryParams, { pageNum: 1, pageSize: 10, keyword: '', status: '', startTime: '', endTime: '' })
  dateRange.value = []
  loadData()
}

const handleExpand = async (row, expandedRows) => {
  if (expandedRows.includes(row) && !row.items) {
    try {
      const detail = await getOrder(row.id)
      row.items = detail.items || []
    } catch (e) {}
  }
}

const handlePay = async (row) => {
  try {
    await ElMessageBox.confirm('确认付款？', '提示', { type: 'warning' })
    await pay(row.id)
    ElMessage.success('付款成功')
    loadData()
  } catch (e) {}
}

const openShipDialog = (row) => {
  shipForm.orderId = row.id
  shipForm.trackingNo = ''
  shipVisible.value = true
}

const submitShip = async () => {
  await shipOrder(shipForm.orderId, { trackingNo: shipForm.trackingNo })
  ElMessage.success('发货成功')
  shipVisible.value = false
  loadData()
}

const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确认收货？', '提示', { type: 'warning' })
    await confirm(row.id)
    ElMessage.success('确认收货成功')
    loadData()
  } catch (e) {}
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消订单？', '提示', { type: 'warning' })
    await cancel(row.id)
    ElMessage.success('取消成功')
    loadData()
  } catch (e) {}
}

const handleRefund = async (row) => {
  try {
    await ElMessageBox.confirm('确定申请退款？', '提示', { type: 'warning' })
    await refund(row.id)
    ElMessage.success('退款申请成功')
    loadData()
  } catch (e) {}
}

const handleCreate = () => {
  Object.assign(createForm, { receiver: '', phone: '', address: '', items: [] })
  addOrderItem()
  createVisible.value = true
}

const addOrderItem = () => {
  createForm.items.push({ productId: null, unitPrice: 0, quantity: 1 })
}

const removeOrderItem = (index) => {
  createForm.items.splice(index, 1)
}

const onOrderProductChange = (row) => {
  const product = products.value.find(p => p.id === row.productId)
  if (product) {
    row.unitPrice = product.salePrice
  }
}

const submitCreate = async () => {
  if (!createForm.receiver || !createForm.phone || !createForm.address) {
    ElMessage.warning('请填写收货信息')
    return
  }
  if (createForm.items.some(i => !i.productId)) {
    ElMessage.warning('请选择商品')
    return
  }
  await createOrder({
    receiver: createForm.receiver,
    phone: createForm.phone,
    address: createForm.address,
    items: createForm.items.map(i => ({ productId: i.productId, quantity: i.quantity, unitPrice: i.unitPrice }))
  })
  ElMessage.success('下单成功')
  createVisible.value = false
  loadData()
}

onMounted(() => {
  loadData()
  loadProducts()
})
</script>

<style scoped lang="scss">
.search-form {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
