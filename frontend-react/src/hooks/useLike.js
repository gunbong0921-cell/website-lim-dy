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
