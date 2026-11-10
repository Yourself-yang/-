<template>
  <div v-if="job">
    <el-card shadow="never">
      <div class="head">
        <div>
          <h2 style="margin:0">{{ job.title }} <el-tag type="danger">{{ job.salaryMin }}-{{ job.salaryMax }}K</el-tag></h2>
          <p style="color:#666;margin:8px 0 0">{{ job.company }} · {{ job.city }} · {{ job.experience }} · {{ job.education }}</p>
          <div style="margin-top:10px">
            <el-tag v-for="t in job.tags?.split(',')" :key="t" size="small" effect="plain" style="margin-right:6px">{{ t }}</el-tag>
          </div>
        </div>
        <el-button type="primary" size="large" @click="deliver">立即沟通 / 投递简历</el-button>
      </div>
    </el-card>
    <el-card shadow="never" style="margin-top:16px">
      <h4>职位描述</h4>
      <p style="line-height:1.8;color:#333">{{ job.description }}</p>
      <h4>HR信息</h4>
      <p>{{ job.hrName }} · {{ job.company }}</p>
    </el-card>

    <el-dialog v-model="dialogVisible" title="投递简历" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="技能"><el-input v-model="form.skills" /></el-form-item>
        <el-form-item label="自我介绍"><el-input v-model="form.selfIntro" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确认投递</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api'

const route = useRoute()
const job = ref(null)
const dialogVisible = ref(false)
const form = ref({})

onMounted(async () => {
  const res = await request.get('/job/' + route.params.id)
  job.value = res.data
})

const deliver = () => { dialogVisible.value = true }
const submit = async () => {
  await request.post('/resume/deliver', { ...form.value, jobId: job.value.id, userId: localStorage.getItem('userId') || 4 })
  ElMessage.success('投递成功，等待HR沟通！')
  dialogVisible.value = false
}
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: center; }
</style>
