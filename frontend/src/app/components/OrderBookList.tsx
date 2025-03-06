"use client";

import { useState, useEffect } from "react";
import type { OrderBook } from "../types";

interface OrderBookListProps {
  orderBook: OrderBook;
  currentPrice: number;
}

export default function OrderBookList({
  orderBook,
  currentPrice,
}: OrderBookListProps) {
  const [clientOrderBook, setClientOrderBook] = useState<OrderBook | null>(
    null
  );
  const [clientCurrentPrice, setClientCurrentPrice] = useState<number | null>(
    null
  );

  // `useEffect`를 사용하여 클라이언트에서만 `orderbook` 데이터를 설정
  useEffect(() => {
    setClientOrderBook(orderBook);
    setClientCurrentPrice(currentPrice);
  }, [orderBook, currentPrice]);

  const formatPrice = (price: number) => price.toLocaleString();
  const formatQuantity = (quantity: number) => quantity.toFixed(4);

  // 서버 사이드 렌더링 중에는 아무것도 표시하지 않음 (Hydration 오류 방지)
  if (!clientOrderBook || clientCurrentPrice === null) return null;

  const { asks, bids } = clientOrderBook;

  return (
    <div className="bg-white rounded-lg shadow-sm overflow-hidden">
      <div className="p-4 border-b border-gray-200">
        <h2 className="text-lg font-semibold">호가 정보</h2>
      </div>

      {/* 테이블 감싸는 div에 overflow 적용하여 스크롤 유지 */}
      <div className="overflow-y-auto max-h-[500px]">
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
            {/* 매도(Asks) */}
            {asks.map((ask, index) => (
              <tr key={`ask-${index}`} className="border-b border-gray-100">
                <td className="px-4 py-2 text-sm text-red-500">
                  {formatPrice(ask.price)}
                </td>
                <td className="px-4 py-2 text-sm text-right">
                  {formatQuantity(ask.quantity)}
                </td>
                <td className="px-4 py-2 text-sm text-right">
                  {formatPrice(ask.total)}
                </td>
              </tr>
            ))}

            {/* 매수(Bids) */}
            {bids.map((bid, index) => (
              <tr key={`bid-${index}`} className="border-b border-gray-100">
                <td className="px-4 py-2 text-sm text-green-500">
                  {formatPrice(bid.price)}
                </td>
                <td className="px-4 py-2 text-sm text-right">
                  {formatQuantity(bid.quantity)}
                </td>
                <td className="px-4 py-2 text-sm text-right">
                  {formatPrice(bid.total)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
