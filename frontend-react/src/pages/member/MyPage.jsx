/**
 * Hexaq
 * 계층: Pages
 * 객체: MyPage
 * 책임: 화면 조립. fetch 금지. hooks 만 호출
 * 문서: [docs/features/01-signup-verification.md](../../../../docs/features/01-signup-verification.md) · [docs/features/03-login-logout.md](../../../../docs/features/03-login-logout.md) · [docs/features/overview.md](../../../../docs/features/overview.md)
 */
import { useEffect, useState } from 'react'
import PageHeader from '../../components/common/PageHeader'
import { useAuth } from '../../hooks/useAuth'
import { isStrongPassword, PASSWORD_RULE_TEXT } from '../../utils/passwordPolicy'

export default function MyPage() {
  const { member, updateProfile, changePassword } = useAuth()
  const [profile, setProfile] = useState({
    name: '',
    phone: '',
    company: '',
    address: '',
    jobTitle: '',
    workplaceAddress: '',
  })
  const [passwords, setPasswords] = useState({ currentPassword: '', newPassword: '', newPasswordConfirm: '' })
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    if (!member) return
    setProfile({
      name: member.name || '',
      phone: member.phone || '',
      company: member.company || '',
      address: member.address || '',
      jobTitle: member.jobTitle || '',
      workplaceAddress: member.workplaceAddress || '',
    })
  }, [member])

  if (!member) {
    return <p className="hx-empty">로그인이 필요합니다.</p>
  }

  async function saveProfile(event) {
    event.preventDefault()
    setError('')
    try {
      const res = await updateProfile({
        ...profile,
        company: member.memberType === 'CORPORATE' ? profile.company : profile.jobTitle,
      })
      setMessage(res.message)
    } catch (err) {
      setError(err.message)
    }
  }

  async function savePassword(event) {
    event.preventDefault()
    setError('')
    if (passwords.newPassword !== passwords.newPasswordConfirm) {
      setError('새 비밀번호가 일치하지 않습니다.')
      return
    }
    if (!isStrongPassword(passwords.newPassword)) {
      setError(`비밀번호는 ${PASSWORD_RULE_TEXT}이어야 합니다.`)
      return
    }
    try {
      const res = await changePassword({
        currentPassword: passwords.currentPassword,
        newPassword: passwords.newPassword,
      })
      setMessage(res.message)
      setPasswords({ currentPassword: '', newPassword: '', newPasswordConfirm: '' })
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <>
      <PageHeader
        kicker={`아이디 ${member.loginId} · ${member.role === 'ADMIN' ? '관리자' : member.memberType === 'CORPORATE' ? '기업회원' : '개인회원'} · ${member.email}`}
        title="마이페이지"
      />
      <section id="content">
        <div className="hx-grid-2">
          <div className="hx-panel" style={{ maxWidth: 'none', margin: 0 }}>
            <form onSubmit={saveProfile}>
              <div className="hx-field">
                <label htmlFor="name">이름</label>
                <input
                  id="name"
                  type="text"
                  value={profile.name}
                  onChange={(e) => setProfile({ ...profile, name: e.target.value })}
                  required
                />
              </div>
              <div className="hx-field">
                <label htmlFor="phone">전화</label>
                <input
                  id="phone"
                  type="text"
                  value={profile.phone}
                  onChange={(e) => setProfile({ ...profile, phone: e.target.value })}
                  required
                />
              </div>
              <div className="hx-field">
                <label htmlFor="jobTitle">{member.memberType === 'CORPORATE' ? '부서/직급' : '소속/직함'}</label>
                <input
                  id="jobTitle"
                  type="text"
                  value={profile.jobTitle}
                  onChange={(e) => setProfile({ ...profile, jobTitle: e.target.value })}
                />
              </div>
              <div className="hx-field">
                <label htmlFor="address">주소</label>
                <input
                  id="address"
                  type="text"
                  value={profile.address}
                  onChange={(e) => setProfile({ ...profile, address: e.target.value })}
                />
              </div>
              {member.memberType === 'CORPORATE' && (
                <>
                  <div className="hx-field">
                    <label htmlFor="company">법인명</label>
                    <input id="company" type="text" value={profile.company} readOnly />
                  </div>
                  <div className="hx-field">
                    <label htmlFor="workplaceAddress">사업장 소재지</label>
                    <input
                      id="workplaceAddress"
                      type="text"
                      value={profile.workplaceAddress}
                      onChange={(e) => setProfile({ ...profile, workplaceAddress: e.target.value })}
                    />
                  </div>
                </>
              )}
              {member.memberType !== 'CORPORATE' && (
              <div className="hx-field">
                <label htmlFor="company">소속</label>
                <input
                  id="company"
                  type="text"
                  value={profile.company}
                  onChange={(e) => setProfile({ ...profile, company: e.target.value })}
                />
              </div>
              )}
              <ul className="actions">
                <li>
                  <input type="submit" className="primary" value="정보 수정" />
                </li>
              </ul>
            </form>
          </div>
          <div className="hx-panel" style={{ maxWidth: 'none', margin: 0 }}>
            {member.oauthProvider === 'GOOGLE' && (
              <p className="hx-muted">Google 계정과 연동되어 있습니다. 비밀번호 로그인이 필요하면 비밀번호 찾기로 설정하세요.</p>
            )}
            {member.oauthProvider === 'GITHUB' && (
              <p className="hx-muted">GitHub 계정과 연동되어 있습니다. 비밀번호 로그인이 필요하면 비밀번호 찾기로 설정하세요.</p>
            )}
            {member.oauthProvider === 'KAKAO' && (
              <>
                <p className="hx-muted">카카오 계정과 연동되어 있습니다. 비밀번호 로그인이 필요하면 비밀번호 찾기로 설정하세요.</p>
                <p className="hx-muted">
                  가입 환영 메시지: {member.kakaoWelcomeSent ? '발송 완료' : '미발송'}
                </p>
                {(member.profileImage || member.nickname) && (
                  <div className="hx-kakao-profile">
                    {member.profileImage && (
                      <img className="hx-avatar" src={member.profileImage} alt="" />
                    )}
                    {member.nickname && <p>닉네임 {member.nickname}</p>}
                  </div>
                )}
                <div className="hx-kakao-friends">
                  <h3>카카오 친구</h3>
                  {(!member.kakaoFriends || member.kakaoFriends.length === 0) ? (
                    <p className="hx-muted">동기화된 친구가 없습니다.</p>
                  ) : (
                    <ul className="hx-friend-list">
                      {member.kakaoFriends.map((friend) => (
                        <li key={friend.kakaoId}>
                          {friend.profileImage && (
                            <img className="hx-avatar-sm" src={friend.profileImage} alt="" />
                          )}
                          <span>{friend.nickname || friend.kakaoId}</span>
                          {friend.favorite ? <small>즐겨찾기</small> : null}
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </>
            )}
            {(!member.phone || member.phone === '-') && (
              <p className="hx-error">휴대폰 번호를 등록해 주세요.</p>
            )}
            <form onSubmit={savePassword}>
              <h3>비밀번호 변경</h3>
              <div className="hx-field">
                <label htmlFor="currentPassword">현재 비밀번호</label>
                <input
                  id="currentPassword"
                  type="password"
                  value={passwords.currentPassword}
                  onChange={(e) => setPasswords({ ...passwords, currentPassword: e.target.value })}
                  required
                />
              </div>
              <div className="hx-field">
                <label htmlFor="newPassword">새 비밀번호</label>
                <input
                  id="newPassword"
                  type="password"
                  value={passwords.newPassword}
                  onChange={(e) => setPasswords({ ...passwords, newPassword: e.target.value })}
                  minLength={8}
                  required
                />
                <small className="hx-muted">{PASSWORD_RULE_TEXT}</small>
              </div>
              <div className="hx-field">
                <label htmlFor="newPasswordConfirm">새 비밀번호 확인</label>
                <input
                  id="newPasswordConfirm"
                  type="password"
                  value={passwords.newPasswordConfirm}
                  onChange={(e) => setPasswords({ ...passwords, newPasswordConfirm: e.target.value })}
                  minLength={8}
                  required
                />
              </div>
              <ul className="actions">
                <li>
                  <input type="submit" value="비밀번호 변경" />
                </li>
              </ul>
            </form>
          </div>
        </div>
        {message && <p className="hx-notice" style={{ marginTop: '1.5em' }}>{message}</p>}
        {error && <p className="hx-error" style={{ marginTop: '1em' }}>{error}</p>}
      </section>
    </>
  )
}
