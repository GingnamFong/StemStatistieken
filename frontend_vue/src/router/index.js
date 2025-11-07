import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ElectionDashboardView from "@/views/ElectionDashboardView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/login',
      name: 'login',
      component: ()=> import('../views/LoginView.vue'),
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/Register',
      name: 'Register',
      component: () => import('../views/RegisterView.vue'),
    }, {
      path: '/MapTest',
      name: 'Maptest',
      component: () => import('../views/MapTestView.vue'),
    },
    {
      path: '/Candidate',
      name: 'Candidate',
      component: () => import('../views/CandidateView.vue'),
    },
    {
      path: '/Candidate/:candidateId',
      name: 'CandidateDetail',
      component: () => import('../views/CandidateDetailView.vue'),
    },
    {
      path: '/dashboard',
      name: 'ElectionDashboard',
      component: () => import('../views/ElectionDashboardView.vue'),
    },

  ],
})

export default router
