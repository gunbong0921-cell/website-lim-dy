/**
 * Hexaq
 * 계층: Hooks
 * 객체: useCompactNav
 * 책임: 1180px 이하(폰·태블릿) 뷰포트 여부. 마크업 금지
 * 문서: [docs/technical/05-responsive.md](../../../docs/technical/05-responsive.md) · [docs/technical/01-architecture.md](../../../docs/technical/01-architecture.md)
 */
import { useEffect, useState } from 'react'

export const COMPACT_NAV_MAX_PX = 1180
export const COMPACT_NAV_QUERY = `(max-width: ${COMPACT_NAV_MAX_PX}px)`

export function useCompactNav() {
  const [compact, setCompact] = useState(() =>
    typeof window !== 'undefined' ? window.matchMedia(COMPACT_NAV_QUERY).matches : false,
  )

  useEffect(() => {
    const mq = window.matchMedia(COMPACT_NAV_QUERY)
    const sync = () => setCompact(mq.matches)
    sync()
    mq.addEventListener('change', sync)
    return () => mq.removeEventListener('change', sync)
  }, [])

  return compact
}
