import { useCallback, useState } from 'react'
import { memberApi } from '../services/api/memberApi'

export function useSignUp() {
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)

  const checkId = useCallback(async (loginId) => memberApi.checkId(loginId), [])
  const checkEmail = useCallback(async (email) => memberApi.checkEmail(email), [])

  const signUp = useCallback(async (payload) => {
    setLoading(true)
    try {
      const res = await memberApi.signUp(payload)
      setResult(res.data)
      return res
    } finally {
      setLoading(false)
    }
  }, [])

  const verifyEmail = useCallback((loginId, code) => memberApi.verifyEmail(loginId, code), [])

  const resendVerification = useCallback(async (loginId) => {
    const res = await memberApi.resendVerification(loginId)
    setResult(res.data)
    return res
  }, [])

  return { loading, result, checkId, checkEmail, signUp, verifyEmail, resendVerification }
}
