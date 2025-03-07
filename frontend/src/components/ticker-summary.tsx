"use client";

import { useState, useEffect } from "react";
import { Change, type Market, type Ticker } from "@/components/types";
import { subscribe, unsubscribe } from "@/lib/api/socket";
import { Skeleton } from "./ui/skeleton";

interface TickerSummaryProps {
  market: string;
  koName: string;
  enName: string;
}

export default function TickerSummary({
  market,
  koName,
  enName,
}: TickerSummaryProps) {
  const [ticker, setTicker] = useState<Ticker | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const handleMessage = (data: Ticker) => {
      try {
        if (data.code === market) {
          setTicker(data);
          setLoading(false);
        }
      } catch (err) {
        setError("Failed to parse WebSocket data");
        setLoading(false);
      }
    };

    subscribe(`/sub/coin/ticker/${market}`, handleMessage);

    return () => {
      unsubscribe(`/sub/coin/ticker/${market}`);
    };
  }, [market, subscribe, unsubscribe]);

  if (!ticker) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        현재가를 불러올 수 없습니다.
      </div>
    );
  }
  if (loading) return <Skeleton className="h-96 w-full rounded-md" />;
  if (error) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {error}
      </div>
    );
  }

  return (
    <>
      <div className="mb-4">
        <h1 className="text-2xl font-bold flex items-center">
          <span className="mr-2">{`${koName}(${market})`} (</span>
          <div className="mt-2">
            <span
              className={`text-xl ${
                ticker.change === Change.RISE
                  ? "text-green-500"
                  : ticker.change === "FALL"
                  ? "text-red-500"
                  : "text-gray-500"
              }`}
            >
              ₩{ticker.tradePrice.toLocaleString()}
            </span>
            <span className="ml-2 text-sm">
              {ticker.signedChangeRate > 0 ? "+" : ""}
              {(ticker.signedChangeRate * 100).toFixed(2)}%
            </span>
          </div>
        </h1>
      </div>

      <div className="bg-blue-50 py-4 mb-4 rounded-lg">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-4 gap-4">
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <div className="text-sm text-gray-500">24시간 거래량</div>
              <div className="text-xl font-bold text-blue-600">
                {ticker
                  ? Math.floor(ticker.accTradeVolume24h).toLocaleString()
                  : "-"}
                <text className="text-sm">EOS</text>
              </div>
            </div>
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <div className="text-sm text-gray-500">전일 종가</div>
              <div className="text-xl font-bold text-blue-600">
                {ticker ? ticker.prevClosingPrice.toLocaleString() : "-"}
                <text className="text-sm">KRW</text>
              </div>
            </div>
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <div className="text-sm text-gray-500">도미넌스</div>
              <div className="text-xl font-bold text-blue-600">52.1%</div>
            </div>
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <div className="text-sm text-gray-500">전일대비</div>
              <div className="text-xl font-bold text-blue-600">
                {ticker
                  ? (ticker.signedChangeRate * 100).toFixed(2) + "%"
                  : "-"}
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
