<template>
  <el-container class="layout-container">
    <el-aside
      :width="isCollapse ? '64px' : '240px'"
      class="aside"
      :class="{ collapsed: isCollapse }"
    >
      <div class="logo">
        <div class="logo-icon">
          <el-icon :size="28"><Box /></el-icon>
        </div>
        <transition name="fade" mode="out-in">
          <span v-if="!isCollapse" key="full" class="logo-text">智供链 SC</span>
          <span v-else key="short" class="logo-text-short">SCM</span>
        </transition>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="transparent"
        text-color="#94a3b8"
        active-text-color="#ffffff"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>
        <el-sub-menu index="product">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </template>
          <el-menu-item index="/products">商品列表</el-menu-item>
          <el-menu-item index="/categories">分类管理</el-menu-item>
          <el-menu-item index="/suppliers">供应商管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/purchase">
          <el-icon><ShoppingCart /></el-icon>
          <template #title>采购管理</template>
        </el-menu-item>
        <el-sub-menu index="inventory">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>库存管理</span>
          </template>
          <el-menu-item index="/inventory">库存查询</el-menu-item>
          <el-menu-item index="/inventory/records">出入库流水</el-menu-item>
          <el-menu-item index="/inventory/alerts">库存预警</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/orders">
          <el-icon><Document /></el-icon>
          <template #title>订单管理</template>
        </el-menu-item>
        <el-sub-menu index="ai">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>AI智能分析</span>
          </template>
          <el-menu-item index="/ai/prediction">销量预测</el-menu-item>
          <el-menu-item index="/ai/replenishment">补货建议</el-menu-item>
          <el-menu-item index="/ai/anomaly">异常预警</el-menu-item>
        </el-sub-menu>
        <el-menu-item v-if="userStore.role === 'ADMIN'" index="/users">
          <el-icon><User /></el-icon>
          <template #title>系统管理</template>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><Setting /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <div class="breadcrumb-area">
            <el-breadcrumb separator="/" class="breadcrumb">
              <el-breadcrumb-item :to="{ path: '/dashboard' }"
                >首页</el-breadcrumb-item
              >
              <el-breadcrumb-item
                v-for="(item, index) in breadcrumbs"
                :key="index"
              >
                {{ item }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>
        <div class="header-right">
          <el-tooltip content="全屏" placement="bottom">
            <el-icon class="header-icon" @click="toggleFullscreen"
              ><FullScreen
            /></el-icon>
          </el-tooltip>
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-info">
              <div class="user-avatar">
                {{
                  userStore.user?.realName?.charAt(0) ||
                  userStore.user?.username?.charAt(0) ||
                  "U"
                }}
              </div>
              <div class="user-detail">
                <span class="user-name">{{
                  userStore.user?.realName || userStore.user?.username
                }}</span>
                <span class="user-role">{{
                  getRoleLabel(userStore.user?.role)
                }}</span>
              </div>
              <el-icon class="user-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <el-dropdown-item command="profile">
                  <el-icon><UserFilled /></el-icon>
                  <span>个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>
                  <span>修改密码</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component, route }">
          <transition name="page" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="440px"
      class="password-dialog"
    >
      <el-form
        :model="passwordForm"
        :rules="passwordRules"
        ref="passwordFormRef"
        label-width="90px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请确认新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false" size="large"
          >取消</el-button
        >
        <el-button type="primary" @click="submitPassword" size="large"
          >确定修改</el-button
        >
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from "@/store/user";
import { logout, changePassword } from "@/api/auth";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const isCollapse = ref(false);
const passwordDialogVisible = ref(false);
const passwordFormRef = ref(null);
const passwordForm = ref({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});
const passwordRules = {
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度至少6位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error("两次输入密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

const activeMenu = computed(() => route.path);

const breadcrumbs = computed(() => {
  const map = {
    "/dashboard": ["工作台"],
    "/products": ["商品管理", "商品列表"],
    "/categories": ["商品管理", "分类管理"],
    "/suppliers": ["商品管理", "供应商管理"],
    "/purchase": ["采购管理"],
    "/purchase/create": ["采购管理", "新建采购单"],
    "/inventory": ["库存管理", "库存查询"],
    "/inventory/records": ["库存管理", "出入库流水"],
    "/inventory/alerts": ["库存管理", "库存预警"],
    "/orders": ["订单管理"],
    "/users": ["系统管理", "用户管理"],
    "/ai/prediction": ["AI智能分析", "销量预测"],
    "/ai/replenishment": ["AI智能分析", "补货建议"],
    "/ai/anomaly": ["AI智能分析", "异常预警"],
    "/profile": ["个人中心"],
  };
  return map[route.path] || [];
});

const getRoleLabel = (role) => {
  const roleMap = {
    ADMIN: "系统管理员",
    MERCHANT: "商户",
    SUPPLIER: "供应商",
  };
  return roleMap[role] || role;
};

const handleCommand = async (command) => {
  if (command === "profile") {
    router.push("/profile");
  } else if (command === "password") {
    passwordForm.value = {
      oldPassword: "",
      newPassword: "",
      confirmPassword: "",
    };
    passwordDialogVisible.value = true;
  } else if (command === "logout") {
    try {
      await ElMessageBox.confirm("确定要退出登录吗？", "提示", {
        confirmButtonText: "确定退出",
        cancelButtonText: "取消",
        type: "warning",
        confirmButtonClass: "el-button--danger",
      });
      try {
        await logout();
      } catch (e) {}
      userStore.logout();
      router.push("/login");
      ElMessage.success("退出成功");
    } catch (e) {}
  }
};

const submitPassword = async () => {
  await passwordFormRef.value.validate();
  await changePassword({
    oldPassword: passwordForm.value.oldPassword,
    newPassword: passwordForm.value.newPassword,
  });
  ElMessage.success("密码修改成功，请重新登录");
  passwordDialogVisible.value = false;
  userStore.logout();
  router.push("/login");
};

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen();
  } else {
    document.exitFullscreen();
  }
};

