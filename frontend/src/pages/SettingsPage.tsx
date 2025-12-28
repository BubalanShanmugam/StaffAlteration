import React from 'react'
import { Layout } from '../components/Layout'
import { Card } from '../components/common'
import { Settings as SettingsIcon, Bell, Lock, User } from 'lucide-react'
import { useAuthStore } from '../store/authStore'

export const SettingsPage: React.FC = () => {
  const user = useAuthStore((state) => state.user)

  const sections = [
    {
      icon: User,
      title: 'Profile Settings',
      items: [
        { label: 'Username', value: user?.username },
        { label: 'Email', value: user?.email },
        { label: 'Role', value: user?.roles.join(', ') },
      ],
    },
    {
      icon: Lock,
      title: 'Security',
      items: [
        { label: 'Change Password', value: 'Secure your account' },
        { label: 'Two-Factor Auth', value: 'Not enabled' },
        { label: 'Last Login', value: 'Today at 2:30 PM' },
      ],
    },
    {
      icon: Bell,
      title: 'Notifications',
      items: [
        { label: 'Email Notifications', value: 'Enabled' },
        { label: 'Schedule Updates', value: 'Enabled' },
        { label: 'System Alerts', value: 'Enabled' },
      ],
    },
  ]

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
            <SettingsIcon className="w-8 h-8 text-blue-600" />
            Settings
          </h1>
          <p className="text-slate-600 mt-1">Manage your account and system preferences</p>
        </div>

        <div className="grid gap-6">
          {sections.map((section, i) => (
            <Card key={i} className="p-6">
              <h2 className="text-lg font-bold text-slate-900 mb-6 flex items-center gap-2">
                <section.icon className="w-5 h-5 text-blue-600" />
                {section.title}
              </h2>
              <div className="space-y-4">
                {section.items.map((item, j) => (
                  <div key={j} className="flex justify-between items-center py-3 border-b border-slate-100 last:border-0">
                    <span className="text-slate-700 font-medium">{item.label}</span>
                    <span className="text-slate-600 text-sm">{item.value}</span>
                  </div>
                ))}
              </div>
            </Card>
          ))}
        </div>
      </div>
    </Layout>
  )
}
