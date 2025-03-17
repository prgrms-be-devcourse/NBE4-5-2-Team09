import { create } from 'zustand';
import { Client, IMessage } from '@stomp/stompjs';

interface WebSocketState {
  client: Client | null;
  isConnected: boolean;
  connect: () => void;
  subscribe: (destination: string, callback: (message: IMessage) => void) => void;
  unsubscribe: (destination: string) => void;
  publish: (destination: string, body: string) => void;
}

export const useWebSocketStore = create<WebSocketState>((set, get) => ({
  client: null,
  isConnected: false,

  // 웹소켓 연결 함수
  connect: () => {
    if (get().client) return; // 이미 연결되어 있으면 중복 연결 방지

    const client = new Client({
      brokerURL: process.env.NEXT_PUBLIC_WEBSOCKET_URL ?? 'http://localhost:8080/websocket',
      reconnectDelay: 5000, // 연결이 완전히 끊어지면 5초 후 재연결 시도
      heartbeatIncoming: 4000, // 서버 → 클라이언트 연결 유지 확인 (4초)
      heartbeatOutgoing: 4000, // 클라이언트 → 서버 연결 유지 확인 (4초)
      onConnect: () => {
        console.log('Connected to WebSocket Server');
        set({ isConnected: true });
      },
      onDisconnect: () => {
        console.log('Disconnected from WebSocket Server');
        set({ isConnected: false, client: null });
      },
      onStompError: (frame) => {
        console.error('STOMP Error:', frame.headers['message']);
      },
    });

    client.activate();
    set({ client });
  },

  // 구독 함수
  subscribe: (destination, callback) => {
    const client = get().client;
    if (!client || !client.connected) {
      console.error(`Cannot subscribe to ${destination} - WebSocket not connected`);
      return;
    }

    client.subscribe(destination, callback);
    console.log(`📩 Subscribed to ${destination}`);
  },

  // 구독 해제 함수
  unsubscribe: (destination) => {
    const client = get().client;
    if (client) {
      client.unsubscribe(destination);
      console.log(`🗑️ Unsubscribed from ${destination}`);
    }
  },

  // 퍼블리시 함수: 메시지 전송때 사용
  publish: (destination: string, body: string, headers: Record<string, string> = {}) => {
    const client = get().client;
    if (!client || !client.connected) {
      console.error(`Cannot publish to ${destination} - WebSocket not connected`);
      return;
    }
    client.publish({ destination, body, headers });
    console.log(`Publishing to ${destination} with body: ${body}`);
  },
}));
