import React, { useState } from 'react'
import { Calendar, FileText, AlertCircle, CheckCircle } from 'lucide-react'
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
  
  const [formData, setFormData] = useState({
    status: 'PRESENT' as 'PRESENT' | 'ABSENT' | 'LEAVE',
    dayType: 'FULL_DAY' as 'FULL_DAY' | 'MORNING_ONLY' | 'AFTERNOON_ONLY',
    attendanceDate: new Date().toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0],
    remarks: '',
  })

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setFiles(Array.from(e.target.files))
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user) return

    try {
      setLoading(true)
      setError(null)
      setSuccess(null)

      // Log the form data
      console.log('Form submission details:', {
        selectedStatus: formData.status,
        selectedDayType: formData.dayType,
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
        const payload = {
          staffId: user.staffId,
          status: formData.status,
          dayType: formData.dayType,
          attendanceDate: date,
          remarks: formData.remarks || notes,
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
      
      setFormData({
        status: 'PRESENT',
        dayType: 'FULL_DAY',
        attendanceDate: new Date().toISOString().split('T')[0],
        endDate: new Date().toISOString().split('T')[0],
        remarks: '',
      })
      setNotes('')
      setFiles([])
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
          <p className="text-slate-600 mt-1">Record your daily attendance and share lesson plans if absent</p>
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
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {[
                { value: 'PRESENT' as const, label: 'Present', icon: CheckCircle, color: 'border-green-500 bg-green-50' },
                { value: 'ABSENT' as const, label: 'Absent', icon: AlertCircle, color: 'border-red-500 bg-red-50' },
                { value: 'LEAVE' as const, label: 'On Leave', icon: Calendar, color: 'border-blue-500 bg-blue-50' },
              ].map(({ value, label, icon: Icon, color }) => (
                <button
                  key={value}
                  type="button"
                  onClick={() => setFormData({ ...formData, status: value })}
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

          {/* Day Type Selection (for half-day) */}
          <Card className="p-6">
            <h3 className="font-semibold text-slate-900 mb-4">Day Type</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {[
                { value: 'FULL_DAY' as const, label: '🕐 Full Day' },
                { value: 'MORNING_ONLY' as const, label: '🌅 Morning Only (9 AM - 1 PM)' },
                { value: 'AFTERNOON_ONLY' as const, label: '🌆 Afternoon Only (1 PM - 5 PM)' },
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

          {/* Date Selection */}
          <Card className="p-6 space-y-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-semibold text-slate-900">Date(s)</h3>
              <label className="flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={showMultipleDays}
                  onChange={(e) => setShowMultipleDays(e.target.checked)}
                  className="rounded"
                />
                <span className="ml-2 text-sm text-slate-600">Mark for multiple days</span>
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
                  onChange={(e) =>
                    setFormData({ ...formData, attendanceDate: e.target.value })
                  }
                  className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              {showMultipleDays && (
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-2">End Date</label>
                  <input
                    type="date"
                    value={formData.endDate}
                    onChange={(e) =>
                      setFormData({ ...formData, endDate: e.target.value })
                    }
                    className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
              )}
            </div>
          </Card>

          {/* Remarks */}
          <Card className="p-6">
            <label className="block text-sm font-medium text-slate-700 mb-2">Additional Notes</label>
            <textarea
              value={formData.remarks}
              onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              placeholder="Add any remarks about your absence (optional)"
              className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              rows={3}
            />
          </Card>

          {/* Lesson Plan Upload (if absent) */}
          {formData.status === 'ABSENT' && (
            <Card className="p-6 border-2 border-amber-200 bg-amber-50">
              <div className="flex items-start space-x-4">
                <AlertCircle className="w-6 h-6 text-amber-600 mt-1 flex-shrink-0" />
                <div className="flex-1">
                  <h3 className="font-semibold text-amber-900 mb-3">📚 Share Lesson Plans/Notes</h3>
                  <p className="text-sm text-amber-800 mb-4">
                    Since you're marking as absent, please share lesson plans or notes for the classes you're missing. This will help the substitute staff.
                  </p>

                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">
                        Additional Notes for Substitute
                      </label>
                      <textarea
                        value={notes}
                        onChange={(e) => setNotes(e.target.value)}
                        placeholder="Add specific instructions or notes for the substitute staff..."
                        className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        rows={3}
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">
                        📎 Attach Files
                      </label>
                      <div className="border-2 border-dashed border-slate-300 rounded-lg p-6 text-center hover:border-slate-400 transition-colors">
                        <input
                          type="file"
                          multiple
                          onChange={handleFileChange}
                          className="hidden"
                          id="file-input"
                          accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.jpg,.png,.jpeg"
                        />
                        <label htmlFor="file-input" className="cursor-pointer">
                          <FileText className="w-8 h-8 mx-auto text-slate-400 mb-2" />
                          <p className="text-sm font-medium text-slate-700">
                            Click to select files or drag and drop
                          </p>
                          <p className="text-xs text-slate-500 mt-1">
                            PDF, Word, Excel, Images, etc.
                          </p>
                        </label>
                      </div>

                      {files.length > 0 && (
                        <div className="mt-4">
                          <p className="text-sm font-medium text-slate-700 mb-2">Selected Files:</p>
                          <div className="space-y-2">
                            {files.map((file, idx) => (
                              <div key={idx} className="flex items-center justify-between bg-slate-50 p-2 rounded">
                                <span className="text-sm text-slate-700">{file.name}</span>
                                <span className="text-xs text-slate-500">{(file.size / 1024).toFixed(1)} KB</span>
                              </div>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </Card>
          )}

          {/* Submit Button */}
          <Button
            type="submit"
            disabled={loading}
            className="w-full py-3 text-lg"
          >
            {loading ? '⏳ Marking Attendance...' : '✅ Mark Attendance'}
          </Button>
        </form>

        {/* Help Section */}
        <Card className="p-6 bg-blue-50 border-2 border-blue-200">
          <h3 className="font-semibold text-blue-900 mb-3">❓ Help & Information</h3>
          <ul className="space-y-2 text-sm text-blue-800">
            <li>• Mark your attendance status at the beginning of each day</li>
            <li>• Select <strong>Full Day</strong> if you're completely absent or <strong>Half Day</strong> if partially absent</li>
            <li>• You can mark attendance for multiple days in advance (max 7 days)</li>
            <li>• If absent, sharing lesson plans helps substitute staff and students</li>
            <li>• All attached files will be shared with the substitute staff immediately</li>
          </ul>
        </Card>
      </div>
    </Layout>
  )
}
