import React, { useState, useEffect, useMemo, useCallback } from 'react'
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
  absenceType: 'FN' | 'AN' | 'AF' | 'ONDUTY' | 'PERIOD_WISE_ABSENT' | 'PERIOD_1' | 'PERIOD_2' | 'PERIOD_3' | 'PERIOD_4' | 'PERIOD_5' | 'PERIOD_6'
  status: string
  remarks?: string
}

interface DateInfo {
  date: string
  type: 'absent-full' | 'absent-morning' | 'absent-afternoon' | 'onduty' | 'meeting' | 'substitution'
  count: number
  attendanceStatus?: string
  dayType?: string
}

interface AttendanceRecord {
  id: number
  staffId: string
  attendanceDate: string
  status: string
  dayType: string
  remarks?: string
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
  const [attendanceByDate, setAttendanceByDate] = useState<{ [date: string]: AttendanceRecord }>({})

  // Memoize loadAlterations to prevent unnecessary re-renders
  const loadAlterations = useCallback(async () => {
    if (!user) {
      console.warn('[AlterationDashboard] User not available, skipping load')
      return
    }
    try {
      setLoading(true)
      setError(null)
      console.log('[AlterationDashboard] Loading alterations for user:', user.id, user.staffId)
      const response = await alterationAPI.getByStaff(user.id.toString())
      const altData = response.data.data || []
      console.log('[AlterationDashboard] Loaded alterations:', altData)
      setAlterations(altData)
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || err.message || 'Failed to load alterations'
      setError(errorMsg)
      console.error('[AlterationDashboard] Failed to load alterations:', err)
    } finally {
      setLoading(false)
    }
  }, [user])

  useEffect(() => {
    loadAlterations()
  }, [user, loadAlterations])

  // Load attendance records so the calendar can show colour-coded statuses
  const loadAttendance = useCallback(async () => {
    const staffId = user?.staffId || user?.username
    if (!staffId) return
    try {
      const res = await attendanceAPI.getStaffAttendance(staffId)
      const records: AttendanceRecord[] = res.data.data || []
      const byDate: { [date: string]: AttendanceRecord } = {}
      records.forEach((r) => { byDate[r.attendanceDate] = r })
      setAttendanceByDate(byDate)
    } catch (err) {
      console.warn('[AlterationDashboard] Could not load attendance records (non-fatal):', err)
    }
  }, [user?.staffId, user?.username])

  useEffect(() => {
    loadAttendance()
  }, [loadAttendance])

  // Set up WebSocket listeners for real-time updates
  useAlterationWebSocket(
    (data: Alteration) => {
      // New alteration created - only add if relevant to current user
      const isRelevant = data.originalStaffId === user?.staffId || data.substituteStaffId === user?.staffId
      if (isRelevant) {
        console.log('[AlterationDashboard] New alteration received:', data)
        setAlterations((prev) => {
          if (prev.some((a) => a.id === data.id)) return prev
          return [data, ...prev]
        })
      }
    },
    (data: Alteration) => {
      // Alteration updated - re-assign automatically or status changed
      console.log('[AlterationDashboard] Alteration updated:', data)
      setAlterations((prev) =>
        prev.map((alt) => (alt.id === data.id ? data : alt))
      )
    },
    () => {
      // Alteration rejected - reload to get new assignments
      console.log('[AlterationDashboard] Alteration rejected, reloading...')
      loadAlterations()
    },
    user?.departmentId
  )

  // Categorize dates for calendar display — driven by ATTENDANCE STATUS, not just alteration type
  const dateInfoByType = useMemo(() => {
    const dates: { [key: string]: DateInfo } = {}

    // 1) User's own attendance records → determine the day colour (absence type / onduty / meeting)
    Object.values(attendanceByDate).forEach((rec) => {
      const status = rec.status?.toUpperCase()
      if (status === 'PRESENT') return // no colour for present days

      const dayType = (rec.dayType || 'FULL_DAY').toUpperCase()
      let type: DateInfo['type']

      if (status === 'ONDUTY') {
        type = 'onduty'
      } else if (status === 'MEETING') {
        type = 'meeting'
      } else {
        // ABSENT or LEAVE
        if (dayType === 'MORNING_ONLY') type = 'absent-morning'
        else if (dayType === 'AFTERNOON_ONLY') type = 'absent-afternoon'
        else type = 'absent-full'
      }

      const altCount = alterations.filter(
        (a) => a.alterationDate === rec.attendanceDate && a.originalStaffId === user?.staffId,
      ).length

      dates[rec.attendanceDate] = {
        date: rec.attendanceDate,
        type,
        count: altCount,
        attendanceStatus: rec.status,
        dayType: rec.dayType,
      }
    })

    // 2) Dates where the user is a SUBSTITUTE (and not already marked by their own attendance)
    alterations.forEach((alt) => {
      if (alt.substituteStaffId !== user?.staffId) return
      const key = alt.alterationDate
      if (dates[key] && dates[key].type !== 'substitution') return // own absence takes priority
      if (!dates[key]) {
        dates[key] = { date: key, type: 'substitution', count: 0 }
      }
      if (dates[key].type === 'substitution') {
        dates[key].count++
      }
    })

    return dates
  }, [alterations, attendanceByDate, user?.staffId])

