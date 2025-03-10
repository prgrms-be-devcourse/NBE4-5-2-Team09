"use client"

import { useEffect, useState } from "react";
import WebSocketProvider from "@/app/context/WebSocketContext";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Button } from "@/components/ui/button";
import { MarketDto } from "@/app/types";
import ClientPage from "./MarketList";

export default function Page() {
  const [markets, setMarkets] = useState<MarketDto[]>([]);
  const [marketCodes, setMarketCodes] = useState<string[]>([]);
  const [quote, setQuote] = useState("KRW");
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true); // 로딩 상태 추가
  const [totalPages, setTotalPages] = useState(0);
  const pageSize = 9;

  // 기준 통화가 바뀔 때마다, 페이지를 넘길 때마다 해당 페이지의 마켓을 불러옴
  useEffect(() => {
    async function fetchMarkets() {
      try {
        setLoading(true); // 데이터 요청 시작 시 로딩 상태를 true로 설정
        let api = process.env.NEXT_PUBLIC_API_URL + "/api/market";
        api += `?type=${quote}&page=${page}&size=${pageSize}`;

        const res = await fetch(api);
        if (!res.ok) return;
        const data = await res.json();
        setMarkets(data.content);
        setTotalPages(data.totalPages);
      } catch (err) {
        console.error("Fetch markets error:", err);
      } finally {
        setLoading(false); // 데이터 요청 완료 후 로딩 상태를 false로 설정
      }
    }

    fetchMarkets();
  }, [quote, page]);

  // markets가 변경될 때 marketCodes를 업데이트
  useEffect(() => {
    setMarketCodes(markets?.map((market) => market.code) || []);
  }, [markets]);

  const paginatedData = markets?.slice(0, pageSize) || [];

  // 기준 통화 변경 시 페이지 번호를 0으로 설정
  const handleQuoteChange = (newQuote: string) => {
    setQuote(newQuote);
    setPage(0);
  }

  if (loading) {
    return <div>Loading...</div>; // 로딩 중일 때 메시지 표시
  }

  return (
    <WebSocketProvider subscriptions={[{ type: "ticker", markets: marketCodes }]}>

      <div className="p-6">

        {/* 기준 통화 필터 탭 */}
        <Tabs value={quote} onValueChange={handleQuoteChange}>
          <TabsList className="grid w-full grid-cols-3 bg-gray-100">
            <TabsTrigger value="KRW">KRW</TabsTrigger>
            <TabsTrigger value="BTC">BTC</TabsTrigger>
            <TabsTrigger value="USDT">USDT</TabsTrigger>
          </TabsList>
        </Tabs>

        {/* 마켓 카드 리스트 */}
        <ClientPage markets={paginatedData}></ClientPage>

        {/* 페이지네이션 */}
        <div className="flex justify-center items-center mt-6 space-x-2">
          <Button onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>
            이전
          </Button>
          <span>{page + 1} / {totalPages + 1}</span>
          <Button onClick={() => setPage((p) => Math.min(totalPages, p + 1))} disabled={page === totalPages}>
            다음
          </Button>
        </div>

      </div>
    </WebSocketProvider>
  );
};