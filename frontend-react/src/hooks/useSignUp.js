import { useCallback, useState } from 'react'
import { memberApi } from '../services/api/memberApi'
import { useRecaptcha } from './useRecaptcha'

export function useSignUp() {
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const { execute } = useRecaptcha()

  const checkId = useCallback(async (loginId) => {
    if (!loginId || !loginId.trim()) {
      throw new Error('아이디를 입력한 뒤 중복확인 해주세요.')
    }
    return memberApi.checkId(loginId)
  }, [])
  const checkEmail = useCallback(async (email) => {
    if (!email || !email.trim()) {
      throw new Error('이메일을 입력한 뒤 중복확인 해주세요.')
    }
    return memberApi.checkEmail(email)
  }, [])

  const signUp = useCallback(async (payload) => {
    setLoading(true)
    try {
      const recaptchaToken = await execute('signup')
      const res = await memberApi.signUp({ ...payload, recaptchaToken, website: payload.website ?? '' })
      setResult(res.data)
      return res
    } finally {
      setLoading(false)
    }
  }, [execute])

  const verifyEmail = useCallback((loginId, code) => memberApi.verifyEmail(loginId, code), [])

  const resendVerification = useCallback(async (loginId) => {
    const res = await memberApi.resendVerification(loginId)
    setResult(res.data)
    return res
  }, [])

  const verifyBusiness = useCallback((payload) => memberApi.verifyBusiness(payload), [])

  return { loading, result, checkId, checkEmail, signUp, verifyEmail, resendVerification, verifyBusiness }
}
