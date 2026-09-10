/**
 * Hexaq
 * 계층: Components
 * 객체: Footer
 * 책임: Footer 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
import { Link } from 'react-router-dom'
import BrandLogo from './BrandLogo'

export default function Footer() {
  return (
    <footer id="footer">
      <p className="hx-footer-logo">
        <Link to="/" aria-label="HEXAQ 홈">
          <BrandLogo />
        </Link>
      </p>
      <ul className="icons">
        <li>
          <Link to="/insights" className="icon solid alt fa-comment">
            <span className="label">인사이트</span>
          </Link>
        </li>
        <li>
          <Link to="/solutions" className="icon solid alt fa-chart-area">
            <span className="label">솔루션</span>
          </Link>
        </li>
        <li>
          <Link to="/board/archive" className="icon solid alt fa-file">
            <span className="label">자료실 (회원)</span>
          </Link>
        </li>
        <li>
          <Link to="/board/qna" className="icon solid alt fa-envelope">
            <span className="label">문의</span>
          </Link>
        </li>
      </ul>
      <ul className="copyright">
        <li>&copy; {new Date().getFullYear()} Hexaq. All rights reserved.</li>
      </ul>
    </footer>
  )
}
