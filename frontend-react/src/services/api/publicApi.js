/**
 * Hexaq
 * 계층: API
 * 객체: publicApi
 * 책임: fetch 와 { success, data, message } 파싱
 * 문서: [docs/security/03-recaptcha-v3.md](../../../../docs/security/03-recaptcha-v3.md)
 */
import { apiRequest } from './client'

export const publicApi = {
  config: () => apiRequest('/api/public/config'),
  issueTicket: () => apiRequest('/api/public/request-ticket', { skipSign: true }),
}
