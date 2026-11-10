<template>
  <div class="login-container">
    <div class="bg-decoration">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
      <div class="bg-grid"></div>
    </div>
    <div class="login-card-wrapper">
      <div class="login-card">
        <div class="login-left">
          <div class="brand-content">
            <div class="brand-logo">
              <el-icon :size="40"><Box /></el-icon>
            </div>
            <h1 class="brand-title">智供链</h1>
            <p class="brand-subtitle">智能电商供应链管理系统</p>
            <div class="brand-features">
              <div class="feature-item">
                <div class="feature-icon">
                  <el-icon><DataAnalysis /></el-icon>
                </div>
                <div class="feature-text">
                  <h3>AI智能分析</h3>
                  <p>销量预测 · 补货建议 · 异常预警</p>
                </div>
              </div>
              <div class="feature-item">
                <div class="feature-icon">
                  <el-icon><Goods /></el-icon>
                </div>
                <div class="feature-text">
                  <h3>全链路管理</h3>
                  <p>采购 · 库存 · 订单一体化</p>
                </div>
              </div>
              <div class="feature-item">
                <div class="feature-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="feature-text">
                  <h3>多角色协作</h3>
                  <p>管理员 · 商户 · 供应商</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="login-right">
          <div class="login-form-wrapper">
            <div class="login-header">
              <h2>欢迎回来</h2>
              <p>请登录您的账号继续使用</p>
            </div>
            <el-form
              :model="loginForm"
              :rules="loginRules"
              ref="loginFormRef"
              class="login-form"
              @keyup.enter="handleLogin"
            >
              <div class="form-tabs">
                <div
                  class="form-tab"
                  v-for="tab in loginTabs"
                  :key="tab.role"
                  :class="{ active: currentTab === tab.role }"
                  @click="switchTab(tab)"
                >
                  {{ tab.label }}
                </div>
              </div>
              <el-form-item prop="username">
                <el-input
                  v-model="loginForm.username"
                  size="large"
                  placeholder="请输入用户名"
                  :prefix-icon="User"
                  clearable
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  size="large"
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  show-password
                  @keyup.enter="handleLogin"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  class="login-btn"
                  :loading="loading"
                  @click="handleLogin"
                >
                  <span v-if="!loading">登 录</span>
                  <span v-else>登录中...</span>
                </el-button>
              </el-form-item>
            </el-form>
            <div class="quick-login">
              <span class="quick-login-label">快速登录：</span>
              <div class="quick-login-btns">
                <el-button text type="primary" @click="quickLogin('admin')">管理员</el-button>
                <el-button text type="primary" @click="quickLogin('merchant')">商户</el-button>
                <el-button text type="primary" @click="quickLogin('supplier')">供应商</el-button>
              </div>
            </div>
          </div>
          <div class="login-footer">
            <p>© 2024 智供链 SC - 智能电商供应链管理系统</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { login, getInfo } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const currentTab = ref('ADMIN')

const loginTabs = [
  { role: 'ADMIN', label: '管理员', username: 'admin', password: '123456' },
  { role: 'MERCHANT', label: '商户', username: 'merchant', password: '123456' },
  { role: 'SUPPLIER', label: '供应商', username: 'supplier', password: '123456' }
]

const loginForm = reactive({
  username: 'admin',
  password: '123456'
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const switchTab = (tab) => {
  currentTab.value = tab.role
  loginForm.username = tab.username
  loginForm.password = tab.password
}

const quickLogin = (role) => {
  const tab = loginTabs.find((t) => t.role.toUpperCase() === role.toUpperCase())
  if (tab) {
    loginForm.username = tab.username
    loginForm.password = tab.password
    handleLogin()
  }
}

const handleLogin = async () => {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    const data = await login(loginForm)
    userStore.setToken(data.token)
    const userInfo = await getInfo()
    userStore.setUser(userInfo)
    ElMessage.success('登录成功，欢迎回来！')
    router.push('/dashboard')
  } catch (e) {
    // 错误已在拦截器中提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #8b5cf6 100%);
}

.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;

  .bg-grid {
    position: absolute;
    inset: 0;
    background-image:
      linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
    background-size: 40px 40px;
  }

  .bg-circle {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.4;
    animation: float 20s ease-in-out infinite;
  }

  .circle-1 {
    width: 400px;
    height: 400px;
    background: #06b6d4;
    top: -100px;
    right: -100px;
    animation-delay: 0s;
  }

  .circle-2 {
    width: 300px;
    height: 300px;
    background: #8b5cf6;
    bottom: -50px;
    left: -50px;
    animation-delay: -7s;
  }

  .circle-3 {
    width: 250px;
    height: 250px;
    background: #ec4899;
    top: 50%;
    left: 50%;
    opacity: 0.2;
    animation-delay: -14s;
  }
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.05);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.95);
  }
}

