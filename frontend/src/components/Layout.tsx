import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { LogOut, Menu, X, BarChart3, Calendar, Settings, Clock, Users, FileText } from 'lucide-react'
import { useAuthStore } from '../store/authStore'

interface LayoutProps {
  children: React.ReactNode
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  const navigate = useNavigate()
  const { user, clearAuth } = useAuthStore()
  const [sidebarOpen, setSidebarOpen] = useState(true)

  const handleLogout = () => {
    clearAuth()
    navigate('/login')
  }

  // Menu items vary by role
  const getMenuItems = () => {
    const commonItems = [
      { icon: Clock, label: 'Mark Attendance', path: '/attendance' },
      { icon: BarChart3, label: 'Alterations', path: '/alterations' },
      { icon: Calendar, label: 'Timetable', path: '/timetables' },
    ]

    const hodAdminItems = [
      { icon: Users, label: 'HOD Dashboard', path: '/hod-dashboard' },
      { icon: FileText, label: 'Manage Timetables', path: '/manage-timetable' },
    ]

    const isHodOrAdmin = user?.roles.some((r) => ['HOD', 'ADMIN'].includes(r))
    return isHodOrAdmin ? [...commonItems, ...hodAdminItems] : commonItems
  }

  const menuItems = getMenuItems()

  return (
    <div className="flex h-screen bg-slate-50">
      {/* Sidebar */}
      <div
        className={`${
          sidebarOpen ? 'w-64' : 'w-20'
        } bg-gradient-to-b from-slate-900 to-slate-800 text-white transition-all duration-300 flex flex-col`}
      >
        {/* Logo */}
        <div className="p-6 border-b border-slate-700 flex items-center justify-between">
          {sidebarOpen && <h1 className="text-xl font-bold gradient-text">SA System</h1>}
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="p-2 hover:bg-slate-700 rounded-lg"
          >
            {sidebarOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>

        {/* Menu Items */}
        <nav className="flex-1 p-4 space-y-2">
          {menuItems.map((item) => (
            <button
              key={item.path}
              onClick={() => navigate(item.path)}
              className="w-full flex items-center gap-3 px-4 py-3 rounded-lg hover:bg-slate-700 transition-colors text-left"
            >
              <item.icon className="w-5 h-5 flex-shrink-0" />
              {sidebarOpen && <span>{item.label}</span>}
            </button>
          ))}
        </nav>

        {/* User Info */}
        <div className="p-4 border-t border-slate-700 space-y-4">
          {sidebarOpen && (
            <div className="text-sm">
              <p className="text-slate-400 text-xs uppercase tracking-wider">Logged in as</p>
              <p className="font-semibold truncate">{user?.username}</p>
              <p className="text-xs text-slate-400 capitalize">{user?.roles && user.roles.length > 0 ? user.roles[0] : 'User'}</p>
            </div>
          )}
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 rounded-lg transition-colors text-sm font-medium"
          >
            <LogOut className="w-4 h-4" />
            {sidebarOpen && 'Logout'}
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top Bar */}
        <div className="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between shadow-sm">
          <h2 className="text-2xl font-bold text-slate-900">Staff Alteration System</h2>
          <div className="flex items-center gap-4">
            <div className="text-right">
              <p className="text-sm font-medium text-slate-900">{user?.username}</p>
              <p className="text-xs text-slate-500">{user?.email}</p>
            </div>
          </div>
        </div>

        {/* Page Content */}
        <div className="flex-1 overflow-auto">
          <main className="p-6">{children}</main>
        </div>
      </div>
    </div>
  )
}
