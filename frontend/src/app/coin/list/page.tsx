"use client";

import { useEffect, useState } from "react";
import WebSocketProvider from "@/context/WebSocketContext";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Button } from "@/components/ui/button";
import ClientPage from "./MarketList";
import { MarketDto } from "@/types";

export default function Page() {
  const [markets, setMarkets] = useState<MarketDto[]>([]);
  const [quote, setQuote] = useState("KRW");
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const pageSize = 9;

  // 기준 통화와 페이지 변경 시 시장 데이터를 fetch
  useEffect(() => {
    async function fetchMarkets() {
      try {
        setLoading(true);
        let api = process.env.NEXT_PUBLIC_API_URL + "/api/market";
        api += `?type=${quote}&page=${page}&size=${pageSize}`;
        const res = await fetch(api);
        if (!res.ok) {
          throw new Error("시장 데이터를 불러오는 중 오류 발생");
        }
        const data = await res.json();
        setMarkets(data.content);
        setTotalPages(data.totalPages);
      } catch (err) {
        console.error("Fetch markets error:", err);
      } finally {
        setLoading(false);
      }
    }
    fetchMarkets();
  }, [quote, page]);

  // 기준 통화 변경 시 페이지 번호 초기화
  const handleQuoteChange = (newQuote: string) => {
    setQuote(newQuote);
    setPage(0);
  };

  if (loading) {
    return <div className="p-6">Loading...</div>;
  }

  return (
      <WebSocketProvider subscriptions={[]}>
        <div className="p-6">
          {/* 기준 통화 필터 탭 */}
          <Tabs value={quote} onValueChange={handleQuoteChange}>
            <TabsList className="grid w-full grid-cols-3 bg-gray-100">
              <TabsTrigger value="KRW">KRW</TabsTrigger>
              <TabsTrigger value="BTC">BTC</TabsTrigger>
              <TabsTrigger value="USDT">USDT</TabsTrigger>
            </TabsList>
          </Tabs>

          {/* 시장 데이터(마켓) 카드 리스트 */}
          <ClientPage markets={markets.slice(0, pageSize)} />

          {/* 페이지네이션 */}
          <div className="flex justify-center items-center mt-6 space-x-2">
            <Button onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>
              이전
            </Button>
            <span>
            {page + 1} / {totalPages + 1}
          </span>
            <Button onClick={() => setPage((p) => Math.min(totalPages, p + 1))} disabled={page === totalPages}>
              다음
            </Button>
          </div>
        </div>
      </WebSocketProvider>
  );
}
