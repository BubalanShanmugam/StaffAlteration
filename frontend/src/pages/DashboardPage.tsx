import React, { useEffect, useState } from 'react'
import {
  BarChart3,
  Users,
  Calendar,
  TrendingUp,
  ArrowRight,
  Clock,
  BookOpen,
} from 'lucide-react'
import { Layout } from '../components/Layout'
import { Card, Button } from '../components/common'
import { useAuthStore } from '../store/authStore'
import { useNavigate } from 'react-router-dom'

export const DashboardPage: React.FC = () => {
  const user = useAuthStore((state) => state.user)
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn">
        {/* Welcome Section */}
        <div className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg p-8 text-white">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-4xl font-bold mb-2">Welcome, {user?.firstName || user?.username}! 👋</h1>
              <p className="text-blue-100 text-lg mb-4">
                {user?.roles.includes('ADMIN')
                  ? "Administrator Dashboard"
                  : user?.roles.includes('HOD')
                  ? "Head of Department Dashboard"
                  : "Staff Dashboard"}
              </p>
              <div className="flex gap-3">
                <Button
                  variant="secondary"
                  onClick={() => navigate('/timetables')}
                >
                  View Timetables
                  <ArrowRight className="w-5 h-5" />
                </Button>
                <Button
                  variant="secondary"
                  onClick={() => navigate('/attendance')}
                >
                  Mark Attendance
                  <ArrowRight className="w-5 h-5" />
                </Button>
              </div>
            </div>
            <div className="hidden lg:block text-6xl opacity-20">📊</div>
          </div>
        </div>

        {/* Quick Info Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Card className="p-6 border-l-4 border-blue-500">
            <p className="text-slate-600 text-sm">Staff ID</p>
            <p className="text-2xl font-bold text-slate-900 mt-1">{user?.staffId}</p>
          </Card>
          <Card className="p-6 border-l-4 border-purple-500">
            <p className="text-slate-600 text-sm">Department</p>
            <p className="text-2xl font-bold text-slate-900 mt-1">{user?.department || 'N/A'}</p>
          </Card>
        </div>

        {/* Main Content Grid */}
        <div className="grid lg:grid-cols-3 gap-6">
          {/* Quick Actions */}
          <Card className="p-6 lg:col-span-2">
            <h2 className="text-xl font-bold text-slate-900 mb-6 flex items-center gap-2">
              <BarChart3 className="w-6 h-6 text-blue-600" />
              Quick Actions
            </h2>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
              {[
                { label: 'View Timetables', action: () => navigate('/timetables') },
                { label: 'Mark Attendance', action: () => navigate('/attendance') },
                { label: 'View Alterations', action: () => navigate('/alterations') },
                { label: 'Staff Directory', action: () => navigate('/staff-management'), roles: ['HOD', 'ADMIN'] },
                { label: 'Manage Classes', action: () => navigate('/class-management'), roles: ['HOD', 'ADMIN'] },
                { label: 'Settings', action: () => navigate('/settings') },
              ]
                .filter((item: any) => !item.roles || (user?.roles && user.roles.some((r) => item.roles.includes(r))))
                .map((item: any, i) => (
                  <button
                    key={i}
                    onClick={item.action}
                    className="p-4 bg-gradient-to-br from-slate-50 to-slate-100 hover:from-blue-50 hover:to-purple-50 rounded-lg border border-slate-200 hover:border-blue-300 transition-all group"
                  >
                    <p className="text-sm font-medium text-slate-900 group-hover:text-blue-600 transition-colors">
                      {item.label}
                    </p>
                  </button>
                ))}
            </div>
          </Card>

          {/* Status Overview */}
          <Card className="p-6">
            <h2 className="text-xl font-bold text-slate-900 mb-6 flex items-center gap-2">
              <Clock className="w-6 h-6 text-purple-600" />
              System Status
            </h2>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-3 bg-green-50 rounded-lg">
                <span className="text-sm font-medium text-slate-900">Application Status</span>
                <span className="inline-block w-2 h-2 bg-green-500 rounded-full"></span>
              </div>
              <div className="flex items-center justify-between p-3 bg-green-50 rounded-lg">
                <span className="text-sm font-medium text-slate-900">Database Connection</span>
                <span className="inline-block w-2 h-2 bg-green-500 rounded-full"></span>
              </div>
              <div className="flex items-center justify-between p-3 bg-green-50 rounded-lg">
                <span className="text-sm font-medium text-slate-900">Email Service</span>
                <span className="inline-block w-2 h-2 bg-green-500 rounded-full"></span>
              </div>
              <p className="text-xs text-slate-500 text-center pt-2">All systems operational</p>
            </div>
          </Card>
        </div>

        {/* Features Overview */}
        <Card className="p-8 bg-gradient-to-r from-blue-50 to-purple-50">
          <h2 className="text-2xl font-bold text-slate-900 mb-6">System Features</h2>
          <div className="grid md:grid-cols-3 gap-6">
            {[
              {
                title: '📅 Timetable Management',
                desc: 'Create and manage class schedules efficiently',
              },
              {
                title: '👥 Staff Assignment',
                desc: 'Assign staff to classes with automatic alterations',
              },
              {
                title: '🔔 Smart Notifications',
                desc: 'Get real-time alerts for staff changes and updates',
              },
            ].map((feature, i) => (
              <div key={i} className="bg-white p-4 rounded-lg border border-slate-200">
                <h3 className="font-semibold text-slate-900 mb-2">{feature.title}</h3>
                <p className="text-sm text-slate-600">{feature.desc}</p>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </Layout>
  )
}
