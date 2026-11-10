<template>
  <div class="purchase-create">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-button @click="$router.back()" icon="ArrowLeft">返回</el-button>
          <span class="title">新建采购单</span>
        </div>
      </template>
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="form.supplierId" placeholder="请选择供应商" style="width: 400px" filterable>
            <el-option v-for="s in suppliers" :key="s.id" :label="s.supplierName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="商品明细">
          <div class="items-header">
            <el-button type="primary" size="small" @click="addItem">添加商品</el-button>
          </div>
          <el-table :data="form.items" border style="width: 100%">
            <el-table-column label="商品" width="300">
              <template #default="{ row, $index }">
                <el-select v-model="row.productId" placeholder="选择商品" filterable style="width: 100%" @change="onProductChange(row, $index)">
                  <el-option v-for="p in products" :key="p.id" :label="`${p.productCode} - ${p.productName}`" :value="p.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="150">
              <template #default="{ row }">
                <el-input-number v-model="row.unitPrice" :precision="2" :min="0" @change="calcTotal" />
              </template>
            </el-table-column>
            <el-table-column label="数量" width="150">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" @change="calcTotal" />
              </template>
            </el-table-column>
            <el-table-column label="小计" width="150">
              <template #default="{ row }">{{ ((row.unitPrice || 0) * (row.quantity || 0)).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button link type="danger" @click="removeItem($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="总金额">
          <span class="total-amount">¥ {{ totalAmount.toFixed(2) }}</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="submitForm" :loading="loading">提交采购单</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listSuppliers } from '@/api/supplier'
import { pageProducts } from '@/api/product'
import { createPO } from '@/api/purchase'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const suppliers = ref([])
const products = ref([])
const form = reactive({
  supplierId: null,
  remark: '',
  items: []
})
const formRules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }]
}

const totalAmount = computed(() => {
  return form.items.reduce((sum, item) => sum + (item.unitPrice || 0) * (item.quantity || 0), 0)
})

const addItem = () => {
  form.items.push({ productId: null, productName: '', unitPrice: 0, quantity: 1 })
}

const removeItem = (index) => {
  form.items.splice(index, 1)
  calcTotal()
}

const onProductChange = (row) => {
  const product = products.value.find(p => p.id === row.productId)
  if (product) {
    row.unitPrice = product.purchasePrice
  }
  calcTotal()
}

const calcTotal = () => {}

const loadSuppliers = async () => {
  suppliers.value = await listSuppliers()
}

const loadProducts = async () => {
  const data = await pageProducts({ pageNum: 1, pageSize: 1000 })
  products.value = data.list || []
}

const submitForm = async () => {
  await formRef.value.validate()
  if (form.items.length === 0) {
    ElMessage.warning('请添加商品明细')
    return
  }
  if (form.items.some(i => !i.productId)) {
    ElMessage.warning('请选择商品')
    return
  }
  loading.value = true
  try {
    await createPO({
      supplierId: form.supplierId,
      remark: form.remark,
      items: form.items.map(i => ({ productId: i.productId, quantity: i.quantity, unitPrice: i.unitPrice }))
    })
    ElMessage.success('创建成功')
    router.push('/purchase')
  } catch (e) {}
  loading.value = false
}

onMounted(() => {
  loadSuppliers()
  loadProducts()
  addItem()
})
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  align-items: center;
}
.title {
  font-size: 18px;
  font-weight: bold;
  margin-left: 20px;
}
.items-header {
  margin-bottom: 10px;
}
.total-amount {
  font-size: 24px;
  font-weight: bold;
  color: #F56C6C;
}
</style>
