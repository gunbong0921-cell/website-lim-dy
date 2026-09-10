/**
 * Hexaq
 * 계층: Components
 * 객체: Layout
 * 책임: 페이지 껍데기. Header 또는 CompactNav 조립
 * 문서: [docs/technical/05-responsive.md](../../../../docs/technical/05-responsive.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
import { Outlet, useLocation } from 'react-router-dom'
import { useCompactNav } from '../../hooks/useCompactNav'
import { useLandedScroll } from '../../hooks/useLandedScroll'
import CompactNav from './CompactNav'
import Footer from './Footer'
import Header from './Header'
import { useEffect } from 'react'

export default function Layout() {
  const { pathname } = useLocation()
  const isHome = pathname === '/'
  const compactNav = useCompactNav()
  useLandedScroll(pathname)

  useEffect(() => {
    document.body.classList.toggle('landing', isHome)
    return () => {
      document.body.classList.remove('landing')
    }
  }, [isHome])

  return (
    <div id="page-wrapper">
      {compactNav ? null : <Header />}
      <CompactNav />
      {isHome ? (
        <Outlet />
      ) : (
        <div id="main" className="wrapper style1">
          <div className="container">
            <Outlet />
          </div>
        </div>
      )}
      <Footer />
    </div>
  )
}
