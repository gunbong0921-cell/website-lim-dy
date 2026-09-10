import { apiRequest } from './client'

export const publicApi = {
  config: () => apiRequest('/api/public/config'),
  issueTicket: () => apiRequest('/api/public/request-ticket', { skipSign: true }),
}
