import { apiRequest } from './client'

export const memberApi = {
  checkId: (loginId) => apiRequest(`/api/members/check-id?loginId=${encodeURIComponent(loginId)}`),
  checkEmail: (email) => apiRequest(`/api/members/check-email?email=${encodeURIComponent(email)}`),
  verifyBusiness: (payload) =>
    apiRequest('/api/members/business/verify', { method: 'POST', body: JSON.stringify(payload) }),
  sendPhoneCode: (phone, recaptchaToken) =>
    apiRequest('/api/members/phone/send-code', { method: 'POST', body: JSON.stringify({ phone, recaptchaToken }) }),
  verifyPhone: (phone, code) =>
    apiRequest('/api/members/phone/verify', { method: 'POST', body: JSON.stringify({ phone, code }) }),
  sendEmailCode: (email, recaptchaToken) =>
    apiRequest('/api/members/email/send-code', { method: 'POST', body: JSON.stringify({ email, recaptchaToken }) }),
  verifyEmailCode: (email, code) =>
    apiRequest('/api/members/email/verify', { method: 'POST', body: JSON.stringify({ email, code }) }),
  signUp: (payload) => apiRequest('/api/members/signup', { method: 'POST', body: JSON.stringify(payload) }),
  resendVerification: (loginId) =>
    apiRequest('/api/members/resend-verification', { method: 'POST', body: JSON.stringify({ loginId }) }),
  verifyEmail: (loginId, code) =>
    apiRequest('/api/members/verify-email', { method: 'POST', body: JSON.stringify({ loginId, code }) }),
  login: (loginId, password, recaptchaToken) =>
    apiRequest('/api/auth/login', { method: 'POST', body: JSON.stringify({ loginId, password, recaptchaToken }) }),
  logout: () => apiRequest('/api/auth/logout', { method: 'POST', body: '{}' }),
  me: () => apiRequest('/api/members/me'),
  updateProfile: (payload) =>
    apiRequest('/api/members/profile', { method: 'PUT', body: JSON.stringify(payload) }),
  changePassword: (payload) =>
    apiRequest('/api/members/password', { method: 'PUT', body: JSON.stringify(payload) }),
  findLoginId: (email, recaptchaToken) =>
    apiRequest('/api/auth/forgot-id', { method: 'POST', body: JSON.stringify({ email, recaptchaToken }) }),
  forgotPassword: (email, recaptchaToken) =>
    apiRequest('/api/auth/forgot-password', { method: 'POST', body: JSON.stringify({ email, recaptchaToken }) }),
  googleAuthorizationUrl: '/oauth2/authorization/google',
  githubAuthorizationUrl: '/oauth2/authorization/github',
  kakaoAuthorizationUrl: '/oauth2/authorization/kakao',
}
