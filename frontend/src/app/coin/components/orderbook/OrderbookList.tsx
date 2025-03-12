'use client';

import { useState, useCallback, useRef } from 'react';
import type { OrderbookDto } from '@/types';
import { OrderbookHeader } from './OrderbookHeader';
import { OrderbookChart } from './OrderbookChart';

interface OrderBookListProps {
  market: string;
  orderbook: OrderbookDto | null;
}

export default function OrderbookList({ market, orderbook }: OrderBookListProps) {
  const [quote, base] = market.split('-');
  const userScrolled = useRef(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const [isTotalMode, setIsTotalMode] = useState(false);

  const handleToggleMode = () => {
    setIsTotalMode((prev) => !prev);
  };

  const setContainerRef = useCallback(
    (node: HTMLDivElement | null) => {
      if (node) {
        containerRef.current = node;
        node.onscroll = () => {
          userScrolled.current = true;
        };
        if (orderbook && !userScrolled.current) {
          node.scrollTo({
            top: node.scrollHeight / 2 - node.clientHeight / 2,
            behavior: 'auto',
          });
        }
      }
    },
    [orderbook],
  );

  const formatPrice = (price: number) => price.toLocaleString();
  const formatQuantity = (quantity: number) => quantity.toFixed(4);

  return (
    <div className="bg-white rounded-lg shadow-sm overflow-hidden">
      <OrderbookHeader
        isTotalMode={isTotalMode}
        handleToggleMode={handleToggleMode}
        quote={quote}
        base={base}
      />
      {orderbook ? (
        <OrderbookChart
          orderbook={orderbook}
          isTotalMode={isTotalMode}
          quote={quote}
          base={base}
          formatPrice={formatPrice}
          formatQuantity={formatQuantity}
          maxAskValue={
            isTotalMode
              ? Math.max(...orderbook.orderbookUnits.map((u) => u.askPrice * u.askSize), 0)
              : Math.max(...orderbook.orderbookUnits.map((u) => u.askSize), 0)
          }
          maxBidValue={
            isTotalMode
              ? Math.max(...orderbook.orderbookUnits.map((u) => u.bidPrice * u.bidSize), 0)
              : Math.max(...orderbook.orderbookUnits.map((u) => u.bidSize), 0)
          }
          setContainerRef={setContainerRef}
        />
      ) : (
        <div className="p-4 text-center text-gray-500">호가 내역 없음</div>
      )}
    </div>
  );
}
