import { useState } from 'react'
import { Link } from 'react-router-dom'
import PageHeader from '../../components/common/PageHeader'
import { useAuth } from '../../hooks/useAuth'

export default function ForgotPasswordPage() {
  const { forgotPassword } = useAuth()
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  async function onSubmit(event) {
    event.preventDefault()
    setError('')
    try {
      const res = await forgotPassword(email)
      setMessage(
        res.data?.debugPassword ? `${res.message} 임시 비밀번호: ${res.data.debugPassword}` : res.message,
      )
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <>
      <PageHeader kicker="Account" title="비밀번호 찾기" />
      <section id="content">
        <div className="hx-panel">
          <p className="hx-muted">가입 이메일로 임시 비밀번호를 보냅니다. 로그인 후 반드시 변경하세요.</p>
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
                <input type="submit" className="primary" value="임시 비밀번호 발급" />
              </li>
            </ul>
          </form>
          <p>
            <Link to="/login">로그인으로</Link>
          </p>
        </div>
      </section>
    </>
  )
}
