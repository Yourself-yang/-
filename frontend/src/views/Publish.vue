<template>
  <el-card shadow="never">
    <h3>发布岗位（HR端）</h3>
    <el-form :model="form" label-width="100px" style="max-width:600px">
      <el-form-item label="岗位名称"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="公司名称"><el-input v-model="form.company" /></el-form-item>
      <el-form-item label="工作城市"><el-input v-model="form.city" placeholder="如：武汉" /></el-form-item>
      <el-form-item label="薪资范围">
        <el-input-number v-model="form.salaryMin" :min="3" :max="100" /> -
        <el-input-number v-model="form.salaryMax" :min="3" :max="100" /> K
      </el-form-item>
      <el-form-item label="经验要求"><el-input v-model="form.experience" placeholder="如：3-5年" /></el-form-item>
      <el-form-item label="学历要求"><el-select v-model="form.education"><el-option label="本科" value="本科" /><el-option label="硕士" value="硕士" /><el-option label="大专" value="大专" /></el-select></el-form-item>
      <el-form-item label="技能标签"><el-input v-model="form.tags" placeholder="用逗号分隔，如 Java,SpringBoot,MySQL" /></el-form-item>
      <el-form-item label="岗位描述"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="publish">发布岗位</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api'

const form = ref({ hrId: 1, hrName: '张建国', city: '武汉', education: '本科' })
const publish = async () => {
  await request.post('/job/publish', form.value)
  ElMessage.success('发布成功')
}
</script>
