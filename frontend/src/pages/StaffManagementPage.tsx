import React, { useState, useEffect } from 'react'
import { Plus, Edit2, Trash2, AlertCircle } from 'lucide-react'
import { Layout } from '../components/Layout'
import { Button, Card, Alert } from '../components/common'
import { useAuthStore } from '../store/authStore'
import * as api from '../api'

interface StaffMember {
  id: number
  staffId: string
  firstName: string
  lastName: string
  email: string
  phoneNumber: string
  departmentCode: string
  status: string
  createdAt: string
}

export const StaffManagementPage: React.FC = () => {
  const user = useAuthStore((state) => state.user)
  const accessToken = useAuthStore((state) => state.accessToken)
  const [staffMembers, setStaffMembers] = useState<StaffMember[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showModal, setShowModal] = useState(false)
  const [editingStaff, setEditingStaff] = useState<StaffMember | null>(null)
  const [formData, setFormData] = useState({
    staffId: '',
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    departmentCode: '',
    password: '',
  })

  const canCreate = user?.roles.includes('ADMIN') || user?.roles.includes('HOD')
  const canView = true // All users can view staff directory

  useEffect(() => {
    loadStaffMembers()
  }, [accessToken])

  const loadStaffMembers = async () => {
    if (!accessToken) {
      setLoading(false)
      return
    }

    try {
      setLoading(true)
      const response = await fetch('http://localhost:8080/staff', {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      })

      if (response.ok) {
        const data = await response.json()
        setStaffMembers(data.data || [])
      } else {
        setError('Failed to load staff members')
      }
    } catch (err) {
      setError('Error loading staff members')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!formData.staffId || !formData.firstName || !formData.lastName || !formData.email) {
      setError('Please fill in all required fields')
      return
    }

    if (!editingStaff && !formData.password) {
      setError('Password is required for new staff')
      return
    }

    try {
      if (editingStaff) {
        // Update existing staff
        const response = await fetch(`http://localhost:8080/staff/${editingStaff.staffId}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${accessToken}`,
          },
          body: JSON.stringify({
            staffId: formData.staffId,
            firstName: formData.firstName,
            lastName: formData.lastName,
            email: formData.email,
            phoneNumber: formData.phoneNumber,
            departmentCode: formData.departmentCode,
          }),
        })

        if (response.ok) {
          loadStaffMembers()
          setShowModal(false)
          setEditingStaff(null)
          resetForm()
        } else {
          setError('Failed to update staff')
        }
      } else {
        // Create new staff
        const response = await fetch(`http://localhost:8080/staff/create?password=${formData.password}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${accessToken}`,
          },
          body: JSON.stringify({
            staffId: formData.staffId,
            firstName: formData.firstName,
            lastName: formData.lastName,
            email: formData.email,
            phoneNumber: formData.phoneNumber,
            departmentCode: formData.departmentCode,
          }),
        })

        if (response.ok) {
          loadStaffMembers()
          setShowModal(false)
          resetForm()
        } else {
          const errorData = await response.json()
          setError(errorData.message || 'Failed to create staff')
        }
      }
    } catch (err) {
      setError('Error saving staff member')
      console.error(err)
    }
  }

  const handleEdit = (staff: StaffMember) => {
    setEditingStaff(staff)
    setFormData({
      staffId: staff.staffId,
      firstName: staff.firstName,
      lastName: staff.lastName,
      email: staff.email,
      phoneNumber: staff.phoneNumber,
      departmentCode: staff.departmentCode,
      password: '',
    })
    setShowModal(true)
  }

  const resetForm = () => {
    setFormData({
      staffId: '',
      firstName: '',
      lastName: '',
      email: '',
      phoneNumber: '',
      departmentCode: '',
      password: '',
    })
    setError(null)
  }

  const handleCloseModal = () => {
    setShowModal(false)
    setEditingStaff(null)
    resetForm()
  }

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn">
        {/* Header */}
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold text-slate-900">Staff Management</h1>
            <p className="text-slate-600 mt-1">Manage staff members and their details</p>
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
              Add Staff
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
            <p className="mt-4 text-slate-600">Loading staff members...</p>
          </Card>
        ) : staffMembers.length === 0 ? (
          <Card className="p-12 text-center">
            <AlertCircle className="w-12 h-12 text-slate-400 mx-auto mb-4" />
            <p className="text-slate-600">No staff members found</p>
            {canCreate && (
              <Button onClick={() => setShowModal(true)} className="mt-4">
                Add First Staff Member
              </Button>
            )}
          </Card>
        ) : (
          <Card className="overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gradient-to-r from-blue-500 to-purple-500 text-white">
                  <tr>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Staff ID</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Name</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Email</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Phone</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Department</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Status</th>
                    {canCreate && <th className="px-6 py-3 text-center text-sm font-semibold">Actions</th>}
                  </tr>
                </thead>
                <tbody>
                  {staffMembers.map((staff, idx) => (
                    <tr
                      key={staff.id}
                      className={`border-b border-slate-200 ${
                        idx % 2 === 0 ? 'bg-white' : 'bg-slate-50'
                      } hover:bg-blue-50 transition-colors`}
                    >
                      <td className="px-6 py-4 text-sm font-medium text-slate-900">{staff.staffId}</td>
                      <td className="px-6 py-4 text-sm text-slate-700">
                        {staff.firstName} {staff.lastName}
                      </td>
                      <td className="px-6 py-4 text-sm text-slate-600">{staff.email}</td>
                      <td className="px-6 py-4 text-sm text-slate-600">{staff.phoneNumber || '—'}</td>
                      <td className="px-6 py-4 text-sm text-slate-600">{staff.departmentCode}</td>
                      <td className="px-6 py-4 text-sm">
                        <span
                          className={`px-3 py-1 rounded-full text-xs font-medium ${
                            staff.status === 'ACTIVE'
                              ? 'bg-green-100 text-green-800'
                              : 'bg-gray-100 text-gray-800'
                          }`}
                        >
                          {staff.status}
                        </span>
                      </td>
                      {canCreate && (
                        <td className="px-6 py-4 text-center">
                          <button
                            onClick={() => handleEdit(staff)}
                            className="text-blue-600 hover:text-blue-800 inline-block mr-3"
                            title="Edit"
                          >
                            <Edit2 className="w-4 h-4" />
                          </button>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
        )}
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <Card className="w-full max-w-md p-6">
            <h2 className="text-2xl font-bold text-slate-900 mb-6">
              {editingStaff ? 'Edit Staff Member' : 'Add New Staff Member'}
            </h2>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Staff ID *</label>
                <input
                  type="text"
                  name="staffId"
                  value={formData.staffId}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="e.g., STF001"
                  disabled={!!editingStaff}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">First Name *</label>
                  <input
                    type="text"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="First name"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Last Name *</label>
                  <input
                    type="text"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Last name"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Email *</label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="email@example.com"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Phone Number</label>
                <input
                  type="tel"
                  name="phoneNumber"
                  value={formData.phoneNumber}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="+91 1234567890"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Department Code</label>
                <input
                  type="text"
                  name="departmentCode"
                  value={formData.departmentCode}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="e.g., CS"
                />
              </div>

              {!editingStaff && (
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Password *</label>
                  <input
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Enter password"
                  />
                </div>
              )}

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
                  {editingStaff ? 'Update' : 'Create'}
                </Button>
              </div>
            </form>
          </Card>
        </div>
      )}
    </Layout>
  )
}
