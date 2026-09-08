import { useEffect, useState } from 'react'
import PageHeader from '../../components/common/PageHeader'
import { useAuth } from '../../hooks/useAuth'

export default function MyPage() {
  const { member, updateProfile, changePassword } = useAuth()
  const [profile, setProfile] = useState({
    name: '',
    phone: '',
    company: '',
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
    })
  }, [member])

  if (!member) {
    return <p className="hx-empty">로그인이 필요합니다.</p>
  }

  async function saveProfile(event) {
    event.preventDefault()
    setError('')
    try {
      const res = await updateProfile(profile)
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
      <PageHeader kicker={`아이디 ${member.loginId} · ${member.email}`} title="마이페이지" />
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
                <label htmlFor="company">소속</label>
                <input
                  id="company"
                  type="text"
                  value={profile.company}
                  onChange={(e) => setProfile({ ...profile, company: e.target.value })}
                />
              </div>
              <ul className="actions">
                <li>
                  <input type="submit" className="primary" value="정보 수정" />
                </li>
              </ul>
            </form>
          </div>
          <div className="hx-panel" style={{ maxWidth: 'none', margin: 0 }}>
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
