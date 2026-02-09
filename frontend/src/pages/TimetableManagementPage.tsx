import React, { useState, useEffect } from 'react'
import { Plus, Edit2, Trash2, Save, X, Eye } from 'lucide-react'
import { Layout } from '../components/Layout'
import { Button, Card, Alert } from '../components/common'
import { timetableAPI, classManagementAPI } from '../api'
import { useAuthStore } from '../store/authStore'

const DAYS = [
  { order: 1, name: 'Monday' },
  { order: 2, name: 'Tuesday' },
  { order: 3, name: 'Wednesday' },
  { order: 4, name: 'Thursday' },
  { order: 5, name: 'Friday' },
  { order: 6, name: 'Saturday' },
]

const PERIODS = [
  { number: 1, time: '9:00 AM - 10:00 AM' },
  { number: 2, time: '10:00 AM - 11:00 AM' },
  { number: 3, time: '11:00 AM - 12:00 PM' },
  { number: 4, time: '1:00 PM - 2:00 PM' },
  { number: 5, time: '2:00 PM - 3:00 PM' },
  { number: 6, time: '3:00 PM - 4:00 PM' },
]

const SUBJECTS = [
  { code: 'DSA', name: 'Data Structures & Algorithms' },
  { code: 'WEB', name: 'Web Development' },
  { code: 'DB', name: 'Database Design' },
  { code: 'OOP', name: 'Object Oriented Programming' },
]

interface Timetable {
  id: number
  subjectName: string
  subjectCode: string
  staffName: string
  staffId: string
  classCode: string
  dayOrder: number
  periodNumber: number
  status: string
}

interface ClassInfo {
  id: number
  classCode: string
  className: string
  departmentId: number
  departmentName: string
}

interface EditingCell {
  dayOrder: number
  periodNumber: number
  classCode: string
}

