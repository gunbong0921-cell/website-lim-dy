/**
 * Hexaq
 * 계층: Components
 * 객체: Pagination
 * 책임: Pagination 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
export default function Pagination({ page, totalPages, onChange }) {
  const last = Math.max(1, totalPages || 1)
  const current = Math.min(Math.max(1, page || 1), last)
  const pages = []
  const start = Math.max(1, current - 2)
  const end = Math.min(last, start + 4)
  for (let i = start; i <= end; i += 1) pages.push(i)

  return (
    <nav className="hx-pager" aria-label="페이지">
      <button type="button" className="button small" disabled={current <= 1} onClick={() => onChange(current - 1)}>
        이전
      </button>
      {pages.map((n) => (
        <button
          key={n}
          type="button"
          className={`button small${n === current ? ' primary' : ''}`}
          onClick={() => onChange(n)}
        >
          {n}
        </button>
      ))}
      <button
        type="button"
        className="button small"
        disabled={current >= last}
        onClick={() => onChange(current + 1)}
      >
        다음
      </button>
    </nav>
  )
}
