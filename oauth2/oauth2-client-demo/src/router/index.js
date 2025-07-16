import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
const AuthorizationCodeView = () => import('@/views/AuthorizationCodeView.vue')
const ImplicitView = () => import('@/views/ImplicitView.vue')
const ClientCredentialsView = () => import('@/views/ClientCredentialsView.vue')
const ResourceOwnerPasswordView = () => import('@/views/ResourceOwnerPasswordView.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/authorization-code',
      name: 'authorization-code',
      component: AuthorizationCodeView,
    },
    {
      path: '/implicit',
      name: 'implicit',
      component: ImplicitView,
    },
    {
      path: '/client-credentials',
      name: 'client-credentials',
      component: ClientCredentialsView,
    },
    {
      path: '/resource-owner-password',
      name: 'resource-owner-password',
      component: ResourceOwnerPasswordView,
    },
  ],
})

export default router
