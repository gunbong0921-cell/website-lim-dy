import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import PageHeader from '../../components/common/PageHeader'
import { useSignUp } from '../../hooks/useSignUp'

const empty = {
  loginId: '',
  password: '',
  passwordConfirm: '',
  name: '',
  email: '',
  phone: '',
  company: '',
}

export default function SignUpPage() {
  const { checkId, checkEmail, signUp, verifyEmail, resendVerification, result, loading } = useSignUp()
  const navigate = useNavigate()
  const [form, setForm] = useState(empty)
  const [idChecked, setIdChecked] = useState(false)
  const [idAvailable, setIdAvailable] = useState(false)
  const [idMessage, setIdMessage] = useState('')
  const [emailMessage, setEmailMessage] = useState('')
  const [code, setCode] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [step, setStep] = useState('form')

  useEffect(() => {
    if (result) setStep('verify')
  }, [result])

  function setField(key, value) {
    setForm((prev) => ({ ...prev, [key]: value }))
    if (key === 'loginId') {
      setIdChecked(false)
      setIdAvailable(false)
      setIdMessage('')
    }
    if (key === 'email') {
      setEmailMessage('')
    }
  }

  async function onCheckId() {
    setError('')
    try {
      const res = await checkId(form.loginId)
      setIdMessage(res.message)
      setIdChecked(true)
      setIdAvailable(Boolean(res.data?.available))
    } catch (err) {
      setIdChecked(false)
      setIdAvailable(false)
      setIdMessage(err.message)
    }
  }

  async function onCheckEmail() {
    setError('')
    try {
      const res = await checkEmail(form.email)
      setEmailMessage(res.message)
    } catch (err) {
      setEmailMessage(err.message)
    }
  }

  async function onSubmit(event) {
    event.preventDefault()
    setError('')
    if (!idChecked || !idAvailable) {
      setError('아이디 중복확인을 먼저 진행하세요.')
      return
    }
    if (form.password !== form.passwordConfirm) {
      setError('비밀번호가 일치하지 않습니다.')
      return
    }
    try {
      const res = await signUp(form)
      setMessage(res.message)
    } catch (err) {
      setError(err.message)
    }
  }

  async function onVerify(event) {
    event.preventDefault()
    setError('')
    try {
      await verifyEmail(form.loginId, code)
      navigate('/login')
    } catch (err) {
      setError(err.message)
    }
  }

  async function onResend() {
    setError('')
    try {
      const res = await resendVerification(form.loginId)
      setMessage(res.message)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <>
      <PageHeader kicker="Account" title={step === 'verify' ? '이메일 인증' : '회원가입'} />
      <section id="content">
        <div className="hx-panel">
          {step === 'verify' ? (
            <form onSubmit={onVerify}>
              <p>{message || '가입한 이메일로 인증 코드를 보냈습니다. 메일함을 확인해 주세요.'}</p>
              {result?.debugCode && !result?.mailSent && (
                <p className="hx-notice">메일 발송에 실패한 경우 임시 코드: {result.debugCode}</p>
              )}
              <div className="hx-field">
                <label htmlFor="code">인증 코드</label>
                <input id="code" type="text" value={code} onChange={(e) => setCode(e.target.value)} required />
              </div>
              {error && <p className="hx-error">{error}</p>}
              <ul className="actions">
                <li>
                  <input type="submit" className="primary" value="인증 완료" />
                </li>
                <li>
                  <input type="button" value="코드 재발송" onClick={onResend} />
                </li>
              </ul>
            </form>
          ) : (
            <form onSubmit={onSubmit}>
              <div className="hx-field">
                <label htmlFor="loginId">아이디</label>
                <div className="hx-id-row">
                  <input
                    id="loginId"
                    type="text"
                    value={form.loginId}
                    onChange={(e) => setField('loginId', e.target.value)}
                    required
                  />
                  <input type="button" value="중복확인" onClick={onCheckId} />
                </div>
                {idMessage && <small className="hx-muted">{idMessage}</small>}
              </div>
              <div className="hx-field">
                <label htmlFor="password">비밀번호</label>
                <input
                  id="password"
                  type="password"
                  value={form.password}
                  onChange={(e) => setField('password', e.target.value)}
                  minLength={8}
                  required
                />
              </div>
              <div className="hx-field">
                <label htmlFor="passwordConfirm">비밀번호 확인</label>
                <input
                  id="passwordConfirm"
                  type="password"
                  value={form.passwordConfirm}
                  onChange={(e) => setField('passwordConfirm', e.target.value)}
                  minLength={8}
                  required
                />
              </div>
              <div className="hx-field">
                <label htmlFor="name">이름</label>
                <input id="name" type="text" value={form.name} onChange={(e) => setField('name', e.target.value)} required />
              </div>
              <div className="hx-field">
                <label htmlFor="email">이메일</label>
                <div className="hx-id-row">
                  <input
                    id="email"
                    type="email"
                    value={form.email}
                    onChange={(e) => setField('email', e.target.value)}
                    required
                  />
                  <input type="button" value="중복확인" onClick={onCheckEmail} />
                </div>
                {emailMessage && <small className="hx-muted">{emailMessage}</small>}
              </div>
              <div className="hx-field">
                <label htmlFor="phone">전화번호</label>
                <input
                  id="phone"
                  type="text"
                  value={form.phone}
                  onChange={(e) => setField('phone', e.target.value)}
                  required
                />
              </div>
              <div className="hx-field">
                <label htmlFor="company">소속 (선택)</label>
                <input id="company" type="text" value={form.company} onChange={(e) => setField('company', e.target.value)} />
              </div>
              {error && <p className="hx-error">{error}</p>}
              <ul className="actions">
                <li>
                  <input type="submit" className="primary" value={loading ? '처리 중...' : '가입하기'} disabled={loading} />
                </li>
              </ul>
              <p className="hx-muted">
                이미 계정이 있나요? <Link to="/login">로그인</Link>
              </p>
            </form>
          )}
        </div>
      </section>
    </>
  )
}
