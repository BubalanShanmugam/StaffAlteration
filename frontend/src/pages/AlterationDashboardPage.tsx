import React, { useState, useEffect } from 'react'
import { AlertCircle, CheckCircle, Clock, FileText, Download, Eye } from 'lucide-react'
import { Layout } from '../components/Layout'
import { Button, Card, Alert } from '../components/common'
import { alterationAPI } from '../api'
import { useAuthStore } from '../store/authStore'
import { useAlterationWebSocket } from '../hooks/useAlterationWebSocket'

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

export const AlterationDashboardPage: React.FC = () => {
  const user = useAuthStore((state) => state.user)
  const [alterations, setAlterations] = useState<Alteration[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState<'as-original' | 'as-substitute'>('as-original')
  const [showDetails, setShowDetails] = useState<number | null>(null)
  const [selectedDate, setSelectedDate] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)

  useEffect(() => {
    loadAlterations()
  }, [activeTab, user])

  // Set up WebSocket listeners for real-time updates
  useAlterationWebSocket(
    (data: Alteration) => {
      // New alteration created
      setAlterations((prev) => [data, ...prev])
    },
    (data: Alteration) => {
      // Alteration updated
      setAlterations((prev) =>
        prev.map((alt) => (alt.id === data.id ? data : alt))
      )
    },
    (data: Alteration) => {
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
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load alterations')
    } finally {
      setLoading(false)
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

  // Get unique dates from alterations and sort them
  const uniqueDates = Array.from(
    new Set(alterations.map((alt) => alt.alterationDate))
  ).sort()

  // Filter alterations by selected date
  const filteredAlterations = selectedDate
    ? alterations.filter((alt) => alt.alterationDate === selectedDate)
    : alterations

  const handleAcknowledge = async (alterationId: number) => {
    try {
      await alterationAPI.updateStatus(alterationId, 'ACKNOWLEDGED')
      loadAlterations()
    } catch (err) {
      console.error('Failed to acknowledge alteration')
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

  const getDayName = (dayOrder: number) => {
    const days = ['', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']
    return days[dayOrder] || `Day ${dayOrder}`
  }

  const getPeriodDisplay = (alteration: Alteration) => {
    // Map absence type to user-friendly display
    const absenceTypeMap: { [key: string]: string } = {
      'FN': 'Full Day Leave',
      'AN': 'Half Day Morning Leave (9AM - 1PM)',
      'AF': 'Half Day Afternoon Leave (1PM - 5PM)',
      'ONDUTY': 'On Duty - Full Day',
      'PERIOD_WISE_ABSENT': `Period ${alteration.periodNumber} Absent (${getPeriodTime(alteration.periodNumber)})`
    }
    
    return absenceTypeMap[alteration.absenceType] || `Period ${alteration.periodNumber} (${getPeriodTime(alteration.periodNumber)})`
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
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Staff Alterations</h1>
          <p className="text-slate-600 mt-1">View and manage your class alterations</p>
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

        {/* Date Filter Dropdown */}
        {uniqueDates.length > 0 && (
          <Card className="p-4">
            <div className="flex items-center justify-between gap-4">
              <label className="font-medium text-slate-700">Filter by Date:</label>
              <select
                value={selectedDate || ''}
                onChange={(e) => setSelectedDate(e.target.value || null)}
                className="px-4 py-2 rounded-lg border border-slate-300 text-slate-900 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Dates ({alterations.length} alterations)</option>
                {uniqueDates.map((date) => {
                  const count = alterations.filter((alt) => alt.alterationDate === date).length
                  return (
                    <option key={date} value={date}>
                      {new Date(date).toLocaleDateString('en-US', {
                        weekday: 'short',
                        month: 'short',
                        day: 'numeric',
                        year: 'numeric',
                      })}{' '}
                      ({count} alterations)
                    </option>
                  )
                })}
              </select>
            </div>
          </Card>
        )}

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
        ) : filteredAlterations.length === 0 ? (
          <Card className="p-12 text-center">
            <CheckCircle className="w-12 h-12 text-green-500 mx-auto mb-4" />
            <p className="text-slate-600 font-medium">
              No alterations for the selected date
            </p>
          </Card>
        ) : (
          <div className="space-y-4">
            {filteredAlterations.map((alteration) => (
              <Card
                key={alteration.id}
                className={`p-6 border-l-4 ${
                  alteration.status === 'ASSIGNED'
                    ? 'border-l-yellow-500'
                    : alteration.status === 'ACKNOWLEDGED'
                      ? 'border-l-blue-500'
                      : 'border-l-green-500'
                } hover:shadow-lg transition-shadow`}
              >
                <div className="space-y-4">
                  {/* Header */}
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-3 mb-2">
                        <h3 className="font-semibold text-slate-900 text-lg">
                          {alteration.subjectName}
                        </h3>
                        <span
                          className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(
                            alteration.status
                          )}`}
                        >
                          {getStatusIcon(alteration.status)}
                          {alteration.status}
                        </span>
                      </div>
                      <p className="text-sm text-slate-600">Class: {alteration.classCode}</p>
                    </div>
                    <button
                      onClick={() =>
                        setShowDetails(showDetails === alteration.id ? null : alteration.id)
                      }
                      className="p-2 hover:bg-slate-100 rounded-lg transition-colors"
                    >
                      <Eye className="w-5 h-5 text-slate-600" />
                    </button>
                  </div>

                  {/* Details Row */}
                  <div className="grid grid-cols-2 md:grid-cols-3 gap-4 text-sm">
                    <div>
                      <p className="text-slate-500 font-medium">Date</p>
                      <p className="text-slate-900">
                        {new Date(alteration.alterationDate).toLocaleDateString('en-US', {
                          weekday: 'short',
                          month: 'short',
                          day: 'numeric',
                        })}
                      </p>
                    </div>
                    <div>
                      <p className="text-slate-500 font-medium">Absence Type</p>
                      <p className="text-slate-900 font-medium">
                        <span className={`px-2 py-1 rounded ${
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

                  {/* Expanded Details */}
                  {showDetails === alteration.id && (
                    <div className="mt-4 pt-4 border-t border-slate-200 space-y-4">
                      {alteration.remarks && (
                        <div>
                          <p className="text-sm font-medium text-slate-700 mb-1">Notes</p>
                          <p className="text-sm text-slate-600 bg-slate-50 p-3 rounded">
                            {alteration.remarks}
                          </p>
                        </div>
                      )}

                      <div className="flex flex-wrap gap-3">
                        <Button
                          variant="secondary"
                          size="sm"
                          className="gap-2"
                          onClick={() => {
                            /* TODO: Download lesson plans */
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
                </div>
              </Card>
            ))}
          </div>
        )}

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
