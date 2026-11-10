<template>
  <div class="inventory-list">
    <el-card shadow="never">
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="关键字">
          <el-input
            v-model="queryParams.keyword"
            placeholder="商品名称/编码"
            clearable
          />
        </el-form-item>
        <el-form-item label="仓库">
          <el-input
            v-model="queryParams.warehouse"
            placeholder="仓库"
            clearable
          />
        </el-form-item>
        <el-form-item label="低库存">
          <el-switch v-model="queryParams.lowStock" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="warning" @click="transferVisible = true"
            >商品调拨</el-button
          >
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="warehouse" label="仓库" width="120" />
        <el-table-column prop="stock" label="当前库存" width="100" />
        <el-table-column prop="safetyStock" label="安全库存" width="100" />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.stock < row.safetyStock ? 'danger' : 'success'">
              {{ row.stock < row.safetyStock ? "库存不足" : "正常" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openStockDialog(row, 'IN')"
              >入库</el-button
            >
            <el-button link type="warning" @click="openStockDialog(row, 'OUT')"
              >出库</el-button
            >
            <el-button link type="info" @click="openCheckDialog(row)"
              >盘点</el-button
            >
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

    <el-dialog
      v-model="stockVisible"
      :title="stockForm.type === 'IN' ? '入库' : '出库'"
      width="500px"
    >
      <el-form
        :model="stockForm"
        :rules="stockRules"
        ref="stockFormRef"
        label-width="100px"
      >
        <el-form-item label="商品">
          {{ stockForm.productName }}
        </el-form-item>
        <el-form-item label="类型" prop="businessType">
          <el-select
            v-model="stockForm.businessType"
            placeholder="请选择业务类型"
            style="width: 100%"
          >
            <el-option label="采购" value="PURCHASE" />
            <el-option label="销售" value="SALE" />
            <el-option label="调拨" value="TRANSFER" />
            <el-option label="退货" value="RETURN" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="stockForm.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="关联单号">
          <el-input v-model="stockForm.relatedNo" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStock">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="checkVisible" title="库存盘点" width="500px">
      <el-form
        :model="checkForm"
        :rules="checkRules"
        ref="checkFormRef"
        label-width="100px"
      >
        <el-form-item label="商品">{{ checkForm.productName }}</el-form-item>
        <el-form-item label="当前库存">{{
          checkForm.currentStock
        }}</el-form-item>
        <el-form-item label="实盘数量" prop="actualStock">
          <el-input-number v-model="checkForm.actualStock" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="checkForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheck">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="transferVisible" title="商品调拨" width="500px">
      <el-form
        :model="transferForm"
        :rules="transferRules"
        ref="transferFormRef"
        label-width="100px"
      >
        <el-form-item label="商品" prop="productId">
          <el-select
            v-model="transferForm.productId"
            placeholder="请选择商品"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="p in products"
              :key="p.id"
              :label="p.productName"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="调出仓库" prop="fromWarehouse">
          <el-input v-model="transferForm.fromWarehouse" />
        </el-form-item>
        <el-form-item label="调入仓库" prop="toWarehouse">
          <el-input v-model="transferForm.toWarehouse" />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="transferForm.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="transferForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTransfer">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { pageInventory, changeStock, check, transfer } from "@/api/inventory";
import { pageProducts } from "@/api/product";

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const products = ref([]);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  warehouse: "",
  lowStock: false,
});

const stockVisible = ref(false);
const stockFormRef = ref(null);
const stockForm = reactive({
  productId: null,
  productName: "",
  type: "IN",
  businessType: "",
  quantity: 1,
  relatedNo: "",
  remark: "",
});
const stockRules = {
  businessType: [
    { required: true, message: "请选择业务类型", trigger: "change" },
  ],
  quantity: [{ required: true, message: "请输入数量", trigger: "blur" }],
};

const checkVisible = ref(false);
const checkFormRef = ref(null);
const checkForm = reactive({
  productId: null,
  productName: "",
  currentStock: 0,
  actualStock: 0,
  remark: "",
});
const checkRules = {
  actualStock: [{ required: true, message: "请输入实盘数量", trigger: "blur" }],
};

const transferVisible = ref(false);
const transferFormRef = ref(null);
const transferForm = reactive({
  productId: null,
  fromWarehouse: "",
  toWarehouse: "",
  quantity: 1,
  remark: "",
});
const transferRules = {
  productId: [{ required: true, message: "请选择商品", trigger: "change" }],
  fromWarehouse: [
    { required: true, message: "请输入调出仓库", trigger: "blur" },
  ],
  toWarehouse: [{ required: true, message: "请输入调入仓库", trigger: "blur" }],
  quantity: [{ required: true, message: "请输入数量", trigger: "blur" }],
};

const loadData = async () => {
  loading.value = true;
  try {
    const data = await pageInventory(queryParams);
    tableData.value = data.list || [];
    total.value = data.total || 0;
  } catch (e) {}
  loading.value = false;
};

const loadProducts = async () => {
  const data = await pageProducts({ pageNum: 1, pageSize: 1000 });
  products.value = data.list || [];
};

const resetQuery = () => {
  Object.assign(queryParams, {
    pageNum: 1,
    pageSize: 10,
    keyword: "",
    warehouse: "",
    lowStock: false,
  });
  loadData();
};

const openStockDialog = (row, type) => {
  Object.assign(stockForm, {
    productId: row.productId,
    productName: row.productName,
    type,
    businessType: type === "IN" ? "PURCHASE" : "SALE",
    quantity: 1,
    relatedNo: "",
    remark: "",
  });
  stockVisible.value = true;
};

const submitStock = async () => {
  await stockFormRef.value.validate();
  await changeStock({
    productId: stockForm.productId,
    type: stockForm.type,
    businessType: stockForm.businessType,
    quantity: stockForm.quantity,
    relatedNo: stockForm.relatedNo,
    remark: stockForm.remark,
  });
  ElMessage.success("操作成功");
  stockVisible.value = false;
  loadData();
};

const openCheckDialog = (row) => {
  Object.assign(checkForm, {
    productId: row.productId,
    productName: row.productName,
    currentStock: row.stock,
    actualStock: row.stock,
    remark: "",
  });
  checkVisible.value = true;
};

const submitCheck = async () => {
  await checkFormRef.value.validate();
  await check({
    productId: checkForm.productId,
    actualStock: checkForm.actualStock,
    remark: checkForm.remark,
  });
  ElMessage.success("盘点成功");
  checkVisible.value = false;
  loadData();
};

const submitTransfer = async () => {
  await transferFormRef.value.validate();
  await transfer({ ...transferForm });
  ElMessage.success("调拨成功");
  transferVisible.value = false;
  loadData();
};

onMounted(() => {
  loadData();
  loadProducts();
});
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
