<template>
  <el-card shadow="never">
    <h3>我的投递记录</h3>
    <el-table :data="list" stripe>
      <el-table-column prop="jobTitle" label="岗位名称" />
      <el-table-column prop="company" label="公司" />
      <el-table-column prop="name" label="投递人" />
      <el-table-column prop="phone" label="联系电话" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="投递时间" />
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api'

const list = ref([])
onMounted(async () => {
  const res = await request.get('/resume/mine/' + (localStorage.getItem('userId') || 4))
  list.value = res.data
})
const statusType = s => ({ PENDING: 'warning', INTERVIEW: 'primary', PASSED: 'success', REJECTED: 'danger' }[s])
const statusText = s => ({ PENDING: '待处理', INTERVIEW: '面试中', PASSED: '已通过', REJECTED: '已拒绝' }[s])
</script>
