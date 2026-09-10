/**
 * Hexaq
 * 계층: Components
 * 객체: CtaSection
 * 책임: CtaSection 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
import { Link } from 'react-router-dom'

export default function CtaSection() {
  return (
    <section id="five" className="wrapper style2 special fade">
      <div className="container">
        <header>
          <h2>함께 디지털 인프라를 만듭니다</h2>
          <p>신규 팀도 기술 파트너로 보이도록, 사례와 지표를 먼저 보여 드립니다.</p>
        </header>
        <ul className="actions special">
          <li>
            <Link to="/signup" className="button primary">
              Hexaq 시작하기
            </Link>
          </li>
        </ul>
      </div>
    </section>
  )
}
