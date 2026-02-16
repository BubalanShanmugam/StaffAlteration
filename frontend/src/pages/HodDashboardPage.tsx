import React, { useState, useEffect } from 'react'
import { Download, Calendar, AlertTriangle, TrendingDown, Eye, Zap, Clock, CheckCircle } from 'lucide-react'
import { Layout } from '../components/Layout'
import { Button, Card, Alert } from '../components/common'
import { alterationAPI, attendanceAPI, reportAPI } from '../api'
import { useAuthStore } from '../store/authStore'
import { useAlterationWebSocket } from '../hooks/useAlterationWebSocket'

interface HodStats {
  totalAlterations: number
  completed: number
  pending: number
  acknowledged: number
}

interface StaffAlteration {
  id: number
  originalStaffName: string
  originalStaffId: string
  substituteStaffName: string
  substituteStaffId: string
  subjectName: string
  classCode: string
  dayOrder: number
  periodNumber: number
  alterationDate: string
  absenceType: 'FN' | 'AN' | 'AF' | 'ONDUTY' | 'PERIOD_WISE_ABSENT'
  status: string
  remarks?: string
  departmentId: number
}

export const HodDashboardPage: React.FC = () => {
  const user = useAuthStore((state) => state.user)
  const [stats, setStats] = useState<HodStats>({
    totalAlterations: 0,
    completed: 0,
    pending: 0,
    acknowledged: 0,
  })
  const [alterations, setAlterations] = useState<StaffAlteration[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [exporting, setExporting] = useState(false)
  const [showExportModal, setShowExportModal] = useState(false)
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0])
  const [expandedAlteration, setExpandedAlteration] = useState<number | null>(null)
  const [exportDates, setExportDates] = useState({
    fromDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    toDate: new Date().toISOString().split('T')[0],
  })

  const departmentId = user?.departmentId

  const loadData = async () => {
    if (!departmentId) return

    try {
      setLoading(true)
      setError(null)

      // Load alterations for selected date
      const alterationResponse = await alterationAPI.getByDate(selectedDate)
      let alterationData = alterationResponse.data.data || []
      
      // Filter by department
      alterationData = alterationData.filter((a: any) => a.departmentId === departmentId)
      
      setAlterations(alterationData)

      // Calculate stats
      const completedCount = alterationData.filter((a: any) => a.status === 'COMPLETED').length
      const pendingCount = alterationData.filter((a: any) => a.status === 'ASSIGNED').length
      const acknowledgedCount = alterationData.filter((a: any) => a.status === 'ACKNOWLEDGED').length

      setStats({
        totalAlterations: alterationData.length,
        completed: completedCount,
        pending: pendingCount,
        acknowledged: acknowledgedCount,
      })
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load data')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [selectedDate, departmentId])

  // Set up WebSocket listeners for real-time updates
  useAlterationWebSocket(
    (data: StaffAlteration) => {
      if (data.departmentId === departmentId) {
        setAlterations((prev) => [data, ...prev])
      }
    },
    (data: StaffAlteration) => {
      if (data.departmentId === departmentId) {
        setAlterations((prev) =>
          prev.map((alt) => (alt.id === data.id ? data : alt))
        )
      }
    },
    (data: StaffAlteration) => {
      if (data.departmentId === departmentId) {
        loadData()
      }
    },
    departmentId,
    (data: StaffAlteration) => {
      if (data.departmentId === departmentId) {
        loadData()
      }
    }
  )

  const handleExport = async () => {
    if (!departmentId) {
      setError('Department ID not found')
      return
    }

    try {
      setExporting(true)
      const response = await reportAPI.exportAlterations(
        exportDates.fromDate,
        exportDates.toDate,
        departmentId
      )

      // Create blob and download
      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `alterations_${exportDates.fromDate}_to_${exportDates.toDate}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      setSuccess('✅ Excel report downloaded successfully!')
      setTimeout(() => setSuccess(null), 3000)
      setShowExportModal(false)
    } catch (err: any) {
      setError('Failed to export report')
      console.error(err)
    } finally {
      setExporting(false)
    }
  }

  const getDayName = (dayOrder: number) => {
    const days = ['', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']
    return days[dayOrder] || `Day ${dayOrder}`
  }

  const getPeriodDisplay = (alteration: StaffAlteration) => {
    const absenceTypeMap: { [key: string]: string } = {
      FN: 'Full Day Leave',
      AN: 'Half Day Morning (9AM - 1PM)',
      AF: 'Half Day Afternoon (1PM - 5PM)',
      ONDUTY: 'On Duty - Full Day',
      PERIOD_WISE_ABSENT: `Period ${alteration.periodNumber} Absent`,
    }
    return absenceTypeMap[alteration.absenceType] || `Period ${alteration.periodNumber}`
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
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold text-slate-900">HOD Dashboard</h1>
            <p className="text-slate-600 mt-1">Monitor staff alterations and manage substitutions</p>
          </div>
          <Button
            onClick={() => setShowExportModal(true)}
            className="flex items-center gap-2"
          >
            <Download className="w-4 h-4" />
            Export to Excel
          </Button>
        </div>

        {error && (
          <Alert
            type="error"
            title="Error"
            message={error}
            onClose={() => setError(null)}
          />
        )}

        {success && (
          <Alert
            type="success"
            title="Success"
            message={success}
            onClose={() => setSuccess(null)}
          />
        )}

        {/* Export Modal */}
        {showExportModal && (
          <Card className="p-6 border-2 border-blue-500">
            <div className="space-y-4">
              <h3 className="text-lg font-bold text-slate-900">Export Alterations Report</h3>
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-2">
                    From Date
                  </label>
                  <input
                    type="date"
                    value={exportDates.fromDate}
                    onChange={(e) =>
                      setExportDates({ ...exportDates, fromDate: e.target.value })
                    }
                    className="w-full px-4 py-2 rounded-lg border border-slate-300 text-slate-900 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-2">
                    To Date
                  </label>
                  <input
                    type="date"
                    value={exportDates.toDate}
                    onChange={(e) =>
                      setExportDates({ ...exportDates, toDate: e.target.value })
                    }
                    className="w-full px-4 py-2 rounded-lg border border-slate-300 text-slate-900 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>

              <div className="flex gap-3 justify-end">
                <button
                  onClick={() => setShowExportModal(false)}
                  className="px-4 py-2 rounded-lg border border-slate-300 text-slate-700 font-medium hover:bg-slate-50"
                >
                  Cancel
                </button>
                <Button
                  onClick={handleExport}
                  disabled={exporting}
                  className="flex items-center gap-2"
                >
                  {exporting ? (
                    <>
                      <Clock className="w-4 h-4 animate-spin" />
                      Exporting...
                    </>
                  ) : (
                    <>
                      <Download className="w-4 h-4" />
                      Download Excel
                    </>
                  )}
                </Button>
              </div>
            </div>
          </Card>
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
                    {stats.pending}
                  </p>
                </div>
                <Zap className="w-12 h-12 text-yellow-300" />
              </div>
            </Card>

            <Card className="p-6 bg-gradient-to-br from-blue-50 to-blue-100 border-l-4 border-blue-500">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-slate-600 text-sm font-medium">Acknowledged</p>
                  <p className="text-3xl font-bold text-blue-900 mt-2">
                    {stats.acknowledged}
                  </p>
                </div>
                <Clock className="w-12 h-12 text-blue-300" />
              </div>
            </Card>

            <Card className="p-6 bg-gradient-to-br from-green-50 to-green-100 border-l-4 border-green-500">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-slate-600 text-sm font-medium">Completed</p>
                  <p className="text-3xl font-bold text-green-900 mt-2">
                    {stats.completed}
                  </p>
                </div>
                <CheckCircle className="w-12 h-12 text-green-300" />
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
                    className={`border-l-4 cursor-pointer transition-all hover:shadow-md ${
                      alteration.status === 'ASSIGNED'
                        ? 'border-l-yellow-500 bg-yellow-50'
                        : alteration.status === 'ACKNOWLEDGED'
                          ? 'border-l-blue-500 bg-blue-50'
                          : 'border-l-green-500 bg-green-50'
                    }`}
                  >
                    <div className="p-4">
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
                          {getDayName(alteration.dayOrder)} | {getPeriodDisplay(alteration)}
                        </p>
                      </div>

                      <div className="text-right ml-4">
                        <p className="text-sm font-medium text-slate-900">{alteration.originalStaffName}</p>
                        <p className="text-xs text-slate-500">Absent</p>
                      </div>

                      <div className="text-right ml-4 hidden sm:block">
                        <p className="text-sm font-medium text-slate-900">
                          {alteration.substituteStaffName || 'Pending'}
                        </p>
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
                            <p className="text-slate-900">
                              {alteration.substituteStaffName || 'Pending'}
                            </p>
                          </div>
                        </div>

                        {alteration.remarks && (
                          <div>
                            <p className="text-slate-500 font-medium text-sm">Remarks</p>
                            <p className="text-slate-900 text-sm">{alteration.remarks}</p>
                          </div>
                        )}

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
                    </div>
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
                      (stats.completed / stats.totalAlterations) * 100
                    )
                  : 0}
                %
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-600">Pending Acknowledgments</span>
              <span className="font-bold text-slate-900">{stats.pending}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-600">Completed Alterations</span>
              <span className="font-bold text-slate-900">{stats.completed}</span>
            </div>
          </div>
        </Card>
      </div>
    </Layout>
  )
}

