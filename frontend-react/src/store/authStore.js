/**
 * Hexaq
 * 계층: Store
 * 객체: authStore
 * 책임: 클라이언트 상태(zustand). 비밀키 없음
 * 문서: [docs/features/overview.md](../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../docs/technical/01-architecture.md)
 */
import { create } from 'zustand'

export const useAuthStore = create((set) => ({
  member: null,
  ready: false,
  setMember: (member) => set({ member, ready: true }),
  clear: () => set({ member: null, ready: true }),
}))
