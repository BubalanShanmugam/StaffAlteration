import React, { useState } from 'react'
import { Calendar, FileText, AlertCircle, CheckCircle, Clock, Briefcase } from 'lucide-react'
import { Layout } from '../components/Layout'
import { Button, Card, Alert } from '../components/common'
import { attendanceAPI } from '../api'
import { useAuthStore } from '../store/authStore'

export const AttendancePage: React.FC = () => {
  const user = useAuthStore((state) => state.user)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [showMultipleDays, setShowMultipleDays] = useState(false)
  const [notes, setNotes] = useState('')
  const [files, setFiles] = useState<File[]>([])
  const [selectedMeetingHours, setSelectedMeetingHours] = useState<Set<number>>(new Set())
  
  const [formData, setFormData] = useState({
    status: 'PRESENT' as 'PRESENT' | 'ABSENT' | 'LEAVE' | 'MEETING',
    dayType: 'FULL_DAY' as 'FULL_DAY' | 'MORNING_ONLY' | 'AFTERNOON_ONLY',
    attendanceDate: new Date().toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0],
    remarks: '',
  })

  // Period numbers for meeting selection (1-6)
  const periods = [1, 2, 3, 4, 5, 6]
  const periodLabels: Record<number, string> = {
    1: 'Period 1 (9:00-10:00)',
    2: 'Period 2 (10:00-1:00)',
    3: 'Period 3 (1:00-2:00)',
    4: 'Period 4 (2:00-3:00)',
    5: 'Period 5 (3:00-4:00)',
    6: 'Period 6 (4:00-5:00)',
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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user) return

    // Validate meeting hours selection
    if (formData.status === 'MEETING' && selectedMeetingHours.size === 0) {
      setError('Please select at least one meeting hour')
      return
    }

    try {
      setLoading(true)
      setError(null)
      setSuccess(null)

      console.log('Form submission details:', {
        selectedStatus: formData.status,
        selectedDayType: formData.dayType,
        meetingHours: formData.status === 'MEETING' ? Array.from(selectedMeetingHours) : [],
        attendanceDate: formData.attendanceDate,
        staffId: user.staffId,
      })

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
          staffId: user.staffId,
          status: formData.status,
          attendanceDate: date,
          remarks: formData.remarks || notes,
        }

        // Only add dayType for LEAVE and ABSENT statuses
        if (formData.status === 'LEAVE' || formData.status === 'ABSENT') {
          payload.dayType = formData.dayType
        }

        // Add meeting hours for MEETING status
        if (formData.status === 'MEETING') {
          payload.meetingHours = Array.from(selectedMeetingHours)
        }

        console.log('Sending attendance payload:', payload)
        await attendanceAPI.markAttendance(payload)
      }

      // Upload files if absent
      if (formData.status === 'ABSENT' && files.length > 0) {
        const formDataUpload = new FormData()
        files.forEach((file) => {
          formDataUpload.append('files', file)
        })
        formDataUpload.append('staffId', user.staffId)
        formDataUpload.append('lessonDate', formData.attendanceDate)
        formDataUpload.append('notes', notes)
        
        // TODO: Create lesson plan upload endpoint
        // await attendanceAPI.uploadLessonPlan(formDataUpload)
      }

      setSuccess(`✅ Attendance marked successfully for ${dates.length} day(s)!`)
      
      // Fetch updated attendance to confirm
      try {
        const updatedAttendance = await attendanceAPI.getStaffAttendance(user.staffId)
        console.log('Updated attendance records:', updatedAttendance.data)
      } catch (err) {
        console.warn('Could not fetch updated attendance:', err)
      }
      
      // Reset form
      setFormData({
        status: 'PRESENT',
        dayType: 'FULL_DAY',
        attendanceDate: new Date().toISOString().split('T')[0],
        endDate: new Date().toISOString().split('T')[0],
        remarks: '',
      })
      setNotes('')
      setFiles([])
      setSelectedMeetingHours(new Set())
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to mark attendance')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn">
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Mark Attendance</h1>
          <p className="text-slate-600 mt-1">Record your daily attendance and manage meeting schedules</p>
        </div>

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

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Status Selection */}
          <Card className="p-6">
            <h3 className="font-semibold text-slate-900 mb-4">Attendance Status</h3>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              {[
                { value: 'PRESENT' as const, label: 'Present', icon: CheckCircle, color: 'border-green-500 bg-green-50' },
                { value: 'MEETING' as const, label: 'In Meeting', icon: Briefcase, color: 'border-purple-500 bg-purple-50' },
                { value: 'LEAVE' as const, label: 'On Leave', icon: Calendar, color: 'border-blue-500 bg-blue-50' },
                { value: 'ABSENT' as const, label: 'Absent', icon: AlertCircle, color: 'border-red-500 bg-red-50' },
              ].map(({ value, label, icon: Icon, color }) => (
                <button
                  key={value}
                  type="button"
                  onClick={() => {
                    setFormData({ ...formData, status: value })
                    setSelectedMeetingHours(new Set())
                  }}
                  className={`border-2 rounded-lg p-4 text-center transition-all ${
                    formData.status === value ? color + ' border-2 shadow-md' : 'border-slate-200 hover:border-slate-300'
                  }`}
                >
                  <Icon className="w-8 h-8 mx-auto mb-2 text-slate-700" />
                  <p className="font-medium text-slate-900">{label}</p>
                </button>
              ))}
            </div>
          </Card>

          {/* Meeting Hours Selection (only for MEETING status) */}
          {formData.status === 'MEETING' && (
            <Card className="p-6 border-l-4 border-purple-500">
              <div className="flex items-center gap-2 mb-4">
                <Clock className="w-5 h-5 text-purple-600" />
                <h3 className="font-semibold text-slate-900">Select Meeting Hours</h3>
              </div>
              <p className="text-sm text-slate-600 mb-4">Check the periods when you'll be in the meeting</p>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                {periods.map((period) => (
                  <label key={period} className="flex items-center gap-3 cursor-pointer p-3 rounded-lg border border-slate-200 hover:bg-slate-50">
                    <input
                      type="checkbox"
                      checked={selectedMeetingHours.has(period)}
                      onChange={() => toggleMeetingHour(period)}
                      className="w-4 h-4 text-purple-600 rounded focus:ring-2 focus:ring-purple-500"
                    />
                    <span className="text-sm font-medium text-slate-900">{periodLabels[period]}</span>
                  </label>
                ))}
              </div>
            </Card>
          )}

          {/* Day Type Selection (for LEAVE and ABSENT) */}
          {(formData.status === 'LEAVE' || formData.status === 'ABSENT') && (
            <Card className="p-6">
              <h3 className="font-semibold text-slate-900 mb-4">Day Type</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {[
                  { value: 'FULL_DAY' as const, label: '🕐 Full Day' },
                  { value: 'MORNING_ONLY' as const, label: '🌅 Morning (9 AM - 1 PM)' },
                  { value: 'AFTERNOON_ONLY' as const, label: '🌆 Afternoon (1 PM - 5 PM)' },
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

          {/* File Upload (for ABSENT only) */}
          {formData.status === 'ABSENT' && (
            <Card className="p-6 space-y-4">
              <div className="flex items-center gap-2 mb-4">
                <FileText className="w-5 h-5 text-blue-600" />
                <h3 className="font-semibold text-slate-900">Upload Lesson Plan (Optional)</h3>
              </div>
              <p className="text-sm text-slate-600">Upload any documents related to your absence</p>
              <input
                type="file"
                multiple
                onChange={handleFileChange}
                className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              {files.length > 0 && (
                <div className="mt-2">
                  <p className="text-sm font-medium text-slate-700 mb-2">Selected files:</p>
                  <ul className="space-y-1">
                    {files.map((file, idx) => (
                      <li key={idx} className="text-sm text-slate-600">✓ {file.name}</li>
                    ))}
                  </ul>
                </div>
              )}
            </Card>
          )}

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
      </div>
    </Layout>
  )
}
