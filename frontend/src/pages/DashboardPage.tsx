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

interface StatCard {
  icon: React.ReactNode
  label: string
  value: string | number
  change?: string
}

export const DashboardPage: React.FC = () => {
  const user = useAuthStore((state) => state.user)
  const navigate = useNavigate()
  const [stats] = useState<StatCard[]>([
    {
      icon: <Calendar className="w-8 h-8" />,
      label: 'Active Classes',
      value: '12',
      change: '+2 this week',
    },
    {
      icon: <Users className="w-8 h-8" />,
      label: 'Staff Members',
      value: '24',
      change: 'All active',
    },
    {
      icon: <BookOpen className="w-8 h-8" />,
      label: 'Total Timetables',
      value: '48',
      change: '+5 this month',
    },
    {
      icon: <Clock className="w-8 h-8" />,
      label: 'Avg. Workload',
      value: '6.2h',
      change: 'Per staff',
    },
  ])

  const recentActivities = [
    { time: '2 hours ago', action: 'Java Class Created', class: 'CS1' },
    { time: '5 hours ago', action: 'Timetable Updated', class: 'CS2' },
    { time: '1 day ago', action: 'New Staff Assigned', class: 'IT1' },
    { time: '2 days ago', action: 'Schedule Changed', class: 'IT2' },
  ]

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn">
        {/* Welcome Section */}
        <div className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg p-8 text-white">
          <h1 className="text-4xl font-bold mb-2">Welcome, {user?.username}! 👋</h1>
          <p className="text-blue-100 text-lg">
            {user?.roles.includes('ADMIN')
              ? "You're logged in as Administrator"
              : user?.roles.includes('HOD')
              ? "You're logged in as Head of Department"
              : "You're logged in as Staff"}
          </p>
          <Button
            variant="secondary"
            className="mt-6"
            onClick={() => navigate('/timetables')}
          >
            Go to Timetables
            <ArrowRight className="w-5 h-5" />
          </Button>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {stats.map((stat, i) => (
            <Card key={i} className="p-6 hover:shadow-lg transition-shadow">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <p className="text-slate-600 text-sm font-medium">{stat.label}</p>
                  <h3 className="text-3xl font-bold text-slate-900 mt-2">{stat.value}</h3>
                  {stat.change && (
                    <p className="text-sm text-green-600 mt-2 flex items-center gap-1">
                      <TrendingUp className="w-4 h-4" />
                      {stat.change}
                    </p>
                  )}
                </div>
                <div className="p-3 bg-gradient-to-br from-blue-100 to-purple-100 rounded-lg text-blue-600">
                  {stat.icon}
                </div>
              </div>
            </Card>
          ))}
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
                { label: 'Create Schedule', action: () => navigate('/timetables') },
                { label: 'Manage Staff', action: () => navigate('/settings') },
                { label: 'View Reports', action: () => alert('Coming soon!') },
                { label: 'Settings', action: () => navigate('/settings') },
                { label: 'Help & Support', action: () => alert('Support coming soon!') },
              ].map((item, i) => (
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

          {/* Recent Activity */}
          <Card className="p-6">
            <h2 className="text-xl font-bold text-slate-900 mb-6 flex items-center gap-2">
              <Clock className="w-6 h-6 text-purple-600" />
              Recent Activity
            </h2>
            <div className="space-y-4">
              {recentActivities.map((activity, i) => (
                <div key={i} className="flex gap-4 pb-4 border-b border-slate-200 last:border-0">
                  <div className="w-2 h-2 bg-blue-600 rounded-full mt-1.5 flex-shrink-0" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-slate-900 truncate">
                      {activity.action}
                    </p>
                    <p className="text-xs text-slate-500 mt-1">{activity.time}</p>
                    <p className="text-xs text-blue-600 font-medium mt-1">{activity.class}</p>
                  </div>
                </div>
              ))}
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
                desc: 'Assign staff to classes with conflict detection',
              },
              {
                title: '📊 Analytics & Reports',
                desc: 'View detailed workload and scheduling reports',
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
