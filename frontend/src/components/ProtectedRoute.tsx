import React from 'react'
import { Navigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

interface ProtectedRouteProps {
  children: React.ReactNode
  requiredRoles?: string[]
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRoles }) => {
  const { user, isAuthenticated } = useAuthStore()

  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />
  }

  if (requiredRoles && user && !requiredRoles.some((role) => user.roles.includes(role))) {
    return (
      <div className="flex items-center justify-center h-screen bg-slate-50">
        <div className="text-center">
          <h1 className="text-4xl font-bold text-slate-900 mb-2">❌ Access Denied</h1>
          <p className="text-slate-600 mb-6">You don't have permission to access this page.</p>
          <p className="text-sm text-slate-500">Required roles: {requiredRoles.join(', ')}</p>
        </div>
      </div>
    )
  }

  return <>{children}</>
}
