"use client";

import { useState, useEffect, useRef, useCallback } from "react";
import type { OrderBook } from "@/components/types";
import { subscribe, unsubscribe } from "@/lib/api/socket";
import { Skeleton } from "./ui/skeleton";

interface OrderBookListProps {
  market: string;
}

export default function OrderBookList({ market }: OrderBookListProps) {
  const [orderBook, setOrderBook] = useState<OrderBook | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const userScrolled = useRef(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const setContainerRef = useCallback(
    (node: HTMLDivElement | null) => {
      if (node) {
        containerRef.current = node;
        node.onscroll = () => {
          userScrolled.current = true;
        };
        if (orderBook && !userScrolled.current) {
          node.scrollTo({
            top: node.scrollHeight / 2 - node.clientHeight / 2,
            behavior: "auto",
          });
        }
      }
    },
    [orderBook]
  );

  useEffect(() => {
    const handleMessage = (data: OrderBook) => {
      try {
        if (data.code === market) {
          setOrderBook(data);
          setLoading(false);
        }
      } catch (err) {
        setError("Failed to parse WebSocket data");
        setLoading(false);
      }
    };

    subscribe(`/sub/coin/orderbook/${market}`, handleMessage);

    return () => {
      unsubscribe(`/sub/coin/orderbook/${market}`);
    };
  }, [market, subscribe, unsubscribe]);

  const formatPrice = (price: number) => price.toLocaleString();
  const formatQuantity = (quantity: number) => quantity.toFixed(4);

  if (loading) return <Skeleton className="h-96 w-full rounded-md" />;
  if (error)
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {error}
      </div>
    );

  return (
    <div className="bg-white rounded-lg shadow-sm overflow-hidden">
      <div className="p-4 border-b border-gray-200">
        <h2 className="text-lg font-semibold">호가 정보</h2>
      </div>

      {/* 테이블 감싸는 div에 overflow 적용하여 스크롤 유지 */}
      <div className="overflow-y-auto max-h-[500px]" ref={setContainerRef}>
        <table className="w-full table-fixed">
          {/* 테이블 헤더 (고정) */}
          <thead className="bg-gray-50 sticky top-0">
            <tr>
              <th className="px-4 py-2 text-xs text-gray-500 text-left w-1/3">
                가격(KRW)
              </th>
              <th className="px-4 py-2 text-xs text-gray-500 text-right w-1/3">
                수량(BTC)
              </th>
              <th className="px-4 py-2 text-xs text-gray-500 text-right w-1/3">
                총액(KRW)
              </th>
            </tr>
          </thead>

          <tbody className="divide-y divide-gray-100">
            {orderBook?.orderbookUnits.map(({ askPrice, askSize }, index) => (
              <tr key={`ask-${index}`} className="border-b border-gray-100">
                <td className="px-4 py-2 text-sm text-red-500">
                  {formatPrice(askPrice)}
                </td>
                <td className="px-4 py-2 text-sm text-right">
                  {formatQuantity(askSize)}
                </td>
                <td className="px-4 py-2 text-sm text-right">
                  {formatPrice(orderBook.totalAskSize)}
                </td>
              </tr>
            ))}
            {orderBook?.orderbookUnits.map(({ bidPrice, bidSize }, index) => (
              <tr key={`bid-${index}`} className="border-b border-gray-100">
                <td className="px-4 py-2 text-sm text-green-500">
                  {formatPrice(bidPrice)}
                </td>
                <td className="px-4 py-2 text-sm text-right">
                  {formatQuantity(bidSize)}
                </td>
                <td className="px-4 py-2 text-sm text-right">
                  {formatPrice(orderBook.totalBidSize)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
