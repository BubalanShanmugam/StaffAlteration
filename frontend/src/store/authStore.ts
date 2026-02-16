import { create } from 'zustand'

export interface User {
  id: number
  username: string
  email: string
  staffId: string  // Staff ID (e.g., "STAFF001")
  firstName?: string
  lastName?: string
  phone?: string
  department?: string
  departmentId?: number
  roles: string[]
}

interface AuthStore {
  user: User | null
  accessToken: string | null
  refreshToken: string | null
  isLoading: boolean
  error: string | null
  setAuth: (user: User, accessToken: string, refreshToken: string) => void
  clearAuth: () => void
  setLoading: (loading: boolean) => void
  setError: (error: string | null) => void
  isAuthenticated: () => boolean
  hasRole: (role: string) => boolean
}

export const useAuthStore = create<AuthStore>((set, get) => ({
  user: null,
  accessToken: localStorage.getItem('accessToken'),
  refreshToken: localStorage.getItem('refreshToken'),
  isLoading: false,
  error: null,
  
  setAuth: (user, accessToken, refreshToken) => {
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    localStorage.setItem('user', JSON.stringify(user))
    set({ user, accessToken, refreshToken })
  },
  
  clearAuth: () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    set({ user: null, accessToken: null, refreshToken: null })
  },
  
  setLoading: (isLoading) => set({ isLoading }),
  setError: (error) => set({ error }),
  
  isAuthenticated: () => {
    const state = get()
    return !!state.accessToken && !!state.user
  },
  
  hasRole: (role: string) => {
    const state = get()
    return state.user?.roles.includes(role) ?? false
  },
}))

// Initialize from localStorage
const storedUser = localStorage.getItem('user')
if (storedUser) {
  try {
    const user = JSON.parse(storedUser)
    useAuthStore.setState({
      user,
      accessToken: localStorage.getItem('accessToken'),
      refreshToken: localStorage.getItem('refreshToken'),
    })
  } catch (e) {
    console.error('Failed to parse stored user:', e)
  }
}
