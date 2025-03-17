'use client';

import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { IMessage } from '@stomp/stompjs';
import { usePathname, useParams } from 'next/navigation';
import type { TickerDto, OrderbookDto, TradeDto, CandleChartDto } from '@/types';
import { useWebSocketStore } from '@/store/web-socket.store';

// 구독 가능한 타입 정의
type SubscriptionType = 'ticker' | 'orderbook' | 'trade' | 'candle' | 'chat';

interface Subscription {
  type: SubscriptionType;
  markets?: string[];
}

interface WebSocketContextProps {
  tickers: Record<string, TickerDto | null>;
  orderbooks: Record<string, OrderbookDto | null>;
  trades: Record<string, TradeDto | null>;
  candleCharts: Record<string, CandleChartDto[] | null>;
  chatMessages: Record<string, any[]>; // 채팅 메시지 전용 상태
  updateSubscriptions: (newSubscriptions: Subscription[]) => void;
  publishMessage: (destination: string, body: string) => void;
}

const WebSocketContext = createContext<WebSocketContextProps>({
  tickers: {},
  orderbooks: {},
  trades: {},
  candleCharts: {},
  chatMessages: {},
  updateSubscriptions: () => {},
  publishMessage: () => {},
});

export const WebSocketProvider = ({
                                    children,
                                    subscriptions = [],
                                  }: {
  children: ReactNode;
  subscriptions: Subscription[];
}) => {
  const { market } = useParams() as { market: string };
  const pathname = usePathname();
  const { connect, isConnected, subscribe, unsubscribe, publish } = useWebSocketStore();

  const [tickers, setTicker] = useState<Record<string, TickerDto | null>>({});
  const [orderbooks, setOrderbook] = useState<Record<string, OrderbookDto | null>>({});
  const [trades, setTrades] = useState<Record<string, TradeDto | null>>({});
  const [candles, setCandles] = useState<Record<string, CandleChartDto[] | null>>({});
  const [chatMessages, setChatMessages] = useState<Record<string, any[]>>({});

  const [currentSubscriptions, setCurrentSubscriptions] = useState<Subscription[]>(subscriptions);

  const updateSubscriptions = (newSubscriptions: Subscription[]) => {
    setCurrentSubscriptions(newSubscriptions);
  };

  useEffect(() => {
    connect();
  }, []);

  useEffect(() => {
    if (!isConnected || currentSubscriptions.length === 0) return;

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
      chat: (market) => ({
        dest: `/sub/coin/chat/${market}`,
        callback: (msg: IMessage) => {
          const parsed = JSON.parse(msg.body);
          setChatMessages((prev) => ({
            ...prev,
            [market]: [...(prev[market] || []), parsed],
          }));
        },
      }),
    };

    const updatedSubscriptions = currentSubscriptions.map(({ type, markets }) => ({
      type,
      markets: markets && markets.length > 0 ? markets : [market],
    }));

    const activeSubscriptions = updatedSubscriptions.flatMap(({ type, markets }) =>
        markets!.map((m) => availableSubscriptions[type](m))
    );

    activeSubscriptions.forEach(({ dest, callback }) => {
      subscribe(dest, callback);
    });

    return () => {
      activeSubscriptions.forEach(({ dest }) => {
        unsubscribe(dest);
      });
    };
  }, [market, pathname, isConnected, currentSubscriptions]);

  return (
      <WebSocketContext.Provider
          value={{
            tickers,
            orderbooks,
            trades,
            candleCharts: candles,
            chatMessages,
            updateSubscriptions,
            publishMessage: publish,
          }}
      >
        {children}
      </WebSocketContext.Provider>
  );
};

export const useWebSocket = () => useContext(WebSocketContext);
export default WebSocketProvider;
