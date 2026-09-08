import { apiRequest } from './client'

export const commentApi = {
  list: (boardId) => apiRequest(`/api/comments/${boardId}`),
  write: (boardId, content) =>
    apiRequest(`/api/comments/${boardId}`, { method: 'POST', body: JSON.stringify({ content }) }),
  update: (commentId, content) =>
    apiRequest(`/api/comments/item/${commentId}`, { method: 'PUT', body: JSON.stringify({ content }) }),
  remove: (commentId) => apiRequest(`/api/comments/item/${commentId}`, { method: 'DELETE' }),
}
