import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/views/layout/LayoutContainer.vue'),
      redirect: '/room', // 默认重定向到机房1的开关页面
      children: [
        {
          path:"/table",
          component: () => import('@/views/table/index.vue'),
        },
        {
          path: '/room',
          component: () => import('@/views/room/index.vue'),
        },
        // 机房1的路由
        {
          path: '/room1/kg',
          component: () => import('@/views/kg/index.vue'),
          meta: { roomId: 1 }
        },
        {
          path: '/room1/wsd',
          component: () => import('@/views/wsd/index.vue'),
          meta: { roomId: 1 }
        },
        {
          path: '/room1/wd',
          component: () => import('@/views/wd/index.vue'),
          meta: { roomId: 1 }
        },
        {
          path: '/room1/sd',
          component: () => import('@/views/sd/index.vue'),
          meta: { roomId: 1 }
        },
        {
          path: '/room1/gg',
          component: () => import('@/views/gg/index.vue'),
          meta: { roomId: 1 }
        },
        {
          path: '/room1/dg',
          component: () => import('@/views/dg/index.vue'),
          meta: { roomId: 1 }
        },
        {
          path: '/room1/sxt',
          component: () => import('@/views/sxt/index.vue'),
          meta: { roomId: 1 }
        },

        // 机房2的路由
        {
          path: '/room2/kg',
          component: () => import('@/views/kg/index.vue'),
          meta: { roomId: 2 }
        },
        {
          path: '/room2/wsd',
          component: () => import('@/views/wsd/index.vue'),
          meta: { roomId: 2 }
        },
        {
          path: '/room2/wd',
          component: () => import('@/views/wd/index.vue'),
          meta: { roomId: 2 }
        },
        {
          path: '/room2/sd',
          component: () => import('@/views/sd/index.vue'),
          meta: { roomId: 2 }
        },
        {
          path: '/room2/gg',
          component: () => import('@/views/gg/index.vue'),
          meta: { roomId: 2 }
        },
        {
          path: '/room2/dg',
          component: () => import('@/views/dg/index.vue'),
          meta: { roomId: 2 }
        },
        {
          path: '/room2/sxt',
          component: () => import('@/views/sxt/index.vue'),
          meta: { roomId: 2 }
        },

        // 机房3的路由
        {
          path: '/room3/kg',
          component: () => import('@/views/kg/index.vue'),
          meta: { roomId: 3 }
        },
        {
          path: '/room3/wsd',
          component: () => import('@/views/wsd/index.vue'),
          meta: { roomId: 3 }
        },
        {
          path: '/room3/wd',
          component: () => import('@/views/wd/index.vue'),
          meta: { roomId: 3 }
        },
        {
          path: '/room3/sd',
          component: () => import('@/views/sd/index.vue'),
          meta: { roomId: 3 }
        },
        {
          path: '/room3/gg',
          component: () => import('@/views/gg/index.vue'),
          meta: { roomId: 3 }
        },
        {
          path: '/room3/dg',
          component: () => import('@/views/dg/index.vue'),
          meta: { roomId: 3 }
        },
        {
          path: '/room3/sxt',
          component: () => import('@/views/sxt/index.vue'),
          meta: { roomId: 3 }
        }
      ],
    }
  ],
})

export default router
