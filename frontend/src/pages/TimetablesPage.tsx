import React, { useState, useEffect } from 'react'
import { Plus, AlertCircle } from 'lucide-react'
import { Layout } from '../components/Layout'
import { Button, Card, Alert } from '../components/common'
import { CreateTimetableModal } from '../components/CreateTimetableModal'
import { TimetableTable } from '../components/TimetableTable'
import { useTimetableStore } from '../store/timetableStore'
import { useAuthStore } from '../store/authStore'
import { timetableAPI } from '../api'

const CLASSES = ['CS1', 'CS2', 'IT1', 'IT2']
const DAYS = [
  { num: 1, name: 'Monday' },
  { num: 2, name: 'Tuesday' },
  { num: 3, name: 'Wednesday' },
  { num: 4, name: 'Thursday' },
  { num: 5, name: 'Friday' },
  { num: 6, name: 'Saturday' },
]

export const TimetablesPage: React.FC = () => {
  const user = useAuthStore((state) => state.user)
  const accessToken = useAuthStore((state) => state.accessToken)
  const timetables = useTimetableStore((state) => state.timetables)
  const setTimetables = useTimetableStore((state) => state.setTimetables)
  const selectedClass = useTimetableStore((state) => state.selectedClass)
  const setSelectedClass = useTimetableStore((state) => state.setSelectedClass)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showModal, setShowModal] = useState(false)

  useEffect(() => {
    loadTimetables()
  }, [selectedClass, accessToken])

  const loadTimetables = async () => {
    if (!accessToken) {
      console.warn('No access token available, skipping load')
      setLoading(false)
      return
    }
    
    try {
      setLoading(true)
      setError(null)
      console.log(`Loading timetables for class: ${selectedClass}`)
      console.log(`Auth token present: ${!!accessToken}`)
      const response = await timetableAPI.getByClass(selectedClass)
      console.log('Timetables loaded:', response.data)
      setTimetables(response.data.data || [])
    } catch (err: any) {
      console.error('Error loading timetables:', err)
      console.error('Error details:', {
        status: err.response?.status,
        data: err.response?.data,
        message: err.message,
      })
      const errorMsg = err.response?.data?.message || err.message || 'Failed to load timetables'
      setError(errorMsg)
    } finally {
      setLoading(false)
    }
  }

  const canCreate = user?.roles.some((r) => ['STAFF', 'HOD', 'ADMIN'].includes(r))

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-slate-900">Timetable Management</h1>
            <p className="text-slate-600 mt-1">Create, view, and manage class timetables</p>
          </div>
          {canCreate && (
            <Button onClick={() => setShowModal(true)} size="lg">
              <Plus className="w-5 h-5" />
              Create Timetable
            </Button>
          )}
        </div>

        {/* Class Filter */}
        <Card className="p-6">
          <h3 className="font-semibold text-slate-900 mb-4">Select Class</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            {CLASSES.map((classCode) => (
              <button
                key={classCode}
                onClick={() => setSelectedClass(classCode)}
                className={`py-3 px-4 rounded-lg font-medium transition-all ${
                  selectedClass === classCode
                    ? 'bg-gradient-to-r from-blue-600 to-purple-600 text-white shadow-lg'
                    : 'bg-slate-100 text-slate-900 hover:bg-slate-200'
                }`}
              >
                {classCode}
              </button>
            ))}
          </div>
        </Card>

        {/* Timetable Grid */}
        {loading ? (
          <Card className="p-12 text-center">
            <div className="inline-block w-12 h-12 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
            <p className="mt-4 text-slate-600">Loading timetables...</p>
          </Card>
        ) : error ? (
          <Alert
            type="error"
            title="Error Loading Timetables"
            message={error}
            onClose={() => setError(null)}
          />
        ) : timetables.length === 0 ? (
          <Card className="p-12 text-center">
            <AlertCircle className="w-12 h-12 text-slate-400 mx-auto mb-4" />
            <p className="text-slate-600">No timetables found for {selectedClass}</p>
            {canCreate && (
              <Button onClick={() => setShowModal(true)} className="mt-4">
                Create First Timetable
              </Button>
            )}
          </Card>
        ) : (
          <div className="grid lg:grid-cols-2 gap-6">
            {/* Calendar View */}
            <Card className="p-6">
              <h3 className="font-semibold text-slate-900 mb-4">Weekly Schedule</h3>
              <div className="space-y-3 overflow-y-auto max-h-96">
                {DAYS.map((day) => {
                  const dayTimetables = timetables.filter((t) => t.dayOrder === day.num)
                  return (
                    <div key={day.num} className="border border-slate-200 rounded-lg p-3">
                      <h4 className="font-semibold text-slate-900 mb-2">{day.name}</h4>
                      {dayTimetables.length === 0 ? (
                        <p className="text-sm text-slate-500">No classes scheduled</p>
                      ) : (
                        <div className="space-y-2">
                          {dayTimetables.map((t) => (
                            <div
                              key={t.id}
                              className="bg-gradient-to-r from-blue-50 to-purple-50 p-2 rounded text-sm border border-blue-200"
                            >
                              <p className="font-medium text-slate-900">
                                Period {t.periodNumber}: {t.subjectName}
                              </p>
                              <p className="text-xs text-slate-600">
                                {t.staffName}
                              </p>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )
                })}
              </div>
            </Card>

            {/* Table View */}
            <Card className="p-6 overflow-x-auto">
              <h3 className="font-semibold text-slate-900 mb-4">Timetable Details</h3>
              <TimetableTable
                timetables={timetables}
                onRefresh={loadTimetables}
              />
            </Card>
          </div>
        )}
      </div>

      {/* Create Modal */}
      <CreateTimetableModal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        onSuccess={() => {
          setShowModal(false)
          loadTimetables()
        }}
      />
    </Layout>
  )
}
