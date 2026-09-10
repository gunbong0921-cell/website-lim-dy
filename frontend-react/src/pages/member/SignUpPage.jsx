import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import SignUpTerms from '../../components/member/SignUpTerms'
import SignUpTypeTabs from '../../components/member/SignUpTypeTabs'
import SignUpVerifyTabs from '../../components/member/SignUpVerifyTabs'
import HoneypotField from '../../components/member/HoneypotField'
import SocialLoginButtons from '../../components/member/SocialLoginButtons'
import PageHeader from '../../components/common/PageHeader'
import { usePhoneVerification } from '../../hooks/usePhoneVerification'
import { useEmailVerification } from '../../hooks/useEmailVerification'
import { useSignUp } from '../../hooks/useSignUp'
import { digitsOnly, formatBusinessNumber, isBusinessNumber, isMobilePhone, isPersonalEmail, isValidEmailFormat } from '../../utils/memberForm'
import { PASSWORD_RULE_TEXT, passwordResetHint } from '../../utils/passwordPolicy'

const empty = {
  memberType: 'INDIVIDUAL',
  email: '',
  website: '',
  password: '',
  passwordConfirm: '',
  name: '',
  phone: '',
  address: '',
  jobTitle: '',
  businessNumber: '',
  companyName: '',
  ceoName: '',
  workplaceAddress: '',
  openingDate: '',
  termsService: false,
  termsPrivacy: false,
  termsMarketing: false,
  termsCorporate: false,
  verificationChannel: 'PHONE',
}

