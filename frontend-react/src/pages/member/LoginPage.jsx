import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import PageHeader from '../../components/common/PageHeader'
import SocialLoginButtons from '../../components/member/SocialLoginButtons'
import { useAuth } from '../../hooks/useAuth'
import { useSavedLoginId } from '../../hooks/useSavedLoginId'

export default function LoginPage() {
  const { login } = useAuth()
  const { savedId, persist } = useSavedLoginId()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    const oauthError = params.get('oauthError')
    if (oauthError) {
      setError(oauthError)
    }
    if (params.get('joined') === '1') {
      setNotice('가입이 완료되었습니다. 로그인하세요.')
    }
  }, [params])

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
            {notice && <p className="hx-muted">{notice}</p>}
            <div className="hx-field">
              <label htmlFor="loginId">이메일 (아이디)</label>
              <input
                id="loginId"
                type="text"
                placeholder="이메일 또는 기존 아이디"
                value={loginId}
                onChange={(e) => setLoginId(e.target.value)}
                required
              />
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
          <div className="hx-social-wrap">
            <SocialLoginButtons />
            <small className="hx-muted">기업 회원은 이메일 로그인을 이용해 주세요. 처음 소셜 로그인하면 개인 회원으로 가입됩니다.</small>
          </div>
          <p className="hx-muted">
            <Link to="/forgot-id">아이디 찾기</Link>
            {' · '}
            <Link to="/forgot-password">비밀번호 찾기</Link>
            {' · '}
            <Link to="/signup">회원가입</Link>
          </p>
        </div>
      </section>
    </>
  )
}
