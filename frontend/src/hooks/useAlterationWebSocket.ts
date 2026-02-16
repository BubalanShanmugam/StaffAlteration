import { useEffect } from 'react'
import webSocketService from '../services/webSocketService'

export const useAlterationWebSocket = (
  onAlterationCreated?: (data: any) => void,
  onAlterationUpdated?: (data: any) => void,
  onAlterationRejected?: (data: any) => void,
  departmentId?: number,
  onDepartmentEvent?: (data: any) => void
) => {
  useEffect(() => {
    // Connect to WebSocket if not already connected
    if (!webSocketService.isConnected()) {
      webSocketService.connect()
        .catch(error => console.error('Failed to connect WebSocket:', error))
    }

    // Set up event listeners
    if (onAlterationCreated) {
      webSocketService.on('alteration_created', onAlterationCreated)
    }

    if (onAlterationUpdated) {
      webSocketService.on('alteration_updated', onAlterationUpdated)
    }

    if (onAlterationRejected) {
      webSocketService.on('alteration_rejected', onAlterationRejected)
    }

    // Subscribe to department-specific events if departmentId is provided
    if (departmentId && onDepartmentEvent) {
      webSocketService.subscribeToDepartment(departmentId, onDepartmentEvent)
    }

    // Cleanup
    return () => {
      if (onAlterationCreated) {
        webSocketService.off('alteration_created', onAlterationCreated)
      }
      if (onAlterationUpdated) {
        webSocketService.off('alteration_updated', onAlterationUpdated)
      }
      if (onAlterationRejected) {
        webSocketService.off('alteration_rejected', onAlterationRejected)
      }
    }
  }, [onAlterationCreated, onAlterationUpdated, onAlterationRejected, departmentId, onDepartmentEvent])
}

export default useAlterationWebSocket
