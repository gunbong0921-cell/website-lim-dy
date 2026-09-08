import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import PageHeader from '../../components/common/PageHeader'
import { useAuth } from '../../hooks/useAuth'
import { useSavedLoginId } from '../../hooks/useSavedLoginId'

export default function LoginPage() {
  const { login } = useAuth()
  const { savedId, persist } = useSavedLoginId()
  const navigate = useNavigate()
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (savedId) {
      setLoginId(savedId)
      setRemember(true)
    }
  }, [savedId])

  async function onSubmit(event) {
    event.preventDefault()
    setError('')
    try {
      await login(loginId, password)
      persist(loginId, remember)
      navigate('/')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <>
      <PageHeader kicker="Account" title="로그인" />
      <section id="content">
        <div className="hx-panel">
          <form onSubmit={onSubmit}>
            <div className="hx-field">
              <label htmlFor="loginId">아이디</label>
              <input id="loginId" type="text" value={loginId} onChange={(e) => setLoginId(e.target.value)} required />
            </div>
            <div className="hx-field">
              <label htmlFor="password">비밀번호</label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <div className="hx-check">
              <input
                type="checkbox"
                id="remember"
                checked={remember}
                onChange={(e) => setRemember(e.target.checked)}
              />
              <label htmlFor="remember">아이디 저장</label>
            </div>
            {error && <p className="hx-error">{error}</p>}
            <ul className="actions">
              <li>
                <input type="submit" className="primary" value="로그인" />
              </li>
            </ul>
          </form>
          <p className="hx-muted">
            <Link to="/forgot-password">비밀번호 찾기</Link>
            {' · '}
            <Link to="/signup">회원가입</Link>
          </p>
        </div>
      </section>
    </>
  )
}
