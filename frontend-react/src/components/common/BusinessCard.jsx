/**
 * Hexaq
 * 계층: Components
 * 객체: BusinessCard
 * 책임: BusinessCard 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
import { Link } from 'react-router-dom'

export default function BusinessCard({ kicker, title, body, to, action }) {
  return (
    <Link to={to} className="hx-business-card">
      <span className="hx-kicker">{kicker}</span>
      <h3>{title}</h3>
      <p>{body}</p>
      {action ? <span className="hx-card-action">{action}</span> : null}
    </Link>
  )
}
