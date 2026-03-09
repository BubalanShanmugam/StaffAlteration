import React, { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { Calendar, FileText, Briefcase, ArrowRight, CheckCircle, Clock, RefreshCw, History } from 'lucide-react'
import { Layout } from '../components/Layout'
import { Button, Card, Alert } from '../components/common'
import { attendanceAPI, alterationAPI } from '../api'
import { useAuthStore } from '../store/authStore'

interface AttendanceRecord {
  id: number
  staffId: string
  attendanceDate: string
  status: string
  dayType: string
  remarks?: string
  createdAt: string
  updatedAt: string
  selectedPeriods?: number[] // Period-wise selected periods (e.g., [1, 2] → PERIOD_1, PERIOD_2 in absence_type)
}

export const AttendancePage: React.FC = () => {
  const user = useAuthStore((state) => state.user)
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [showSuccessModal, setShowSuccessModal] = useState(false)
  const [createdAlterationCount, setCreatedAlterationCount] = useState(0)
  const [showMultipleDays, setShowMultipleDays] = useState(false)
  const [notes, setNotes] = useState('')
  const [files, setFiles] = useState<File[]>([])
  const [selectedMeetingHours, setSelectedMeetingHours] = useState<Set<number>>(new Set())
  const [selectedPeriods, setSelectedPeriods] = useState<Set<number>>(new Set())
  const [usePeriodWiseMarking, setUsePeriodWiseMarking] = useState(false)

  // DB state
  const [attendanceRecords, setAttendanceRecords] = useState<AttendanceRecord[]>([])
  const [dbLoading, setDbLoading] = useState(false)
  const [todayRecord, setTodayRecord] = useState<AttendanceRecord | null>(null)
  
  const [formData, setFormData] = useState({
    status: 'LEAVE' as 'LEAVE' | 'ONDUTY' | 'MEETING',
    dayType: 'FULL_DAY' as 'FULL_DAY' | 'MORNING_ONLY' | 'AFTERNOON_ONLY',
    attendanceDate: new Date().toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0],
    remarks: '',
  })

  // Load attendance records from database
  const loadAttendanceFromDB = useCallback(async () => {
    const staffId = user?.staffId || user?.username
    if (!staffId) return
    setDbLoading(true)
    try {
      const res = await attendanceAPI.getStaffAttendance(staffId)
      const records: AttendanceRecord[] = (res.data.data || []).sort(
        (a: AttendanceRecord, b: AttendanceRecord) =>
          new Date(b.attendanceDate).getTime() - new Date(a.attendanceDate).getTime()
      )
      setAttendanceRecords(records)
      const today = new Date().toISOString().split('T')[0]
      const todayRec = records.find((r) => r.attendanceDate === today) || null
      setTodayRecord(todayRec)
    } catch (err) {
      console.error('Failed to load attendance from DB:', err)
    } finally {
      setDbLoading(false)
    }
  }, [user?.staffId])

  useEffect(() => {
    loadAttendanceFromDB()
  }, [loadAttendanceFromDB])

  // Period numbers for selection (1-6)
  const periods = [1, 2, 3, 4, 5, 6]
  const periodLabels: Record<number, string> = {
    1: 'Period 1 (9:00-10:00)',
    2: 'Period 2 (10:00-11:00)',
    3: 'Period 3 (11:00-12:00)',
    4: 'Period 4 (12:00-1:00)',
    5: 'Period 5 (1:00-2:00)',
    6: 'Period 6 (2:00-3:00)',
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setFiles(Array.from(e.target.files))
    }
  }

  const toggleMeetingHour = (period: number) => {
    const newHours = new Set(selectedMeetingHours)
    if (newHours.has(period)) {
      newHours.delete(period)
    } else {
      newHours.add(period)
    }
    setSelectedMeetingHours(newHours)
  }

  const togglePeriod = (period: number) => {
    const newPeriods = new Set(selectedPeriods)
    if (newPeriods.has(period)) {
      newPeriods.delete(period)
    } else {
      newPeriods.add(period)
    }
    setSelectedPeriods(newPeriods)
  }

  const handleStatusChange = (status: any) => {
    setFormData({ ...formData, status })
    setSelectedMeetingHours(new Set())
    setSelectedPeriods(new Set())
    setUsePeriodWiseMarking(false)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user) return

    // Validate period-wise marking
    if (usePeriodWiseMarking && selectedPeriods.size === 0) {
      setError('Please select at least one period')
      return
    }

    // Validate meeting hours
    if (formData.status === 'MEETING' && !usePeriodWiseMarking && !['FULL_DAY', 'MORNING_ONLY', 'AFTERNOON_ONLY'].includes(formData.dayType)) {
      setError('Please select meeting duration')
      return
    }

    try {
      setLoading(true)
      setError(null)
      setSuccess(null)

      // Prepare dates
      const dates: string[] = []
      if (showMultipleDays) {
        const start = new Date(formData.attendanceDate)
        const end = new Date(formData.endDate)
        for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
          dates.push(d.toISOString().split('T')[0])
        }
      } else {
        dates.push(formData.attendanceDate)
      }

      // Mark attendance for each date
      for (const date of dates) {
        const payload: any = {
          staffId: user.staffId || user.username,  // staffId == username in seeded data
          status: formData.status,
          attendanceDate: date,
          remarks: formData.remarks || notes,
        }

        // Always include dayType (default or selected)
        payload.dayType = formData.dayType

        // Add period selection if period-wise marking enabled
        if (usePeriodWiseMarking) {
          payload.selectedPeriods = Array.from(selectedPeriods)
        }

        // Add meeting hours for MEETING status
        if (formData.status === 'MEETING' && usePeriodWiseMarking) {
          payload.meetingHours = Array.from(selectedPeriods)
        }

        console.log('Sending attendance payload:', payload)
        await attendanceAPI.markAttendance(payload)
      }

      // Upload files if needed
      if (files.length > 0) {
        const formDataUpload = new FormData()
        files.forEach((file) => {
          formDataUpload.append('files', file)
        })
        formDataUpload.append('staffId', user.staffId)
        formDataUpload.append('lessonDate', formData.attendanceDate)
        formDataUpload.append('notes', notes)
        formDataUpload.append('classCode', 'GENERAL')
        formDataUpload.append('subjectId', '0')
        
        try {
          await attendanceAPI.uploadLessonPlan(formDataUpload)
        } catch (uploadErr) {
          console.warn('File upload failed:', uploadErr)
        }
      }

      setSuccess(`✅ Attendance marked successfully for ${dates.length} day(s)!`)
      
      // Reload attendance records from DB immediately to reflect the update
      await loadAttendanceFromDB()

      // If marked as LEAVE, ONDUTY, or MEETING, fetch and display created alterations
      if (['LEAVE', 'ONDUTY', 'MEETING'].includes(formData.status)) {
        try {
          const alterationsResponse = await alterationAPI.getByStaff(user.id.toString())
          const alterations = alterationsResponse.data.data || []
          
          // Count alterations for the marked dates
          const createdAlterations = alterations.filter((alt: any) => {
            const altDate = alt.alterationDate.split('T')[0]
            return dates.includes(altDate)
          })
          
          if (createdAlterations.length > 0) {
            setCreatedAlterationCount(createdAlterations.length)
            setShowSuccessModal(true)
          }
        } catch (err) {
          console.warn('Could not fetch alterations:', err)
        }
      }
      
      // Reset form
      setFormData({
        status: 'LEAVE',
        dayType: 'FULL_DAY',
        attendanceDate: new Date().toISOString().split('T')[0],
        endDate: new Date().toISOString().split('T')[0],
        remarks: '',
      })
      setNotes('')
      setFiles([])
      setSelectedMeetingHours(new Set())
      setSelectedPeriods(new Set())
      setUsePeriodWiseMarking(false)
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to mark attendance')
    } finally {
      setLoading(false)
    }
  }

  const getStatusBadgeColor = (status: string) => {
    const map: Record<string, string> = {
      PRESENT:            'bg-green-100 text-green-800',
      LEAVE:              'bg-blue-100 text-blue-800',
      ABSENT:             'bg-red-100 text-red-800',
      ONDUTY:             'bg-orange-100 text-orange-800',
      MEETING:            'bg-purple-100 text-purple-800',
      PERIOD_WISE_ABSENT: 'bg-yellow-100 text-yellow-800',
    }
    return map[status] || 'bg-slate-100 text-slate-700'
  }

  const formatDayType = (dayType: string) => {
    const map: Record<string, string> = {
      FULL_DAY:        'Full Day',
      MORNING_ONLY:    'Morning (9AM–1PM)',
      AFTERNOON_ONLY:  'Afternoon (1PM–5PM)',
    }
    return map[dayType] || dayType
  }

  // Shows period-wise labels when selectedPeriods are present, otherwise falls back to dayType
  const formatDuration = (rec: AttendanceRecord) => {
    if (rec.selectedPeriods && rec.selectedPeriods.length > 0) {
      const sorted = [...rec.selectedPeriods].sort((a, b) => a - b)
      return sorted.map((p) => periodLabels[p] || `Period ${p}`).join(', ')
    }
    return formatDayType(rec.dayType)
  }

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-slate-900">Mark Attendance</h1>
            <p className="text-slate-600 mt-1">Record your daily attendance and manage meeting schedules</p>
          </div>
          <button
            onClick={loadAttendanceFromDB}
            disabled={dbLoading}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              dbLoading ? 'bg-slate-100 text-slate-400 cursor-not-allowed' : 'bg-slate-100 hover:bg-slate-200 text-slate-700'
            }`}
          >
            <RefreshCw className={`w-4 h-4 ${dbLoading ? 'animate-spin' : ''}`} />
            Refresh from DB
          </button>
        </div>

        {/* TODAY'S STATUS BANNER — shows what's currently in the database */}
        <Card className={`p-5 border-2 ${todayRecord ? 'border-green-400 bg-green-50' : 'border-slate-200 bg-slate-50'}`}>
          <div className="flex items-center gap-3">
            <div className={`p-2 rounded-full ${todayRecord ? 'bg-green-100' : 'bg-slate-100'}`}>
              {todayRecord ? (
                <CheckCircle className="w-6 h-6 text-green-600" />
              ) : (
                <Clock className="w-6 h-6 text-slate-500" />
              )}
            </div>
            <div className="flex-1">
              <p className="font-semibold text-slate-900">
                Today ({new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })})
              </p>
              {todayRecord ? (
                <div className="flex items-center gap-3 mt-1 flex-wrap">
                  <span className={`px-3 py-1 rounded-full text-sm font-semibold ${getStatusBadgeColor(todayRecord.status)}`}>
                    {todayRecord.status}
                  </span>
                  <span className="text-sm text-slate-600">{formatDayType(todayRecord.dayType)}</span>
                  {todayRecord.remarks && (
                    <span className="text-sm text-slate-500 italic">"{todayRecord.remarks}"</span>
                  )}
                  <span className="text-xs text-green-700 font-medium">✓ Saved in DB (ID: {todayRecord.id})</span>
                </div>
              ) : (
                <p className="text-sm text-slate-500 mt-1">No attendance marked yet — use the form below</p>
              )}
            </div>
          </div>
        </Card>

        {/* Alerts */}
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

        {/* Alterations Created Modal */}
        {showSuccessModal && createdAlterationCount > 0 && (
          <Card className="p-6 bg-green-50 border-2 border-green-300">
            <div className="flex items-start gap-4">
              <CheckCircle className="w-8 h-8 text-green-600 flex-shrink-0 mt-1" />
              <div className="flex-1">
                <h3 className="text-lg font-semibold text-green-900 mb-2">
                  ✅ {createdAlterationCount} Staff Alteration(s) Created
                </h3>
                <p className="text-green-800 mb-4">
                  Substitutes have been automatically assigned for your absence. You can view and manage them in the Alterations page.
                </p>
                <div className="flex gap-3">
                  <button
                    onClick={() => {
                      setShowSuccessModal(false)
                      navigate('/alterations')
                    }}
                    className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 font-medium flex items-center gap-2"
                  >
                    View Alterations
                    <ArrowRight className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => setShowSuccessModal(false)}
                    className="px-4 py-2 bg-green-200 text-green-900 rounded-lg hover:bg-green-300 font-medium"
                  >
                    Close
                  </button>
                </div>
              </div>
            </div>
          </Card>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Status Selection - Simplified */}
          <Card className="p-6">
            <h3 className="font-semibold text-slate-900 mb-4">Attendance Status</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {[
                { value: 'LEAVE' as const, label: 'Leave', icon: Calendar, color: 'border-blue-500 bg-blue-50' },
                { value: 'ONDUTY' as const, label: 'On Duty', icon: Briefcase, color: 'border-orange-500 bg-orange-50' },
                { value: 'MEETING' as const, label: 'In Meeting', icon: Clock, color: 'border-purple-500 bg-purple-50' },
              ].map(({ value, label, icon: Icon, color }) => (
                <button
                  key={value}
                  type="button"
                  onClick={() => handleStatusChange(value)}
                  className={`border-2 rounded-lg p-4 text-center transition-all ${
                    formData.status === value ? color + ' border-2 shadow-md' : 'border-slate-200 hover:border-slate-300'
                  }`}
                >
                  <Icon className="w-6 h-6 mx-auto mb-2 text-slate-700" />
                  <p className="font-medium text-slate-900">{label}</p>
                </button>
              ))}
            </div>
          </Card>

          {/* Absence Duration — combined duration buttons + period-wise selection */}
          {['LEAVE', 'ONDUTY', 'MEETING'].includes(formData.status) && (
            <Card className="p-6">
              <h3 className="font-semibold text-slate-900 mb-4">Absence Duration</h3>

              {/* Default: Full Day / Morning / Afternoon buttons */}
              {!usePeriodWiseMarking && (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-5">
                  {[
                    { value: 'FULL_DAY' as const, label: 'Full Day' },
                    { value: 'MORNING_ONLY' as const, label: 'Morning (9 AM - 1 PM)' },
                    { value: 'AFTERNOON_ONLY' as const, label: 'Afternoon (1 PM - 5 PM)' },
                  ].map(({ value, label }) => (
                    <button
                      key={value}
                      type="button"
                      onClick={() => setFormData({ ...formData, dayType: value })}
                      className={`border-2 rounded-lg p-3 text-center transition-all ${
                        formData.dayType === value
                          ? 'border-blue-500 bg-blue-50 shadow-md'
                          : 'border-slate-200 hover:border-slate-300'
                      }`}
                    >
                      <p className="font-medium text-slate-900">{label}</p>
                    </button>
                  ))}
                </div>
              )}

              {/* Period-wise: individual period checkboxes */}
              {usePeriodWiseMarking && (
                <div className="mb-5">
                  <p className="text-sm text-slate-600 mb-3">
                    Select the specific periods for this {formData.status.toLowerCase()}
                  </p>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                    {periods.map((period) => (
                      <label
                        key={period}
                        className="flex items-center gap-3 cursor-pointer p-3 rounded-lg border border-slate-200 hover:bg-slate-50"
                      >
                        <input
                          type="checkbox"
                          checked={selectedPeriods.has(period)}
                          onChange={() => togglePeriod(period)}
                          className="w-4 h-4 text-blue-600 rounded focus:ring-2 focus:ring-blue-500"
                        />
                        <span className="text-sm font-medium text-slate-900">{periodLabels[period]}</span>
                      </label>
                    ))}
                  </div>
                </div>
              )}

              {/* Toggle: Period wise selection checkbox */}
              <div className="border-t border-slate-100 pt-4">
                <label className="flex items-center gap-3 cursor-pointer select-none">
                  <input
                    type="checkbox"
                    checked={usePeriodWiseMarking}
                    onChange={(e) => {
                      setUsePeriodWiseMarking(e.target.checked)
                      setSelectedPeriods(new Set())
                    }}
                    className="w-4 h-4 rounded accent-blue-600"
                  />
                  <div>
                    <p className="font-medium text-slate-900 text-sm">Period wise selection</p>
                    <p className="text-xs text-slate-500">Choose individual periods instead of full / half day</p>
                  </div>
                </label>
              </div>
            </Card>
          )}

          {/* Lesson Plan Upload Section */}
          {['LEAVE', 'ONDUTY'].includes(formData.status) && (
            <Card className="p-6 border-l-4 border-green-500">
              <div className="flex items-center gap-2 mb-4">
                <FileText className="w-5 h-5 text-green-600" />
                <h3 className="font-semibold text-slate-900">Share Lesson Plans</h3>
              </div>
              <p className="text-sm text-slate-600 mb-4">Upload lesson plan documents for substitute staff</p>
              
              <div className="space-y-3">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-2">Lesson Plan Files</label>
                  <input
                    type="file"
                    multiple
                    onChange={handleFileChange}
                    accept=".pdf,.doc,.docx,.ppt,.pptx,.jpg,.png"
                    className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-900 focus:outline-none focus:ring-2 focus:ring-green-500"
                  />
                  <p className="text-xs text-slate-500 mt-1">Supported: PDF, DOC, DOCX, PPT, PPTX, JPG, PNG</p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-2">Notes for Substitute</label>
                  <textarea
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    placeholder="Add any important instructions or notes..."
                    className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-green-500"
                    rows={3}
                  />
                </div>

                {files.length > 0 && (
                  <div className="bg-green-50 border border-green-200 rounded-lg p-3">
                    <p className="text-sm font-medium text-green-900 mb-2">Files selected: {files.length}</p>
                    <ul className="space-y-1">
                      {files.map((file, idx) => (
                        <li key={idx} className="text-sm text-green-800">• {file.name}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            </Card>
          )}

          {/* Date Selection */}
          <Card className="p-6 space-y-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-semibold text-slate-900">Date(s)</h3>
              <label className="flex items-center cursor-pointer gap-2 text-sm text-slate-600 hover:text-slate-900">
                <input
                  type="checkbox"
                  checked={showMultipleDays}
                  onChange={(e) => setShowMultipleDays(e.target.checked)}
                  className="rounded"
                />
                Mark multiple days
              </label>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">
                  {showMultipleDays ? 'Start Date' : 'Attendance Date'}
                </label>
                <input
                  type="date"
                  value={formData.attendanceDate}
                  onChange={(e) => setFormData({ ...formData, attendanceDate: e.target.value })}
                  className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
              {showMultipleDays && (
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-2">End Date</label>
                  <input
                    type="date"
                    value={formData.endDate}
                    onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                    className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    required
                  />
                </div>
              )}
            </div>
          </Card>

          {/* Remarks */}
          <Card className="p-6 space-y-4">
            <h3 className="font-semibold text-slate-900">Remarks (Optional)</h3>
            <textarea
              value={formData.remarks || notes}
              onChange={(e) => {
                setFormData({ ...formData, remarks: e.target.value })
                setNotes(e.target.value)
              }}
              placeholder="Add any additional remarks..."
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
              rows={3}
            />
          </Card>

          {/* Submit Button */}
          <div className="flex gap-3">
            <Button
              type="submit"
              disabled={loading}
              className="flex-1 bg-blue-600 hover:bg-blue-700 text-white"
            >
              {loading ? 'Marking...' : 'Mark Attendance'}
            </Button>
          </div>
        </form>

        {/* ── ATTENDANCE HISTORY FROM DATABASE ── */}
        <Card className="p-6">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <History className="w-5 h-5 text-slate-600" />
              <h3 className="font-semibold text-slate-900">Attendance History (from DB)</h3>
            </div>
            {dbLoading && <RefreshCw className="w-4 h-4 animate-spin text-slate-500" />}
          </div>

          {attendanceRecords.length === 0 ? (
            <div className="text-center py-8 text-slate-500">
              {dbLoading ? 'Loading from database...' : 'No attendance records found in database.'}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-slate-200">
                    <th className="text-left py-2 px-3 font-semibold text-slate-700">Date</th>
                    <th className="text-left py-2 px-3 font-semibold text-slate-700">Status</th>
                    <th className="text-left py-2 px-3 font-semibold text-slate-700">Duration</th>
                    <th className="text-left py-2 px-3 font-semibold text-slate-700">Remarks</th>
                    <th className="text-left py-2 px-3 font-semibold text-slate-700 text-xs text-slate-500">DB ID</th>
                  </tr>
                </thead>
                <tbody>
                  {attendanceRecords.slice(0, 20).map((rec) => {
                    const isToday = rec.attendanceDate === new Date().toISOString().split('T')[0]
                    return (
                      <tr
                        key={rec.id}
                        className={`border-b border-slate-100 hover:bg-slate-50 transition-colors ${isToday ? 'bg-green-50' : ''}`}
                      >
                        <td className="py-2 px-3 font-medium text-slate-900">
                          {new Date(rec.attendanceDate).toLocaleDateString('en-US', {
                            weekday: 'short', month: 'short', day: 'numeric', year: 'numeric',
                          })}
                          {isToday && (
                            <span className="ml-2 text-xs bg-green-600 text-white px-1.5 py-0.5 rounded">TODAY</span>
                          )}
                        </td>
                        <td className="py-2 px-3">
                          <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${getStatusBadgeColor(rec.status)}`}>
                            {rec.status}
                          </span>
                        </td>
                        <td className="py-2 px-3 text-slate-600">{formatDuration(rec)}</td>
                        <td className="py-2 px-3 text-slate-500 max-w-xs truncate">{rec.remarks || '—'}</td>
                        <td className="py-2 px-3 text-xs text-slate-400">#{rec.id}</td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
              {attendanceRecords.length > 20 && (
                <p className="text-sm text-slate-500 mt-2 text-center">
                  Showing latest 20 of {attendanceRecords.length} records
                </p>
              )}
            </div>
          )}
        </Card>
      </div>
    </Layout>
  )
}
