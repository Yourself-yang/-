<template>
  <div>
    <el-container v-if="$route.path !== '/login'" class="layout">
      <el-header class="header">
        <div class="logo" @click="$router.push('/home')">
          <el-icon><Briefcase /></el-icon>
          <span>直聘邦</span>
        </div>
        <el-menu mode="horizontal" :default-active="$route.path" router class="menu">
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/match">智能匹配</el-menu-item>
          <el-menu-item index="/resume">我的投递</el-menu-item>
          <el-menu-item index="/publish">发布岗位</el-menu-item>
          <el-menu-item index="/chat">在线沟通</el-menu-item>
        </el-menu>
        <div class="user">
          <el-avatar :size="32" icon="UserFilled" />
          <span style="margin-left:8px">{{ username || '未登录' }}</span>
          <el-button text @click="$router.push('/login')">登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
    <router-view v-else />
  </div>
</template>

<script setup>
import { ref } from 'vue'
const username = ref(localStorage.getItem('realName') || '')
</script>

<style scoped>
.layout { min-height: 100vh; }
.header {
  background: #fff; display: flex; align-items: center;
  border-bottom: 1px solid #e4e7ed; padding: 0 24px;
}
.logo {
  font-size: 20px; font-weight: bold; color: #ff6b35;
  display: flex; align-items: center; gap: 6px; cursor: pointer; margin-right: 40px;
}
.menu { border-bottom: none; flex: 1; }
.user { display: flex; align-items: center; }
.main { background: #f5f6f8; padding: 20px; }
</style>
