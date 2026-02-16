import { Client, IMessage } from '@stomp/stompjs'

class WebSocketService {
  private client: Client | null = null
  private wsUrl = 'ws://localhost:8080/ws'
  private listeners: { [key: string]: Function[] } = {}

  public connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        this.client = new Client({
          brokerURL: this.wsUrl,
          onConnect: () => {
            console.log('WebSocket connected')
            this.subscribeToTopics()
            resolve()
          },
          onDisconnect: () => {
            console.log('WebSocket disconnected')
          },
          onStompError: (error) => {
            console.error('STOMP error:', error)
            reject(error)
          },
          onWebSocketError: (error) => {
            console.error('WebSocket error:', error)
            reject(error)
          },
        })

        this.client.activate()
      } catch (error) {
        console.error('Failed to create WebSocket connection:', error)
        reject(error)
      }
    })
  }

  private subscribeToTopics() {
    if (!this.client) return

    // Subscribe to global alteration events
    this.client.subscribe('/topic/alterations/created', (message: IMessage) => {
      const data = JSON.parse(message.body)
      this.emit('alteration_created', data)
    })

    this.client.subscribe('/topic/alterations/updated', (message: IMessage) => {
      const data = JSON.parse(message.body)
      this.emit('alteration_updated', data)
    })

    this.client.subscribe('/topic/alterations/rejected', (message: IMessage) => {
      const data = JSON.parse(message.body)
      this.emit('alteration_rejected', data)
    })
  }

  public subscribeToDepartment(departmentId: number, callback: (data: any) => void) {
    if (!this.client) return

    this.client.subscribe(`/topic/department/${departmentId}/alterations`, (message: IMessage) => {
      const data = JSON.parse(message.body)
      callback(data)
    })
  }

  public on(event: string, callback: Function) {
    if (!this.listeners[event]) {
      this.listeners[event] = []
    }
    this.listeners[event].push(callback)
  }

  public off(event: string, callback: Function) {
    if (this.listeners[event]) {
      this.listeners[event] = this.listeners[event].filter((cb) => cb !== callback)
    }
  }

  private emit(event: string, data: any) {
    if (this.listeners[event]) {
      this.listeners[event].forEach((callback) => callback(data))
    }
  }

  public disconnect() {
    if (this.client && this.client.active) {
      this.client.deactivate()
    }
  }

  public isConnected(): boolean {
    return this.client ? this.client.active : false
  }
}

export default new WebSocketService()
