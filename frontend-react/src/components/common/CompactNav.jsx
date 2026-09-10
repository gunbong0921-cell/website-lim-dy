/**
 * Hexaq
 * 계층: Components
 * 객체: CompactNav
 * 책임: 폰·태블릿 햄버거 바와 슬라이드 패널. 데스크톱 Header 금지
 * 문서: [docs/technical/05-responsive.md](../../../../docs/technical/05-responsive.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
import { useState } from 'react'
import { createPortal } from 'react-dom'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

export default function CompactNav() {
  const { member, logout } = useAuth()
  const navigate = useNavigate()
  const [panelOpen, setPanelOpen] = useState(false)

  function closeAll() {
    setPanelOpen(false)
    document.body.classList.remove('navPanel-visible')
  }

  function togglePanel() {
    setPanelOpen((open) => {
      const next = !open
      document.body.classList.toggle('navPanel-visible', next)
      return next
    })
  }

  async function handleLogout(event) {
    event.preventDefault()
    await logout()
    closeAll()
    navigate('/')
  }

  return createPortal(
    <>
      <div id="titleBar">
        <button type="button" className="toggle" aria-label="메뉴" onClick={togglePanel} />
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
              <a href="#logout" className="link depth-0" onClick={handleLogout}>
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
  )
}
