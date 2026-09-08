import { useCallback, useEffect, useState } from 'react'
import { boardApi } from '../services/api/boardApi'

export function useBoardDetail(type, id, solution) {
  const [post, setPost] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    if (!type || !id) {
      setLoading(false)
      return
    }
    setLoading(true)
    setError('')
    try {
      const res = await boardApi.read(type, id, type === 'qna' ? { solution } : undefined)
      setPost(res.data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [type, id, solution])

  useEffect(() => {
    load()
  }, [load])

  return { post, loading, error, reload: load, setPost }
}
