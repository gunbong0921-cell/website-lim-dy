import { useState } from 'react'
import { Link } from 'react-router-dom'
import PageHeader from '../../components/common/PageHeader'
import { useAuth } from '../../hooks/useAuth'

export default function FindIdPage() {
  const { findLoginId } = useAuth()
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  async function onSubmit(event) {
    event.preventDefault()
    setError('')
    try {
      const res = await findLoginId(email)
      setMessage(res.data?.debugLoginId ? `${res.message} 아이디: ${res.data.debugLoginId}` : res.message)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <>
      <PageHeader kicker="Account" title="아이디 찾기" />
      <section id="content">
        <div className="hx-panel">
          <p className="hx-muted">가입 이메일로 아이디를 안내합니다.</p>
          <form onSubmit={onSubmit}>
            <div className="hx-field">
              <label htmlFor="email">이메일</label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            {message && <p className="hx-notice">{message}</p>}
            {error && <p className="hx-error">{error}</p>}
            <ul className="actions">
              <li>
                <input type="submit" className="primary" value="아이디 찾기" />
              </li>
            </ul>
          </form>
          <p className="hx-muted">
            <Link to="/forgot-password">비밀번호 찾기</Link>
            {' · '}
            <Link to="/login">로그인으로</Link>
          </p>
        </div>
      </section>
    </>
  )
}
