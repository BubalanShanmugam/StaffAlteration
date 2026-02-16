import React from 'react'
import { useNotificationStore } from '../store/notificationStore'
import Notification from './Notification'

export const NotificationCenter: React.FC = () => {
  const notifications = useNotificationStore((state) => state.notifications)
  const removeNotification = useNotificationStore((state) => state.removeNotification)

  return (
    <div className="fixed top-4 right-4 z-50 space-y-3 max-w-md">
      {notifications.map((notification) => (
        <Notification
          key={notification.id}
          type={notification.type}
          title={notification.title}
          message={notification.message}
          duration={notification.duration || 5000}
          onClose={() => removeNotification(notification.id)}
        />
      ))}
    </div>
  )
}

export default NotificationCenter
