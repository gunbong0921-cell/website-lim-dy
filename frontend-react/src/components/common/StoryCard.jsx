/**
 * Hexaq
 * 계층: Components
 * 객체: StoryCard
 * 책임: StoryCard 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
import { Link } from 'react-router-dom'

export default function StoryCard({ tag, title, body, to }) {
  const inner = (
    <>
      <span className="hx-kicker">{tag}</span>
      <h3>{title}</h3>
      {body ? <p>{body}</p> : null}
    </>
  )

  if (to) {
    return (
      <Link to={to} className="hx-story-card">
        {inner}
      </Link>
    )
  }

  return <article className="hx-story-card">{inner}</article>
}