export default function SignUpPage() {
  const { checkEmail, signUp, verifyEmail, resendVerification, verifyBusiness, result, loading } = useSignUp()
  const phoneVerify = usePhoneVerification()
  const emailVerify = useEmailVerification()
  const navigate = useNavigate()
  const [form, setForm] = useState(empty)
  const [emailChecked, setEmailChecked] = useState(false)
  const [emailAvailable, setEmailAvailable] = useState(false)
  const [emailMessage, setEmailMessage] = useState('')
  const [businessMessage, setBusinessMessage] = useState('')
  const [businessVerified, setBusinessVerified] = useState(false)
  const [code, setCode] = useState('')
  const [smsCode, setSmsCode] = useState('')
  const [emailCode, setEmailCode] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [step, setStep] = useState('form')
  const corporate = form.memberType === 'CORPORATE'

  useEffect(() => {
    if (result && result.needsEmailVerification) setStep('verify')
  }, [result])

  function setField(key, value) {
    setForm((prev) => ({ ...prev, [key]: value }))
    if (key === 'email') {
      setEmailChecked(false)
      setEmailAvailable(false)
      setEmailMessage('')
      if (emailVerify.sent || emailVerify.verified) {
        emailVerify.reset()
        setEmailCode('')
      }
    }
    if (key === 'phone' && (phoneVerify.sent || phoneVerify.verified)) {
      phoneVerify.reset()
      setSmsCode('')
    }
    if (key === 'businessNumber' || key === 'ceoName' || key === 'companyName' || key === 'openingDate' || key === 'workplaceAddress') {
      setBusinessMessage('')
      setBusinessVerified(false)
    }
  }

  function selectChannel(verificationChannel) {
    setField('verificationChannel', verificationChannel)
    phoneVerify.reset()
    emailVerify.reset()
    setSmsCode('')
    setEmailCode('')
    setError('')
    setMessage('')
  }

  function selectType(memberType) {
    setForm({ ...empty, memberType })
    setEmailChecked(false)
    setEmailAvailable(false)
    setEmailMessage('')
    setBusinessMessage('')
    setBusinessVerified(false)
    setError('')
    phoneVerify.reset()
    emailVerify.reset()
    setSmsCode('')
    setEmailCode('')
  }

  async function onCheckEmail() {
    setError('')
    if (!isValidEmailFormat(form.email)) {
      setEmailChecked(false)
      setEmailAvailable(false)
      setEmailMessage('이메일 형식이 올바르지 않습니다.')
      return
    }
    try {
      const res = await checkEmail(form.email)
      setEmailMessage(res.message)
      setEmailChecked(true)
      setEmailAvailable(Boolean(res.data?.available))
    } catch (err) {
      setEmailChecked(false)
      setEmailAvailable(false)
      setEmailMessage(err.message)
    }
  }

  async function onCheckBusiness() {
    setError('')
    if (!isBusinessNumber(form.businessNumber)) {
      setBusinessVerified(false)
      setBusinessMessage('사업자등록번호 10자리(000-00-00000)를 입력하세요.')
      return
    }
    if (!form.ceoName.trim() || !form.openingDate) {
      setBusinessVerified(false)
      setBusinessMessage('진위확인을 위해 대표자명과 개업일자를 함께 입력하세요.')
      return
    }
    try {
      const res = await verifyBusiness({
        businessNumber: form.businessNumber,
        openingDate: form.openingDate,
        representativeName: form.ceoName,
        companyName: form.companyName,
        address: form.workplaceAddress,
      })
      setBusinessVerified(Boolean(res.data?.matched && res.data?.operating))
      setBusinessMessage(res.message)
    } catch (err) {
      setBusinessVerified(false)
      setBusinessMessage(err.message)
    }
  }

  async function onSubmit(event) {
    event.preventDefault()
    setError('')
    if (!emailChecked || !emailAvailable) {
      setError('이메일 중복확인을 먼저 진행하세요.')
      return
    }
    if (form.password !== form.passwordConfirm) {
      setError('비밀번호가 일치하지 않습니다.')
      return
    }
    const hint = passwordResetHint(form.password)
    if (hint) {
      setError(hint)
      return
    }
    if (!form.termsService || !form.termsPrivacy) {
      setError('필수 약관에 동의해 주세요.')
      return
    }
    if (corporate && !businessVerified) {
      setError('국세청 사업자 진위확인을 먼저 진행하세요.')
      return
    }
    if (corporate && !form.termsCorporate) {
      setError('법인 대표자 또는 위임받은 대리인 가입 확인에 동의해 주세요.')
      return
    }
    if (!isMobilePhone(form.phone)) {
      setError('휴대폰 번호는 숫자 10~11자리로 입력하세요.')
      return
    }
    if (form.verificationChannel === 'PHONE') {
      if (!isMobilePhone(form.phone)) {
        setError('휴대폰 번호는 숫자 10~11자리로 입력하세요.')
        return
      }
      if (!phoneVerify.verified || !phoneVerify.token) {
        setError('휴대폰으로 받은 인증번호 6자리를 확인한 뒤 가입해 주세요.')
        return
      }
    } else if (!emailVerify.verified || !emailVerify.token) {
      setError('이메일로 받은 인증번호 6자리를 확인한 뒤 가입해 주세요.')
      return
    }
    try {
      const res = await signUp({
        ...form,
        phoneVerificationToken: form.verificationChannel === 'PHONE' ? phoneVerify.token : emailVerify.token,
      })
      setMessage(res.message)
      if (!res.data?.needsEmailVerification) {
        navigate('/login?joined=1')
      }
    } catch (err) {
      setError(err.message)
    }
  }

  async function onVerify(event) {
    event.preventDefault()
    setError('')
    try {
      await verifyEmail(form.email, code)
      navigate('/login')
    } catch (err) {
      setError(err.message)
    }
  }

  async function onResend() {
    setError('')
    try {
      const res = await resendVerification(form.email)
      setMessage(res.message)
    } catch (err) {
      setError(err.message)
    }
  }

  async function onSendPhoneCode() {
    setError('')
    try {
      const res = await phoneVerify.send(form.phone)
      setMessage(res.message)
    } catch (err) {
      setError(err.message)
    }
  }

  async function onVerifyPhone() {
    setError('')
    try {
      const res = await phoneVerify.verify(form.phone, smsCode)
      setMessage(res.message)
    } catch (err) {
      setError(err.message)
    }
  }

  async function onSendEmailCode() {
    setError('')
    if (!emailChecked || !emailAvailable) {
      setError('이메일 중복확인을 먼저 진행하세요.')
      return
    }
    try {
      const res = await emailVerify.send(form.email)
      setMessage(res.message)
    } catch (err) {
      setError(err.message)
    }
  }

  async function onVerifyEmailCode() {
    setError('')
    try {
      const res = await emailVerify.verify(form.email, emailCode)
      setMessage(res.message)
    } catch (err) {
      setError(err.message)
    }
  }

  const passwordHint = passwordResetHint(form.password)
  const confirmHint =
    form.passwordConfirm && form.password !== form.passwordConfirm
      ? '비밀번호가 일치하지 않습니다. 다시 입력해 주세요.'
      : ''
  const emailHint =
    corporate && form.email && isPersonalEmail(form.email)
      ? '회사 도메인 이메일 사용을 권장합니다.'
      : ''

  return (
    <>
      <PageHeader kicker="Account" title={step === 'verify' ? '이메일 인증' : '회원가입'} />
      <section id="content">
        <div className="hx-panel hx-panel-wide">
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
              <HoneypotField value={form.website} onChange={(value) => setField('website', value)} />
              <SignUpTypeTabs memberType={form.memberType} onChange={selectType} />
              {!corporate && (
                <div className="hx-social-wrap">
                  <SocialLoginButtons />
                  <small className="hx-muted">회원가입양식</small>
                </div>
              )}

              <div className="hx-form-section">
                <h3>{corporate ? '담당자 정보' : '기본 정보'}</h3>
                <div className="hx-field">
                  <div className="hx-label-row">
                    <label>본인 인증 [필수]</label>
                    <small className="hx-muted">버튼 선택 시 해당 항목에서 인증진행</small>
                  </div>
                  <SignUpVerifyTabs channel={form.verificationChannel} onChange={selectChannel} />
                  <small className="hx-muted">
                    {form.verificationChannel === 'PHONE'
                      ? '휴대폰으로 받은 인증번호 6자리로 본인 확인합니다.'
                      : '이메일로 받은 인증번호 6자리로 본인 확인합니다.'}
                  </small>
                </div>
                <div className="hx-field">
                  <label htmlFor="email">{corporate ? '담당자 이메일 (아이디)' : '이메일 (아이디)'} [필수]</label>
                  <div className="hx-id-row">
                    <input
                      id="email"
                      type="email"
                      placeholder={corporate ? 'name@company.com' : 'example@email.com'}
                      value={form.email}
                      onChange={(e) => setField('email', e.target.value)}
                      required
                    />
                    <input type="button" value="중복확인" onClick={onCheckEmail} />
                    {form.verificationChannel === 'EMAIL' && (
                      <input
                        type="button"
                        value={
                          emailVerify.loading
                            ? '처리 중...'
                            : emailVerify.cooldown > 0
                              ? `${emailVerify.cooldown}초 후 재전송`
                              : emailVerify.sent
                                ? '재전송'
                                : '인증번호 전송'
                        }
                        onClick={onSendEmailCode}
                        disabled={emailVerify.loading || emailVerify.cooldown > 0}
                      />
                    )}
                  </div>
                  {form.verificationChannel === 'EMAIL' && (
                    <div className="hx-id-row hx-id-row-follow">
                      <input
                        id="emailCode"
                        type="text"
                        inputMode="numeric"
                        autoComplete="one-time-code"
                        placeholder="이메일 인증번호 6자리"
                        value={emailCode}
                        onChange={(e) => setEmailCode(digitsOnly(e.target.value, 6))}
                        maxLength={6}
                      />
                      <input type="button" value="인증 확인" onClick={onVerifyEmailCode} disabled={emailVerify.loading} />
                    </div>
                  )}
                  <small className="hx-muted">
                    {form.verificationChannel === 'EMAIL'
                      ? emailVerify.verified
                        ? '이메일 인증이 완료되었습니다.'
                        : '중복확인 후 인증번호를 받아 6자리를 입력하세요.'
                      : '아이디로 사용할 이메일입니다.'}
                  </small>
                  {emailHint && <p className="hx-error">{emailHint}</p>}
                  {emailMessage && (
                    <p className={emailChecked && emailAvailable ? 'hx-muted' : 'hx-error'}>{emailMessage}</p>
                  )}
                </div>
                <div className="hx-field">
                  <label htmlFor="password">비밀번호 [필수]</label>
                  <input
                    id="password"
                    type="password"
                    value={form.password}
                    onChange={(e) => setField('password', e.target.value)}
                    aria-invalid={Boolean(passwordHint)}
                    required
                  />
                  <small className="hx-muted">{PASSWORD_RULE_TEXT}</small>
                  {passwordHint && <p className="hx-error">{passwordHint}</p>}
                </div>
                <div className="hx-field">
                  <label htmlFor="passwordConfirm">비밀번호 확인 [필수]</label>
                  <input
                    id="passwordConfirm"
                    type="password"
                    value={form.passwordConfirm}
                    onChange={(e) => setField('passwordConfirm', e.target.value)}
                    aria-invalid={Boolean(confirmHint)}
                    required
                  />
                  {confirmHint && <p className="hx-error">{confirmHint}</p>}
                </div>
                <div className="hx-field">
                  <label htmlFor="name">{corporate ? '담당자 이름' : '이름'} [필수]</label>
                  <input
                    id="name"
                    type="text"
                    placeholder="실명 입력"
                    value={form.name}
                    onChange={(e) => setField('name', e.target.value)}
                    required
                  />
                </div>
                {corporate && (
                  <div className="hx-field">
                    <label htmlFor="jobTitle">담당자 부서/직급 [필수]</label>
                    <input
                      id="jobTitle"
                      type="text"
                      placeholder="예) IT기획팀 / 책임연구원"
                      value={form.jobTitle}
                      onChange={(e) => setField('jobTitle', e.target.value)}
                      required
                    />
                  </div>
                )}
                <div className="hx-field">
                  <label htmlFor="phone">{corporate ? '담당자 연락처' : '휴대폰 번호'} [필수]</label>
                  {form.verificationChannel === 'PHONE' ? (
                    <>
                      <div className="hx-id-row">
                        <input
                          id="phone"
                          type="text"
                          inputMode="numeric"
                          autoComplete="tel"
                          placeholder="하이픈 없이 숫자만"
                          value={form.phone}
                          onChange={(e) => setField('phone', digitsOnly(e.target.value))}
                          required
                        />
                        <input
                          type="button"
                          value={
                            phoneVerify.loading
                              ? '처리 중...'
                              : phoneVerify.cooldown > 0
                                ? `${phoneVerify.cooldown}초 후 재전송`
                                : phoneVerify.sent
                                  ? '재전송'
                                  : '인증번호 전송'
                          }
                          onClick={onSendPhoneCode}
                          disabled={phoneVerify.loading || phoneVerify.cooldown > 0}
                        />
                      </div>
                      <div className="hx-id-row hx-id-row-follow">
                        <input
                          id="smsCode"
                          type="text"
                          inputMode="numeric"
                          autoComplete="one-time-code"
                          placeholder="휴대폰 인증번호 6자리"
                          value={smsCode}
                          onChange={(e) => setSmsCode(digitsOnly(e.target.value, 6))}
                          maxLength={6}
                        />
                        <input type="button" value="인증 확인" onClick={onVerifyPhone} disabled={phoneVerify.loading} />
                      </div>
                      <small className="hx-muted">
                        {phoneVerify.verified
                          ? '휴대폰 인증이 완료되었습니다.'
                          : '인증번호 전송 후, 문자로 받은 6자리를 입력해 본인 확인합니다.'}
                      </small>
                    </>
                  ) : (
                    <>
                      <input
                        id="phone"
                        type="text"
                        inputMode="numeric"
                        autoComplete="tel"
                        placeholder="하이픈 없이 숫자만"
                        value={form.phone}
                        onChange={(e) => setField('phone', digitsOnly(e.target.value))}
                        required
                      />
                      <small className="hx-muted">숫자만 입력합니다. 본인 확인은 이메일 인증으로 진행됩니다.</small>
                    </>
                  )}
                </div>
                {!corporate && (
                  <div className="hx-field">
                    <label htmlFor="address">주소 [필수]</label>
                    <input
                      id="address"
                      type="text"
                      placeholder="주소 입력"
                      value={form.address}
                      onChange={(e) => setField('address', e.target.value)}
                      required
                    />
                  </div>
                )}
              </div>

              {corporate ? (
                <div className="hx-form-section">
                  <h3>기업 정보</h3>
                  <div className="hx-field">
                    <label htmlFor="businessNumber">사업자등록번호 [필수]</label>
                    <input
                      id="businessNumber"
                      type="text"
                      placeholder="000-00-00000"
                      value={form.businessNumber}
                      onChange={(e) => setField('businessNumber', formatBusinessNumber(e.target.value))}
                      required
                    />
                  </div>
                  <div className="hx-field">
                    <label htmlFor="companyName">법인명(회사명) [필수]</label>
                    <input
                      id="companyName"
                      type="text"
                      placeholder="사업자등록증 상의 정식 상호명"
                      value={form.companyName}
                      onChange={(e) => setField('companyName', e.target.value)}
                      required
                    />
                  </div>
                  <div className="hx-field">
                    <label htmlFor="ceoName">대표자명 [필수]</label>
                    <input
                      id="ceoName"
                      type="text"
                      placeholder="대표자 실명"
                      value={form.ceoName}
                      onChange={(e) => setField('ceoName', e.target.value)}
                      required
                    />
                  </div>
                  <div className="hx-field">
                    <label htmlFor="openingDate">개업일자 [필수]</label>
                    <div className="hx-id-row">
                      <input
                        id="openingDate"
                        type="date"
                        value={form.openingDate}
                        onChange={(e) => setField('openingDate', e.target.value)}
                        required
                      />
                      <input type="button" value="국세청 진위확인" onClick={onCheckBusiness} />
                    </div>
                    <small className="hx-muted">사업자등록증의 사업자번호·대표자명·개업연월일로 국세청 진위확인을 합니다.</small>
                    {businessMessage && (
                      <p className={businessVerified ? 'hx-muted' : 'hx-error'}>{businessMessage}</p>
                    )}
                  </div>
                  <div className="hx-field">
                    <label htmlFor="workplaceAddress">사업장 소재지 [선택]</label>
                    <input
                      id="workplaceAddress"
                      type="text"
                      placeholder="세금계산서 발행 및 계약용 주소"
                      value={form.workplaceAddress}
                      onChange={(e) => setField('workplaceAddress', e.target.value)}
                    />
                  </div>
                </div>
              ) : (
                <div className="hx-form-section">
                  <h3>선택 정보</h3>
                  <div className="hx-field">
                    <label htmlFor="jobTitle">소속/직함 [선택]</label>
                    <input
                      id="jobTitle"
                      type="text"
                      placeholder="예) Hexaq 개발팀 / 팀장"
                      value={form.jobTitle}
                      onChange={(e) => setField('jobTitle', e.target.value)}
                    />
                  </div>
                </div>
              )}

              <SignUpTerms
                memberType={form.memberType}
                terms={form}
                onChange={(key, value) => setField(key, value)}
              />

              {message && <p className="hx-muted">{message}</p>}
              {error && <p className="hx-error">{error}</p>}
              <ul className="actions">
                <li>
                  <input
                    type="submit"
                    className="primary"
                    value={loading ? '처리 중...' : '가입하기'}
                    disabled={
                      loading ||
                      Boolean(passwordHint) ||
                      Boolean(confirmHint) ||
                      (form.verificationChannel === 'PHONE' ? !phoneVerify.verified : !emailVerify.verified)
                    }
                  />
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
