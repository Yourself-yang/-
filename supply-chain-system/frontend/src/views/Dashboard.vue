<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-left">
        <div class="welcome-avatar">
          <el-icon :size="28"><Sunny /></el-icon>
        </div>
        <div class="welcome-text">
          <h2>
            {{ getGreeting() }}，{{
              userStore.user?.realName || userStore.user?.username
            }}
            👋
          </h2>
          <p>今天是 {{ currentDate }}，祝您工作顺利！</p>
        </div>
      </div>
      <div class="welcome-stats">
        <div class="welcome-stat">
          <span class="stat-label">今日处理</span>
          <span class="stat-value">{{
            dashboardData.stats?.pendingOrders || 0
          }}</span>
        </div>
        <div class="welcome-stat">
          <span class="stat-label">待办事项</span>
          <span class="stat-value">{{
            (dashboardData.stats?.pendingPO || 0) +
            (dashboardData.stats?.lowStock || 0)
          }}</span>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col
        :xs="12"
        :sm="12"
        :md="8"
        :lg="6"
        v-for="(item, index) in statCards"
        :key="index"
      >
        <el-card shadow="never" class="stat-card" :class="'stat-card-' + index">
          <div class="stat-header">
            <div class="stat-icon-wrapper">
              <div class="stat-icon-bg"></div>
              <el-icon :size="24" class="stat-icon"
                ><component :is="item.icon"
              /></el-icon>
            </div>
            <div class="stat-trend" v-if="item.trend !== undefined">
              <el-icon :size="14"
                ><component :is="item.trend > 0 ? 'Top' : 'Bottom'"
              /></el-icon>
              <span>{{ Math.abs(item.trend) }}%</span>
            </div>
          </div>
          <div class="stat-content">
            <div class="stat-value">
              <span class="number-animate">{{ item.value }}</span>
              <span class="stat-unit" v-if="item.unit">{{ item.unit }}</span>
            </div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
          <div class="stat-progress">
            <el-progress
              :percentage="item.progress || 0"
              :show-text="false"
              :stroke-width="3"
              :color="item.color"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :lg="16" :md="24">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <span class="title-dot"></span>
                近7天销售趋势
              </div>
              <div class="card-actions">
                <el-radio-group v-model="trendPeriod" size="small">
                  <el-radio-button value="7">近7天</el-radio-button>
                  <el-radio-button value="30">近30天</el-radio-button>
                  <el-radio-button value="90">近90天</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :lg="8" :md="24">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <span class="title-dot"></span>
                热门商品销量
              </div>
              <div class="card-actions">
                <el-button text type="primary" size="small">查看更多</el-button>
              </div>
            </div>
          </template>
          <div ref="hotChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部数据行 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :lg="12" :md="24">
        <el-card shadow="never" class="list-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <span class="title-dot"></span>
                待审核采购单
              </div>
              <el-button text type="primary" size="small">全部</el-button>
            </div>
          </template>
          <div class="simple-list">
            <div class="simple-list-item" v-for="i in 3" :key="i">
              <div class="list-item-left">
                <div class="list-item-avatar">
                  <el-icon><ShoppingCart /></el-icon>
                </div>
                <div class="list-item-content">
                  <div class="list-item-title">PO2026082{{ 20 + i }}001</div>
                  <div class="list-item-desc">
                    优选食品有限公司 · ¥{{ 3000 + i * 500 }}
                  </div>
                </div>
              </div>
              <el-tag type="warning" size="small">待审核</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :lg="12" :md="24">
        <el-card shadow="never" class="list-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <span class="title-dot"></span>
                库存预警
              </div>
              <el-button text type="primary" size="small">全部</el-button>
            </div>
          </template>
          <div class="simple-list">
            <div
              class="simple-list-item"
              v-for="(item, idx) in lowStockItems"
              :key="idx"
            >
              <div class="list-item-left">
                <div class="list-item-avatar danger">
                  <el-icon><Warning /></el-icon>
                </div>
                <div class="list-item-content">
                  <div class="list-item-title">{{ item.name }}</div>
                  <div class="list-item-desc">
                    当前库存：{{ item.stock }} / 安全库存：{{ item.safe }}
                  </div>
                </div>
              </div>
              <el-tag type="danger" size="small">库存不足</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from "vue";
import * as echarts from "echarts";
import { useUserStore } from "@/store/user";
import { dashboard } from "@/api/ai";

const userStore = useUserStore();

