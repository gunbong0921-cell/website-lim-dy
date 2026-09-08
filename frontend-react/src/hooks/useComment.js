import { useCallback, useEffect, useState } from 'react'
import { commentApi } from '../services/api/commentApi'

export function useComment(boardId) {
  const [comments, setComments] = useState([])
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    if (!boardId) return
    try {
      const res = await commentApi.list(boardId)
      setComments(res.data || [])
      setError('')
    } catch (err) {
      setError(err.message)
    }
  }, [boardId])

  useEffect(() => {
    load()
  }, [load])

  const write = useCallback(async (content) => {
    await commentApi.write(boardId, content)
    await load()
  }, [boardId, load])

  const update = useCallback(async (commentId, content) => {
    await commentApi.update(commentId, content)
    await load()
  }, [load])

  const remove = useCallback(async (commentId) => {
    await commentApi.remove(commentId)
    await load()
  }, [load])

  return { comments, error, write, update, remove, reload: load }
}
