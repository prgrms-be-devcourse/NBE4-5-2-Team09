"use client";

import { useState, useEffect } from "react";
import { type TradeItem, AskBid } from "../types";

interface TradeListProps {
  trades: TradeItem[];
}

export default function TradeList({ trades }: TradeListProps) {
  const [clientTrades, setClientTrades] = useState<TradeItem[]>([]);

  // `useEffect`를 사용하여 클라이언트에서만 `trades` 데이터를 설정
  useEffect(() => {
    setClientTrades(trades);
  }, []);

  // `timestamp` 값을 한국 시간(KST) 기준으로 포맷
  const formatTime = (timestamp: number) => {
    return new Date(timestamp).toLocaleTimeString("ko-KR", {
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
      hour12: false,
    });
  };

  // 가격을 `1,000` 단위로 구분하여 포맷 (e.g., 45,678,900)
  const formatPrice = (price: number) => {
    return price.toLocaleString();
  };

  // 서버 사이드 렌더링 중에는 아무것도 표시하지 않음 (Hydration 오류 방지)
  if (clientTrades.length === 0) return null;

  return (
    <div className="bg-white rounded-lg shadow-sm overflow-hidden">
      <div className="p-4 border-b border-gray-200">
        <h2 className="text-lg font-semibold">체결 내역</h2>
      </div>

      <div className="overflow-y-auto max-h-[400px]">
        <table className="w-full">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-2 text-xs text-gray-500 text-left">
                시간
              </th>
              <th className="px-4 py-2 text-xs text-gray-500 text-right">
                가격(KRW)
              </th>
              <th className="px-4 py-2 text-xs text-gray-500 text-right">
                수량(BTC)
              </th>
              <th className="px-4 py-2 text-xs text-gray-500 text-center">
                구분
              </th>
            </tr>
          </thead>
          <tbody>
            {clientTrades.map((trade) => (
              <tr key={trade.id} className="border-b border-gray-100">
                <td className="px-4 py-2 text-sm text-gray-600">
                  {formatTime(trade.timestamp)}
                </td>
                <td
                  className={`px-4 py-2 text-sm text-right ${
                    trade.side === AskBid.ASK
                      ? "text-red-500"
                      : "text-green-500"
                  }`}
                >
                  {formatPrice(trade.price)}
                </td>
                <td className="px-4 py-2 text-sm text-right">
                  {trade.quantity.toFixed(4)}
                </td>
                <td className="px-4 py-2 text-sm text-center">
                  <span
                    className={`inline-block px-2 py-1 rounded-full text-xs ${
                      trade.side === AskBid.ASK
                        ? "bg-red-100 text-red-800"
                        : "bg-green-100 text-green-800"
                    }`}
                  >
                    {trade.side === AskBid.ASK ? "매도" : "매수"}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
