
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/admin-login',
    name: 'AdminLogin',
    component: () => import('../views/AdminLogin.vue')
  },
  {
    path: '/books',
    name: 'Books',
    component: () => import('../views/Books.vue')
  },
  {
    path: '/books/:id',
    name: 'BookDetail',
    component: () => import('../views/BookDetail.vue')
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/Cart.vue')
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/Orders.vue')
  },
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: () => import('../views/OrderDetail.vue')
  },
  {
    path: '/after-sales',
    name: 'AfterSales',
    component: () => import('../views/AfterSales.vue')
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('../views/Checkout.vue')
  },
  {
    path: '/buy-now',
    name: 'BuyNow',
    component: () => import('../views/BuyNow.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue')
  },
  {
    path: '/vip',
    name: 'VIP',
    component: () => import('../views/VIP.vue')
  },
  {
    path: '/payment',
    name: 'Payment',
    component: () => import('../views/Payment.vue')
  },
  {
    path: '/scan-payment',
    name: 'ScanPayment',
    component: () => import('../views/ScanPayment.vue')
  },
  {
    path: '/addresses',
    name: 'Addresses',
    component: () => import('../views/Addresses.vue')
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('../views/Favorites.vue')
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue')
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('../views/Search.vue')
  },
  {
    path: '/rankings',
    name: 'Rankings',
    component: () => import('../views/Rankings.vue')
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('../views/Messages.vue')
  },
  {
    path: '/notes',
    name: 'Notes',
    component: () => import('../views/Notes.vue')
  },
  {
    path: '/coupons',
    name: 'Coupons',
    component: () => import('../views/Coupons.vue')
  },
  {
    path: '/points',
    name: 'Points',
    component: () => import('../views/Points.vue')
  },
  {
    path: '/checkin',
    name: 'CheckIn',
    component: () => import('../views/CheckIn.vue')
  },
  {
    path: '/design-preview',
    name: 'DesignPreview',
    component: () => import('../views/DesignPreview.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const requiresAuth = ['AfterSales', 'Profile', 'Orders', 'OrderDetail', 'Checkout', 'Payment', 'ScanPayment', 'Addresses', 'VIP', 'Favorites', 'Notes', 'Messages', 'Coupons', 'Points', 'CheckIn']
  const requiresAdmin = ['Admin']
  const publicPages = ['Login', 'Register', 'AdminLogin']
  const isLoggedIn = localStorage.getItem('token')
  const userRole = localStorage.getItem('role')

  if (requiresAdmin.includes(to.name) && (!isLoggedIn || userRole?.toUpperCase() !== 'ADMIN')) {
    next('/admin-login')
  } else if (requiresAuth.includes(to.name) && !isLoggedIn) {
    next('/login')
  } else if (publicPages.includes(to.name) && isLoggedIn && to.name !== 'AdminLogin') {
    next('/')
  } else {
    next()
  }
})

export default router
