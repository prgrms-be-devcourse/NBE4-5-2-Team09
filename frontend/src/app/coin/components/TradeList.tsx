"use client";

import { useState, useEffect } from "react";
import { type TradeItem, AskBid, TradeDto } from "@/app/types";

interface TradeListProps {
  trades: Record<string, TradeDto | null>;
}

export default function TradeList({ trades }: TradeListProps) {
  const [clientTrades, setClientTrades] = useState<TradeDto[]>([]);

  useEffect(() => {
    const tradeArray = Object.values(trades).filter((trade) => trade !== null) as TradeDto[];

    if (tradeArray.length > 0) {
      setClientTrades((prevTrades) => {
        const newTrades = tradeArray.filter(
          (trade) => !prevTrades.some((t) => t.sequentialId === trade.sequentialId)
        );

        const updatedTrades = [...newTrades, ...prevTrades];
        return updatedTrades.slice(0, 10);
      });
    }
  }, [trades]);

  const formatTime = (timestamp: number) => {
    return new Date(timestamp).toLocaleTimeString("ko-KR", {
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
      hour12: false,
    });
  };

  const formatPrice = (price: number) => {
    return price.toLocaleString();
  };

  return (
    <div className="bg-white rounded-lg shadow-sm overflow-hidden">
      <div className="p-4 border-b border-gray-200">
        <h2 className="text-lg font-semibold">체결 내역</h2>
      </div>
      {clientTrades.length === 0 ? (
        <div className="p-4 text-center text-gray-500">체결 내역 없음</div>
      ) : (
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
                    className={`px-4 py-2 text-sm text-right ${trade.askBid === AskBid.ASK ? "text-red-500" : "text-green-500"
                      }`}
                  >
                    {formatPrice(trade.tradePrice)}
                  </td>
                  <td className="px-4 py-2 text-sm text-right">
                    {trade.tradeVolume.toFixed(4)}
                  </td>
                  <td className="px-4 py-2 text-sm text-center">
                    <span
                      className={`inline-block px-2 py-1 rounded-full text-xs ${trade.askBid === AskBid.ASK ? "bg-red-100 text-red-800" : "bg-green-100 text-green-800"
                        }`}
                    >
                      {trade.askBid === AskBid.ASK ? "매도" : "매수"}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
