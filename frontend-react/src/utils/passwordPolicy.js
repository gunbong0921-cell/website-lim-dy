/**
 * Hexaq
 * 계층: Utils
 * 객체: passwordPolicy
 * 책임: 도메인 규칙. HTTP·DB 모름
 * 문서: [docs/features/overview.md](../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../docs/technical/01-architecture.md)
 */
export const PASSWORD_RULE_TEXT = '8~20자, 영문+숫자+특수문자 조합'

export function isStrongPassword(password) {
  if (!password || password.length < 8 || password.length > 20) {
    return false
  }
  if (/\s/.test(password)) {
    return false
  }
  return /[A-Za-z]/.test(password) && /[0-9]/.test(password) && /[^A-Za-z0-9]/.test(password)
}

export function passwordResetHint(password) {
  if (!password) {
    return ''
  }
  const missing = []
  if (password.length < 8 || password.length > 20) {
    missing.push('8~20자')
  }
  if (!/[A-Za-z]/.test(password)) {
    missing.push('영문')
  }
  if (!/[0-9]/.test(password)) {
    missing.push('숫자')
  }
  if (/\s/.test(password) || !/[^A-Za-z0-9]/.test(password)) {
    missing.push('특수문자')
  }
  if (missing.length === 0) {
    return ''
  }
  return `${PASSWORD_RULE_TEXT}이 아닙니다(${missing.join(', ')} 필요). 조건을 맞춰 다시 입력해 주세요.`
}
