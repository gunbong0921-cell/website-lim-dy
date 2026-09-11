/**
 * Hexaq
 * 계층: Utils Test
 * 객체: memberForm.email.test
 * 책임: 화면 힌트 형식. 차단의 정본은 서버
 * 문서: [docs/security/05-disposable-email.md](../../../docs/security/05-disposable-email.md)
 */
import assert from 'node:assert/strict'
import { describe, it } from 'node:test'

import { isValidEmailFormat } from './memberForm.js'

describe('isValidEmailFormat', () => {
  it('정상적인 이메일은 통과한다', () => {
    assert.equal(isValidEmailFormat('user@gmail.com'), true)
  })

  it('빈 값·길이 초과·연속 점은 거절한다', () => {
    assert.equal(isValidEmailFormat(''), false)
    assert.equal(isValidEmailFormat('a'.repeat(91) + '@gmail.com'), false)
    assert.equal(isValidEmailFormat('a..b@x.com'), false)
  })
})
