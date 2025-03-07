"use client";

import { useSocket } from "@/components/providers/socket-provider";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { useState, useEffect } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import CandleChart from "@/components/candle-chart";
import OrderBookList from "@/components/orderbook-list";
import TradeList from "@/components/trade-list";
import NewsList from "@/components/news-list";
import { Market } from "@/components/types";
import TickerSummary from "@/components/ticker-summary";
import {
  generateMockCandles,
  generateMockNews,
  generateMockTrades,
} from "@/lib/utils/mockData";
import { useMultiValue } from "@/components/providers/multivalue-provider";

export default function DashboardCodeClientPage() {
  const router = useRouter();
  const { market } = useParams();
  const { values } = useMultiValue();
  const { market: code, koreanName, englishName } = values as Market;

  const trades = generateMockTrades();
  const candles = generateMockCandles();
  const news = generateMockNews();
  return (
    <div>
      {/* 상단 티커 및 마켓 요약 */}
      <TickerSummary market={code} koName={koreanName} enName={englishName} />

      {/* 나머지 컴포넌트 배치 */}
      <div className="space-y-4">
        <div className="w-full">
          <CandleChart candles={candles} />
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <TradeList trades={trades} />
          <OrderBookList market={market as string} />
        </div>
        <div className="w-full">
          <NewsList news={news} />
        </div>
      </div>
    </div>
  );
}
