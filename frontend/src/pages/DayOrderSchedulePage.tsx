import React, { useState, useEffect } from 'react'
import { Plus, Edit2, Trash2, AlertCircle } from 'lucide-react'
import { Layout } from '../components/Layout'
import { Button, Card, Alert } from '../components/common'
import { useAuthStore } from '../store/authStore'

interface DayOrder {
  id: number
  dayName: string
  dayNumber: number
  periods: PeriodStaff[]
  createdAt: string
}

interface PeriodStaff {
  periodNumber: number
  staffId: string
  staffName: string
}

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

export const DayOrderSchedulePage: React.FC = () => {
  const user = useAuthStore((state) => state.user)
  const accessToken = useAuthStore((state) => state.accessToken)
  const [dayOrders, setDayOrders] = useState<DayOrder[]>([])
  const [staffList, setStaffList] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showModal, setShowModal] = useState(false)
  const [editingDayOrder, setEditingDayOrder] = useState<DayOrder | null>(null)
  const [formData, setFormData] = useState({
    dayNumber: 1,
    periods: PERIODS.map((p) => ({
      periodNumber: p.num,
      staffId: '',
      staffName: '',
    })),
  })

  const canCreate = user?.roles.includes('ADMIN') || user?.roles.includes('HOD')

  useEffect(() => {
    loadData()
  }, [accessToken])

  const loadData = async () => {
    if (!accessToken) {
      setLoading(false)
      return
    }

    try {
      setLoading(true)
      // Load staff for dropdown
      const staffResponse = await fetch('http://localhost:8080/staff', {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      })

      if (staffResponse.ok) {
        const staffData = await staffResponse.json()
        setStaffList(staffData.data || [])
      }

      // For now, we'll use mock data since the backend doesn't have day order API yet
      // In the future, replace with actual API call
      const mockDayOrders: DayOrder[] = []
      setDayOrders(mockDayOrders)
    } catch (err) {
      console.error(err)
      setError('Error loading data')
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (periodIndex: number, field: string, value: string) => {
    setFormData((prev) => ({
      ...prev,
      periods: prev.periods.map((p, idx) =>
        idx === periodIndex
          ? { ...p, [field]: value }
          : p
      ),
    }))
  }

  const handleDayChange = (value: string) => {
    setFormData((prev) => ({
      ...prev,
      dayNumber: parseInt(value),
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!formData.dayNumber) {
      setError('Please select a day')
      return
    }

    try {
      // TODO: Integrate with backend API when available
      // For now, just update local state
      const newDayOrder: DayOrder = {
        id: editingDayOrder?.id || Date.now(),
        dayName: DAYS.find((d) => d.num === formData.dayNumber)?.name || '',
        dayNumber: formData.dayNumber,
        periods: formData.periods,
        createdAt: new Date().toISOString(),
      }

      if (editingDayOrder) {
        setDayOrders(dayOrders.map((d) => (d.id === editingDayOrder.id ? newDayOrder : d)))
      } else {
        setDayOrders([...dayOrders, newDayOrder])
      }

      setShowModal(false)
      setEditingDayOrder(null)
      resetForm()
    } catch (err) {
      setError('Error saving day order')
      console.error(err)
    }
  }

  const handleEdit = (dayOrder: DayOrder) => {
    setEditingDayOrder(dayOrder)
    setFormData({
      dayNumber: dayOrder.dayNumber,
      periods: dayOrder.periods,
    })
    setShowModal(true)
  }

  const handleDelete = (id: number) => {
    if (window.confirm('Are you sure you want to delete this day order?')) {
      setDayOrders(dayOrders.filter((d) => d.id !== id))
    }
  }

  const resetForm = () => {
    setFormData({
      dayNumber: 1,
      periods: PERIODS.map((p) => ({
        periodNumber: p.num,
        staffId: '',
        staffName: '',
      })),
    })
    setError(null)
  }

  const handleCloseModal = () => {
    setShowModal(false)
    setEditingDayOrder(null)
    resetForm()
  }

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn">
        {/* Header */}
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold text-slate-900">Day Order Schedule</h1>
            <p className="text-slate-600 mt-1">Create and manage daily staff schedules for each period</p>
          </div>
          {canCreate && (
            <Button
              onClick={() => {
                resetForm()
                setShowModal(true)
              }}
              size="lg"
            >
              <Plus className="w-5 h-5" />
              Add Day Order
            </Button>
          )}
        </div>

        {/* Error Alert */}
        {error && (
          <Alert
            type="error"
            title="Error"
            message={error}
            onClose={() => setError(null)}
          />
        )}

        {/* Content */}
        {loading ? (
          <Card className="p-12 text-center">
            <div className="inline-block w-12 h-12 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
            <p className="mt-4 text-slate-600">Loading day orders...</p>
          </Card>
        ) : dayOrders.length === 0 ? (
          <Card className="p-12 text-center">
            <AlertCircle className="w-12 h-12 text-slate-400 mx-auto mb-4" />
            <p className="text-slate-600">No day orders created yet</p>
            {canCreate && (
              <Button onClick={() => setShowModal(true)} className="mt-4">
                Create First Day Order
              </Button>
            )}
          </Card>
        ) : (
          <div className="grid gap-6">
            {dayOrders.map((dayOrder) => (
              <Card key={dayOrder.id} className="overflow-hidden">
                <div className="bg-gradient-to-r from-blue-500 to-purple-500 text-white p-4 flex justify-between items-center">
                  <h3 className="text-lg font-semibold">{dayOrder.dayName}</h3>
                  {canCreate && (
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleEdit(dayOrder)}
                        className="p-2 hover:bg-blue-600 rounded-lg transition-colors"
                        title="Edit"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(dayOrder.id)}
                        className="p-2 hover:bg-red-600 rounded-lg transition-colors"
                        title="Delete"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  )}
                </div>

                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead className="bg-slate-100 border-b border-slate-200">
                      <tr>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">Period</th>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">Time</th>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">Staff ID</th>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">Staff Name</th>
                      </tr>
                    </thead>
                    <tbody>
                      {dayOrder.periods.map((period, idx) => (
                        <tr
                          key={period.periodNumber}
                          className={`border-b border-slate-200 ${idx % 2 === 0 ? 'bg-white' : 'bg-slate-50'}`}
                        >
                          <td className="px-6 py-4 text-sm font-medium text-slate-900">Period {period.periodNumber}</td>
                          <td className="px-6 py-4 text-sm text-slate-600">
                            {PERIODS.find((p) => p.num === period.periodNumber)?.time}
                          </td>
                          <td className="px-6 py-4 text-sm text-slate-600">{period.staffId || '—'}</td>
                          <td className="px-6 py-4 text-sm text-slate-600">{period.staffName || '—'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 overflow-y-auto">
          <Card className="w-full max-w-2xl p-6 my-8">
            <h2 className="text-2xl font-bold text-slate-900 mb-6">
              {editingDayOrder ? 'Edit Day Order' : 'Create Day Order'}
            </h2>

            <form onSubmit={handleSubmit} className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">Select Day</label>
                <select
                  value={formData.dayNumber}
                  onChange={(e) => handleDayChange(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  {DAYS.map((day) => (
                    <option key={day.num} value={day.num}>
                      {day.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="space-y-4">
                <h3 className="font-semibold text-slate-900">Period Assignments</h3>
                {formData.periods.map((period, idx) => (
                  <div key={period.periodNumber} className="border border-slate-200 rounded-lg p-4">
                    <div className="grid grid-cols-3 gap-4">
                      <div>
                        <label className="block text-xs font-medium text-slate-600 mb-1">Period</label>
                        <input
                          type="text"
                          value={`Period ${period.periodNumber}`}
                          disabled
                          className="w-full px-3 py-2 bg-slate-100 border border-slate-300 rounded-lg text-slate-600"
                        />
                      </div>
                      <div>
                        <label className="block text-xs font-medium text-slate-600 mb-1">Time</label>
                        <input
                          type="text"
                          value={PERIODS.find((p) => p.num === period.periodNumber)?.time || ''}
                          disabled
                          className="w-full px-3 py-2 bg-slate-100 border border-slate-300 rounded-lg text-slate-600"
                        />
                      </div>
                      <div>
                        <label className="block text-xs font-medium text-slate-600 mb-1">Select Staff</label>
                        <select
                          value={period.staffId}
                          onChange={(e) => {
                            const staff = staffList.find((s) => s.staffId === e.target.value)
                            handleInputChange(idx, 'staffId', e.target.value)
                            handleInputChange(idx, 'staffName', staff ? `${staff.firstName} ${staff.lastName}` : '')
                          }}
                          className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        >
                          <option value="">Select Staff</option>
                          {staffList.map((staff) => (
                            <option key={staff.staffId} value={staff.staffId}>
                              {staff.firstName} {staff.lastName} ({staff.staffId})
                            </option>
                          ))}
                        </select>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <div className="flex gap-3 pt-4">
                <Button
                  type="button"
                  variant="secondary"
                  onClick={handleCloseModal}
                  className="flex-1"
                >
                  Cancel
                </Button>
                <Button type="submit" className="flex-1">
                  {editingDayOrder ? 'Update' : 'Create'}
                </Button>
              </div>
            </form>
          </Card>
        </div>
      )}
    </Layout>
  )
}
