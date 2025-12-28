import React, { useState, useEffect } from 'react'
import { Users, Calendar, AlertTriangle, TrendingDown, Eye, Zap } from 'lucide-react'
import { Layout } from '../components/Layout'
import { Button, Card, Alert } from '../components/common'
import { alterationAPI, attendanceAPI } from '../api'

interface HodStats {
  totalAlterations: number
  pendingAlterations: number
  unableToAlter: number
  averageWorkload: number
}

interface StaffAlteration {
  id: number
  originalStaffName: string
  substituteStaffName: string
  subjectName: string
  classCode: string
  dayOrder: number
  periodNumber: number
  alterationDate: string
  status: string
}

export const HodDashboardPage: React.FC = () => {
  const [stats, setStats] = useState<HodStats>({
    totalAlterations: 0,
    pendingAlterations: 0,
    unableToAlter: 0,
    averageWorkload: 0,
  })
  const [alterations, setAlterations] = useState<StaffAlteration[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0])
  const [expandedAlteration, setExpandedAlteration] = useState<number | null>(null)

  useEffect(() => {
    loadData()
  }, [selectedDate])

  const loadData = async () => {
    try {
      setLoading(true)
      setError(null)

      // Load alterations for selected date
      const alterationResponse = await alterationAPI.getByDate(selectedDate)
      const alterationData = alterationResponse.data.data || []
      setAlterations(alterationData)

      // Calculate stats
      const pendingCount = alterationData.filter((a: any) => a.status === 'ASSIGNED').length
      const totalCount = alterationData.length

      setStats({
        totalAlterations: totalCount,
        pendingAlterations: pendingCount,
        unableToAlter: 0, // TODO: Calculate from backend
        averageWorkload: 0, // TODO: Calculate from backend
      })
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load data')
    } finally {
      setLoading(false)
    }
  }

  const getDayName = (dayOrder: number) => {
    const days = ['', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']
    return days[dayOrder] || `Day ${dayOrder}`
  }

  const getStatusColor = (status: string) => {
    const colors: { [key: string]: string } = {
      ASSIGNED: 'bg-yellow-100 text-yellow-800 border-yellow-300',
      ACKNOWLEDGED: 'bg-blue-100 text-blue-800 border-blue-300',
      COMPLETED: 'bg-green-100 text-green-800 border-green-300',
      CANCELLED: 'bg-gray-100 text-gray-800 border-gray-300',
    }
    return colors[status] || 'bg-slate-100 text-slate-800'
  }

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn">
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold text-slate-900">HOD Dashboard</h1>
          <p className="text-slate-600 mt-1">Monitor staff alterations and manage substitutions</p>
        </div>

        {error && (
          <Alert
            type="error"
            title="Error"
            message={error}
            onClose={() => setError(null)}
          />
        )}

        {/* Date Selector */}
        <Card className="p-4 flex items-center gap-4">
          <label className="font-medium text-slate-700">Select Date:</label>
          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500"
          />
          <Button variant="secondary" size="sm" onClick={loadData}>
            🔄 Refresh
          </Button>
        </Card>

        {/* Stats Grid */}
        {!loading && (
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <Card className="p-6 bg-gradient-to-br from-blue-50 to-blue-100 border-l-4 border-blue-500">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-slate-600 text-sm font-medium">Total Alterations</p>
                  <p className="text-3xl font-bold text-blue-900 mt-2">{stats.totalAlterations}</p>
                </div>
                <Calendar className="w-12 h-12 text-blue-300" />
              </div>
            </Card>

            <Card className="p-6 bg-gradient-to-br from-yellow-50 to-yellow-100 border-l-4 border-yellow-500">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-slate-600 text-sm font-medium">Pending</p>
                  <p className="text-3xl font-bold text-yellow-900 mt-2">
                    {stats.pendingAlterations}
                  </p>
                </div>
                <Zap className="w-12 h-12 text-yellow-300" />
              </div>
            </Card>

            <Card className="p-6 bg-gradient-to-br from-red-50 to-red-100 border-l-4 border-red-500">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-slate-600 text-sm font-medium">Unable to Alter</p>
                  <p className="text-3xl font-bold text-red-900 mt-2">{stats.unableToAlter}</p>
                </div>
                <AlertTriangle className="w-12 h-12 text-red-300" />
              </div>
            </Card>

            <Card className="p-6 bg-gradient-to-br from-purple-50 to-purple-100 border-l-4 border-purple-500">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-slate-600 text-sm font-medium">Avg Workload</p>
                  <p className="text-3xl font-bold text-purple-900 mt-2">
                    {stats.averageWorkload}
                  </p>
                </div>
                <TrendingDown className="w-12 h-12 text-purple-300" />
              </div>
            </Card>
          </div>
        )}

        {/* Alterations List */}
        <div>
          <h2 className="text-xl font-bold text-slate-900 mb-4">Alterations for {selectedDate}</h2>

          {loading ? (
            <Card className="p-12 text-center">
              <div className="inline-block w-12 h-12 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
              <p className="mt-4 text-slate-600">Loading alterations...</p>
            </Card>
          ) : alterations.length === 0 ? (
            <Card className="p-12 text-center bg-green-50 border-2 border-green-200">
              <div className="text-4xl mb-3">✅</div>
              <p className="text-slate-700 font-medium">No alterations for this date</p>
              <p className="text-slate-600 text-sm mt-2">All staff are present or no absences recorded</p>
            </Card>
          ) : (
            <div className="space-y-3">
              {alterations.map((alteration) => (
                <div
                  key={alteration.id}
                  onClick={() =>
                    setExpandedAlteration(
                      expandedAlteration === alteration.id ? null : alteration.id
                    )
                  }
                >
                  <Card
                    className={`p-4 border-l-4 cursor-pointer transition-all hover:shadow-md ${
                      alteration.status === 'ASSIGNED'
                        ? 'border-l-yellow-500 bg-yellow-50'
                        : alteration.status === 'ACKNOWLEDGED'
                          ? 'border-l-blue-500 bg-blue-50'
                          : 'border-l-green-500 bg-green-50'
                    }`}
                  >
                  {/* Compact View */}
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-2">
                        <span className={`px-3 py-1 rounded text-xs font-medium ${getStatusColor(alteration.status)}`}>
                          {alteration.status}
                        </span>
                        <h3 className="font-semibold text-slate-900">
                          {alteration.subjectName} - {alteration.classCode}
                        </h3>
                      </div>
                      <p className="text-sm text-slate-600">
                        {getDayName(alteration.dayOrder)} | Period {alteration.periodNumber}
                      </p>
                    </div>

                    <div className="text-right ml-4">
                      <p className="text-sm font-medium text-slate-900">{alteration.originalStaffName}</p>
                      <p className="text-xs text-slate-500">Absent</p>
                    </div>

                    <div className="text-right ml-4 hidden sm:block">
                      <p className="text-sm font-medium text-slate-900">{alteration.substituteStaffName}</p>
                      <p className="text-xs text-slate-500">Substitute</p>
                    </div>

                    <button
                      className="p-2 hover:bg-slate-200 rounded-lg transition-colors ml-4"
                      onClick={(e) => {
                        e.stopPropagation()
                        setExpandedAlteration(
                          expandedAlteration === alteration.id ? null : alteration.id
                        )
                      }}
                    >
                      <Eye className="w-5 h-5 text-slate-600" />
                    </button>
                  </div>

                  {/* Expanded View */}
                  {expandedAlteration === alteration.id && (
                    <div className="mt-4 pt-4 border-t border-slate-200 space-y-3">
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                        <div>
                          <p className="text-slate-500 font-medium">Subject</p>
                          <p className="text-slate-900">{alteration.subjectName}</p>
                        </div>
                        <div>
                          <p className="text-slate-500 font-medium">Class</p>
                          <p className="text-slate-900">{alteration.classCode}</p>
                        </div>
                        <div>
                          <p className="text-slate-500 font-medium">Original Staff</p>
                          <p className="text-slate-900">{alteration.originalStaffName}</p>
                        </div>
                        <div>
                          <p className="text-slate-500 font-medium">Substitute</p>
                          <p className="text-slate-900">{alteration.substituteStaffName}</p>
                        </div>
                      </div>

                      <div className="flex gap-2 pt-3">
                        <Button variant="secondary" size="sm" className="gap-2">
                          📧 Contact Staff
                        </Button>
                        <Button variant="secondary" size="sm" className="gap-2">
                          📋 View Lesson Plans
                        </Button>
                      </div>
                    </div>
                  )}
                </Card>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Quick Stats Section */}
        <Card className="p-6 bg-slate-50">
          <h3 className="font-semibold text-slate-900 mb-4">📊 Key Metrics</h3>
          <div className="space-y-3 text-sm">
            <div className="flex justify-between items-center">
              <span className="text-slate-600">Alteration Success Rate</span>
              <span className="font-bold text-slate-900">
                {stats.totalAlterations > 0
                  ? Math.round(
                      ((stats.totalAlterations - stats.unableToAlter) / stats.totalAlterations) * 100
                    )
                  : 0}
                %
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-600">Pending Acknowledgments</span>
              <span className="font-bold text-slate-900">{stats.pendingAlterations}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-600">Staff Workload Balance</span>
              <span className="font-bold text-slate-900">Good ✅</span>
            </div>
          </div>
        </Card>
      </div>
    </Layout>
  )
}
