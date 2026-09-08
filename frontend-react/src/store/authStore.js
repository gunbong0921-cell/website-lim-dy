import { create } from 'zustand'

export const useAuthStore = create((set) => ({
  member: null,
  ready: false,
  setMember: (member) => set({ member, ready: true }),
  clear: () => set({ member: null, ready: true }),
}))
