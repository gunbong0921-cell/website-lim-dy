import { useCallback } from 'react'
import { memberApi } from '../services/api/memberApi'
import { useAuthStore } from '../store/authStore'

export function useAuth() {
  const member = useAuthStore((s) => s.member)
  const ready = useAuthStore((s) => s.ready)
  const setMember = useAuthStore((s) => s.setMember)
  const clear = useAuthStore((s) => s.clear)

  const bootstrap = useCallback(async () => {
    try {
      const res = await memberApi.me()
      setMember(res.data)
    } catch {
      clear()
    }
  }, [setMember, clear])

  const login = useCallback(async (loginId, password) => {
    const res = await memberApi.login(loginId, password)
    setMember(res.data)
    return res
  }, [setMember])

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
  const forgotPassword = useCallback((email) => memberApi.forgotPassword(email), [])

  return { member, ready, bootstrap, login, logout, updateProfile, changePassword, forgotPassword }
}
