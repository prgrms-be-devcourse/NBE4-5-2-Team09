import { Client, Message, StompSubscription, Frame } from "@stomp/stompjs";

let socketClient: Client | null = null;

type SubscriptionInfo = {
  destination: string;
  callback: (body: any) => void;
  subscription: StompSubscription | null;
};
const subscriptionMap = new Map<string, SubscriptionInfo>();

export function connectSocket(
  onConnect?: (frame: Frame) => void,
  onError?: (frame: Frame) => void
) {
  if (socketClient && socketClient.active) {
    // 이미 연결된 상태
    return socketClient;
  }

  const sockJsUrl =
    process.env.NEXT_PUBLIC_WEBSOCKET_URL ?? "http://localhost:8080/websocket";
  const sock = new WebSocket(sockJsUrl);

  const client = new Client({
    webSocketFactory: () => sock as WebSocket,
    // debug: (str) => console.log(`[WEBSOCKET] ${str}`),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: (frame) => {
      console.log("WEBSOCKET 연결 성공:", frame);
      subscriptionMap.forEach((info, dest) => {
        const sub = client.subscribe(dest, (message: Message) => {
          handleMessage(message, info.callback);
        });
        info.subscription = sub;
      });
      onConnect?.(frame);
    },
    onStompError: (frame) => {
      console.error("SOCKET 에러:", frame);
      onError?.(frame);
    },
    onDisconnect: () => {
      console.log("SOCKET 연결 해제됨");
    },
  });

  client.activate();
  socketClient = client;
  return client;
}

function handleMessage(message: Message, callback: (body: any) => void) {
  if (!message.body) return;
  try {
    const parsed = JSON.parse(message.body);
    callback(parsed);
  } catch (e) {
    console.error("메시지 파싱 오류:", e);
  }
}

export function subscribe(
  destination: string,
  callback: (body: any) => void
): StompSubscription | null {
  if (!socketClient || !socketClient.active) {
    console.warn("SOCKET 아직 연결 전이거나 비활성");
    // 연결 안 된 시점에서도 구독 정보를 저장 => onConnect 때 재구독
    subscriptionMap.set(destination, {
      destination,
      callback,
      subscription: null,
    });
    return null;
  }

  // 이미 구독 중이라면 해제 후 다시 구독
  unsubscribe(destination);

  const sub = socketClient.subscribe(destination, (message: Message) => {
    handleMessage(message, callback);
  });

  subscriptionMap.set(destination, {
    destination,
    callback,
    subscription: sub,
  });
  return sub;
}

export function unsubscribe(destination: string) {
  const info = subscriptionMap.get(destination);
  if (info && info.subscription) {
    info.subscription.unsubscribe();
  }
  subscriptionMap.delete(destination);
}

export function sendMessage(destination: string, body: any) {
  if (!socketClient || !socketClient.active) {
    console.warn("SOCKET not connected");
    return;
  }
  socketClient.publish({
    destination,
    body: JSON.stringify(body),
  });
}

export function disconnect() {
  if (socketClient) {
    subscriptionMap.forEach((info) => {
      info.subscription?.unsubscribe();
    });
    subscriptionMap.clear();

    socketClient.deactivate();
    socketClient = null;
  }
}