const handleResize = () => {
  if (window.innerWidth < 768) {
    isCollapse.value = true;
  }
};

onMounted(() => {
  handleResize();
  window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
});

watch(
  () => route.path,
  () => {
    if (window.innerWidth < 768) {
      isCollapse.value = true;
    }
  },
);
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  overflow: hidden;
}

.aside {
  background: var(--sc-gradient-sidebar);
  transition: width var(--sc-transition-slow);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 2px 0 12px rgba(15, 23, 42, 0.18);
  z-index: var(--sc-z-fixed);

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    color: #fff;
    background: linear-gradient(
      135deg,
      rgba(37, 99, 235, 0.15) 0%,
      rgba(15, 23, 42, 0.8) 100%
    );
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
    gap: 12px;
    overflow: hidden;

    .logo-icon {
      width: 36px;
      height: 36px;
      min-width: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #2563eb 0%, #4f46e5 100%);
      border-radius: var(--sc-radius-md);
      color: #fff;
      box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
    }

    .logo-text {
      font-size: 18px;
      font-weight: 700;
      background: linear-gradient(90deg, #fff 0%, #93c5fd 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      letter-spacing: 0.5px;
      white-space: nowrap;
    }

    .logo-text-short {
      font-size: 16px;
      font-weight: 700;
      color: #fff;
      letter-spacing: 1px;
    }
  }

  &.collapsed .logo {
    justify-content: center;
    padding: 0;
  }
}

.sidebar-menu {
  flex: 1;
  overflow-x: hidden;
  overflow-y: auto;
  padding: 8px 0;
  border-right: none !important;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.1);
  }

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 48px;
    line-height: 48px;
    margin: 2px 8px;
    border-radius: var(--sc-radius-base);
    transition: all var(--sc-transition-fast);

    &:hover {
      background-color: rgba(255, 255, 255, 0.06) !important;
      color: #fff !important;
    }
  }

  :deep(.el-menu-item.is-active) {
    background: linear-gradient(
      90deg,
      rgba(37, 99, 235, 0.9) 0%,
      rgba(79, 70, 229, 0.9) 100%
    );
    color: #fff !important;
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
    position: relative;

    &::before {
      content: "";
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 20px;
      background: #fff;
      border-radius: 0 2px 2px 0;
    }
  }

  :deep(.el-sub-menu .el-menu-item) {
    margin: 0 8px 0 8px;
    padding-left: 48px !important;
    min-width: auto;
    font-size: 13px;
    height: 42px;
    line-height: 42px;
  }

  :deep(.el-sub-menu__title) {
    font-weight: 500;
  }
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  z-index: var(--sc-z-sticky);
  position: relative;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    flex: 1;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: var(--sc-color-text-regular);
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: var(--sc-radius-base);
      transition: all var(--sc-transition-fast);

      &:hover {
        background-color: var(--sc-color-bg-hover);
        color: var(--sc-color-primary);
      }
    }

    .breadcrumb-area {
      flex: 1;

      .breadcrumb {
        font-size: 14px;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;

    .header-icon {
      font-size: 18px;
      cursor: pointer;
      color: var(--sc-color-text-regular);
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: var(--sc-radius-base);
      transition: all var(--sc-transition-fast);

      &:hover {
        background-color: var(--sc-color-bg-hover);
        color: var(--sc-color-primary);
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 6px 12px 6px 6px;
      border-radius: var(--sc-radius-lg);
      transition: background var(--sc-transition-fast);
      gap: 10px;

      &:hover {
        background-color: var(--sc-color-bg-hover);
      }

      .user-avatar {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background: linear-gradient(135deg, #2563eb 0%, #4f46e5 100%);
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        font-size: 14px;
        box-shadow: 0 2px 8px rgba(37, 99, 235, 0.2);
      }

      .user-detail {
        display: flex;
        flex-direction: column;
        line-height: 1.3;

        .user-name {
          color: var(--sc-color-text-primary);
          font-weight: 500;
          font-size: 14px;
        }

        .user-role {
          color: var(--sc-color-text-placeholder);
          font-size: 12px;
        }
      }

      .user-arrow {
        color: var(--sc-color-text-placeholder);
        font-size: 12px;
      }
    }
  }
}

.user-dropdown {
  :deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 16px;
    font-size: 14px;

    .el-icon {
      font-size: 16px;
    }
  }
}

.main {
  background: transparent;
  padding: 20px;
  overflow: auto;
  flex: 1;
}

.page-enter-active,
.page-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .header {
    padding: 0 12px !important;

    .header-left .breadcrumb-area {
      display: none;
    }
  }

  .main {
    padding: 12px !important;
  }

  .user-detail {
    display: none !important;
  }
}
</style>
