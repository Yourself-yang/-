<template>
  <div class="supplier-list">
    <el-card shadow="never">
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="供应商名称/联系人" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="handleAdd">新增供应商</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="supplierName" label="供应商名称" min-width="150" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="address" label="地址" min-width="200" />
        <el-table-column prop="rating" label="评级" width="90">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score text-color="#ff9900" score-template="{value}" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
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
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="form.supplierName" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="评级" prop="rating">
          <el-rate v-model="form.rating" show-score text-color="#ff9900" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
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
import { pageSuppliers, addSupplier, updateSupplier, updateSupplierStatus, deleteSupplier } from '@/api/supplier'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10, keyword: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const form = ref({})
const formRules = {
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await pageSuppliers(queryParams.value)
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (e) {}
  loading.value = false
}

const resetQuery = () => {
  queryParams.value = { pageNum: 1, pageSize: 10, keyword: '' }
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增供应商'
  form.value = { status: 1, rating: 5 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑供应商'
  form.value = { ...row }
  dialogVisible.value = true
}

const submitForm = async () => {
  await formRef.value.validate()
  if (form.value.id) {
    await updateSupplier(form.value.id, form.value)
    ElMessage.success('修改成功')
  } else {
    await addSupplier(form.value)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(`确定要${newStatus === 1 ? '启用' : '停用'}该供应商吗？`, '提示', { type: 'warning' })
    await updateSupplierStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    loadData()
  } catch (e) {}
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该供应商吗？', '提示', { type: 'warning' })
    await deleteSupplier(row.id)
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
