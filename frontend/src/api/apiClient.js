import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle token expiration
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      localStorage.removeItem('userId');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth endpoints
export const authAPI = {
  signup: (email, password, firstName, lastName, countyId, constituencyId, wardId) =>
    apiClient.post('/auth/signup', {
      email,
      password,
      firstName,
      lastName,
      countyId,
      constituencyId,
      wardId
    }),
  login: (email, password) =>
    apiClient.post('/auth/login', { email, password }),
  googleAuth: (idToken) =>
    apiClient.post('/auth/google', { idToken }),
  logout: () => apiClient.post('/auth/logout'),
  forgotPassword: (email) =>
    apiClient.post('/auth/forgot-password', { email }),
  resetPassword: (token, newPassword) =>
    apiClient.post('/auth/reset-password', { token, newPassword }),
  getCurrentUser: () => apiClient.get('/auth/me'),
  checkToken: () => apiClient.get('/auth/check'),
};

// Profile endpoints
export const profileAPI = {
  getProfile: (userId) => apiClient.get(`/profiles/${userId}`),
  updateProfile: (userId, data) =>
    apiClient.put(`/profiles/${userId}`, data),
  completeProfile: (userId, data) =>
    apiClient.put(`/profiles/${userId}/complete`, data),
};

// Leaders endpoints
export const leadersAPI = {
  getAllLeaders: () => apiClient.get('/leaders'),
  getLeader: (id) => apiClient.get(`/leaders/${id}`),
  getLeadersByPosition: (position) =>
    apiClient.get(`/leaders/position/${position}`),
  getLeadersByCounty: (countyId) =>
    apiClient.get(`/leaders/county/${countyId}`),
  getLeadersByConstituency: (constituencyId) =>
    apiClient.get(`/leaders/constituency/${constituencyId}`),
  rateLeader: (leaderId, rating) =>
    apiClient.post(`/leaders/${leaderId}/rate`, { rating }),
};

// Locations endpoints
export const locationsAPI = {
  getCounties: () => apiClient.get('/locations/counties'),
  getConstituencies: (countyId) =>
    apiClient.get(`/locations/constituencies/${countyId}`),
  getWards: (constituencyId) =>
    apiClient.get(`/locations/wards/${constituencyId}`),
  getLocationHierarchy: (countyId) =>
    apiClient.get(`/locations/hierarchy/${countyId}`),
};

// Ratings endpoints
export const ratingsAPI = {
  getRatings: (leaderId) => apiClient.get(`/ratings/leader/${leaderId}`),
  createRating: (leaderId, rating) =>
    apiClient.post('/ratings', { leaderId, rating }),
  updateRating: (ratingId, rating) =>
    apiClient.put(`/ratings/${ratingId}`, { rating }),
};

// Comments endpoints
export const commentsAPI = {
  getComments: (leaderId, page = 0, size = 10) =>
    apiClient.get(`/comments/leader/${leaderId}`, { params: { page, size } }),
  createComment: (leaderId, comment, rating, parentId) =>
    apiClient.post('/comments', { leaderId, comment, rating, parentId }),
  deleteComment: (commentId) =>
    apiClient.delete(`/comments/${commentId}`),
};

// Polls endpoints
export const pollsAPI = {
  castVote: (candidateId, candidateType, position, countyId, constituencyId, wardId) =>
    apiClient.post('/polls/vote', { candidateId, candidateType, position, countyId, constituencyId, wardId }),
  getPollResults: (position, countyId, constituencyId, wardId) =>
    apiClient.get('/polls/results', { params: { position, countyId, constituencyId, wardId } }),
  checkUserVote: (position, countyId, constituencyId, wardId) =>
    apiClient.get('/polls/check-vote', { params: { position, countyId, constituencyId, wardId } }),
  getUserVote: (position, countyId, constituencyId, wardId) =>
    apiClient.get('/polls/user-vote', { params: { position, countyId, constituencyId, wardId } }),
  getCandidates: (position, countyId, constituencyId, wardId) =>
    apiClient.get('/polls/candidates', { params: { position, countyId, constituencyId, wardId } }),
};

// Admin endpoints
export const adminAPI = {
  // Aspirant management
  getAllAspirants: () => apiClient.get('/admin/aspirants'),
  getAspirantById: (id) => apiClient.get(`/admin/aspirants/${id}`),
  createAspirant: (data) => apiClient.post('/admin/aspirants', data),
  updateAspirant: (id, data) => apiClient.put(`/admin/aspirants/${id}`, data),
  deleteAspirant: (id) => apiClient.delete(`/admin/aspirants/${id}`),

  // Comment moderation
  getAllComments: (hidden) => apiClient.get('/admin/comments', { params: { hidden } }),
  getFlaggedComments: () => apiClient.get('/admin/comments/flagged'),
  hideComment: (id) => apiClient.put(`/admin/comments/${id}/hide`),
  unhideComment: (id) => apiClient.put(`/admin/comments/${id}/unhide`),
  deleteComment: (id) => apiClient.delete(`/admin/comments/${id}`),

  // Admin dashboard
  getAdminStats: () => apiClient.get('/admin/stats'),
  getDashboardStats: () => apiClient.get('/admin/stats'),
  getAllUsers: () => apiClient.get('/admin/users'),
  getAllLeadersAdmin: () => apiClient.get('/admin/leaders'),
  updateLeader: (leaderId, data) =>
    apiClient.put(`/admin/leaders/${leaderId}`, data),
  deleteLeader: (leaderId) =>
    apiClient.delete(`/admin/leaders/${leaderId}`),
};

export default apiClient;
