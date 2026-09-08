import { apiRequest } from './client'

export const memberApi = {
  checkId: (loginId) => apiRequest(`/api/members/check-id?loginId=${encodeURIComponent(loginId)}`),
  checkEmail: (email) => apiRequest(`/api/members/check-email?email=${encodeURIComponent(email)}`),
  signUp: (payload) => apiRequest('/api/members/signup', { method: 'POST', body: JSON.stringify(payload) }),
  resendVerification: (loginId) =>
    apiRequest('/api/members/resend-verification', { method: 'POST', body: JSON.stringify({ loginId }) }),
  verifyEmail: (loginId, code) =>
    apiRequest('/api/members/verify-email', { method: 'POST', body: JSON.stringify({ loginId, code }) }),
  login: (loginId, password) =>
    apiRequest('/api/auth/login', { method: 'POST', body: JSON.stringify({ loginId, password }) }),
  logout: () => apiRequest('/api/auth/logout', { method: 'POST', body: '{}' }),
  me: () => apiRequest('/api/members/me'),
  updateProfile: (payload) =>
    apiRequest('/api/members/profile', { method: 'PUT', body: JSON.stringify(payload) }),
  changePassword: (payload) =>
    apiRequest('/api/members/password', { method: 'PUT', body: JSON.stringify(payload) }),
  forgotPassword: (email) =>
    apiRequest('/api/auth/forgot-password', { method: 'POST', body: JSON.stringify({ email }) }),
}
