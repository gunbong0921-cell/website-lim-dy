/**
 * Hexaq
 * 계층: Hooks
 * 객체: useBoardList
 * 책임: 화면 유스케이스. services/api 호출
 * 문서: [docs/features/overview.md](../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../docs/technical/01-architecture.md)
 */
import { useCallback, useEffect, useState } from 'react'
import { boardApi } from '../services/api/boardApi'

export function useBoardList(type, solution) {
  const [page, setPage] = useState(1)
  const [keyword, setKeyword] = useState('')
  const [searchType, setSearchType] = useState('title')
  const [applied, setApplied] = useState({ keyword: '', searchType: 'title' })
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = useCallback(async (nextPage, nextSearch = applied) => {
    setLoading(true)
    setError('')
    try {
      const res = await boardApi.list(type, {
        page: nextPage,
        size: 10,
        searchType: nextSearch.searchType,
        keyword: nextSearch.keyword,
        ...(type === 'qna' ? { solution } : {}),
      })
      setData(res.data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [type, solution, applied])

  useEffect(() => {
    setPage(1)
  }, [type, solution])

  useEffect(() => {
    load(page)
  }, [load, page])

  const search = useCallback(() => {
    const next = { keyword, searchType }
    setApplied(next)
    setPage(1)
    load(1, next)
  }, [keyword, searchType, load])

  return { data, loading, error, page, setPage, keyword, setKeyword, searchType, setSearchType, reload: search }
}
