import { apiRequest } from './client'

function qs({ page, size, searchType, keyword, solution }) {
  const params = new URLSearchParams()
  params.set('page', String(page || 1))
  params.set('size', String(size || 10))
  if (searchType) params.set('searchType', searchType)
  if (keyword) params.set('keyword', keyword)
  if (solution) params.set('solution', solution)
  return params.toString()
}

export const boardApi = {
  list: (type, query) => apiRequest(`/api/boards/${type}?${qs(query)}`),
  read: (type, id, query) => {
    const params = new URLSearchParams()
    if (query?.solution) params.set('solution', query.solution)
    const suffix = params.toString()
    return apiRequest(`/api/boards/${type}/${id}${suffix ? `?${suffix}` : ''}`)
  },
  writeFree: (payload) => apiRequest('/api/boards/free', { method: 'POST', body: JSON.stringify(payload) }),
  updateFree: (id, payload) =>
    apiRequest(`/api/boards/free/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteFree: (id, password) =>
    apiRequest(`/api/boards/free/${id}`, { method: 'DELETE', body: JSON.stringify({ password }) }),
  writeQna: (payload) => apiRequest('/api/boards/qna', { method: 'POST', body: JSON.stringify(payload) }),
  updateQna: (id, payload) =>
    apiRequest(`/api/boards/qna/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteQna: (id) => apiRequest(`/api/boards/qna/${id}`, { method: 'DELETE' }),
  writeArchive: (formData) => apiRequest('/api/boards/archive', { method: 'POST', body: formData }),
  updateArchive: (id, formData) => apiRequest(`/api/boards/archive/${id}`, { method: 'PUT', body: formData }),
  deleteArchive: (id) => apiRequest(`/api/boards/archive/${id}`, { method: 'DELETE' }),
  like: (type, id) => apiRequest(`/api/boards/${type}/${id}/like`, { method: 'POST', body: '{}' }),
}
