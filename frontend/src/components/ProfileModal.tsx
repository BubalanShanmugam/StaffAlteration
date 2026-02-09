import React from 'react'
import { X, Mail, Phone, Building2, User, Briefcase, Calendar, Shield } from 'lucide-react'
import { useAuthStore } from '../store/authStore'

interface ProfileModalProps {
  isOpen: boolean
  onClose: () => void
}

export const ProfileModal: React.FC<ProfileModalProps> = ({ isOpen, onClose }) => {
  const user = useAuthStore((state) => state.user)

  if (!isOpen || !user) return null

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 animate-fadeIn">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full animate-slideInUp">
        {/* Header */}
        <div className="bg-gradient-to-r from-blue-600 to-purple-600 px-6 py-8 flex items-start justify-between">
          <div className="flex items-center space-x-4">
            <div className="w-16 h-16 rounded-full bg-white/20 flex items-center justify-center">
              <User className="w-8 h-8 text-white" />
            </div>
            <div className="text-white">
              <h2 className="text-xl font-bold">{user.firstName && user.lastName ? `${user.firstName} ${user.lastName}` : user.username}</h2>
              <p className="text-sm text-white/80">{user.username}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-white hover:bg-white/20 rounded-lg p-1 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Profile Details */}
        <div className="px-6 py-6 space-y-5">
          {/* Username */}
          <div className="flex items-start space-x-4">
            <User className="w-5 h-5 text-blue-600 mt-1 flex-shrink-0" />
            <div className="flex-1">
              <p className="text-sm text-slate-600 font-medium">Username</p>
              <p className="text-slate-900 font-mono">{user.username}</p>
            </div>
          </div>

          {/* Staff ID */}
          <div className="flex items-start space-x-4">
            <Briefcase className="w-5 h-5 text-blue-600 mt-1 flex-shrink-0" />
            <div className="flex-1">
              <p className="text-sm text-slate-600 font-medium">Staff ID</p>
              <p className="text-slate-900 font-mono">{user.staffId}</p>
            </div>
          </div>

          {/* Email */}
          <div className="flex items-start space-x-4">
            <Mail className="w-5 h-5 text-blue-600 mt-1 flex-shrink-0" />
            <div className="flex-1">
              <p className="text-sm text-slate-600 font-medium">Email Address</p>
              <p className="text-slate-900 break-all text-sm">{user.email}</p>
            </div>
          </div>

          {/* Phone */}
          {user.phone && (
            <div className="flex items-start space-x-4">
              <Phone className="w-5 h-5 text-blue-600 mt-1 flex-shrink-0" />
              <div className="flex-1">
                <p className="text-sm text-slate-600 font-medium">Phone Number</p>
                <p className="text-slate-900">{user.phone}</p>
              </div>
            </div>
          )}

          {/* Department */}
          {user.department && (
            <div className="flex items-start space-x-4">
              <Building2 className="w-5 h-5 text-blue-600 mt-1 flex-shrink-0" />
              <div className="flex-1">
                <p className="text-sm text-slate-600 font-medium">Department</p>
                <p className="text-slate-900">{user.department}</p>
              </div>
            </div>
          )}

          {/* Roles */}
          <div className="flex items-start space-x-4">
            <Shield className="w-5 h-5 text-blue-600 mt-1 flex-shrink-0" />
            <div className="flex-1">
              <p className="text-sm text-slate-600 font-medium">Assigned Roles</p>
              <div className="flex flex-wrap gap-2 mt-2">
                {user.roles && user.roles.length > 0 ? (
                  user.roles.map((role) => (
                    <span
                      key={role}
                      className="px-3 py-1 bg-gradient-to-r from-blue-500 to-purple-500 text-white rounded-full text-xs font-semibold"
                    >
                      {role}
                    </span>
                  ))
                ) : (
                  <span className="text-slate-500 text-sm">No roles assigned</span>
                )}
              </div>
            </div>
          </div>

          {/* User ID (if available) */}
          {user.id && (
            <div className="flex items-start space-x-4 pt-2 border-t border-slate-200">
              <User className="w-5 h-5 text-slate-400 mt-1 flex-shrink-0" />
              <div className="flex-1">
                <p className="text-xs text-slate-500">User ID</p>
                <p className="text-slate-600 font-mono text-xs">{user.id}</p>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="bg-slate-50 px-6 py-4 flex justify-end gap-3">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-slate-200 text-slate-900 rounded-lg hover:bg-slate-300 transition-colors font-medium"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  )
}
