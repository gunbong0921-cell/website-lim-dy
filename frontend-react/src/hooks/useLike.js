/**
 * Hexaq
 * 계층: Hooks
 * 객체: useLike
 * 책임: 화면 유스케이스. services/api 호출
 * 문서: [docs/features/overview.md](../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../docs/technical/01-architecture.md)
 */
import { useCallback, useState } from 'react'
import { boardApi } from '../services/api/boardApi'

export function useLike(type, id, initialCount = 0) {
  const [count, setCount] = useState(initialCount)
  const [message, setMessage] = useState('')

  const like = useCallback(async () => {
    setMessage('')
    try {
      const res = await boardApi.like(type, id)
      setCount(res.data)
      setMessage(res.message || '좋아요가 반영되었습니다.')
    } catch (err) {
      setMessage(err.message)
    }
  }, [type, id])

  return { count, message, like, setCount }
}
