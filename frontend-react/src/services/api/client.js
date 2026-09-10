/**
 * Hexaq
 * 계층: API
 * 객체: client
 * 책임: client 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/security/01-hmac-request-signing.md](../../../../docs/security/01-hmac-request-signing.md)
 */
import { canonicalString, hmacSha256Hex, sha256Hex } from '../../utils/requestSignature'

const SIGNED_POSTS = new Set([
  '/api/auth/login',
  '/api/members/signup',
  '/api/members/email/send-code',
  '/api/members/phone/send-code',
  '/api/auth/forgot-id',
  '/api/auth/forgot-password',
  '/api/boards/free',
])

export async function apiRequest(path, options = {}) {
  const { skipSign, ...fetchOptions } = options
  const isForm = fetchOptions.body instanceof FormData
  const method = (fetchOptions.method || 'GET').toUpperCase()
  const requestPath = path.split('?')[0]
  const headers = isForm
    ? { ...(fetchOptions.headers || {}) }
    : { 'Content-Type': 'application/json', ...(fetchOptions.headers || {}) }

  if (!skipSign && !isForm && method === 'POST' && SIGNED_POSTS.has(requestPath)) {
    const ticketRes = await apiRequest('/api/public/request-ticket', { skipSign: true })
    const ticket = ticketRes.data
    const timestamp = String(Date.now())
    const bodyStr = typeof fetchOptions.body === 'string' ? fetchOptions.body : ''
    const bodyHash = await sha256Hex(bodyStr)
    const signature = await hmacSha256Hex(
      ticket.signingKey,
      canonicalString(timestamp, method, requestPath, bodyHash)
    )
    headers['X-Hexaq-Timestamp'] = timestamp
    headers['X-Hexaq-Ticket'] = ticket.ticketId
    headers['X-Hexaq-Signature'] = signature
  }

  const response = await fetch(path, {
    credentials: 'include',
    ...fetchOptions,
    headers,
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
