import React, { useState, useEffect, useMemo } from 'react'
import { AlertCircle, CheckCircle, Clock, FileText, Download, Eye, RotateCcw } from 'lucide-react'
import { Layout } from '../components/Layout'
import { Button, Card, Alert } from '../components/common'
import { alterationAPI, attendanceAPI, lessonPlanAPI } from '../api'
import { useAuthStore } from '../store/authStore'
import { useAlterationWebSocket } from '../hooks/useAlterationWebSocket'
import { AlterationCalendar } from '../components/AlterationCalendar'

interface Alteration {
  id: number
  originalStaffId: string
  originalStaffName: string
  substituteStaffId: string
  substituteStaffName: string
  classCode: string
  subjectName: string
  dayOrder: number
  periodNumber: number
  alterationDate: string
  absenceType: 'FN' | 'AN' | 'AF' | 'ONDUTY' | 'PERIOD_WISE_ABSENT'
  status: string
  remarks?: string
}

interface DateInfo {
  date: string
  type: 'absence' | 'substitution' // absence = red, substitution = yellow
  count: number
}

export const AlterationDashboardPage: React.FC = () => {
  const user = useAuthStore((state) => state.user)
  const [alterations, setAlterations] = useState<Alteration[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState<'as-original' | 'as-substitute'>('as-original')
  const [showDetails, setShowDetails] = useState<number | null>(null)
  const [selectedDate, setSelectedDate] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [viewMode, setViewMode] = useState<'monthly' | 'weekly'>('monthly')
  const [isRefreshing, setIsRefreshing] = useState(false)

  useEffect(() => {
    loadAlterations()
  }, [activeTab, user])

  // Set up WebSocket listeners for real-time updates
  useAlterationWebSocket(
    (data: Alteration) => {
      // New alteration created - only add if relevant to current user
      const isRelevant = data.originalStaffId === user?.staffId || data.substituteStaffId === user?.staffId
      if (isRelevant) {
        setAlterations((prev) => {
          if (prev.some((a) => a.id === data.id)) return prev
          return [data, ...prev]
        })
      }
    },
    (data: Alteration) => {
      // Alteration updated
      setAlterations((prev) =>
        prev.map((alt) => (alt.id === data.id ? data : alt))
      )
    },
    () => {
      // Alteration rejected - reload to get new assignments
      loadAlterations()
    }
  )

  const loadAlterations = async () => {
    if (!user) return
    try {
      setLoading(true)
      const response = await alterationAPI.getByStaff(user.id.toString())
      setAlterations(response.data.data || [])
      console.log('Loaded alterations:', response.data.data)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load alterations')
      console.error('Failed to load alterations:', err)
    } finally {
      setLoading(false)
    }
  }

  // Categorize alterations for calendar display
  const dateInfoByType = useMemo(() => {
    const dates: { [key: string]: DateInfo } = {}
    
    alterations.forEach((alt) => {
      const key = alt.alterationDate
      const isOriginalStaff = alt.originalStaffId === user?.staffId
      const type = isOriginalStaff ? 'absence' : 'substitution'
      
      if (!dates[key]) {
        dates[key] = { date: key, type, count: 0 }
      }
      dates[key].count++
    })
    
    return dates
  }, [alterations, user?.staffId])

  const handleRefresh = async () => {
    setIsRefreshing(true)
    try {
      await loadAlterations()
      setSuccess('✅ Alterations refreshed!')
      setTimeout(() => setSuccess(null), 2000)
    } catch (err) {
      setError('Failed to refresh alterations')
    } finally {
      setIsRefreshing(false)
    }
  }

  const handleReject = async (alterationId: number) => {
    if (!confirm('Are you sure you want to reject this alteration? The system will find another substitute.')) {
      return
    }

    try {
      await alterationAPI.reject(alterationId)
      setSuccess('✅ Alteration rejected! Finding new substitute...')
      setTimeout(() => {
        loadAlterations()
        setSuccess(null)
      }, 2000)
    } catch (err: any) {
      setError('Failed to reject alteration')
    }
  }

  // Filter alterations by selected date and active tab
  const filteredAlterations = alterations.filter((alt) => {
    const dateMatch = !selectedDate || alt.alterationDate === selectedDate
    return dateMatch
  })

  const handleAcknowledge = async (alterationId: number) => {
    try {
      await alterationAPI.updateStatus(alterationId, 'ACKNOWLEDGED')
      loadAlterations()
      setSuccess('✅ Alteration acknowledged!')
      setTimeout(() => setSuccess(null), 2000)
    } catch (err) {
      console.error('Failed to acknowledge alteration')
      setError('Failed to acknowledge alteration')
    }
  }

  const getStatusColor = (status: string) => {
    const colors: { [key: string]: string } = {
      ASSIGNED: 'bg-yellow-100 text-yellow-800',
      ACKNOWLEDGED: 'bg-blue-100 text-blue-800',
      COMPLETED: 'bg-green-100 text-green-800',
      CANCELLED: 'bg-gray-100 text-gray-800',
    }
    return colors[status] || 'bg-slate-100 text-slate-800'
  }

  const getStatusIcon = (status: string) => {
    const icons: { [key: string]: React.ReactNode } = {
      ASSIGNED: <AlertCircle className="w-4 h-4" />,
      ACKNOWLEDGED: <CheckCircle className="w-4 h-4" />,
      COMPLETED: <CheckCircle className="w-4 h-4" />,
      CANCELLED: <Clock className="w-4 h-4" />,
    }
    return icons[status]
  }

  const getPeriodDisplay = (alteration: Alteration) => {
    // Map absence type to user-friendly display
    const absenceTypeMap: { [key: string]: string } = {
      'FN': 'Full Day Leave',
      'AN': 'Half Day Morning Leave (9AM - 1PM)',
      'AF': 'Half Day Afternoon Leave (1PM - 5PM)',
      'ONDUTY': 'On Duty - Full Day',
      'PERIOD_WISE_ABSENT': `Period ${alteration.periodNumber} Absent`
    }
    
    return absenceTypeMap[alteration.absenceType] || `Period ${alteration.periodNumber}`
  }

  const getPeriodTime = (periodNumber: number) => {
    const periods: { [key: number]: string } = {
      1: '9:00-10:00',
      2: '10:00-11:00',
      3: '11:00-12:00',
      4: '12:00-1:00',
      5: '1:00-2:00',
      6: '2:00-3:00',
    }
    return periods[periodNumber] || 'Unknown'
  }

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-slate-900">Staff Alterations</h1>
            <p className="text-slate-600 mt-1">View and manage your class alterations</p>
          </div>
          <button
            onClick={handleRefresh}
            disabled={isRefreshing}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg transition-colors ${
              isRefreshing
                ? 'bg-slate-100 text-slate-400 cursor-not-allowed'
                : 'bg-blue-600 text-white hover:bg-blue-700'
            }`}
          >
            <RotateCcw className={`w-4 h-4 ${isRefreshing ? 'animate-spin' : ''}`} />
            Refresh
          </button>
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

        {/* Tabs */}
        <div className="flex gap-2 border-b border-slate-200">
          <button
            onClick={() => setActiveTab('as-original')}
            className={`px-6 py-3 font-medium border-b-2 transition-colors ${
              activeTab === 'as-original'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-slate-600 hover:text-slate-900'
            }`}
          >
            Classes I'm Missing (Need Substitute)
          </button>
          <button
            onClick={() => setActiveTab('as-substitute')}
            className={`px-6 py-3 font-medium border-b-2 transition-colors ${
              activeTab === 'as-substitute'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-slate-600 hover:text-slate-900'
            }`}
          >
            Classes I'm Substituting
          </button>
        </div>

        {/* View Mode Toggle */}
        <div className="flex gap-2">
          <button
            onClick={() => setViewMode('monthly')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              viewMode === 'monthly'
                ? 'bg-blue-600 text-white'
                : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
            }`}
          >
            Monthly
          </button>
          <button
            onClick={() => setViewMode('weekly')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              viewMode === 'weekly'
                ? 'bg-blue-600 text-white'
                : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
            }`}
          >
            Weekly
          </button>
          {selectedDate && (
            <button
              onClick={() => setSelectedDate(null)}
              className="ml-auto px-4 py-2 rounded-lg bg-slate-100 text-slate-700 hover:bg-slate-200 font-medium transition-colors"
            >
              Clear Selection
            </button>
          )}
        </div>

        {/* Content */}
        {loading ? (
          <Card className="p-12 text-center">
            <div className="inline-block w-12 h-12 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
            <p className="mt-4 text-slate-600">Loading alterations...</p>
          </Card>
        ) : alterations.length === 0 ? (
          <Card className="p-12 text-center">
            <CheckCircle className="w-12 h-12 text-green-500 mx-auto mb-4" />
            <p className="text-slate-600 font-medium">
              {activeTab === 'as-original'
                ? 'No absences recorded'
                : 'No substitute assignments'}
            </p>
          </Card>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Calendar */}
            <div className="lg:col-span-1">
              <AlterationCalendar
                alterations={filteredAlterations}
                dateInfoByType={dateInfoByType}
                onDateSelect={setSelectedDate}
                selectedDate={selectedDate}
                viewMode={viewMode}
              />
            </div>

            {/* Alterations List */}
            <div className="lg:col-span-2">
              {selectedDate ? (
                <div className="space-y-4">
                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                    <h3 className="font-semibold text-blue-900">
                      Alterations for {new Date(selectedDate).toLocaleDateString('en-US', {
                        weekday: 'long',
                        month: 'long',
                        day: 'numeric',
                        year: 'numeric',
                      })}
                    </h3>
                    <p className="text-sm text-blue-700 mt-1">
                      {filteredAlterations.length} alteration{filteredAlterations.length !== 1 ? 's' : ''}
                    </p>
                  </div>

                  {filteredAlterations.length === 0 ? (
                    <Card className="p-8 text-center">
                      <CheckCircle className="w-10 h-10 text-green-500 mx-auto mb-3" />
                      <p className="text-slate-600">No alterations for this date</p>
                    </Card>
                  ) : (
                    <div className="space-y-3">
                      {filteredAlterations.map((alteration) => (
                        <Card
                          key={alteration.id}
                          className={`p-4 border-l-4 transition-all hover:shadow-md ${
                            alteration.status === 'ASSIGNED'
                              ? 'border-l-yellow-500'
                              : alteration.status === 'ACKNOWLEDGED'
                                ? 'border-l-blue-500'
                                : 'border-l-green-500'
                          }`}
                        >
                          <div className="flex items-start justify-between">
                            <div className="flex-1">
                              <div className="flex items-center gap-3 mb-2">
                                <div>
                                  <h4 className="font-semibold text-slate-900">{alteration.subjectName}</h4>
                                  <p className="text-xs text-slate-500">Class: {alteration.classCode}</p>
                                </div>
                              </div>
                              <div className="grid grid-cols-2 gap-3 text-sm mt-3">
                                <div>
                                  <p className="text-slate-500 font-medium">Absence Type</p>
                                  <p className="text-slate-900 font-semibold">
                                    <span className={`px-2 py-1 rounded text-xs ${
                                      alteration.absenceType === 'FN' ? 'bg-red-100 text-red-700' :
                                      alteration.absenceType === 'AN' ? 'bg-orange-100 text-orange-700' :
                                      alteration.absenceType === 'AF' ? 'bg-amber-100 text-amber-700' :
                                      alteration.absenceType === 'ONDUTY' ? 'bg-purple-100 text-purple-700' :
                                      'bg-yellow-100 text-yellow-700'
                                    }`}>
                                      {getPeriodDisplay(alteration)}
                                    </span>
                                  </p>
                                </div>
                                <div>
                                  <p className="text-slate-500 font-medium">
                                    {activeTab === 'as-original' ? 'Substitute' : 'Original Staff'}
                                  </p>
                                  <p className="text-slate-900">
                                    {activeTab === 'as-original'
                                      ? alteration.substituteStaffName
                                      : alteration.originalStaffName}
                                  </p>
                                </div>
                              </div>
                            </div>
                            <div className="ml-4 flex flex-col gap-2">
                              <span
                                className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(
                                  alteration.status
                                )}`}
                              >
                                {getStatusIcon(alteration.status)}
                                {alteration.status}
                              </span>
                              <button
                                onClick={() =>
                                  setShowDetails(showDetails === alteration.id ? null : alteration.id)
                                }
                                className="p-1 hover:bg-slate-100 rounded transition-colors"
                              >
                                <Eye className="w-4 h-4 text-slate-600" />
                              </button>
                            </div>
                          </div>

                          {/* Expanded Details */}
                          {showDetails === alteration.id && (
                            <div className="mt-4 pt-4 border-t border-slate-200 space-y-4">
                              {alteration.remarks && (
                                <div>
                                  <p className="text-sm font-medium text-slate-700 mb-1">Notes</p>
                                  <p className="text-sm text-slate-600 bg-slate-50 p-2 rounded">
                                    {alteration.remarks}
                                  </p>
                                </div>
                              )}

                              <div className="flex flex-wrap gap-2">
                                <Button
                                  variant="secondary"
                                  size="sm"
                                  className="gap-2"
                                  onClick={async () => {
                                    try {
                                      const res = await attendanceAPI.getLessonPlansForAlteration(alteration.id)
                                      const plans = res.data?.data || []
                                      if (plans.length === 0) {
                                        alert('No lesson plans uploaded for this alteration')
                                        return
                                      }
                                      for (const lp of plans) {
                                        const blob = await lessonPlanAPI.downloadFile(lp.id).then((r) => r.data)
                                        const url = window.URL.createObjectURL(blob)
                                        const a = document.createElement('a')
                                        a.href = url
                                        a.download = lp.originalFileName || `lesson-plan-${lp.id}`
                                        document.body.appendChild(a)
                                        a.click()
                                        document.body.removeChild(a)
                                        window.URL.revokeObjectURL(url)
                                      }
                                    } catch (err) {
                                      console.error('Failed to download lesson plans:', err)
                                      alert('Failed to download lesson plans')
                                    }
                                  }}
                                >
                                  <Download className="w-4 h-4" />
                                  Lesson Plans
                                </Button>

                                {activeTab === 'as-substitute' &&
                                  alteration.status === 'ASSIGNED' && (
                                    <div className="flex gap-2">
                                      <Button
                                        size="sm"
                                        className="gap-2"
                                        onClick={() => handleAcknowledge(alteration.id)}
                                      >
                                        <CheckCircle className="w-4 h-4" />
                                        Acknowledge
                                      </Button>
                                      <Button
                                        size="sm"
                                        variant="danger"
                                        className="gap-2"
                                        onClick={() => handleReject(alteration.id)}
                                      >
                                        ❌ Reject
                                      </Button>
                                    </div>
                                  )}
                              </div>
                            </div>
                          )}
                        </Card>
                      ))}
                    </div>
                  )}
                </div>
              ) : (
                <Card className="p-12 text-center">
                  <Clock className="w-12 h-12 text-blue-500 mx-auto mb-4" />
                  <p className="text-slate-600 font-medium">Select a date from the calendar to view alterations</p>
                </Card>
              )}
            </div>
          </div>
        )}

        {/* Calendar Legend */}
        <Card className="p-6 bg-slate-50 border-2 border-slate-300">
          <h3 className="font-semibold text-slate-900 mb-4">📅 Calendar Color Guide</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="flex items-start gap-3 p-3 bg-red-50 rounded-lg border border-red-300">
              <span className="text-2xl">🔴</span>
              <div>
                <p className="font-semibold text-red-900">RED - ABSENCE</p>
                <p className="text-sm text-red-800">You are absent/on-duty (original staff)</p>
              </div>
            </div>
            <div className="flex items-start gap-3 p-3 bg-yellow-50 rounded-lg border border-yellow-300">
              <span className="text-2xl">🟡</span>
              <div>
                <p className="font-semibold text-yellow-900">YELLOW - SUBSTITUTION</p>
                <p className="text-sm text-yellow-800">You need to substitute for another staff</p>
              </div>
            </div>
            <div className="flex items-start gap-3 p-3 bg-blue-50 rounded-lg border border-blue-300">
              <span className="text-2xl">🔵</span>
              <div>
                <p className="font-semibold text-blue-900">BLUE - SELECTED</p>
                <p className="text-sm text-blue-800">You clicked on this date</p>
              </div>
            </div>
          </div>
        </Card>

        {/* Help Section */}
        {activeTab === 'as-original' && (
          <Card className="p-6 bg-blue-50 border-2 border-blue-200">
            <h3 className="font-semibold text-blue-900 mb-3">ℹ️ About Alterations</h3>
            <ul className="space-y-2 text-sm text-blue-800">
              <li>
                • When you mark as absent, the system automatically finds a suitable substitute
              </li>
              <li>• The substitute is chosen based on presence, subject expertise, and workload</li>
              <li>• You'll be notified via SMS, email, and app notification about the assignment</li>
              <li>• Share lesson plans or notes to help the substitute teach effectively</li>
            </ul>
          </Card>
        )}

        {activeTab === 'as-substitute' && (
          <Card className="p-6 bg-amber-50 border-2 border-amber-200">
            <h3 className="font-semibold text-amber-900 mb-3">📌 As a Substitute</h3>
            <ul className="space-y-2 text-sm text-amber-800">
              <li>
                • You've been assigned to teach a class due to the original staff's absence
              </li>
              <li>• Download the lesson plans shared by the original staff</li>
              <li>• Acknowledge the assignment once you've reviewed the materials</li>
              <li>• Contact the original staff if you need clarification</li>
            </ul>
          </Card>
        )}
      </div>
    </Layout>
  )
}
