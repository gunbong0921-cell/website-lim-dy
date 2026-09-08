export async function apiRequest(path, options = {}) {
  const isForm = options.body instanceof FormData
  const response = await fetch(path, {
    credentials: 'include',
    ...options,
    headers: isForm
      ? { ...(options.headers || {}) }
      : { 'Content-Type': 'application/json', ...(options.headers || {}) },
  })

  const contentType = response.headers.get('content-type') || ''
  if (!contentType.includes('application/json')) {
    if (!response.ok) {
      throw new Error('요청에 실패했습니다.')
    }
    return response
  }

  const json = await response.json()
  if (!json.success) {
    throw new Error(json.message || '요청에 실패했습니다.')
  }
  return json
}
