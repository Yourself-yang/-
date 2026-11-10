<template>
  <div class="prediction">
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" :loading="running" @click="handleRun">执行预测</el-button>
        <el-select v-model="queryParams.days" placeholder="预测周期" style="width: 120px; margin-left: 10px">
          <el-option label="7天" :value="7" />
          <el-option label="15天" :value="15" />
          <el-option label="30天" :value="30" />
        </el-select>
        <el-select v-model="queryParams.productId" placeholder="筛选商品" filterable clearable style="width: 200px; margin-left: 10px">
          <el-option v-for="p in products" :key="p.id" :label="p.productName" :value="p.id" />
        </el-select>
        <el-button @click="loadData" style="margin-left: 10px">查询</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe @row-click="handleRowClick" highlight-current-row>
        <el-table-column prop="productName" label="商品" min-width="150" />
        <el-table-column prop="predictDate" label="预测日期" width="120" />
        <el-table-column prop="predictSales" label="预测销量" width="100" />
        <el-table-column prop="model" label="模型" width="120" />
        <el-table-column label="置信度" width="200">
          <template #default="{ row }">
            <el-progress :percentage="Math.round((row.confidence || 0) * 100)" />
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
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>预测趋势图</template>
      <div ref="chartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { runPrediction, getPrediction } from '@/api/ai'
import { pageProducts } from '@/api/product'

const loading = ref(false)
const running = ref(false)
const tableData = ref([])
const total = ref(0)
const products = ref([])
const queryParams = reactive({ pageNum: 1, pageSize: 10, days: 7, productId: null })
const chartRef = ref(null)
let chart = null
const selectedProduct = ref(null)

const initChart = () => {
  chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value' },
    series: [{ name: '预测销量', type: 'line', smooth: true, data: [], itemStyle: { color: '#409EFF' } }]
  })
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await getPrediction(queryParams)
    tableData.value = data.list || []
    total.value = data.total || 0
    if (!selectedProduct.value && tableData.value.length > 0) {
      selectedProduct.value = tableData.value[0].productId
    }
    updateChart()
  } catch (e) {}
  loading.value = false
}

const loadProducts = async () => {
  const data = await pageProducts({ pageNum: 1, pageSize: 1000 })
  products.value = data.list || []
}

const handleRun = async () => {
  running.value = true
  try {
    await runPrediction()
    ElMessage.success('预测完成')
    loadData()
  } catch (e) {}
  running.value = false
}

const handleRowClick = (row) => {
  selectedProduct.value = row.productId
  updateChart()
}

const updateChart = () => {
  if (!chart) return
  const productData = tableData.value.filter(r => r.productId === selectedProduct.value)
  const dates = [...new Set(productData.map(r => r.predictDate))].sort()
  const sales = dates.map(d => productData.find(r => r.predictDate === d)?.predictSales || 0)
  chart.setOption({
    xAxis: { data: dates },
    series: [{ data: sales }]
  })
}

const handleResize = () => chart?.resize()

onMounted(async () => {
  await nextTick()
  initChart()
  await loadProducts()
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
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
.chart-container {
  height: 400px;
}
</style>