const trendChartRef = ref(null);
const hotChartRef = ref(null);
const trendPeriod = ref("7");
let trendChart = null;
let hotChart = null;

const currentDate = new Date().toLocaleDateString("zh-CN", {
  year: "numeric",
  month: "long",
  day: "numeric",
  weekday: "long",
});

const statCards = reactive([
  {
    label: "商品总数",
    value: 0,
    icon: "Goods",
    color: "#3b82f6",
    unit: "",
    progress: 0,
  },
  {
    label: "低库存商品",
    value: 0,
    icon: "Warning",
    color: "#f59e0b",
    unit: "",
    progress: 0,
  },
  {
    label: "待审核采购单",
    value: 0,
    icon: "Document",
    color: "#10b981",
    unit: "",
    progress: 0,
  },
  {
    label: "待处理订单",
    value: 0,
    icon: "List",
    color: "#8b5cf6",
    unit: "",
    progress: 0,
  },
  {
    label: "今日销售额",
    value: "0.00",
    icon: "Money",
    color: "#ef4444",
    unit: "元",
    progress: 0,
  },
  {
    label: "未处理预警",
    value: 0,
    icon: "Bell",
    color: "#ec4899",
    unit: "",
    progress: 0,
  },
]);

const lowStockItems = ref([
  { name: "蓝月亮洗衣液3kg", stock: 5, safe: 20 },
  { name: "良品铺子猪肉脯100g", stock: 0, safe: 50 },
]);

const dashboardData = ref({
  stats: {},
  trend: [],
  hotProducts: [],
});

const getGreeting = () => {
  const hour = new Date().getHours();
  if (hour < 6) return "凌晨好";
  if (hour < 9) return "早上好";
  if (hour < 12) return "上午好";
  if (hour < 14) return "中午好";
  if (hour < 18) return "下午好";
  if (hour < 22) return "晚上好";
  return "夜深了";
};

const animateNumber = (
  target,
  finalValue,
  duration = 1000,
  isDecimal = false,
) => {
  const startTime = performance.now();
  const startValue = 0;

  const update = (currentTime) => {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    const easeProgress = 1 - Math.pow(1 - progress, 3);
    const currentValue = startValue + (finalValue - startValue) * easeProgress;

    if (isDecimal) {
      target.value = currentValue.toFixed(2);
    } else {
      target.value = Math.floor(currentValue);
    }

    if (progress < 1) {
      requestAnimationFrame(update);
    }
  };

  requestAnimationFrame(update);
};

const initCharts = () => {
  trendChart = echarts.init(trendChartRef.value);
  hotChart = echarts.init(hotChartRef.value);

  const trendOption = {
    tooltip: {
      trigger: "axis",
      backgroundColor: "rgba(255, 255, 255, 0.95)",
      borderColor: "#e2e8f0",
      borderWidth: 1,
      textStyle: { color: "#1e293b" },
      padding: [10, 14],
      axisPointer: {
        type: "line",
        lineStyle: { color: "#94a3b8", type: "dashed" },
      },
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      top: "15%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: [],
      axisLine: { lineStyle: { color: "#e2e8f0" } },
      axisLabel: { color: "#64748b", fontSize: 12 },
      axisTick: { show: false },
    },
    yAxis: {
      type: "value",
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: "#f1f5f9", type: "dashed" } },
      axisLabel: { color: "#64748b", fontSize: 12 },
    },
    series: [
      {
        name: "销售额",
        type: "line",
        smooth: true,
        symbol: "circle",
        symbolSize: 6,
        showSymbol: false,
        lineStyle: { width: 3, color: "#3b82f6" },
        itemStyle: { color: "#3b82f6", borderColor: "#fff", borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(59, 130, 246, 0.25)" },
            { offset: 1, color: "rgba(59, 130, 246, 0.02)" },
          ]),
        },
        data: [],
      },
    ],
  };

  const hotOption = {
    tooltip: {
      trigger: "axis",
      backgroundColor: "rgba(255, 255, 255, 0.95)",
      borderColor: "#e2e8f0",
      borderWidth: 1,
      textStyle: { color: "#1e293b" },
      padding: [10, 14],
    },
    grid: {
      left: "3%",
      right: "8%",
      bottom: "15%",
      top: "10%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: [],
      axisLine: { lineStyle: { color: "#e2e8f0" } },
      axisLabel: { color: "#64748b", fontSize: 11, rotate: 25, interval: 0 },
      axisTick: { show: false },
    },
    yAxis: {
      type: "value",
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: "#f1f5f9", type: "dashed" } },
      axisLabel: { color: "#64748b", fontSize: 12 },
    },
    series: [
      {
        name: "销量",
        type: "bar",
        barWidth: "50%",
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "#10b981" },
            { offset: 1, color: "#34d399" },
          ]),
        },
        data: [],
      },
    ],
  };

  trendChart.setOption(trendOption);
  hotChart.setOption(hotOption);
};

