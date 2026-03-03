import { Client, IMessage } from '@stomp/stompjs'

class WebSocketService {
  private client: Client | null = null
  private listeners: { [key: string]: Function[] } = {}
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectDelay = 3000
  private isManuallyDisconnected = false
  private healthCheckInterval: ReturnType<typeof setInterval> | null = null

  private getWsUrl(): string {
    // Use same origin in dev (goes through Vite proxy) or direct backend URL
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    return `${protocol}//${host}/api/ws`
  }

  public connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        this.isManuallyDisconnected = false
        const wsUrl = this.getWsUrl()
        console.log('[WebSocket] Connecting to:', wsUrl)
        
        this.client = new Client({
          brokerURL: wsUrl,
          reconnectDelay: this.reconnectDelay,
          heartbeatIncoming: 10000,
          heartbeatOutgoing: 10000,
          onConnect: () => {
            console.log('[WebSocket] ✅ Connected successfully')
            this.reconnectAttempts = 0
            this.subscribeToTopics()
            this.startHealthCheck()
            resolve()
          },
          onDisconnect: () => {
            console.log('[WebSocket] ⚠️ Disconnected')
            if (!this.isManuallyDisconnected && this.reconnectAttempts < this.maxReconnectAttempts) {
              this.scheduleReconnect()
            }
          },
          onStompError: (frame: any) => {
            console.error('[WebSocket] STOMP error:', frame)
            if (this.reconnectAttempts === 0) {
              reject(new Error('STOMP connection failed'))
            }
          },
          onWebSocketError: (error: any) => {
            console.error('[WebSocket] WebSocket error:', error)
            if (this.reconnectAttempts === 0) {
              reject(new Error('WebSocket connection failed'))
            }
          },
          // Enable debugging to see what's happening
          debug: (msg: string) => {
            if (msg.includes('ERROR')) {
              console.error('[WebSocket Debug]', msg)
            }
          },
        })

        this.client.activate()
        
        // Set a timeout for initial connection
        setTimeout(() => {
          if (!this.client?.active && this.reconnectAttempts === 0) {
            reject(new Error('WebSocket connection timeout'))
          }
        }, 10000)
      } catch (error) {
        console.error('[WebSocket] Failed to create WebSocket connection:', error)
        reject(error)
      }
    })
  }

  private scheduleReconnect() {
    this.reconnectAttempts++
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1) // Exponential backoff
    console.log(`[WebSocket] Reconnecting attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts} in ${delay}ms`)
    
    setTimeout(() => {
      if (!this.isManuallyDisconnected) {
        this.connect().catch((err) => {
          console.error('[WebSocket] Reconnection failed:', err)
        })
      }
    }, delay)
  }

  private startHealthCheck() {
    // Clear any existing health check
    if (this.healthCheckInterval) {
      clearInterval(this.healthCheckInterval)
    }

    // Check connection every 30 seconds
    this.healthCheckInterval = setInterval(() => {
      if (!this.isConnected()) {
        console.warn('[WebSocket] Health check failed - connection lost')
        if (!this.isManuallyDisconnected && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.scheduleReconnect()
        }
      } else {
        console.log('[WebSocket] ✅ Health check passed')
      }
    }, 30000)
  }

  private subscribeToTopics() {
    if (!this.client) return

    console.log('[WebSocket] Subscribing to topics')
    
    // Subscribe to global alteration events
    this.client.subscribe('/topic/alterations/created', (message: IMessage) => {
      try {
        const data = JSON.parse(message.body)
        this.emit('alteration_created', data)
      } catch (error) {
        console.error('[WebSocket] Error parsing alteration_created message:', error)
      }
    })

    this.client.subscribe('/topic/alterations/updated', (message: IMessage) => {
      try {
        const data = JSON.parse(message.body)
        this.emit('alteration_updated', data)
      } catch (error) {
        console.error('[WebSocket] Error parsing alteration_updated message:', error)
      }
    })

    this.client.subscribe('/topic/alterations/rejected', (message: IMessage) => {
      try {
        const data = JSON.parse(message.body)
        this.emit('alteration_rejected', data)
      } catch (error) {
        console.error('[WebSocket] Error parsing alteration_rejected message:', error)
      }
    })
  }

  public subscribeToDepartment(departmentId: number, callback: (data: any) => void) {
    if (!this.client) {
      console.warn('[WebSocket] Cannot subscribe - client not connected')
      return
    }

    console.log('[WebSocket] Subscribing to department topic:', departmentId)
    this.client.subscribe(`/topic/department/${departmentId}/alterations`, (message: IMessage) => {
      try {
        const data = JSON.parse(message.body)
        callback(data)
      } catch (error) {
        console.error('[WebSocket] Error parsing department message:', error)
      }
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
      this.listeners[event].forEach((callback) => {
        try {
          callback(data)
        } catch (error) {
          console.error(`[WebSocket] Error in ${event} callback:`, error)
        }
      })
    }
  }

  public disconnect() {
    console.log('[WebSocket] Manually disconnecting')
    this.isManuallyDisconnected = true
    if (this.healthCheckInterval) {
      clearInterval(this.healthCheckInterval)
    }
    if (this.client && this.client.active) {
      this.client.deactivate()
    }
  }

  public isConnected(): boolean {
    const connected = this.client ? this.client.active : false
    return connected
  }

  public getConnectionStatus(): {
    connected: boolean
    reconnectAttempts: number
    url: string
  } {
    return {
      connected: this.isConnected(),
      reconnectAttempts: this.reconnectAttempts,
      url: this.getWsUrl(),
    }
  }
}

export default new WebSocketService()

