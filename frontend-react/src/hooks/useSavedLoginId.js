import { useCallback, useEffect, useState } from 'react'

const COOKIE = 'savedLoginId'

function readCookie(name) {
  const matched = document.cookie.split('; ').find((row) => row.startsWith(`${name}=`))
  return matched ? decodeURIComponent(matched.split('=')[1]) : ''
}

export function useSavedLoginId() {
  const [savedId, setSavedId] = useState('')

  useEffect(() => {
    setSavedId(readCookie(COOKIE))
  }, [])

  const persist = useCallback((loginId, remember) => {
    if (remember && loginId) {
      document.cookie = `${COOKIE}=${encodeURIComponent(loginId)}; path=/; max-age=${60 * 60 * 24 * 30}`
    } else {
      document.cookie = `${COOKIE}=; path=/; max-age=0`
    }
  }, [])

  return { savedId, persist }
}
