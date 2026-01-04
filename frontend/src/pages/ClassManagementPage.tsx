import React, { useState, useEffect } from 'react'
import { Plus, Edit2, Trash2, AlertCircle } from 'lucide-react'
import { Layout } from '../components/Layout'
import { Button, Card, Alert } from '../components/common'
import { useAuthStore } from '../store/authStore'

interface ClassRoom {
  id: number
  classCode: string
  className: string
  departmentCode: string
  createdAt: string
}

export const ClassManagementPage: React.FC = () => {
  const user = useAuthStore((state) => state.user)
  const accessToken = useAuthStore((state) => state.accessToken)
  const [classes, setClasses] = useState<ClassRoom[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showModal, setShowModal] = useState(false)
  const [editingClass, setEditingClass] = useState<ClassRoom | null>(null)
  const [formData, setFormData] = useState({
    classCode: '',
    className: '',
    departmentCode: '',
  })

  const canCreate = user?.roles.includes('ADMIN') || user?.roles.includes('HOD')

  useEffect(() => {
    loadClasses()
  }, [accessToken])

  const loadClasses = async () => {
    if (!accessToken) {
      setLoading(false)
      return
    }

    try {
      setLoading(true)
      const response = await fetch('http://localhost:8080/api/classroom', {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      })

      if (response.ok) {
        const data = await response.json()
        setClasses(data.data || [])
      } else {
        setError('Failed to load classes')
      }
    } catch (err) {
      console.error(err)
      setError('Error loading classes')
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

    if (!formData.classCode || !formData.className || !formData.departmentCode) {
      setError('Please fill in all required fields')
      return
    }

    try {
      if (editingClass) {
        // Update existing class
        const response = await fetch(`http://localhost:8080/api/classroom/${editingClass.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${accessToken}`,
          },
          body: JSON.stringify({
            classCode: formData.classCode,
            className: formData.className,
            departmentCode: formData.departmentCode,
          }),
        })

        if (response.ok) {
          loadClasses()
          setShowModal(false)
          setEditingClass(null)
          resetForm()
        } else {
          setError('Failed to update class')
        }
      } else {
        // Create new class
        const response = await fetch('http://localhost:8080/api/classroom', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${accessToken}`,
          },
          body: JSON.stringify({
            classCode: formData.classCode,
            className: formData.className,
            departmentCode: formData.departmentCode,
          }),
        })

        if (response.ok) {
          loadClasses()
          setShowModal(false)
          resetForm()
        } else {
          const errorData = await response.json()
          setError(errorData.message || 'Failed to create class')
        }
      }
    } catch (err) {
      setError('Error saving class')
      console.error(err)
    }
  }

  const handleEdit = (classRoom: ClassRoom) => {
    setEditingClass(classRoom)
    setFormData({
      classCode: classRoom.classCode,
      className: classRoom.className,
      departmentCode: classRoom.departmentCode,
    })
    setShowModal(true)
  }

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this class?')) {
      try {
        const response = await fetch(`http://localhost:8080/api/classroom/${id}`, {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        })

        if (response.ok) {
          loadClasses()
        } else {
          setError('Failed to delete class')
        }
      } catch (err) {
        setError('Error deleting class')
        console.error(err)
      }
    }
  }

  const resetForm = () => {
    setFormData({
      classCode: '',
      className: '',
      departmentCode: '',
    })
    setError(null)
  }

  const handleCloseModal = () => {
    setShowModal(false)
    setEditingClass(null)
    resetForm()
  }

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn">
        {/* Header */}
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold text-slate-900">Class Management</h1>
            <p className="text-slate-600 mt-1">Create, edit, and manage classes</p>
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
              Add Class
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
            <p className="mt-4 text-slate-600">Loading classes...</p>
          </Card>
        ) : classes.length === 0 ? (
          <Card className="p-12 text-center">
            <AlertCircle className="w-12 h-12 text-slate-400 mx-auto mb-4" />
            <p className="text-slate-600">No classes found</p>
            {canCreate && (
              <Button onClick={() => setShowModal(true)} className="mt-4">
                Add First Class
              </Button>
            )}
          </Card>
        ) : (
          <Card className="overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gradient-to-r from-blue-500 to-purple-500 text-white">
                  <tr>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Class Code</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Class Name</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Department</th>
                    {canCreate && <th className="px-6 py-3 text-center text-sm font-semibold">Actions</th>}
                  </tr>
                </thead>
                <tbody>
                  {classes.map((classRoom, idx) => (
                    <tr
                      key={classRoom.id}
                      className={`border-b border-slate-200 ${
                        idx % 2 === 0 ? 'bg-white' : 'bg-slate-50'
                      } hover:bg-blue-50 transition-colors`}
                    >
                      <td className="px-6 py-4 text-sm font-medium text-slate-900">{classRoom.classCode}</td>
                      <td className="px-6 py-4 text-sm text-slate-700">{classRoom.className}</td>
                      <td className="px-6 py-4 text-sm text-slate-600">{classRoom.departmentCode}</td>
                      {canCreate && (
                        <td className="px-6 py-4 text-center space-x-2">
                          <button
                            onClick={() => handleEdit(classRoom)}
                            className="text-blue-600 hover:text-blue-800 inline-block"
                            title="Edit"
                          >
                            <Edit2 className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDelete(classRoom.id)}
                            className="text-red-600 hover:text-red-800 inline-block"
                            title="Delete"
                          >
                            <Trash2 className="w-4 h-4" />
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
              {editingClass ? 'Edit Class' : 'Add New Class'}
            </h2>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Class Code *</label>
                <input
                  type="text"
                  name="classCode"
                  value={formData.classCode}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="e.g., CS1"
                  disabled={!!editingClass}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Class Name *</label>
                <input
                  type="text"
                  name="className"
                  value={formData.className}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="e.g., Computer Science - Semester 1"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Department Code *</label>
                <input
                  type="text"
                  name="departmentCode"
                  value={formData.departmentCode}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="e.g., CS"
                />
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
                  {editingClass ? 'Update' : 'Create'}
                </Button>
              </div>
            </form>
          </Card>
        </div>
      )}
    </Layout>
  )
}
