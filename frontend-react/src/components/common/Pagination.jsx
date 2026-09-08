export default function Pagination({ page, totalPages, onChange }) {
  if (!totalPages || totalPages < 2) return null
  const pages = []
  const start = Math.max(1, page - 2)
  const end = Math.min(totalPages, start + 4)
  for (let i = start; i <= end; i += 1) pages.push(i)

  return (
    <nav className="hx-pager" aria-label="페이지">
      <button type="button" className="button small" disabled={page <= 1} onClick={() => onChange(page - 1)}>
        이전
      </button>
      {pages.map((n) => (
        <button
          key={n}
          type="button"
          className={`button small${n === page ? ' primary' : ''}`}
          onClick={() => onChange(n)}
        >
          {n}
        </button>
      ))}
      <button
        type="button"
        className="button small"
        disabled={page >= totalPages}
        onClick={() => onChange(page + 1)}
      >
        다음
      </button>
    </nav>
  )
}
