<template>
  <div class="inventory-alerts">
    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="warehouse" label="仓库" width="120" />
        <el-table-column prop="currentStock" label="当前库存" width="100" />
        <el-table-column prop="safetyStock" label="安全库存" width="100" />
        <el-table-column label="预警信息" min-width="200">
          <template #default="{ row }">
            <el-tag type="danger">库存低于安全库存，需补货 {{ row.safetyStock - row.currentStock }} 件</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { alerts } from '@/api/inventory'

const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    tableData.value = await alerts()
  } catch (e) {}
  loading.value = false
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
</style>
