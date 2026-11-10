<template>
  <div class="replenishment">
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="handleConvert" :loading="converting">一键生成采购单</el-button>
        <el-button @click="loadData" style="margin-left: 10px">刷新</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品" min-width="150" />
        <el-table-column prop="currentStock" label="当前库存" width="100" />
        <el-table-column prop="predict7dSales" label="7天预测销量" width="130" />
        <el-table-column prop="safetyStock" label="安全库存" width="100" />
        <el-table-column prop="suggestQty" label="建议补货量" width="120">
          <template #default="{ row }">
            <span style="color: #E6A23C; font-weight: bold">{{ row.suggestQty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'">
              {{ row.status === 'PENDING' ? '待处理' : '已生成' }}
            </el-tag>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { listReplenishment, convertReplenishment } from '@/api/ai'

const loading = ref(false)
const converting = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10 })

const loadData = async () => {
  loading.value = true
  try {
    const data = await listReplenishment(queryParams)
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (e) {}
  loading.value = false
}

const handleConvert = async () => {
  try {
    await ElMessageBox.confirm('确定要根据补货建议生成采购单吗？', '提示', { type: 'warning' })
    converting.value = true
    await convertReplenishment()
    ElMessage.success('采购单生成成功')
    loadData()
  } catch (e) {}
  converting.value = false
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