.login-card-wrapper {
  position: relative;
  z-index: 1;
  padding: 20px;
  width: 100%;
  max-width: 960px;
}

.login-card {
  display: flex;
  background: #fff;
  border-radius: 24px;
  overflow: hidden;
  box-shadow:
    0 25px 50px -12px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  min-height: 560px;
}

.login-left {
  flex: 1;
  background: linear-gradient(160deg, #1e40af 0%, #3b82f6 50%, #8b5cf6 100%);
  color: #fff;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -30%;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
    border-radius: 50%;
  }
}

.brand-content {
  position: relative;
  z-index: 1;
}

.brand-logo {
  width: 72px;
  height: 72px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.brand-subtitle {
  font-size: 15px;
  opacity: 0.85;
  margin: 0 0 40px 0;
  letter-spacing: 0.5px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.12);
    transform: translateX(4px);
  }

  .feature-icon {
    width: 40px;
    height: 40px;
    min-width: 40px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
  }

  .feature-text {
    h3 {
      font-size: 14px;
      font-weight: 600;
      margin: 0 0 2px 0;
    }
    p {
      font-size: 12px;
      opacity: 0.75;
      margin: 0;
      line-height: 1.4;
    }
  }
}

.login-right {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
}

.login-form-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-header {
  margin-bottom: 28px;

  h2 {
    font-size: 26px;
    font-weight: 700;
    color: var(--sc-color-text-primary);
    margin: 0 0 6px 0;
  }

  p {
    font-size: 14px;
    color: var(--sc-color-text-secondary);
    margin: 0;
  }
}

.form-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  padding: 4px;
  background: var(--sc-color-bg-hover);
  border-radius: var(--sc-radius-lg);

  .form-tab {
    flex: 1;
    text-align: center;
    padding: 8px 12px;
    font-size: 13px;
    font-weight: 500;
    color: var(--sc-color-text-secondary);
    border-radius: var(--sc-radius-md);
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      color: var(--sc-color-primary);
    }

    &.active {
      background: #fff;
      color: var(--sc-color-primary);
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    }
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 20px;
  }

  :deep(.el-input__wrapper) {
    padding: 4px 12px;
    box-shadow: 0 0 0 1px var(--sc-color-border) inset;
    transition: all 0.2s ease;

    &:hover {
      box-shadow: 0 0 0 1px var(--sc-color-primary-light-3) inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 2px var(--sc-color-primary-light-5) inset;
    }
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  border-radius: var(--sc-radius-md);
  margin-top: 8px;
  background: linear-gradient(135deg, #2563eb 0%, #4f46e5 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.3);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(37, 99, 235, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

.quick-login {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px dashed var(--sc-color-border);

  .quick-login-label {
    font-size: 13px;
    color: var(--sc-color-text-secondary);
    display: block;
    margin-bottom: 10px;
  }

  .quick-login-btns {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;

    :deep(.el-button) {
      font-size: 13px;
      padding: 0 8px;
    }
  }
}

.login-footer {
  text-align: center;
  padding-top: 20px;

  p {
    font-size: 12px;
    color: var(--sc-color-text-placeholder);
    margin: 0;
  }
}

@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
    min-height: auto;
    border-radius: 16px;
  }

  .login-left {
    padding: 32px 24px;

    .brand-features {
      display: none;
    }

    .brand-title {
      font-size: 28px;
    }

    .brand-logo {
      width: 56px;
      height: 56px;
      margin-bottom: 16px;
    }
  }

  .login-right {
    padding: 32px 24px;
  }

  .login-header h2 {
    font-size: 22px;
  }
}
</style>