export const TimetableManagementPage: React.FC = () => {
  const user = useAuthStore((state) => state.user)
  const [timetables, setTimetables] = useState<Timetable[]>([])
  const [classes, setClasses] = useState<ClassInfo[]>([])
  const [selectedClass, setSelectedClass] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [editingCell, setEditingCell] = useState<EditingCell | null>(null)
  const [editFormData, setEditFormData] = useState({
    subjectCode: '',
    staffId: '',
  })
  const [showAddClassModal, setShowAddClassModal] = useState(false)
  const [newClassData, setNewClassData] = useState({
    classCode: '',
    className: '',
    departmentId: 1,
  })

  useEffect(() => {
    loadClasses()
  }, [])

  useEffect(() => {
    if (selectedClass) {
      loadTimetables()
    }
  }, [selectedClass])

  const loadClasses = async () => {
    try {
      const response = await classManagementAPI.getAll()
      setClasses(response.data.data || [])
      if (response.data.data && response.data.data.length > 0) {
        setSelectedClass(response.data.data[0].classCode)
      }
    } catch (err: any) {
      setError('Failed to load classes')
    }
  }

  const loadTimetables = async () => {
    try {
      setLoading(true)
      const response = await timetableAPI.getByClass(selectedClass)
      setTimetables(response.data.data || [])
    } catch (err: any) {
      setError('Failed to load timetable')
    } finally {
      setLoading(false)
    }
  }

  const handleAddClass = async () => {
    if (!newClassData.classCode || !newClassData.className) {
      setError('Please fill in all fields')
      return
    }

    try {
      await classManagementAPI.create(newClassData)
      setSuccess('✅ Class created successfully!')
      setShowAddClassModal(false)
      setNewClassData({ classCode: '', className: '', departmentId: 1 })
      loadClasses()
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create class')
    }
  }

  const getTimetableForSlot = (dayOrder: number, periodNumber: number) => {
    return timetables.find(
      (t) => t.dayOrder === dayOrder && t.periodNumber === periodNumber && t.classCode === selectedClass
    )
  }

  const handleEditStart = (dayOrder: number, periodNumber: number, timetable?: Timetable) => {
    setEditingCell({ dayOrder, periodNumber, classCode: selectedClass })
    if (timetable) {
      setEditFormData({
        subjectCode: timetable.subjectCode,
        staffId: timetable.staffId,
      })
    } else {
      setEditFormData({ subjectCode: '', staffId: '' })
    }
  }

  const handleSave = async () => {
    if (!editingCell || !editFormData.subjectCode || !editFormData.staffId) {
      setError('Please fill in all fields')
      return
    }

    try {
      setLoading(true)
      const existing = getTimetableForSlot(editingCell.dayOrder, editingCell.periodNumber)

      if (existing) {
        await timetableAPI.update(existing.id, {
          templateName: `${selectedClass}-${editingCell.dayOrder}-${editingCell.periodNumber}`,
          classCode: selectedClass,
          dayOrder: editingCell.dayOrder,
          periodNumber: editingCell.periodNumber,
          subjectCode: editFormData.subjectCode,
          staffId: editFormData.staffId,
        })
      } else {
        await timetableAPI.create({
          templateName: `${selectedClass}-${editingCell.dayOrder}-${editingCell.periodNumber}`,
          classCode: selectedClass,
          dayOrder: editingCell.dayOrder,
          periodNumber: editingCell.periodNumber,
          subjectCode: editFormData.subjectCode,
          staffId: editFormData.staffId,
        })
      }

      setSuccess('✅ Timetable updated successfully!')
      setEditingCell(null)
      loadTimetables()
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save timetable')
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (timetableId: number) => {
    if (!confirm('Are you sure you want to delete this timetable entry?')) return

    try {
      await timetableAPI.delete(timetableId)
      setSuccess('✅ Timetable entry deleted!')
      loadTimetables()
    } catch (err: any) {
      setError('Failed to delete timetable entry')
    }
  }

  const isHodOrAdmin = user?.roles.some((r) => ['HOD', 'ADMIN'].includes(r))

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn">
        {/* Header */}
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold text-slate-900">Timetable Management</h1>
            <p className="text-slate-600 mt-1">Create, edit, and manage class timetables</p>
          </div>
          {isHodOrAdmin && (
            <button
              onClick={() => setShowAddClassModal(true)}
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 font-medium flex items-center gap-2"
            >
              <Plus className="w-5 h-5" />
              Add New Class
            </button>
          )}
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

        {/* Add Class Modal */}
        {showAddClassModal && isHodOrAdmin && (
          <Card className="p-6 bg-blue-50 border-2 border-blue-200">
            <h3 className="text-lg font-semibold text-slate-900 mb-4">Add New Class</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Class Code</label>
                <input
                  type="text"
                  placeholder="e.g., CS, IT, EC"
                  value={newClassData.classCode}
                  onChange={(e) =>
                    setNewClassData({ ...newClassData, classCode: e.target.value.toUpperCase() })
                  }
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Class Name</label>
                <input
                  type="text"
                  placeholder="e.g., Computer Science Year 1"
                  value={newClassData.className}
                  onChange={(e) =>
                    setNewClassData({ ...newClassData, className: e.target.value })
                  }
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg"
                />
              </div>
              <div className="flex gap-3 pt-2">
                <button
                  onClick={handleAddClass}
                  className="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 font-medium"
                >
                  Create Class
                </button>
                <button
                  onClick={() => setShowAddClassModal(false)}
                  className="flex-1 px-4 py-2 bg-slate-300 text-slate-900 rounded-lg hover:bg-slate-400 font-medium"
                >
                  Cancel
                </button>
              </div>
            </div>
          </Card>
        )}

        {/* Class Selector */}
        <Card className="p-6">
          <h3 className="font-semibold text-slate-900 mb-4">Select Class</h3>
          <div className="flex gap-3 flex-wrap">
            {classes.map((classInfo) => (
              <button
                key={classInfo.classCode}
                onClick={() => setSelectedClass(classInfo.classCode)}
                className={`px-6 py-2 rounded-lg font-medium transition-all ${
                  selectedClass === classInfo.classCode
                    ? 'bg-blue-600 text-white shadow-lg'
                    : 'bg-slate-100 text-slate-900 hover:bg-slate-200'
                }`}
              >
                {classInfo.classCode}
              </button>
            ))}
          </div>
        </Card>

        {/* Timetable Grid */}
        <Card className="p-6 overflow-x-auto">
          <h3 className="font-semibold text-slate-900 mb-4">Weekly Timetable - {selectedClass}</h3>

          {loading ? (
            <div className="text-center py-8">
              <div className="inline-block w-8 h-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
              <p className="mt-3 text-slate-600">Loading timetable...</p>
            </div>
          ) : (
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-slate-100">
                  <th className="border border-slate-300 p-3 text-left font-semibold text-slate-900">
                    Period
                  </th>
                  <th className="border border-slate-300 p-3 text-left font-semibold text-slate-900">
                    Time
                  </th>
                  {DAYS.map((day) => (
                    <th
                      key={day.order}
                      className="border border-slate-300 p-3 text-left font-semibold text-slate-900"
                    >
                      {day.name}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {PERIODS.map((period) => (
                  <tr key={period.number}>
                    <td className="border border-slate-300 p-3 font-semibold text-slate-900 bg-slate-50">
                      Period {period.number}
                    </td>
                    <td className="border border-slate-300 p-3 text-sm text-slate-600 bg-slate-50">
                      {period.time}
                    </td>
                    {DAYS.map((day) => {
                      const timetable = getTimetableForSlot(day.order, period.number)
                      const isEditing =
                        editingCell?.dayOrder === day.order &&
                        editingCell?.periodNumber === period.number

                      return (
                        <td
                          key={`${day.order}-${period.number}`}
                          className="border border-slate-300 p-2 min-w-64"
                        >
                          {isEditing ? (
                            <div className="space-y-2">
                              <select
                                value={editFormData.subjectCode}
                                onChange={(e) =>
                                  setEditFormData({
                                    ...editFormData,
                                    subjectCode: e.target.value,
                                  })
                                }
                                className="w-full px-2 py-1 border border-slate-300 rounded text-sm"
                              >
                                <option value="">Select Subject</option>
                                {SUBJECTS.map((subject) => (
                                  <option key={subject.code} value={subject.code}>
                                    {subject.name}
                                  </option>
                                ))}
                              </select>
                              <input
                                type="text"
                                placeholder="Staff ID"
                                value={editFormData.staffId}
                                onChange={(e) =>
                                  setEditFormData({
                                    ...editFormData,
                                    staffId: e.target.value,
                                  })
                                }
                                className="w-full px-2 py-1 border border-slate-300 rounded text-sm"
                              />
                              <div className="flex gap-2">
                                <button
                                  onClick={handleSave}
                                  className="flex-1 px-2 py-1 bg-green-500 text-white rounded text-sm font-medium hover:bg-green-600 flex items-center justify-center gap-1"
                                >
                                  <Save className="w-4 h-4" />
                                  Save
                                </button>
                                <button
                                  onClick={() => setEditingCell(null)}
                                  className="flex-1 px-2 py-1 bg-slate-300 text-slate-900 rounded text-sm font-medium hover:bg-slate-400 flex items-center justify-center gap-1"
                                >
                                  <X className="w-4 h-4" />
                                  Cancel
                                </button>
                              </div>
                            </div>
                          ) : timetable ? (
                            <div className="bg-blue-50 border border-blue-200 rounded p-2 space-y-1">
                              <p className="font-medium text-sm text-slate-900">
                                {timetable.subjectCode}
                              </p>
                              <p className="text-xs text-slate-600">{timetable.staffId}</p>
                              <div className="flex gap-1 pt-1">
                                <button
                                  onClick={() => handleEditStart(day.order, period.number, timetable)}
                                  className="flex-1 px-2 py-1 bg-blue-500 text-white rounded text-xs font-medium hover:bg-blue-600 flex items-center justify-center gap-1"
                                >
                                  <Edit2 className="w-3 h-3" />
                                  Edit
                                </button>
                                <button
                                  onClick={() => handleDelete(timetable.id)}
                                  className="flex-1 px-2 py-1 bg-red-500 text-white rounded text-xs font-medium hover:bg-red-600 flex items-center justify-center gap-1"
                                >
                                  <Trash2 className="w-3 h-3" />
                                  Delete
                                </button>
                              </div>
                            </div>
                          ) : (
                            <button
                              onClick={() => handleEditStart(day.order, period.number)}
                              className="w-full px-3 py-2 border-2 border-dashed border-slate-300 rounded text-slate-600 hover:border-blue-400 hover:bg-blue-50 transition-all flex items-center justify-center gap-2 text-sm font-medium"
                            >
                              <Plus className="w-4 h-4" />
                              Add Class
                            </button>
                          )}
                        </td>
                      )
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>

        {/* Legend */}
        <Card className="p-6 bg-slate-50">
          <h3 className="font-semibold text-slate-900 mb-3">📋 How to Use</h3>
          <ul className="space-y-2 text-sm text-slate-700">
            <li>
              • <strong>Click "Add New Class"</strong> (HOD/Admin only) to create new classes
            </li>
            <li>
              • <strong>Click "Add Class"</strong> on any empty slot to add a new timetable entry
            </li>
            <li>
              • <strong>Select Subject</strong> and <strong>Staff ID</strong> from the dropdown
            </li>
            <li>• <strong>Click "Edit"</strong> to modify existing entries</li>
            <li>• <strong>Click "Delete"</strong> to remove an entry</li>
            <li>• Changes are saved immediately and reflected across the system</li>
          </ul>
        </Card>
      </div>
    </Layout>
  )
}
