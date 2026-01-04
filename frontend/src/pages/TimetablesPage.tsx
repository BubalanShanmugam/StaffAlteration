import React, { useState, useEffect } from 'react'
import { Plus, AlertCircle, Grid3x3, List, Calendar as CalendarIcon } from 'lucide-react'
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

const PERIODS = [
  { num: 1, time: '9:00 - 10:10' },
  { num: 2, time: '10:10 - 11:05' },
  { num: 3, time: '11:30 - 12:25' },
  { num: 4, time: '12:25 - 1:20' },
  { num: 5, time: '2:15 - 3:10' },
]

const BREAKS = [
  { time: '11:05 - 11:30', label: 'Break' },
  { time: '1:20 - 2:15', label: 'Lunch' },
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
  const [editingTimetable, setEditingTimetable] = useState<any>(null)
  const [viewMode, setViewMode] = useState<'grid' | 'table'>('grid')
  const [selectedSlot, setSelectedSlot] = useState<{ day: number; period: number } | null>(null)
  const [currentDate, setCurrentDate] = useState(new Date())

  const getDayName = (date: Date) => {
    const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']
    return days[date.getDay()]
  }

  const formatDate = (date: Date) => {
    const d = new Date(date)
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const year = d.getFullYear()
    return `${day}/${month}/${year}`
  }

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

  const getTimetableForSlot = (dayOrder: number, periodNumber: number) => {
    return timetables.find((t) => t.dayOrder === dayOrder && t.periodNumber === periodNumber)
  }

  const getPeriodColor = (periodNumber: number) => {
    const colors = [
      'bg-blue-50 border-blue-300 hover:bg-blue-100',
      'bg-purple-50 border-purple-300 hover:bg-purple-100',
      'bg-pink-50 border-pink-300 hover:bg-pink-100',
      'bg-orange-50 border-orange-300 hover:bg-orange-100',
      'bg-green-50 border-green-300 hover:bg-green-100',
      'bg-cyan-50 border-cyan-300 hover:bg-cyan-100',
    ]
    return colors[periodNumber - 1] || 'bg-slate-50 border-slate-300'
  }

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn">
        {/* Header with Date */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-slate-900">Timetable Management</h1>
            <div className="flex items-center gap-2 text-slate-600 mt-2">
              <CalendarIcon className="w-5 h-5" />
              <p>{getDayName(currentDate)}, {formatDate(currentDate)}</p>
            </div>
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

        {/* View Mode Toggle */}
        {timetables.length > 0 && (
          <Card className="p-4 flex gap-2">
            <Button
              size="sm"
              variant={viewMode === 'grid' ? 'primary' : 'secondary'}
              onClick={() => setViewMode('grid')}
              className="gap-2"
            >
              <Grid3x3 className="w-4 h-4" />
              Grid View
            </Button>
            <Button
              size="sm"
              variant={viewMode === 'table' ? 'primary' : 'secondary'}
              onClick={() => setViewMode('table')}
              className="gap-2"
            >
              <List className="w-4 h-4" />
              Table View
            </Button>
          </Card>
        )}

        {/* Timetable Content */}
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
        ) : viewMode === 'grid' ? (
          <Card className="p-6 overflow-x-auto">
            <h3 className="font-semibold text-slate-900 mb-6">Weekly Timetable Grid</h3>
            <div className="overflow-x-auto">
              <table className="w-full border-collapse">
                <thead>
                  <tr>
                    <th className="border border-slate-300 bg-slate-100 p-3 text-left font-semibold text-slate-900">
                      Period
                    </th>
                    {DAYS.map((day) => (
                      <th
                        key={day.num}
                        className="border border-slate-300 bg-gradient-to-b from-blue-500 to-purple-500 p-3 text-center font-semibold text-white min-w-32"
                      >
                        <div className="text-sm">{day.name}</div>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {PERIODS.map((period, idx) => (
                    <React.Fragment key={period.num}>
                      <tr>
                        <td className="border border-slate-300 bg-slate-50 p-3 font-semibold text-slate-900">
                          <div className="text-sm">Period {period.num}</div>
                          <div className="text-xs text-slate-600">{period.time}</div>
                        </td>
                        {DAYS.map((day) => {
                          const cellTimetable = timetables.find(
                            (t) => t.dayOrder === day.num && t.periodNumber === period.num
                          )
                          return (
                            <td
                              key={`${day.num}-${period.num}`}
                              className="border border-slate-300 p-2 text-center align-middle h-24 hover:bg-slate-50 transition-colors cursor-pointer"
                              onClick={() => {
                                if (cellTimetable) {
                                  setEditingTimetable(cellTimetable)
                                } else {
                                  setSelectedSlot({ day: day.num, period: period.num })
                                  setShowModal(true)
                                }
                              }}
                            >
                              {cellTimetable ? (
                                <div className="h-full flex flex-col justify-center bg-gradient-to-br from-blue-100 to-purple-100 rounded p-2 border border-blue-300 hover:shadow-md transition-shadow">
                                  <p className="font-semibold text-sm text-slate-900">
                                    {cellTimetable.subjectName}
                                  </p>
                                  <p className="text-xs text-slate-700">
                                    {cellTimetable.staffName}
                                  </p>
                                </div>
                              ) : (
                                <div className="h-full flex flex-col items-center justify-center text-slate-400 hover:text-slate-600 transition-colors">
                                  <span className="text-xl">+</span>
                                  <span className="text-xs mt-1">Click to add</span>
                                </div>
                              )}
                            </td>
                          )
                        })}
                      </tr>
                      {idx === 1 && (
                        <tr>
                          <td
                            colSpan={7}
                            className="border border-slate-300 bg-gradient-to-b from-blue-500 to-purple-500 p-3 text-center font-semibold text-white"
                          >
                            {BREAKS[0].time} - {BREAKS[0].label}
                          </td>
                        </tr>
                      )}
                      {idx === 3 && (
                        <tr>
                          <td
                            colSpan={7}
                            className="border border-slate-300 bg-gradient-to-b from-blue-500 to-purple-500 p-3 text-center font-semibold text-white"
                          >
                            {BREAKS[1].time} - {BREAKS[1].label}
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
        ) : (
          <Card className="p-6 overflow-x-auto">
            <h3 className="font-semibold text-slate-900 mb-4">Timetable Details</h3>
            <TimetableTable
              timetables={timetables}
              onRefresh={loadTimetables}
              onEdit={(timetable) => setEditingTimetable(timetable)}
            />
          </Card>
        )}
      </div>

      {/* Create Modal */}
      <CreateTimetableModal
        isOpen={showModal && !editingTimetable}
        onClose={() => {
          setShowModal(false)
          setSelectedSlot(null)
        }}
        initialData={
          selectedSlot
            ? {
                dayOrder: selectedSlot.day,
                periodNumber: selectedSlot.period,
                className: selectedClass,
              }
            : undefined
        }
        onSuccess={() => {
          setShowModal(false)
          setSelectedSlot(null)
          loadTimetables()
        }}
      />

      {/* Edit Modal */}
      {editingTimetable && (
        <CreateTimetableModal
          isOpen={true}
          initialData={editingTimetable}
          onClose={() => setEditingTimetable(null)}
          onSuccess={() => {
            setEditingTimetable(null)
            loadTimetables()
          }}
        />
      )}
    </Layout>
  )
}
