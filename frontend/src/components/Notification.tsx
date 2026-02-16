import React, { useState, useEffect } from 'react'
import { AlertCircle, CheckCircle, Clock, X, Bell } from 'lucide-react'

interface NotificationProps {
  type?: 'info' | 'success' | 'error' | 'warning'
  title: string
  message: string
  duration?: number
  onClose?: () => void
}

export const Notification: React.FC<NotificationProps> = ({
  type = 'info',
  title,
  message,
  duration = 5000,
  onClose,
}) => {
  const [isVisible, setIsVisible] = useState(true)

  useEffect(() => {
    if (duration > 0) {
      const timer = setTimeout(() => {
        setIsVisible(false)
        onClose?.()
      }, duration)

      return () => clearTimeout(timer)
    }
    return undefined
  }, [duration, onClose])

  if (!isVisible) return null

  const getStyles = (): string => {
    switch (type) {
      case 'success':
        return 'bg-green-50 border-l-4 border-green-500 text-green-800'
      case 'error':
        return 'bg-red-50 border-l-4 border-red-500 text-red-800'
      case 'warning':
        return 'bg-yellow-50 border-l-4 border-yellow-500 text-yellow-800'
      default:
        return 'bg-blue-50 border-l-4 border-blue-500 text-blue-800'
    }
  }

  const getIcon = (): React.ReactNode => {
    switch (type) {
      case 'success':
        return <CheckCircle className="w-5 h-5" />
      case 'error':
        return <AlertCircle className="w-5 h-5" />
      case 'warning':
        return <AlertCircle className="w-5 h-5" />
      default:
        return <Bell className="w-5 h-5" />
    }
  }

  return (
    <div className={`p-4 rounded-lg shadow-lg ${getStyles()}`}>
      <div className="flex items-start gap-3">
        {getIcon()}
        <div className="flex-1">
          <h4 className="font-semibold">{title}</h4>
          <p className="text-sm mt-1">{message}</p>
        </div>
        <button
          onClick={() => {
            setIsVisible(false)
            onClose?.()
          }}
          className="text-gray-400 hover:text-gray-600 flex-shrink-0"
        >
          <X className="w-4 h-4" />
        </button>
      </div>
    </div>
  )
}

export default Notification
