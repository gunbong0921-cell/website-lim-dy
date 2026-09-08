import { useState } from 'react'
import { createPortal } from 'react-dom'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import BrandLogo from './BrandLogo'

export default function Header() {
  const { member, logout } = useAuth()
  const navigate = useNavigate()
  const [panelOpen, setPanelOpen] = useState(false)
  const [communityOpen, setCommunityOpen] = useState(false)

  async function handleLogout() {
    await logout()
    closeAll()
    navigate('/')
  }

  function closeAll() {
    setPanelOpen(false)
    setCommunityOpen(false)
    document.body.classList.remove('navPanel-visible')
  }

  function togglePanel() {
    setPanelOpen((open) => {
      const next = !open
      document.body.classList.toggle('navPanel-visible', next)
      return next
    })
  }

  function toggleCommunity(event) {
    event.preventDefault()
    setCommunityOpen((value) => !value)
  }

  function goHome(event) {
    event.preventDefault()
    closeAll()
    navigate('/')
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
  }

  return (
    <>
      <header id="header">
        <h1 id="logo">
          <NavLink to="/" end onClick={goHome} aria-label="HEXAQ 홈">
            <BrandLogo />
          </NavLink>
        </h1>
        <nav id="nav">
          <ul>
            <li>
              <NavLink to="/" end onClick={closeAll}>
                홈
              </NavLink>
            </li>
            <li>
              <NavLink to="/solutions" onClick={closeAll}>
                솔루션
              </NavLink>
            </li>
            <li>
              <NavLink to="/insights" onClick={closeAll}>
                인사이트
              </NavLink>
            </li>
            <li className={communityOpen ? 'open' : ''}>
              <a href="#community" onClick={toggleCommunity}>
                커뮤니티
              </a>
              <ul className="dropotron level-0">
                <li>
                  <NavLink to="/board/free" onClick={closeAll}>
                    자유게시판 (비회원)
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/board/qna" onClick={closeAll}>
                    Q&amp;A
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/board/archive" onClick={closeAll}>
                    자료실 (회원)
                  </NavLink>
                </li>
              </ul>
            </li>
            {member ? (
              <>
                <li>
                  <NavLink to="/mypage" onClick={closeAll}>
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
                  <NavLink to="/login" onClick={closeAll}>
                    로그인
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/signup" className="button primary" onClick={closeAll}>
                    시작하기
                  </NavLink>
                </li>
              </>
            )}
          </ul>
        </nav>
      </header>
      {createPortal(
        <>
          <div id="titleBar">
            <button type="button" className="toggle" aria-label="메뉴" onClick={togglePanel} />
            <span className="title">
              <NavLink to="/" end onClick={goHome} aria-label="HEXAQ 홈">
                <BrandLogo />
              </NavLink>
            </span>
          </div>
          <div id="navPanel">
            <nav>
              <NavLink to="/" end className="link depth-0" onClick={closeAll}>
                홈
              </NavLink>
              <NavLink to="/solutions" className="link depth-0" onClick={closeAll}>
                솔루션
              </NavLink>
              <NavLink to="/insights" className="link depth-0" onClick={closeAll}>
                인사이트
              </NavLink>
              <span className="link depth-0">커뮤니티</span>
              <NavLink to="/board/free" className="link depth-1" onClick={closeAll}>
                <span className="indent-1" />
                자유게시판 (비회원)
              </NavLink>
              <NavLink to="/board/qna" className="link depth-1" onClick={closeAll}>
                <span className="indent-1" />
                Q&amp;A
              </NavLink>
              <NavLink to="/board/archive" className="link depth-1" onClick={closeAll}>
                <span className="indent-1" />
                자료실 (회원)
              </NavLink>
              {member ? (
                <>
                  <NavLink to="/mypage" className="link depth-0" onClick={closeAll}>
                    {member.role === 'ADMIN' ? `${member.name} (관리자)` : member.name}
                  </NavLink>
                  <a
                    href="#logout"
                    className="link depth-0"
                    onClick={(event) => {
                      event.preventDefault()
                      handleLogout()
                    }}
                  >
                    로그아웃
                  </a>
                </>
              ) : (
                <>
                  <NavLink to="/login" className="link depth-0" onClick={closeAll}>
                    로그인
                  </NavLink>
                  <NavLink to="/signup" className="link depth-0" onClick={closeAll}>
                    시작하기
                  </NavLink>
                </>
              )}
            </nav>
          </div>
        </>,
        document.body,
      )}
    </>
  )
}
