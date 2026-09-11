/**
 * Hexaq
 * 계층: Components Test
 * 객체: HoneypotField.source.test
 * 책임: 숨김 필드 이름·접근성·화면 밖 CSS가 문서와 같다
 * 문서: [docs/security/04-honeypot-field.md](../../../../docs/security/04-honeypot-field.md)
 */
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { describe, it } from 'node:test'
import { fileURLToPath } from 'node:url'

const dir = dirname(fileURLToPath(import.meta.url))
const jsx = readFileSync(join(dir, 'HoneypotField.jsx'), 'utf8')
const css = readFileSync(join(dir, '../../styles/hexaq.css'), 'utf8')
const hook = readFileSync(join(dir, '../../hooks/useSignUp.js'), 'utf8')

describe('HoneypotField', () => {
  it('name=website 이고 탭·자동완성을 끈다', () => {
    assert.match(jsx, /name="website"/)
    assert.match(jsx, /autoComplete="off"/)
    assert.match(jsx, /tabIndex=\{-1\}/)
    assert.match(jsx, /aria-hidden="true"/)
    assert.match(jsx, /className="hx-hp"/)
  })

  it('CSS는 display:none만 쓰지 않고 화면 밖에 둔다', () => {
    const block = css.match(/\.hx-hp\s*\{[^}]+\}/)
    assert.ok(block, '.hx-hp 규칙이 있다')
    assert.match(block[0], /left:\s*-10000px/)
    assert.doesNotMatch(block[0], /display:\s*none/)
  })

  it('가입 훅은 website가 없으면 빈 문자열을 보낸다', () => {
    assert.match(hook, /website: payload\.website \?\? ''/)
  })
})