  const handleRefresh = async () => {
    setIsRefreshing(true)
    try {
      await Promise.all([loadAlterations(), loadAttendance()])
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

  // Filter alterations by selected date AND active tab
  const filteredAlterations = useMemo(() => {
    return alterations.filter((alt) => {
      const dateMatch = !selectedDate || alt.alterationDate === selectedDate
      const tabMatch =
        activeTab === 'as-original'
          ? alt.originalStaffId === user?.staffId
          : alt.substituteStaffId === user?.staffId
      return dateMatch && tabMatch
    })
  }, [alterations, selectedDate, activeTab, user?.staffId])

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
      'PERIOD_WISE_ABSENT': `Period ${alteration.periodNumber} Absent`,
      'PERIOD_1': 'Period 1 (9:00-10:00)',
      'PERIOD_2': 'Period 2 (10:00-11:00)',
      'PERIOD_3': 'Period 3 (11:00-12:00)',
      'PERIOD_4': 'Period 4 (12:00-1:00)',
      'PERIOD_5': 'Period 5 (1:00-2:00)',
      'PERIOD_6': 'Period 6 (2:00-3:00)',
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

  const getAttendanceStatusDisplay = (status: string, dayType: string) => {
    const dayLabel: Record<string, string> = {
      FULL_DAY: 'Full Day',
      MORNING_ONLY: 'Forenoon (FN)',
      AFTERNOON_ONLY: 'Afternoon (AN)',
    }
    const statusLabel: Record<string, string> = {
      ABSENT: 'Absent',
      LEAVE: 'Leave',
      ONDUTY: 'On Duty',
      MEETING: 'Meeting',
      PERIOD_WISE_ABSENT: 'Period-wise Absent',
      PERIOD_1: 'Period 1 (9:00-10:00)',
      PERIOD_2: 'Period 2 (10:00-11:00)',
      PERIOD_3: 'Period 3 (11:00-12:00)',
      PERIOD_4: 'Period 4 (12:00-1:00)',
      PERIOD_5: 'Period 5 (1:00-2:00)',
      PERIOD_6: 'Period 6 (2:00-3:00)',
      PRESENT: 'Present',
    }
    const statusColor: Record<string, string> = {
      ABSENT:             'bg-red-100 text-red-800 border border-red-300',
      LEAVE:              'bg-red-100 text-red-800 border border-red-300',
      ONDUTY:             'bg-purple-100 text-purple-800 border border-purple-300',
      MEETING:            'bg-teal-100 text-teal-800 border border-teal-300',
      PERIOD_WISE_ABSENT: 'bg-orange-100 text-orange-800 border border-orange-300',
      PERIOD_1: 'bg-yellow-100 text-yellow-800 border border-yellow-300',
      PERIOD_2: 'bg-yellow-100 text-yellow-800 border border-yellow-300',
      PERIOD_3: 'bg-yellow-100 text-yellow-800 border border-yellow-300',
      PERIOD_4: 'bg-yellow-100 text-yellow-800 border border-yellow-300',
      PERIOD_5: 'bg-yellow-100 text-yellow-800 border border-yellow-300',
      PERIOD_6: 'bg-yellow-100 text-yellow-800 border border-yellow-300',
      PRESENT:            'bg-green-100 text-green-800 border border-green-300',
    }
    return {
      label: statusLabel[status] ?? status,
      dayLabel: dayLabel[dayType] ?? dayType,
      colorClass: statusColor[status] ?? 'bg-slate-100 text-slate-800',
    }
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
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Calendar */}
            <div className="lg:col-span-1">
              <AlterationCalendar
                alterations={alterations}
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
                  {/* Attendance Record Banner */}
                  {(() => {
                    const rec = attendanceByDate[selectedDate]
                    if (!rec || rec.status?.toUpperCase() === 'PRESENT') return null
                    const { label, dayLabel, colorClass } = getAttendanceStatusDisplay(
                      rec.status?.toUpperCase(),
                      rec.dayType?.toUpperCase() || 'FULL_DAY',
                    )
                    return (
                      <div className={`rounded-lg p-4 ${colorClass}`}>
                        <div className="flex items-center gap-3 flex-wrap">
                          <span className="text-lg">
                            {rec.status?.toUpperCase() === 'ABSENT' || rec.status?.toUpperCase() === 'LEAVE'
                              ? '🔴'
                              : rec.status?.toUpperCase() === 'ONDUTY'
                              ? '🟣'
                              : rec.status?.toUpperCase() === 'MEETING'
                              ? '🔵'
                              : '🟡'}
                          </span>
                          <div>
                            <p className="font-bold text-sm">
                              Attendance: {label} — {dayLabel}
                            </p>
                            {rec.remarks && (
                              <p className="text-xs mt-0.5 opacity-80">Remarks: {rec.remarks}</p>
                            )}
                          </div>
                          <span className="ml-auto text-xs opacity-60">DB #{rec.id}</span>
                        </div>
                      </div>
                    )
                  })()}

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
                                      alteration.absenceType.startsWith('PERIOD_') ? 'bg-yellow-100 text-yellow-700' :
                                      'bg-yellow-100 text-yellow-700'
                                    }`}>
                                      {getPeriodDisplay(alteration)}
                                    </span>
                                  </p>
                                </div>
                                <div>
                                  <p className="text-slate-500 font-medium">
                                    {activeTab === 'as-original' ? 'Substitute' : 'Original Staff'}</p>
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
                <div className="space-y-4">
                  <div className="bg-slate-50 border border-slate-200 rounded-lg p-4">
                    <h3 className="font-semibold text-slate-900">
                      {activeTab === 'as-original'
                        ? 'All Classes I\'m Missing'
                        : 'All Classes I\'m Substituting'}
                    </h3>
                    <p className="text-sm text-slate-600 mt-1">
                      {filteredAlterations.length} alteration{filteredAlterations.length !== 1 ? 's' : ''} — click a date on the calendar to filter
                    </p>
                  </div>

                  {filteredAlterations.length === 0 ? (
                    <Card className="p-8 text-center">
                      <CheckCircle className="w-10 h-10 text-green-500 mx-auto mb-3" />
                      <p className="text-slate-600">
                        {activeTab === 'as-original'
                          ? 'No absences recorded'
                          : 'No substitute assignments'}
                      </p>
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
                                  <p className="text-xs text-slate-500">
                                    Class: {alteration.classCode} ·{' '}
                                    {new Date(alteration.alterationDate).toLocaleDateString('en-US', {
                                      weekday: 'short', month: 'short', day: 'numeric',
                                    })}
                                  </p>
                                </div>
                              </div>
                              <div className="grid grid-cols-2 gap-3 text-sm mt-3">
                                <div>
                                  <p className="text-slate-500 font-medium">Absence Type</p>
                                  <span className={`px-2 py-1 rounded text-xs ${
                                    alteration.absenceType === 'FN' ? 'bg-red-100 text-red-700' :
                                    alteration.absenceType === 'AN' ? 'bg-orange-100 text-orange-700' :
                                    alteration.absenceType === 'AF' ? 'bg-amber-100 text-amber-700' :
                                    alteration.absenceType === 'ONDUTY' ? 'bg-purple-100 text-purple-700' :
                                    alteration.absenceType.startsWith('PERIOD_') ? 'bg-yellow-100 text-yellow-700' :
                                    'bg-yellow-100 text-yellow-700'
                                  }`}>
                                    {getPeriodDisplay(alteration)}
                                  </span>
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
                            <span
                              className={`ml-4 inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(
                                alteration.status
                              )}`}
                            >
                              {getStatusIcon(alteration.status)}
                              {alteration.status}
                            </span>
                          </div>
                        </Card>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        )}

        {/* Calendar Legend */}
        <Card className="p-6 bg-slate-50 border-2 border-slate-300">
          <h3 className="font-semibold text-slate-900 mb-4">📅 Calendar Color Guide</h3>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
            <div className="flex items-start gap-2 p-3 bg-red-50 rounded-lg border border-red-300">
              <span className="text-xl">🔴</span>
              <div>
                <p className="font-semibold text-red-900 text-sm">RED — Absent / Leave (Full Day)</p>
                <p className="text-xs text-red-700">FN — Full day absent/leave</p>
              </div>
            </div>
            <div className="flex items-start gap-2 p-3 bg-orange-50 rounded-lg border border-orange-300">
              <span className="text-xl">🟠</span>
              <div>
                <p className="font-semibold text-orange-900 text-sm">ORANGE — Leave (FN)</p>
                <p className="text-xs text-orange-700">Forenoon half-day (Morning)</p>
              </div>
            </div>
            <div className="flex items-start gap-2 p-3 bg-amber-50 rounded-lg border border-amber-300">
              <span className="text-xl">🟡</span>
              <div>
                <p className="font-semibold text-amber-900 text-sm">AMBER — Leave (AN)</p>
                <p className="text-xs text-amber-700">Afternoon half-day</p>
              </div>
            </div>
            <div className="flex items-start gap-2 p-3 bg-purple-50 rounded-lg border border-purple-300">
              <span className="text-xl">🟣</span>
              <div>
                <p className="font-semibold text-purple-900 text-sm">PURPLE — On Duty</p>
                <p className="text-xs text-purple-700">Official duty outside campus</p>
              </div>
            </div>
            <div className="flex items-start gap-2 p-3 bg-teal-50 rounded-lg border border-teal-300">
              <span className="text-xl">🔵</span>
              <div>
                <p className="font-semibold text-teal-900 text-sm">TEAL — Meeting</p>
                <p className="text-xs text-teal-700">In-campus meeting (period-wise)</p>
              </div>
            </div>
            <div className="flex items-start gap-2 p-3 bg-yellow-50 rounded-lg border border-yellow-300">
              <span className="text-xl">⭐</span>
              <div>
                <p className="font-semibold text-yellow-900 text-sm">YELLOW — Substitute</p>
                <p className="text-xs text-yellow-700">You are covering another staff</p>
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
