import React, { useState } from 'react'
import { Trash2, Edit2, RefreshCw } from 'lucide-react'
import { TimetableTemplate } from '../api'
import { timetableAPI } from '../api'
import { Button } from './common'

interface TimetableTableProps {
  timetables: TimetableTemplate[]
  onRefresh: () => void
  onEdit?: (timetable: TimetableTemplate) => void
}

export const TimetableTable: React.FC<TimetableTableProps> = ({ timetables, onRefresh, onEdit }) => {
  const [deleting, setDeleting] = useState<number | null>(null)

  const handleDelete = async (id: number) => {
    if (confirm('Are you sure you want to delete this timetable?')) {
      try {
        setDeleting(id)
        await timetableAPI.delete(id)
        onRefresh()
      } catch (err) {
        alert('Failed to delete timetable')
      } finally {
        setDeleting(null)
      }
    }
  }

  const getStatusColor = (status: string) => {
    const colors = {
      ACTIVE: 'bg-green-100 text-green-800',
      DRAFT: 'bg-yellow-100 text-yellow-800',
      INACTIVE: 'bg-gray-100 text-gray-800',
      ARCHIVED: 'bg-red-100 text-red-800',
    }
    return colors[status as keyof typeof colors] || 'bg-slate-100 text-slate-800'
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button
          variant="secondary"
          size="sm"
          onClick={onRefresh}
          className="gap-2"
        >
          <RefreshCw className="w-4 h-4" />
          Refresh
        </Button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-200 bg-slate-50">
              <th className="px-4 py-3 text-left font-semibold text-slate-900">Subject</th>
              <th className="px-4 py-3 text-left font-semibold text-slate-900">Staff</th>
              <th className="px-4 py-3 text-left font-semibold text-slate-900">Day</th>
              <th className="px-4 py-3 text-left font-semibold text-slate-900">Period</th>
              <th className="px-4 py-3 text-left font-semibold text-slate-900">Status</th>
              <th className="px-4 py-3 text-center font-semibold text-slate-900">Actions</th>
            </tr>
          </thead>
          <tbody>
            {timetables.map((timetable) => (
              <tr key={timetable.id} className="border-b border-slate-100 hover:bg-slate-50 transition-colors">
                <td className="px-4 py-3">
                  <div>
                    <p className="font-medium text-slate-900">{timetable.subjectName}</p>
                    <p className="text-xs text-slate-500">{timetable.subjectCode}</p>
                  </div>
                </td>
                <td className="px-4 py-3">
                  <p className="font-medium text-slate-900">
                    {timetable.staffName}
                  </p>
                  <p className="text-xs text-slate-500">{timetable.staffId}</p>
                </td>
                <td className="px-4 py-3">
                  <p className="font-medium text-slate-900">Day {timetable.dayOrder}</p>
                </td>
                <td className="px-4 py-3">
                  <p className="font-medium text-slate-900">Period {timetable.periodNumber}</p>
                </td>
                <td className="px-4 py-3">
                  <span className={`inline-block px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(timetable.status)}`}>
                    {timetable.status}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <div className="flex gap-2 justify-center">
                    <button
                      onClick={() => onEdit?.(timetable)}
                      className="p-2 hover:bg-blue-100 rounded-lg text-blue-600 transition-colors"
                      title="Edit"
                    >
                      <Edit2 className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handleDelete(timetable.id)}
                      disabled={deleting === timetable.id}
                      className="p-2 hover:bg-red-100 rounded-lg text-red-600 transition-colors disabled:opacity-50"
                      title="Delete"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
