import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/home', component: () => import('../views/Home.vue') },
  { path: '/job/:id', component: () => import('../views/JobDetail.vue') },
  { path: '/publish', component: () => import('../views/Publish.vue') },
  { path: '/resume', component: () => import('../views/Resume.vue') },
  { path: '/chat', component: () => import('../views/Chat.vue') },
  { path: '/match', component: () => import('../views/Match.vue') }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
