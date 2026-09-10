/**
 * Hexaq
 * 계층: Hooks
 * 객체: useAuth
 * 책임: 화면 유스케이스. services/api 호출
 * 문서: [docs/features/overview.md](../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../docs/technical/01-architecture.md)
 */
import { useCallback } from 'react'
import { memberApi } from '../services/api/memberApi'
import { useAuthStore } from '../store/authStore'
import { useRecaptcha } from './useRecaptcha'

export function useAuth() {
  const member = useAuthStore((s) => s.member)
  const ready = useAuthStore((s) => s.ready)
  const setMember = useAuthStore((s) => s.setMember)
  const clear = useAuthStore((s) => s.clear)
  const { execute } = useRecaptcha()

  const bootstrap = useCallback(async () => {
    try {
      const res = await memberApi.me()
      setMember(res.data)
    } catch {
      clear()
    }
  }, [setMember, clear])

  const login = useCallback(async (loginId, password) => {
    const recaptchaToken = await execute('login')
    const res = await memberApi.login(loginId, password, recaptchaToken)
    setMember(res.data)
    return res
  }, [execute, setMember])

  const logout = useCallback(async () => {
    await memberApi.logout()
    clear()
  }, [clear])

  const updateProfile = useCallback(async (payload) => {
    const res = await memberApi.updateProfile(payload)
    setMember(res.data)
    return res
  }, [setMember])

  const changePassword = useCallback((payload) => memberApi.changePassword(payload), [])
  const findLoginId = useCallback(async (email) => {
    const recaptchaToken = await execute('forgot_id')
    return memberApi.findLoginId(email, recaptchaToken)
  }, [execute])
  const forgotPassword = useCallback(async (email) => {
    const recaptchaToken = await execute('forgot_password')
    return memberApi.forgotPassword(email, recaptchaToken)
  }, [execute])

  return {
    member,
    ready,
    bootstrap,
    login,
    logout,
    updateProfile,
    changePassword,
    findLoginId,
    forgotPassword,
  }
}
