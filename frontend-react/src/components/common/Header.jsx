/**
 * Hexaq
 * 계층: Components
 * 객체: Header
 * 책임: 데스크톱 상단 바(#header). 1180px 이하는 CompactNav
 * 문서: [docs/technical/05-responsive.md](../../../../docs/technical/05-responsive.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
import { useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import BrandLogo from './BrandLogo'

export default function Header() {
  const { member, logout } = useAuth()
  const navigate = useNavigate()
  const [communityOpen, setCommunityOpen] = useState(false)

  async function handleLogout() {
    await logout()
    setCommunityOpen(false)
    navigate('/')
  }

  function goHome(event) {
    event.preventDefault()
    setCommunityOpen(false)
    navigate('/')
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
  }

  function toggleCommunity(event) {
    event.preventDefault()
    setCommunityOpen((value) => !value)
  }

  function closeCommunity() {
    setCommunityOpen(false)
  }

  return (
    <header id="header">
      <h1 id="logo">
        <NavLink to="/" end onClick={goHome} aria-label="HEXAQ 홈">
          <BrandLogo />
        </NavLink>
      </h1>
      <nav id="nav">
        <ul>
          <li>
            <NavLink to="/" end onClick={closeCommunity}>
              홈
            </NavLink>
          </li>
          <li>
            <NavLink to="/solutions" onClick={closeCommunity}>
              솔루션
            </NavLink>
          </li>
          <li>
            <NavLink to="/insights" onClick={closeCommunity}>
              인사이트
            </NavLink>
          </li>
          <li className={communityOpen ? 'open' : ''}>
            <a href="#community" onClick={toggleCommunity}>
              커뮤니티
            </a>
            <ul className="dropotron level-0">
              <li>
                <NavLink to="/board/free" onClick={closeCommunity}>
                  자유게시판 (비회원)
                </NavLink>
              </li>
              <li>
                <NavLink to="/board/qna" onClick={closeCommunity}>
                  Q&amp;A
                </NavLink>
              </li>
              <li>
                <NavLink to="/board/archive" onClick={closeCommunity}>
                  자료실 (회원)
                </NavLink>
              </li>
            </ul>
          </li>
          {member ? (
            <>
              <li>
                <NavLink to="/mypage" onClick={closeCommunity}>
                  {member.role === 'ADMIN' ? `${member.name} (관리자)` : member.name}
                </NavLink>
              </li>
              <li>
                <a
                  href="#logout"
                  className="button"
                  onClick={(event) => {
                    event.preventDefault()
                    handleLogout()
                  }}
                >
                  로그아웃
                </a>
              </li>
            </>
          ) : (
            <>
              <li>
                <NavLink to="/login" onClick={closeCommunity}>
                  로그인
                </NavLink>
              </li>
              <li>
                <NavLink to="/signup" className="button primary" onClick={closeCommunity}>
                  시작하기
                </NavLink>
              </li>
            </>
          )}
        </ul>
      </nav>
    </header>
  )
}
