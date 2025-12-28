import React, { useState } from 'react'
import { X, AlertCircle } from 'lucide-react'
import { Button, FormInput, Card, Alert } from './common'
import { timetableAPI } from '../api'

interface CreateTimetableModalProps {
  isOpen: boolean
  onClose: () => void
  onSuccess: () => void
}

const CLASSES = ['CS1', 'CS2', 'IT1', 'IT2']
const SUBJECTS = ['JAVA', 'PY', 'WEB', 'DB']
const STAFF = ['Staff1', 'Staff2', 'Staff3', 'Staff4', 'Staff5']
const DAYS = Array.from({ length: 6 }, (_, i) => i + 1)
const PERIODS = Array.from({ length: 6 }, (_, i) => i + 1)

export const CreateTimetableModal: React.FC<CreateTimetableModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [formData, setFormData] = useState({
    templateName: '',
    classCode: 'CS1',
    dayOrder: 1,
    periodNumber: 1,
    subjectCode: 'JAVA',
    staffId: 'Staff1',
    remarks: '',
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)

    try {
      await timetableAPI.create(formData)
      onSuccess()
      setFormData({
        templateName: '',
        classCode: 'CS1',
        dayOrder: 1,
        periodNumber: 1,
        subjectCode: 'JAVA',
        staffId: 'Staff1',
        remarks: '',
      })
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create timetable')
    } finally {
      setLoading(false)
    }
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50 animate-fadeIn">
      <Card className="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="sticky top-0 bg-white flex items-center justify-between p-6 border-b border-slate-200">
          <h2 className="text-2xl font-bold text-slate-900">Create Timetable</h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-slate-100 rounded-lg transition-colors"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <Alert
              type="error"
              title="Error"
              message={error}
              onClose={() => setError(null)}
            />
          )}

          <FormInput
            label="Template Name"
            type="text"
            placeholder="e.g., CS1 - Java Class"
            value={formData.templateName}
            onChange={(e) => setFormData({ ...formData, templateName: e.target.value })}
            required
          />

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Class</label>
              <select
                value={formData.classCode}
                onChange={(e) => setFormData({ ...formData, classCode: e.target.value })}
                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {CLASSES.map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Subject</label>
              <select
                value={formData.subjectCode}
                onChange={(e) => setFormData({ ...formData, subjectCode: e.target.value })}
                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {SUBJECTS.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Staff</label>
              <select
                value={formData.staffId}
                onChange={(e) => setFormData({ ...formData, staffId: e.target.value })}
                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {STAFF.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Day</label>
              <select
                value={formData.dayOrder}
                onChange={(e) => setFormData({ ...formData, dayOrder: parseInt(e.target.value) })}
                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {DAYS.map((d) => (
                  <option key={d} value={d}>
                    Day {d}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">Period</label>
            <select
              value={formData.periodNumber}
              onChange={(e) => setFormData({ ...formData, periodNumber: parseInt(e.target.value) })}
              className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {PERIODS.map((p) => (
                <option key={p} value={p}>
                  Period {p}
                </option>
              ))}
            </select>
          </div>

          <FormInput
            label="Remarks (Optional)"
            type="text"
            placeholder="Additional notes"
            value={formData.remarks}
            onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
          />

          <div className="flex gap-3 pt-4">
            <Button
              type="submit"
              loading={loading}
              className="flex-1"
              size="lg"
            >
              Create Timetable
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={onClose}
              className="flex-1"
              size="lg"
              disabled={loading}
            >
              Cancel
            </Button>
          </div>
        </form>
      </Card>
    </div>
  )
}
