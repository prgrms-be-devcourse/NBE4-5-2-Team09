"use client";

import {
  createContext,
  useContext,
  useEffect,
  useState,
  ReactNode,
} from "react";
import { useWebSocketStore } from "@/app/store/webSocketStore";
import { IMessage } from "@stomp/stompjs";
import { useParams, usePathname } from "next/navigation";
import type {
  TickerDto,
  OrderbookDto,
  TradeDto,
  CandleChartDto,
} from "@/app/types";

// 1. 가능한 구독 키들을 타입으로 정의
type SubscriptionKey = "ticker" | "orderbook" | "trade" | "candle";

interface WebSocketContextProps {
  ticker: TickerDto | null;
  orderbook: OrderbookDto | null;
  trades: TradeDto[] | null;
  candleCharts: CandleChartDto[] | null;
}

const WebSocketContext = createContext<WebSocketContextProps>({
  ticker: null,
  orderbook: null,
  trades: null,
  candleCharts: null,
});

export const WebSocketProvider = ({
  children,
  subscriptions = [],
}: {
  children: ReactNode;
  subscriptions: SubscriptionKey[]; // 2. 구독 키를 제한된 타입으로 설정
}) => {
  const { market } = useParams();
  const pathname = usePathname();
  const { connect, isConnected, subscribe, unsubscribe } = useWebSocketStore();

  const [ticker, setTicker] = useState<TickerDto | null>(null);
  const [orderbook, setOrderbook] = useState<OrderbookDto | null>(null);
  const [trades, setTrades] = useState<TradeDto[] | null>(null);
  const [candles, setCandles] = useState<CandleChartDto[] | null>(null);

  // WebSocket 연결
  useEffect(() => {
    connect();
  }, []);

  // 페이지에서 받은 subscriptions에 맞춰 동적으로 구독 수행
  useEffect(() => {
    if (!market || !isConnected || subscriptions.length === 0) return;

    // 3. availableSubscriptions의 타입을 명시적으로 정의
    const availableSubscriptions: Record<
      SubscriptionKey,
      { dest: string; callback: (msg: IMessage) => void }
    > = {
      ticker: {
        dest: `/sub/coin/ticker/${market}`,
        callback: (msg: IMessage) => setTicker(JSON.parse(msg.body)),
      },
      orderbook: {
        dest: `/sub/coin/orderbook/${market}`,
        callback: (msg: IMessage) => setOrderbook(JSON.parse(msg.body)),
      },
      trade: {
        dest: `/sub/coin/trade/${market}`,
        callback: (msg: IMessage) => setTrades(JSON.parse(msg.body)),
      },
      candle: {
        dest: `/sub/coin/candle/${market}`,
        callback: (msg: IMessage) => setCandles(JSON.parse(msg.body)),
      },
    };

    // 4. 올바른 키로 접근할 수 있도록 TypeScript가 인식할 수 있게 처리
    const activeSubscriptions = subscriptions
      .map((key) => availableSubscriptions[key as SubscriptionKey]) // 타입 캐스팅 추가
      .filter(Boolean); // 존재하는 값만 필터링

    activeSubscriptions.forEach(({ dest, callback }) => {
      subscribe(dest, callback);
    });

    return () => {
      activeSubscriptions.forEach(({ dest }) => {
        unsubscribe(dest);
      });
    };
  }, [market, pathname, isConnected, subscriptions]);

  return (
    <WebSocketContext.Provider
      value={{ ticker, orderbook, trades, candleCharts: candles }}
    >
      {children}
    </WebSocketContext.Provider>
  );
};

// WebSocket 데이터를 쉽게 가져오는 커스텀 훅
export const useWebSocket = () => useContext(WebSocketContext);
export default WebSocketProvider;
