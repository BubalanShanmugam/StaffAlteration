import React, { useState, useMemo } from 'react'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { AlterationDTO } from '../api'

interface DateInfo {
  date: string
  type: 'absence' | 'substitution'
  count: number
}

interface AlterationCalendarProps {
  alterations: AlterationDTO[]
  dateInfoByType: { [key: string]: DateInfo }
  onDateSelect: (date: string | null) => void
  selectedDate: string | null
  viewMode: 'monthly' | 'weekly'
}

// Helper function to format date as YYYY-MM-DD without timezone conversion
const formatDateToString = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export const AlterationCalendar: React.FC<AlterationCalendarProps> = ({
  alterations,
  dateInfoByType,
  onDateSelect,
  selectedDate,
  viewMode,
}) => {
  const [currentYear, setCurrentYear] = useState(new Date().getFullYear())
  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth())
  const [currentWeekStart, setCurrentWeekStart] = useState(getWeekStart(new Date()))

  // Get unique dates from alterations for quick lookup
  const alterationDates = useMemo(
    () => new Set(alterations.map((alt) => alt.alterationDate)),
    [alterations]
  )

  // Count alterations per date for badge display
  const alterationCountByDate = useMemo(() => {
    const counts: { [key: string]: number } = {}
    alterations.forEach((alt) => {
      counts[alt.alterationDate] = (counts[alt.alterationDate] || 0) + 1
    })
    return counts
  }, [alterations])

  function getWeekStart(date: Date): Date {
    const d = new Date(date)
    const day = d.getDay()
    const diff = d.getDate() - day + (day === 0 ? -6 : 1) // adjust when day is Sunday
    return new Date(d.setDate(diff))
  }

  const handlePrevMonth = () => {
    if (currentMonth === 0) {
      setCurrentYear(currentYear - 1)
      setCurrentMonth(11)
    } else {
      setCurrentMonth(currentMonth - 1)
    }
  }

  const handleNextMonth = () => {
    if (currentMonth === 11) {
      setCurrentYear(currentYear + 1)
      setCurrentMonth(0)
    } else {
      setCurrentMonth(currentMonth + 1)
    }
  }

  const handlePrevWeek = () => {
    const newStart = new Date(currentWeekStart)
    newStart.setDate(newStart.getDate() - 7)
    setCurrentWeekStart(newStart)
  }

  const handleNextWeek = () => {
    const newStart = new Date(currentWeekStart)
    newStart.setDate(newStart.getDate() + 7)
    setCurrentWeekStart(newStart)
  }

  // Monthly view rendering
  const renderMonthly = () => {
    const firstDay = new Date(currentYear, currentMonth, 1)
    const lastDay = new Date(currentYear, currentMonth + 1, 0)
    const prevLastDay = new Date(currentYear, currentMonth, 0)
    const nextFirstDay = new Date(currentYear, currentMonth + 1, 1)

    const daysInMonth = lastDay.getDate()
    const daysInPrevMonth = prevLastDay.getDate()
    const startingDayOfWeek = firstDay.getDay()

    const days: (number | null)[] = []

    // Previous month's days
    for (let i = startingDayOfWeek - 1; i >= 0; i--) {
      days.push(null) // Placeholder for previous month
    }

    // Current month's days
    for (let i = 1; i <= daysInMonth; i++) {
      days.push(i)
    }

    // Next month's days
    const remainingDays = 42 - days.length // 6 rows * 7 days
    for (let i = 1; i <= remainingDays; i++) {
      days.push(null) // Placeholder for next month
    }

    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between mb-6">
          <button
            onClick={handlePrevMonth}
            className="p-2 hover:bg-slate-100 rounded-lg transition-colors"
          >
            <ChevronLeft className="w-5 h-5" />
          </button>
          <h3 className="text-lg font-semibold text-slate-900">
            {new Date(currentYear, currentMonth).toLocaleDateString('en-US', {
              month: 'long',
              year: 'numeric',
            })}
          </h3>
          <button
            onClick={handleNextMonth}
            className="p-2 hover:bg-slate-100 rounded-lg transition-colors"
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        </div>

        {/* Day headers */}
        <div className="grid grid-cols-7 gap-1 mb-2">
          {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((day) => (
            <div key={day} className="text-center font-semibold text-slate-600 text-sm p-2">
              {day}
            </div>
          ))}
        </div>

        {/* Calendar days */}
        <div className="grid grid-cols-7 gap-1">
          {days.map((day, index) => {
            // Only show alterations for actual days in current month, not placeholder days
            const dateStr = day
              ? formatDateToString(new Date(currentYear, currentMonth, day))
              : null
            const isSelected = dateStr === selectedDate
            const dateInfo = day !== null ? dateInfoByType[dateStr] : null
            const hasAlterations = dateInfo !== undefined && day !== null
            const count = dateInfo?.count || 0
            const type = dateInfo?.type

            return (
              <button
                key={index}
                onClick={() => onDateSelect(isSelected ? null : dateStr)}
                className={`
                  aspect-square p-2 rounded-lg text-sm font-medium transition-colors
                  ${day === null ? 'bg-slate-50 text-slate-300 cursor-default' : ''}
                  ${
                    day !== null && isSelected
                      ? 'bg-blue-600 text-white ring-2 ring-blue-400'
                      : day !== null && type === 'absence'
                        ? 'bg-red-50 border-2 border-red-400 text-slate-900 hover:bg-red-100'
                        : day !== null && type === 'substitution'
                          ? 'bg-yellow-50 border-2 border-yellow-400 text-slate-900 hover:bg-yellow-100'
                          : day !== null
                            ? 'bg-white text-slate-900 border border-slate-200 hover:bg-slate-50'
                            : ''
                  }
                `}
                disabled={day === null}
              >
                <div className="text-center">
                  {day}
                  {hasAlterations && (
                    <div className="text-xs mt-1">
                      <span className={`inline-block px-1.5 py-0.5 rounded font-bold ${
                        type === 'absence' ? 'bg-red-500 text-white' : 'bg-yellow-600 text-white'
                      }`}>
                        {count}
                      </span>
                    </div>
                  )}
                </div>
              </button>
            )
          })}
        </div>
      </div>
    )
  }

  // Weekly view rendering
  const renderWeekly = () => {
    const weekDays: Date[] = []
    for (let i = 0; i < 7; i++) {
      const d = new Date(currentWeekStart)
      d.setDate(d.getDate() + i)
      weekDays.push(d)
    }

    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between mb-6">
          <button
            onClick={handlePrevWeek}
            className="p-2 hover:bg-slate-100 rounded-lg transition-colors"
          >
            <ChevronLeft className="w-5 h-5" />
          </button>
          <h3 className="text-lg font-semibold text-slate-900">
            {weekDays[0].toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} -{' '}
            {weekDays[6].toLocaleDateString('en-US', {
              month: 'short',
              day: 'numeric',
              year: 'numeric',
            })}
          </h3>
          <button
            onClick={handleNextWeek}
            className="p-2 hover:bg-slate-100 rounded-lg transition-colors"
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        </div>

        <div className="grid grid-cols-7 gap-2">
          {weekDays.map((date, index) => {
            const dateStr = formatDateToString(date)
            const isSelected = dateStr === selectedDate
            const dateInfo = dateInfoByType[dateStr]
            const hasAlterations = dateInfo !== undefined
            const count = dateInfo?.count || 0
            const type = dateInfo?.type
            const dayName = date.toLocaleDateString('en-US', { weekday: 'short' })

            return (
              <button
                key={index}
                onClick={() => onDateSelect(isSelected ? null : dateStr)}
                className={`
                  p-4 rounded-lg text-center transition-colors
                  ${
                    isSelected
                      ? 'bg-blue-600 text-white ring-2 ring-blue-400'
                      : type === 'absence'
                        ? 'bg-red-50 border-2 border-red-400 text-slate-900 hover:bg-red-100'
                        : type === 'substitution'
                          ? 'bg-yellow-50 border-2 border-yellow-400 text-slate-900 hover:bg-yellow-100'
                          : 'bg-white border border-slate-200 text-slate-900 hover:bg-slate-50'
                  }
                `}
              >
                <div className="font-semibold text-sm">{dayName}</div>
                <div className="text-lg font-bold mt-1">
                  {date.getDate()}
                </div>
                {hasAlterations && (
                  <div className="mt-2">
                    <span className={`inline-block px-2 py-1 text-xs rounded font-bold ${
                      type === 'absence' ? 'bg-red-500 text-white' : 'bg-yellow-600 text-white'
                    }`}>
                      {count} alter.
                    </span>
                  </div>
                )}
              </button>
            )
          })}
        </div>
      </div>
    )
  }

  return <div className="bg-white rounded-lg p-6 border border-slate-200">
    {viewMode === 'monthly' ? renderMonthly() : renderWeekly()}
  </div>
}