const updateCharts = () => {
  const trend = dashboardData.value.trend || [];
  trendChart.setOption({
    xAxis: { data: trend.map((t) => t.date) },
    series: [{ data: trend.map((t) => t.sales) }],
  });

  const hotProducts = dashboardData.value.hotProducts || [];
  hotChart.setOption({
    xAxis: { data: hotProducts.map((p) => p.productName) },
    series: [{ data: hotProducts.map((p) => p.sales) }],
  });
};

const loadData = async () => {
  try {
    const data = await dashboard();
    dashboardData.value = data;
    const stats = data.stats || {};

    nextTick(() => {
      animateNumber(statCards[0], stats.products || 0);
      animateNumber(statCards[1], stats.lowStock || 0);
      animateNumber(statCards[2], stats.pendingPO || 0);
      animateNumber(statCards[3], stats.pendingOrders || 0);
      animateNumber(statCards[4], stats.todaySales || 0, 1000, true);
      animateNumber(statCards[5], stats.unhandledAlerts || 0);

      statCards[0].progress = Math.min(((stats.products || 0) / 50) * 100, 100);
      statCards[1].progress = Math.min(((stats.lowStock || 0) / 20) * 100, 100);
      statCards[2].progress = Math.min(
        ((stats.pendingPO || 0) / 30) * 100,
        100,
      );
      statCards[3].progress = Math.min(
        ((stats.pendingOrders || 0) / 20) * 100,
        100,
      );
      statCards[4].progress = Math.min(
        ((stats.todaySales || 0) / 10000) * 100,
        100,
      );
      statCards[5].progress = Math.min(
        ((stats.unhandledAlerts || 0) / 50) * 100,
        100,
      );

      updateCharts();
    });
  } catch (e) {
    console.error("Dashboard load error:", e);
  }
};

const handleResize = () => {
  trendChart?.resize();
  hotChart?.resize();
};

onMounted(() => {
  initCharts();
  loadData();
  window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
  trendChart?.dispose();
  hotChart?.dispose();
});
</script>

