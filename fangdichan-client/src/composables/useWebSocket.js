import { ref, onUnmounted } from 'vue'

export function useWebSocket(onMessage) {
  const isConnected = ref(false)
  let ws = null
  let reconnectTimer = null
  let reconnectAttempts = 0

  function connect() {
    const token = localStorage.getItem('token')
    if (!token) return
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    ws = new WebSocket(`${protocol}//${window.location.host}/ws?token=${token}`)

    ws.onopen = () => {
      isConnected.value = true
      reconnectAttempts = 0
    }

    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data)
        onMessage(msg)
      } catch {}
    }

    ws.onclose = () => {
      isConnected.value = false
      const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000)
      reconnectAttempts++
      reconnectTimer = setTimeout(connect, delay)
    }
  }

  function send(data) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(data))
    }
  }

  function disconnect() {
    if (ws) ws.close()
    if (reconnectTimer) clearTimeout(reconnectTimer)
  }

  onUnmounted(disconnect)

  return { isConnected, connect, send, disconnect }
}
