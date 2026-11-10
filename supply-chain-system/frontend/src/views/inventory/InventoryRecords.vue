<template>
  <div class="inventory-records">
    <el-card shadow="never">
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="商品ID">
          <el-input v-model="queryParams.productId" placeholder="商品ID" clearable />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="queryParams.businessType" placeholder="请选择" clearable>
            <el-option label="采购" value="PURCHASE" />
            <el-option label="销售" value="SALE" />
            <el-option label="调拨" value="TRANSFER" />
            <el-option label="退货" value="RETURN" />
            <el-option label="盘点" value="CHECK" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'IN' ? 'success' : 'danger'">{{ row.type === 'IN' ? '入库' : '出库' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessType" label="业务类型" width="100" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="beforeStock" label="变动前" width="100" />
        <el-table-column prop="afterStock" label="变动后" width="100" />
        <el-table-column prop="relatedNo" label="关联单号" width="150" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="createTime" label="时间" width="170" />
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
import { inventoryRecords } from '@/api/inventory'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, productId: '', businessType: '' })

const loadData = async () => {
  loading.value = true
  try {
    const data = await inventoryRecords(queryParams)
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (e) {}
  loading.value = false
}

const resetQuery = () => {
  Object.assign(queryParams, { pageNum: 1, pageSize: 10, productId: '', businessType: '' })
  loadData()
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
