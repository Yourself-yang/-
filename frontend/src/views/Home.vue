<template>
  <div>
    <el-card shadow="never" class="search-card">
      <el-input v-model="keyword" placeholder="搜索岗位 / 公司 / 技能标签" prefix-icon="Search" style="width:400px" @keyup.enter="loadJobs" />
      <el-button type="primary" @click="loadJobs" style="margin-left:12px">搜索</el-button>
      <el-button text @click="loadJobs">重置</el-button>
    </el-card>

    <div v-for="job in jobs" :key="job.id" class="job-item" @click="$router.push('/job/' + job.id)">
      <div class="salary">{{ job.salaryMin }}-{{ job.salaryMax }}K</div>
      <div class="info">
        <div class="title-row">
          <span class="title">{{ job.title }}</span>
          <el-tag size="small" type="danger">{{ job.city }}</el-tag>
        </div>
        <div class="company">{{ job.company }} · {{ job.education }} · {{ job.experience }}</div>
        <div class="tags">
          <el-tag v-for="t in job.tags?.split(',')" :key="t" size="small" effect="plain" style="margin-right:6px">{{ t }}</el-tag>
        </div>
      </div>
      <div class="hr">
        <el-avatar :size="36" icon="UserFilled" />
        <div class="hr-info">
          <div>{{ job.hrName }}</div>
          <div class="hr-title">HR · 刚刚活跃</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api'

const jobs = ref([])
const keyword = ref('')

const loadJobs = async () => {
  const res = await request.get('/job/list', { params: { keyword: keyword.value } })
  jobs.value = res.data
}
onMounted(loadJobs)
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.job-item {
  background: #fff; padding: 20px 24px; border-radius: 8px; margin-bottom: 12px;
  display: flex; align-items: center; cursor: pointer; transition: all .2s;
}
.job-item:hover { box-shadow: 0 4px 16px rgba(0,0,0,.08); }
.salary { color: #ff6b35; font-size: 18px; font-weight: bold; width: 110px; }
.info { flex: 1; }
.title-row { display: flex; align-items: center; gap: 8px; }
.title { font-size: 16px; font-weight: 600; }
.company { color: #666; margin: 6px 0; }
.tags { margin-top: 4px; }
.hr { display: flex; align-items: center; gap: 10px; }
.hr-info { font-size: 13px; }
.hr-title { color: #999; font-size: 12px; }
</style>
