<template>
  <div class="anomaly">
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" :loading="detecting" @click="handleDetect">执行异常检测</el-button>
        <el-select v-model="queryParams.type" placeholder="异常类型" clearable style="width: 150px; margin-left: 10px">
          <el-option label="库存异常" value="INVENTORY" />
          <el-option label="销量异常" value="SALES" />
          <el-option label="订单异常" value="ORDER" />
        </el-select>
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px; margin-left: 10px">
          <el-option label="未处理" value="PENDING" />
          <el-option label="已处理" value="HANDLED" />
        </el-select>
        <el-button @click="loadData" style="margin-left: 10px">查询</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeMap[row.type]?.type">{{ typeMap[row.type]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="商品/单号" min-width="180" />
        <el-table-column label="异常分数" width="200">
          <template #default="{ row }">
            <el-progress :percentage="Math.round((row.score || 0) * 100)" type="exception" />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="250" />
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'danger' : 'success'">
              {{ row.status === 'PENDING' ? '未处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" link type="primary" @click="handleHandle(row)">标记已处理</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { detectAnomaly, listAnomaly, handleAnomaly } from '@/api/ai'

const loading = ref(false)
const detecting = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, type: '', status: '' })

const typeMap = {
  INVENTORY: { label: '库存异常', type: 'warning' },
  SALES: { label: '销量异常', type: 'danger' },
  ORDER: { label: '订单异常', type: 'info' }
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await listAnomaly(queryParams)
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (e) {}
  loading.value = false
}

const handleDetect = async () => {
  detecting.value = true
  try {
    await detectAnomaly()
    ElMessage.success('检测完成')
    loadData()
  } catch (e) {}
  detecting.value = false
}

const handleHandle = async (row) => {
  await handleAnomaly(row.id)
  ElMessage.success('已标记为处理')
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.toolbar {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
