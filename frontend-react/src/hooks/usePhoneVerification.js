import { useCallback, useEffect, useRef, useState } from 'react'
import { memberApi } from '../services/api/memberApi'
import { isMobilePhone } from '../utils/memberForm'

export function usePhoneVerification() {
  const [verified, setVerified] = useState(false)
  const [token, setToken] = useState('')
  const [sent, setSent] = useState(false)
  const [cooldown, setCooldown] = useState(0)
  const [loading, setLoading] = useState(false)
  const timerRef = useRef(null)

  useEffect(() => () => clearInterval(timerRef.current), [])

  const startCooldown = useCallback((seconds) => {
    clearInterval(timerRef.current)
    setCooldown(seconds)
    if (seconds <= 0) {
      return
    }
    timerRef.current = setInterval(() => {
      setCooldown((prev) => {
        if (prev <= 1) {
          clearInterval(timerRef.current)
          return 0
        }
        return prev - 1
      })
    }, 1000)
  }, [])

  const reset = useCallback(() => {
    clearInterval(timerRef.current)
    setVerified(false)
    setToken('')
    setSent(false)
    setCooldown(0)
  }, [])

  const send = useCallback(async (phone) => {
    if (!isMobilePhone(phone)) {
      throw new Error('휴대폰 번호는 숫자 10~11자리로 입력하세요.')
    }
    setLoading(true)
    try {
      const res = await memberApi.sendPhoneCode(phone)
      setSent(true)
      setVerified(false)
      setToken('')
      startCooldown(res.data?.cooldownSeconds ?? 60)
      return res
    } finally {
      setLoading(false)
    }
  }, [startCooldown])

  const verify = useCallback(async (phone, code) => {
    if (!code || !/^\d{6}$/.test(code.trim())) {
      throw new Error('인증번호 6자리를 입력하세요.')
    }
    setLoading(true)
    try {
      const res = await memberApi.verifyPhone(phone, code.trim())
      setVerified(true)
      setToken(res.data?.verificationToken || '')
      return res
    } finally {
      setLoading(false)
    }
  }, [])

  return { verified, token, sent, cooldown, loading, send, verify, reset }
}
