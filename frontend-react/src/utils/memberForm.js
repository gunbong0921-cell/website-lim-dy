/**
 * Hexaq
 * 계층: Utils
 * 객체: memberForm
 * 책임: memberForm 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../docs/technical/01-architecture.md)
 */
export function digitsOnly(value, max = 11) {
  return (value || '').replace(/\D/g, '').slice(0, max)
}

export function formatBusinessNumber(value) {
  const digits = digitsOnly(value, 10)
  if (digits.length <= 3) return digits
  if (digits.length <= 5) return `${digits.slice(0, 3)}-${digits.slice(3)}`
  return `${digits.slice(0, 3)}-${digits.slice(3, 5)}-${digits.slice(5)}`
}

export function isBusinessNumber(value) {
  return digitsOnly(value, 10).length === 10
}

export function isMobilePhone(value) {
  return /^01[016789]\d{7,8}$/.test(digitsOnly(value, 11))
}

export function isPersonalEmail(email) {
  return /@(gmail|naver|daum|hanmail|kakao|hotmail|yahoo|outlook)\./i.test(email || '')
}

export function isValidEmailFormat(email) {
  const value = (email || '').trim()
  if (!value || value.length > 100 || value.includes('..')) {
    return false
  }
  return /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(value)
}
