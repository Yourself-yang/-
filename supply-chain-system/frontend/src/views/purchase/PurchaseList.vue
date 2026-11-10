<template>
  <div class="purchase-list">
    <el-card shadow="never">
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="采购单号/供应商" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="已审核" value="APPROVED" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已入库" value="RECEIVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="$router.push('/purchase/create')">新建采购单</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="poNo" label="采购单号" width="170" />
        <el-table-column prop="supplierName" label="供应商" min-width="150" />
        <el-table-column label="总金额" width="120">
          <template #default="{ row }">{{ row.totalAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button v-if="userStore.role === 'ADMIN' && row.status === 'PENDING'" link type="success" @click="handleApprove(row, true)">通过</el-button>
            <el-button v-if="userStore.role === 'ADMIN' && row.status === 'PENDING'" link type="danger" @click="handleApprove(row, false)">驳回</el-button>
            <el-button v-if="row.status === 'APPROVED'" link type="warning" @click="handleShip(row)">发货</el-button>
            <el-button v-if="row.status === 'SHIPPED'" link type="primary" @click="handleReceive(row)">入库</el-button>
            <el-button v-if="row.status === 'PENDING' || row.status === 'REJECTED'" link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="detailVisible" title="采购单详情" width="800px">
      <el-descriptions :column="2" border v-if="currentPO">
        <el-descriptions-item label="采购单号">{{ currentPO.poNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ currentPO.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="总金额">{{ currentPO.totalAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusMap[currentPO.status]?.type">{{ statusMap[currentPO.status]?.label }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentPO.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentPO.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="currentPO?.items || []" border style="margin-top: 20px">
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="quantity" label="采购数量" width="100" />
        <el-table-column prop="receivedQuantity" label="已接收" width="100" />
        <el-table-column label="单价" width="120">
          <template #default="{ row }">{{ row.unitPrice?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }">{{ (row.quantity * row.unitPrice)?.toFixed(2) }}</template>
        </el-table-column>
        <template v-if="receiveVisible" #default="{ row }">
          <el-table-column label="本次入库" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.receiveQty" :min="0" :max="row.quantity - (row.receivedQuantity || 0)" size="small" />
            </template>
          </el-table-column>
        </template>
      </el-table>
      <template #footer v-if="receiveVisible">
        <el-button @click="receiveVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReceive">确认入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { pagePOs, approve, ship, receive, deletePO, getPO } from '@/api/purchase'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '' })
const detailVisible = ref(false)
const receiveVisible = ref(false)
const currentPO = ref(null)

const statusMap = {
  PENDING: { label: '待审核', type: 'warning' },
  APPROVED: { label: '已审核', type: 'primary' },
  SHIPPED: { label: '已发货', type: 'info' },
  RECEIVED: { label: '已入库', type: 'success' },
  REJECTED: { label: '已驳回', type: 'danger' }
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await pagePOs(queryParams)
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (e) {}
  loading.value = false
}

const resetQuery = () => {
  Object.assign(queryParams, { pageNum: 1, pageSize: 10, keyword: '', status: '' })
  loadData()
}

const handleDetail = async (row) => {
  try {
    currentPO.value = await getPO(row.id)
    receiveVisible.value = false
    detailVisible.value = true
  } catch (e) {}
}

const handleApprove = async (row, approved) => {
  try {
    await ElMessageBox.confirm(`确定要${approved ? '通过' : '驳回'}该采购单吗？`, '提示', { type: 'warning' })
    await approve(row.id, { approved, remark: approved ? '' : '驳回' })
    ElMessage.success('操作成功')
    loadData()
  } catch (e) {}
}

const handleShip = async (row) => {
  try {
    await ElMessageBox.confirm('确定要标记为已发货吗？', '提示', { type: 'warning' })
    await ship(row.id)
    ElMessage.success('发货成功')
    loadData()
  } catch (e) {}
}

const handleReceive = async (row) => {
  try {
    const po = await getPO(row.id)
    po.items = po.items.map(item => ({ ...item, receiveQty: item.quantity - (item.receivedQuantity || 0) }))
    currentPO.value = po
    receiveVisible.value = true
    detailVisible.value = true
  } catch (e) {}
}

const submitReceive = async () => {
  const items = currentPO.value.items.map(item => ({ itemId: item.id, quantity: item.receiveQty }))
  await receive(currentPO.value.id, { items })
  ElMessage.success('入库成功')
  detailVisible.value = false
  receiveVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该采购单吗？', '提示', { type: 'warning' })
    await deletePO(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

onMounted(() => {
  loadData()
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
