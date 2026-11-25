import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('account')
      localStorage.removeItem('username')
      localStorage.removeItem('userId')
      localStorage.removeItem('discount')
      localStorage.removeItem('role')
      // 避免在登录页重复跳转
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export const authAPI = {
  login: (data) => api.post('/api/auth/login', data),
  register: (data) => api.post('/api/auth/register', data),
  getUser: () => api.get('/api/auth/user'),
  updateUser: (data) => api.put('/api/auth/user', data),
  changePassword: (data) => api.post('/api/auth/change-password', data)
}

export const bookAPI = {
  getAll: (params) => api.get('/api/books', { params }),
  getById: (id) => api.get(`/api/books/${id}`),
  getByCategory: (categoryId) => api.get('/api/books', { params: { categoryId } })
}

export const categoryAPI = {
  getAll: () => api.get('/api/categories')
}

export const cartAPI = {
  getCart: () => api.get('/api/cart'),
  addToCart: (data) => api.post('/api/cart/add', null, { params: data }),
  updateCartItem: (cartItemId, data) => api.put(`/api/cart/${cartItemId}`, data),
  removeFromCart: (cartItemId) => api.delete(`/api/cart/${cartItemId}`),
  clearCart: () => api.delete('/api/cart/clear')
}

export const orderAPI = {
  create: (data) => api.post('/api/orders', data),
  createPendingOrder: (data) => api.post('/api/orders/pending', data),
  getAll: () => api.get('/api/orders'),
  getById: (id) => api.get(`/api/orders/${id}`),
  delete: (id) => api.delete(`/api/orders/${id}`),
  updateStatus: (id, status) => api.put(`/api/orders/${id}/status`, { status })
}

export const addressAPI = {
  getAll: () => api.get('/api/addresses'),
  getById: (id) => api.get(`/api/addresses/${id}`),
  create: (data) => api.post('/api/addresses', data),
  update: (id, data) => api.put(`/api/addresses/${id}`, data),
  delete: (id) => api.delete(`/api/addresses/${id}`),
  setDefault: (id) => api.post(`/api/addresses/${id}/default`)
}

export const adminAPI = {
  getDashboard: () => api.get('/api/admin/dashboard'),
  getAllUsers: () => api.get('/api/admin/users'),
  updateUserRole: (userId, role) => api.put(`/api/admin/users/${userId}/role`, { role }),
  deleteUser: (userId) => api.delete(`/api/admin/users/${userId}`),
  getAllOrders: () => api.get('/api/admin/orders'),
  shipOrder: (orderId) => api.put(`/api/admin/orders/${orderId}/ship`),
  completeOrder: (orderId) => api.put(`/api/admin/orders/${orderId}/complete`),
  cancelOrder: (orderId) => api.put(`/api/admin/orders/${orderId}/cancel`),
  getAllBooks: () => api.get('/api/admin/books'),
  createBook: (data) => api.post('/api/admin/books', data),
  updateBook: (id, data) => api.put(`/api/admin/books/${id}`, data),
  deleteBook: (id) => api.delete(`/api/admin/books/${id}`),
  batchUpdatePrice: (data) => api.put('/api/admin/books/price', data)
}

export const reviewAPI = {
  getByBook: (bookId) => api.get(`/api/reviews/book/${bookId}`),
  getStats: (bookId) => api.get(`/api/reviews/book/${bookId}/stats`),
  getMyReviews: () => api.get('/api/reviews/my'),
  create: (data) => api.post('/api/reviews', data),
  update: (id, data) => api.put(`/api/reviews/${id}`, data),
  delete: (id) => api.delete(`/api/reviews/${id}`)
}

export const favoriteAPI = {
  getAll: () => api.get('/api/favorites'),
  check: (bookId) => api.get(`/api/favorites/check/${bookId}`),
  add: (bookId) => api.post(`/api/favorites/${bookId}`),
  remove: (bookId) => api.delete(`/api/favorites/${bookId}`)
}

export const tagAPI = {
  getAll: () => api.get('/api/tags'),
  getByBook: (bookId) => api.get(`/api/tags/book/${bookId}`),
  create: (name) => api.post('/api/tags', { name }),
  addToBook: (bookId, tagId) => api.post(`/api/tags/book/${bookId}`, { tagId }),
  removeFromBook: (bookId, tagId) => api.delete(`/api/tags/book/${bookId}/${tagId}`)
}

export const couponAPI = {
  getAvailable: () => api.get('/api/coupons/available'),
  getMyCoupons: () => api.get('/api/coupons/my'),
  claim: (couponId) => api.post(`/api/coupons/${couponId}/claim`),
  calculate: (data) => api.post('/api/coupons/calculate', data)
}

export const pointsAPI = {
  getMyPoints: () => api.get('/api/points'),
  getLog: () => api.get('/api/points/log')
}

export const checkInAPI = {
  getStatus: () => api.get('/api/checkin/status'),
  checkIn: () => api.post('/api/checkin')
}

export const noteAPI = {
  getByBook: (bookId) => api.get(`/api/notes/book/${bookId}`),
  getMyNotes: () => api.get('/api/notes/my'),
  create: (data) => api.post('/api/notes', data),
  update: (id, data) => api.put(`/api/notes/${id}`, data),
  delete: (id) => api.delete(`/api/notes/${id}`),
  like: (id) => api.post(`/api/notes/${id}/like`)
}

export const messageAPI = {
  getAll: () => api.get('/api/messages'),
  getUnreadCount: () => api.get('/api/messages/unread-count'),
  markAsRead: (id) => api.put(`/api/messages/${id}/read`),
  markAllAsRead: () => api.put('/api/messages/read-all')
}

export const bannerAPI = {
  getActive: () => api.get('/api/banners/active')
}

export const quoteAPI = {
  getRandom: () => api.get('/api/quotes/random'),
  getByBook: (bookId) => api.get(`/api/quotes/book/${bookId}`)
}

export const rankingAPI = {
  getSales: () => api.get('/api/rankings/sales'),
  getRating: () => api.get('/api/rankings/rating'),
  getNew: () => api.get('/api/rankings/new')
}

export const recommendAPI = {
  guessYouLike: () => api.get('/api/recommend/guess-you-like'),
  sameCategory: (bookId) => api.get(`/api/recommend/same-category/${bookId}`)
}

export default api
