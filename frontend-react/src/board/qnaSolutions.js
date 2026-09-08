export const QNA_SOLUTIONS = [
  { slug: 'web', label: 'Web Platform', shortLabel: 'Web' },
  { slug: 'mobile', label: 'Mobile', shortLabel: 'Mobile' },
  { slug: 'ai', label: 'Applied AI', shortLabel: 'AI' },
  { slug: 'fintech', label: 'Fintech Infra', shortLabel: 'Fintech' },
  { slug: 'general', label: '일반 Q&A', shortLabel: '일반' },
]

const ALLOWED = new Set(QNA_SOLUTIONS.map((item) => item.slug))

export function resolveQnaSolution(value) {
  const key = String(value || '').trim().toLowerCase()
  return ALLOWED.has(key) ? key : 'general'
}

export function qnaSolutionLabel(value) {
  const slug = resolveQnaSolution(value)
  return QNA_SOLUTIONS.find((item) => item.slug === slug)?.label || 'Q&A'
}

export function qnaBoardPath(solution, ...parts) {
  const base = `/board/qna/${resolveQnaSolution(solution)}`
  const extra = parts.filter((part) => part !== undefined && part !== null && part !== '').join('/')
  return extra ? `${base}/${extra}` : base
}
