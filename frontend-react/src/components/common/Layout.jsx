import { Outlet, useLocation } from 'react-router-dom'
import { useLandedScroll } from '../../hooks/useLandedScroll'
import Footer from './Footer'
import Header from './Header'
import { useEffect } from 'react'

export default function Layout() {
  const { pathname } = useLocation()
  const isHome = pathname === '/'
  useLandedScroll(pathname)

  useEffect(() => {
    document.body.classList.toggle('landing', isHome)
    return () => {
      document.body.classList.remove('landing')
    }
  }, [isHome])

  return (
    <div id="page-wrapper">
      <Header />
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