<style scoped lang="scss">
.dashboard {
  .welcome-banner {
    background: linear-gradient(135deg, #1e40af 0%, #3b82f6 50%, #8b5cf6 100%);
    border-radius: var(--sc-radius-xl);
    padding: var(--sc-spacing-xl);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: var(--sc-spacing-lg);
    position: relative;
    overflow: hidden;

    &::before {
      content: "";
      position: absolute;
      top: -50%;
      right: -10%;
      width: 300px;
      height: 300px;
      background: radial-gradient(
        circle,
        rgba(255, 255, 255, 0.1) 0%,
        transparent 70%
      );
      border-radius: 50%;
    }

    .welcome-left {
      display: flex;
      align-items: center;
      gap: var(--sc-spacing-lg);
      position: relative;
      z-index: 1;

      .welcome-avatar {
        width: 56px;
        height: 56px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.25);
      }

      .welcome-text {
        h2 {
          font-size: 20px;
          font-weight: 600;
          margin: 0 0 4px 0;
        }
        p {
          font-size: 13px;
          opacity: 0.85;
          margin: 0;
        }
      }
    }

    .welcome-stats {
      display: flex;
      gap: var(--sc-spacing-2xl);
      position: relative;
      z-index: 1;

      .welcome-stat {
        text-align: center;

        .stat-label {
          display: block;
          font-size: 12px;
          opacity: 0.8;
          margin-bottom: 4px;
        }
        .stat-value {
          display: block;
          font-size: 24px;
          font-weight: 700;
        }
      }
    }
  }

  .stat-cards {
    margin-bottom: var(--sc-spacing-lg);

    :deep(.el-col) {
      margin-bottom: var(--sc-spacing-lg);
    }
  }

  .stat-card {
    border-radius: var(--sc-radius-lg);
    border: 1px solid var(--sc-color-border-light);
    transition: all 0.3s ease;
    cursor: pointer;
    overflow: hidden;
    position: relative;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 24px -8px rgba(0, 0, 0, 0.1);
      border-color: var(--sc-color-border);

      .stat-icon-wrapper {
        transform: scale(1.1) rotate(5deg);
      }
    }

    :deep(.el-card__body) {
      padding: var(--sc-spacing-lg);
    }

    .stat-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: var(--sc-spacing-md);

      .stat-icon-wrapper {
        position: relative;
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: transform 0.3s ease;

        .stat-icon-bg {
          position: absolute;
          inset: 0;
          border-radius: 12px;
          opacity: 0.15;
        }

        .stat-icon {
          position: relative;
          z-index: 1;
        }
      }

      .stat-trend {
        display: flex;
        align-items: center;
        gap: 2px;
        font-size: 12px;
        font-weight: 500;
        color: var(--sc-color-success);
      }
    }

    .stat-content {
      margin-bottom: var(--sc-spacing-sm);

      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: var(--sc-color-text-primary);
        line-height: 1.2;
        margin-bottom: 4px;

        .number-animate {
          font-variant-numeric: tabular-nums;
        }

        .stat-unit {
          font-size: 14px;
          font-weight: 500;
          margin-left: 2px;
        }
      }

      .stat-label {
        font-size: 13px;
        color: var(--sc-color-text-secondary);
      }
    }

    .stat-progress {
      margin-top: var(--sc-spacing-sm);
    }

    &.stat-card-0 {
      .stat-icon-bg {
        background: #3b82f6;
      }
      .stat-icon {
        color: #3b82f6;
      }
    }
    &.stat-card-1 {
      .stat-icon-bg {
        background: #f59e0b;
      }
      .stat-icon {
        color: #f59e0b;
      }
    }
    &.stat-card-2 {
      .stat-icon-bg {
        background: #10b981;
      }
      .stat-icon {
        color: #10b981;
      }
    }
    &.stat-card-3 {
      .stat-icon-bg {
        background: #8b5cf6;
      }
      .stat-icon {
        color: #8b5cf6;
      }
    }
    &.stat-card-4 {
      .stat-icon-bg {
        background: #ef4444;
      }
      .stat-icon {
        color: #ef4444;
      }
    }
    &.stat-card-5 {
      .stat-icon-bg {
        background: #ec4899;
      }
      .stat-icon {
        color: #ec4899;
      }
    }
  }

  .charts-row {
    margin-bottom: var(--sc-spacing-lg);

    :deep(.el-col) {
      margin-bottom: var(--sc-spacing-lg);
    }
  }

  .chart-card {
    border-radius: var(--sc-radius-lg);
    height: 100%;

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .card-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 15px;
        font-weight: 600;
        color: var(--sc-color-text-primary);

        .title-dot {
          width: 4px;
          height: 16px;
          background: linear-gradient(180deg, #3b82f6, #8b5cf6);
          border-radius: 2px;
        }
      }
    }

    .chart-container {
      height: 320px;
      width: 100%;
    }
  }

  .bottom-row {
    .list-card {
      border-radius: var(--sc-radius-lg);

      .simple-list {
        .simple-list-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 12px 0;
          border-bottom: 1px solid var(--sc-color-border-light);
          transition: background 0.2s ease;

          &:last-child {
            border-bottom: none;
            padding-bottom: 0;
          }

          &:first-child {
            padding-top: 0;
          }

          &:hover {
            background: var(--sc-color-bg-hover);
            margin: 0 -12px;
            padding-left: 12px;
            padding-right: 12px;
            border-radius: var(--sc-radius-base);
          }

          .list-item-left {
            display: flex;
            align-items: center;
            gap: 12px;

            .list-item-avatar {
              width: 40px;
              height: 40px;
              background: rgba(59, 130, 246, 0.1);
              color: #3b82f6;
              border-radius: 10px;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 18px;

              &.danger {
                background: rgba(239, 68, 68, 0.1);
                color: #ef4444;
              }
            }

            .list-item-content {
              .list-item-title {
                font-size: 14px;
                font-weight: 500;
                color: var(--sc-color-text-primary);
                margin-bottom: 2px;
              }
              .list-item-desc {
                font-size: 12px;
                color: var(--sc-color-text-secondary);
              }
            }
          }
        }
      }
    }
  }

  @media (max-width: 768px) {
    .welcome-banner {
      flex-direction: column;
      gap: 16px;
      text-align: center;

      .welcome-left {
        flex-direction: column;
        text-align: center;
      }
    }

    .stat-value {
      font-size: 22px !important;
    }

    .chart-container {
      height: 280px !important;
    }
  }
}
</style>
