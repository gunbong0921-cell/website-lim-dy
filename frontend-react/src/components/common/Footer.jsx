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
            <span className="label">자료실</span>
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
