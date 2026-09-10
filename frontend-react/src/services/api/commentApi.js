/**
 * Hexaq
 * 계층: API
 * 객체: commentApi
 * 책임: fetch 와 { success, data, message } 파싱
 * 문서: [docs/features/overview.md](../../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
import { apiRequest } from './client'

export const commentApi = {
  list: (boardId) => apiRequest(`/api/comments/${boardId}`),
  write: (boardId, content) =>
    apiRequest(`/api/comments/${boardId}`, { method: 'POST', body: JSON.stringify({ content }) }),
  update: (commentId, content) =>
    apiRequest(`/api/comments/item/${commentId}`, { method: 'PUT', body: JSON.stringify({ content }) }),
  remove: (commentId) => apiRequest(`/api/comments/item/${commentId}`, { method: 'DELETE' }),
}
