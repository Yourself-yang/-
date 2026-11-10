import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { noAuth: true } },
  {
    path: '/',
    component: () => import('@/layout/Index.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue') },
      { path: 'products', name: 'ProductList', component: () => import('@/views/product/ProductList.vue') },
      { path: 'categories', name: 'CategoryList', component: () => import('@/views/product/CategoryList.vue') },
      { path: 'suppliers', name: 'SupplierList', component: () => import('@/views/product/SupplierList.vue') },
      { path: 'purchase', name: 'PurchaseList', component: () => import('@/views/purchase/PurchaseList.vue') },
      { path: 'purchase/create', name: 'PurchaseCreate', component: () => import('@/views/purchase/PurchaseCreate.vue') },
      { path: 'inventory', name: 'InventoryList', component: () => import('@/views/inventory/InventoryList.vue') },
      { path: 'inventory/records', name: 'InventoryRecords', component: () => import('@/views/inventory/InventoryRecords.vue') },
      { path: 'inventory/alerts', name: 'InventoryAlerts', component: () => import('@/views/inventory/InventoryAlerts.vue') },
      { path: 'orders', name: 'OrderList', component: () => import('@/views/order/OrderList.vue') },
      { path: 'users', name: 'UserList', component: () => import('@/views/system/UserList.vue'), meta: { roles: ['ADMIN'] } },
      { path: 'ai/prediction', name: 'Prediction', component: () => import('@/views/ai/Prediction.vue') },
      { path: 'ai/replenishment', name: 'Replenishment', component: () => import('@/views/ai/Replenishment.vue') },
      { path: 'ai/anomaly', name: 'Anomaly', component: () => import('@/views/ai/Anomaly.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.roles && userStore.user && !to.meta.roles.includes(userStore.user.role)) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
