/**
 * Hexaq
 * 계층: Components
 * 객체: PageHeader
 * 책임: PageHeader 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
export default function PageHeader({ eyebrow, kicker, title }) {
  return (
    <header className="major">
      {eyebrow ? <p className="hx-kicker">{eyebrow}</p> : null}
      <h2>{title}</h2>
      {kicker ? <p>{kicker}</p> : null}
    </header>
  )
}
