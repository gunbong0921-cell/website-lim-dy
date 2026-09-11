/**
 * Hexaq
 * 계층: Utils Test
 * 객체: requestSignature.test
 * 책임: 프론트 HMAC-SHA256이 서버 공개 벡터와 같다
 * 문서: [docs/security/01-hmac-request-signing.md](../../../docs/security/01-hmac-request-signing.md)
 */
import assert from 'node:assert/strict'
import { describe, it } from 'node:test'

import { canonicalString, hmacSha256Hex, sha256Hex } from './requestSignature.js'

describe('requestSignature', () => {
  it('빈 문자열 SHA-256은 공개 벡터와 같다', async () => {
    assert.equal(
      await sha256Hex(''),
      'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855'
    )
  })

  it('HMAC-SHA256 hex는 소문자이고 공개 벡터와 같다', async () => {
    assert.equal(
      await hmacSha256Hex('key', 'The quick brown fox jumps over the lazy dog'),
      'f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8'
    )
  })

  it('정규 문자열은 timestamp + METHOD + path + bodyHash 이다', () => {
    assert.equal(
      canonicalString('1710000000000', 'POST', '/api/auth/login', 'abcd'),
      '1710000000000\nPOST\n/api/auth/login\nabcd'
    )
  })
})
