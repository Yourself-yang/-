<template>
  <el-card shadow="never">
    <h3>智能匹配助手 <el-tag type="success">基于技能标签匹配算法</el-tag></h3>
    <p style="color:#666">系统根据您的技能标签与岗位要求进行智能匹配，推荐最适合您的岗位：</p>
    <div v-for="item in list" :key="item.job.id" class="match-item">
      <div class="score">
        <el-progress type="circle" :percentage="item.score" :width="70" :color="scoreColor(item.score)" />
        <div class="score-text">{{ item.score }}% 匹配</div>
      </div>
      <div class="info">
        <div class="title">{{ item.job.title }} <span class="salary">{{ item.job.salaryMin }}-{{ item.job.salaryMax }}K</span></div>
        <div class="company">{{ item.job.company }} · {{ item.job.city }}</div>
        <div class="hits">命中标签：
          <el-tag v-for="t in item.hitTags" :key="t" size="small" type="success" effect="plain" style="margin-left:6px">{{ t }}</el-tag>
        </div>
      </div>
      <el-button type="primary" plain @click="$router.push('/job/' + item.job.id)">查看详情</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api'

const list = ref([])
onMounted(async () => {
  const res = await request.get('/match/jobs/4')
  list.value = res.data
})
const scoreColor = s => s >= 80 ? '#67c23a' : s >= 50 ? '#e6a23c' : '#f56c6c'
</script>

<style scoped>
.match-item {
  display: flex; align-items: center; gap: 24px; padding: 20px;
  border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 12px;
}
.score { text-align: center; }
.score-text { font-size: 12px; color: #666; margin-top: 4px; }
.info { flex: 1; }
.title { font-size: 16px; font-weight: 600; }
.salary { color: #ff6b35; margin-left: 10px; }
.company { color: #666; margin: 6px 0; }
.hits { margin-top: 6px; }
</style>
