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
import { usePathname, useParams } from "next/navigation";
import type {
  TickerDto,
  OrderbookDto,
  TradeDto,
  CandleChartDto,
} from "@/app/types";

// 구독 가능한 타입 정의
type SubscriptionType = "ticker" | "orderbook" | "trade" | "candle";

interface Subscription {
  type: SubscriptionType;
  markets?: string[]; // markets가 비어있을 수도 있음
}

interface WebSocketContextProps {
  tickers: Record<string, TickerDto | null>;
  orderbooks: Record<string, OrderbookDto | null>;
  trades: Record<string, TradeDto | null>;
  candleCharts: Record<string, CandleChartDto[] | null>;
}

const WebSocketContext = createContext<WebSocketContextProps>({
  tickers: {},
  orderbooks: {},
  trades: {},
  candleCharts: {},
});

export const WebSocketProvider = ({
  children,
  subscriptions = [],
}: {
  children: ReactNode;
  subscriptions: Subscription[];
}) => {
  const { market } = useParams() as { market: string }; // URL에서 market 가져오기
  const pathname = usePathname();
  const { connect, isConnected, subscribe, unsubscribe } = useWebSocketStore();

  const [tickers, setTicker] = useState<Record<string, TickerDto | null>>({});
  const [orderbooks, setOrderbook] = useState<
    Record<string, OrderbookDto | null>
  >({});
  const [trades, setTrades] = useState<Record<string, TradeDto | null>>({});
  const [candles, setCandles] = useState<
    Record<string, CandleChartDto[] | null>
  >({});

  // WebSocket 연결
  useEffect(() => {
    connect();
  }, []);

  useEffect(() => {
    if (!isConnected || subscriptions.length === 0) return;

    const availableSubscriptions: Record<
      SubscriptionType,
      (market: string) => { dest: string; callback: (msg: IMessage) => void }
    > = {
      ticker: (market) => ({
        dest: `/sub/coin/ticker/${market}`,
        callback: (msg: IMessage) =>
          setTicker((prev) => ({ ...prev, [market]: JSON.parse(msg.body) })),
      }),
      orderbook: (market) => ({
        dest: `/sub/coin/orderbook/${market}`,
        callback: (msg: IMessage) =>
          setOrderbook((prev) => ({ ...prev, [market]: JSON.parse(msg.body) })),
      }),
      trade: (market) => ({
        dest: `/sub/coin/trade/${market}`,
        callback: (msg: IMessage) =>
          setTrades((prev) => ({ ...prev, [market]: JSON.parse(msg.body) })),
      }),
      candle: (market) => ({
        dest: `/sub/coin/candle/${market}`,
        callback: (msg: IMessage) =>
          setCandles((prev) => ({ ...prev, [market]: JSON.parse(msg.body) })),
      }),
    };

    // `markets`가 없거나 빈 배열이면 `useParams()`에서 market을 가져와 사용
    const updatedSubscriptions = subscriptions.map(({ type, markets }) => ({
      type,
      markets: markets && markets.length > 0 ? markets : [market],
    }));

    const activeSubscriptions = updatedSubscriptions.flatMap(
      ({ type, markets }) =>
        markets!.map((market) => availableSubscriptions[type](market))
    );

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
      value={{ tickers, orderbooks, trades, candleCharts: candles }}
    >
      {children}
    </WebSocketContext.Provider>
  );
};

// WebSocket 데이터를 쉽게 가져오는 커스텀 훅
export const useWebSocket = () => useContext(WebSocketContext);
export default WebSocketProvider;