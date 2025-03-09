"use client";

import { useState, useEffect } from "react";
import { type TradeItem, AskBid, TradeDto } from "@/app/types";

interface TradeListProps {
  trades: Record<string, TradeDto | null>;
}

export default function TradeList({ trades }: TradeListProps) {
  const [clientTrades, setClientTrades] = useState<TradeDto[]>([]);

  // `useEffect`를 사용하여 클라이언트에서만 `trades` 데이터를 설정
  useEffect(() => {
    // `Object.values(trades)`를 사용해 `TradeDto` 배열을 설정하고, null 값은 필터링
    const tradeArray = Object.values(trades).filter((trade) => trade !== null) as TradeDto[];

    // 새로 수신된 trade가 추가되면, 배열이 10개를 초과하지 않도록 관리
    if (tradeArray.length > 0) {
      setClientTrades((prevTrades) => {
        const updatedTrades = [tradeArray[0], ...prevTrades]; // 새로운 trade를 앞에 추가
        return updatedTrades.slice(0, 10); // 최대 10개까지 유지
      });
    }
  }, [trades]); // `trades`가 변경될 때마다 실행

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
              <th className="px-4 py-2 text-xs text-gray-500 text-left">시간</th>
              <th className="px-4 py-2 text-xs text-gray-500 text-right">가격(KRW)</th>
              <th className="px-4 py-2 text-xs text-gray-500 text-right">수량(BTC)</th>
              <th className="px-4 py-2 text-xs text-gray-500 text-center">구분</th>
            </tr>
          </thead>
          <tbody>
            {clientTrades.map((trade) => (
              <tr key={trade.sequentialId} className="border-b border-gray-100">
                <td className="px-4 py-2 text-sm text-gray-600">
                  {formatTime(trade.timestamp)}
                </td>
                <td
                  className={`px-4 py-2 text-sm text-right ${trade.askBid === AskBid.ASK ? "text-red-500" : "text-green-500"}`}
                >
                  {formatPrice(trade.tradePrice)}
                </td>
                <td className="px-4 py-2 text-sm text-right">
                  {trade.tradeVolume.toFixed(4)}
                </td>
                <td className="px-4 py-2 text-sm text-center">
                  <span
                    className={`inline-block px-2 py-1 rounded-full text-xs ${trade.askBid === AskBid.ASK ? "bg-red-100 text-red-800" : "bg-green-100 text-green-800"}`}
                  >
                    {trade.askBid === AskBid.ASK ? "매도" : "매수"}
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
