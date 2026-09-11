/**
 * Hexaq
 * 계층: Utils Test
 * 객체: memberForm.phone.test
 * 책임: 화면 휴대폰 형식 힌트. 6자리 확인의 정본은 서버
 * 문서: [docs/security/06-identity-verification.md](../../../docs/security/06-identity-verification.md)
 */
import assert from 'node:assert/strict'
import { describe, it } from 'node:test'

import { isMobilePhone } from './memberForm.js'

describe('isMobilePhone', () => {
  it('010 계열 10~11자리를 통과한다', () => {
    assert.equal(isMobilePhone('010-1234-5678'), true)
    assert.equal(isMobilePhone('0101234567'), true)
  })

  it('유선·빈 값은 거절한다', () => {
    assert.equal(isMobilePhone('02-123-4567'), false)
    assert.equal(isMobilePhone(''), false)
  })
})
