<template>
  <div class="login-page">
    <div class="login-box">
      <div class="left">
        <h1>直聘邦</h1>
        <p>找工作，直聊就行</p>
        <p class="sub">在线招聘求职平台 · 让招聘更高效</p>
      </div>
      <div class="right">
        <el-tabs v-model="tab">
          <el-tab-pane label="登录" name="login">
            <el-form :model="form" label-width="0">
              <el-form-item>
                <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
              </el-form-item>
              <el-form-item>
                <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
              </el-form-item>
              <el-button type="primary" style="width:100%" @click="doLogin">登 录</el-button>
              <p class="tip">演示账号：zhangsan / 123456（求职者），hr_zhang / 123456（HR）</p>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="注册" name="register">
            <el-form :model="regForm" label-width="0">
              <el-form-item>
                <el-input v-model="regForm.username" placeholder="用户名" />
              </el-form-item>
              <el-form-item>
                <el-input v-model="regForm.password" type="password" placeholder="密码" show-password />
              </el-form-item>
              <el-form-item>
                <el-input v-model="regForm.realName" placeholder="真实姓名" />
              </el-form-item>
              <el-form-item>
                <el-radio-group v-model="regForm.role">
                  <el-radio value="SEEKER">求职者</el-radio>
                  <el-radio value="HR">招聘者</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-button type="primary" style="width:100%" @click="doRegister">注 册</el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api'

const router = useRouter()
const tab = ref('login')
const form = ref({ username: 'zhangsan', password: '123456' })
const regForm = ref({ role: 'SEEKER' })

const doLogin = async () => {
  const res = await request.post('/auth/login', form.value)
  localStorage.setItem('token', res.data.token)
  localStorage.setItem('userId', res.data.user.id)
  localStorage.setItem('realName', res.data.user.realName)
  localStorage.setItem('role', res.data.user.role)
  ElMessage.success('登录成功')
  router.push('/home')
}

const doRegister = async () => {
  await request.post('/auth/register', regForm.value)
  ElMessage.success('注册成功，请登录')
  tab.value = 'login'
}
</script>

<style scoped>
.login-page {
  height: 100vh; background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
  display: flex; align-items: center; justify-content: center;
}
.login-box {
  width: 800px; height: 460px; background: #fff; border-radius: 12px;
  display: flex; overflow: hidden; box-shadow: 0 20px 60px rgba(0,0,0,.2);
}
.left {
  width: 40%; background: linear-gradient(160deg, #ff6b35, #ff4500);
  color: #fff; padding: 60px 40px; display: flex; flex-direction: column; justify-content: center;
}
.left h1 { font-size: 36px; margin-bottom: 16px; }
.left p { font-size: 18px; }
.left .sub { font-size: 13px; opacity: .8; margin-top: 20px; }
.right { flex: 1; padding: 40px 50px; }
.tip { font-size: 12px; color: #999; margin-top: 12px; }
</style>
