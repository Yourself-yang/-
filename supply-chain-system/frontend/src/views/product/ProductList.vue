<template>
  <div class="product-list">
    <el-card shadow="never">
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="商品名称/编码" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryParams.categoryId" placeholder="请选择" clearable>
            <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="handleAdd">新增商品</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="spec" label="规格" width="100" />
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column label="进货价" width="100">
          <template #default="{ row }">{{ row.purchasePrice?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="售价" width="100">
          <template #default="{ row }">{{ row.salePrice?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="商品编码" prop="productCode">
          <el-input v-model="form.productCode" />
        </el-form-item>
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="form.brand" />
        </el-form-item>
        <el-form-item label="规格" prop="spec">
          <el-input v-model="form.spec" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" />
        </el-form-item>
        <el-form-item label="进货价" prop="purchasePrice">
          <el-input-number v-model="form.purchasePrice" :precision="2" :min="0" />
        </el-form-item>
        <el-form-item label="售价" prop="salePrice">
          <el-input-number v-model="form.salePrice" :precision="2" :min="0" />
        </el-form-item>
        <el-form-item label="图片地址" prop="imageUrl">
          <el-input v-model="form.imageUrl" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageProducts, addProduct, updateProduct, updateStatus, deleteProduct } from '@/api/product'
import { listCategories } from '@/api/category'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const categories = ref([])
const queryParams = ref({ pageNum: 1, pageSize: 10, keyword: '', categoryId: null, status: null })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const form = ref({})
const formRules = {
  productCode: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  purchasePrice: [{ required: true, message: '请输入进货价', trigger: 'blur' }],
  salePrice: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await pageProducts(queryParams.value)
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (e) {}
  loading.value = false
}

const loadCategories = async () => {
  try {
    categories.value = await listCategories()
  } catch (e) {}
}

const resetQuery = () => {
  queryParams.value = { pageNum: 1, pageSize: 10, keyword: '', categoryId: null, status: null }
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增商品'
  form.value = { status: 1 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑商品'
  form.value = { ...row }
  dialogVisible.value = true
}

const submitForm = async () => {
  await formRef.value.validate()
  if (form.value.id) {
    await updateProduct(form.value.id, form.value)
    ElMessage.success('修改成功')
  } else {
    await addProduct(form.value)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(`确定要${newStatus === 1 ? '上架' : '下架'}该商品吗？`, '提示', { type: 'warning' })
    await updateStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    loadData()
  } catch (e) {}
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', { type: 'warning' })
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped lang="scss">
.product-list {
  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: var(--sc-spacing-lg);

    .page-title {
      font-size: 20px;
      font-weight: 600;
      color: var(--sc-color-text-primary);
      margin: 0;
    }

    .page-actions {
      display: flex;
      gap: var(--sc-spacing-sm);
    }
  }

  .search-card {
    border-radius: var(--sc-radius-lg);
    margin-bottom: var(--sc-spacing-lg);

    :deep(.el-card__body) {
      padding: var(--sc-spacing-lg) var(--sc-spacing-xl) var(--sc-spacing-sm);
    }

    .search-form {
      :deep(.el-form-item) {
        margin-bottom: var(--sc-spacing-base);
      }
    }
  }

  .table-card {
    border-radius: var(--sc-radius-lg);

    :deep(.el-table) {
      --el-table-border-color: transparent;
    }

    :deep(.el-table__row) {
      transition: background-color var(--sc-transition-fast);
    }

    .table-toolbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: var(--sc-spacing-base);
    }
  }

  .pagination {
    margin-top: var(--sc-spacing-lg);
    display: flex;
    justify-content: flex-end;
  }

  .price-cell {
    font-weight: 500;
    color: var(--sc-color-text-primary);
    font-variant-numeric: tabular-nums;
  }
}

@media (max-width: 768px) {
  .product-list {
    .search-card :deep(.el-card__body) {
      padding: var(--sc-spacing-base);
    }
  }
}
</style>
