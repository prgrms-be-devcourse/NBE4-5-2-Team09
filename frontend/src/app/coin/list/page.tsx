'use client';

import { useEffect, useState } from 'react';
import WebSocketProvider from '@/context/WebSocketContext';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import ClientPage from './ClientPage';
import { MarketDto, MarketsDto, PaginationDto } from '@/types';
import { fetchApi } from '@/lib/api';
import { Skeleton } from '@/components/ui/skeleton';

import PaginationComponent from '@/components/Pagination';

export default function Page() {
  const [pagination, setPagination] = useState<PaginationDto>({
    page: 1,
    size: 10,
    totalElements: 0,
    totalPages: 0,
  });
  const [markets, setMarkets] = useState<MarketDto[]>([]);
  const [quote, setQuote] = useState('KRW');
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(9);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function fetchMarkets() {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchApi<MarketsDto>(
        `/api/markets?type=${quote}&page=${page - 1}&size=${size}`,
        {
          method: 'GET',
        },
      );
      setMarkets(data.content);
      setPagination(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  }
  // 기준 통화와 페이지 변경 시 시장 데이터를 fetch
  useEffect(() => {
    fetchMarkets();
  }, [quote, page, size]);

  // 기준 통화 변경 시 페이지 번호 초기화
  const handleQuoteChange = (newQuote: string) => {
    setQuote(newQuote);
    setPage(1);
  };

  if (loading) return <Skeleton className="h-96 w-full rounded-md" />;
  if (error) return <p className="text-red-500">{error}</p>;
  if (!markets) return <p>No data found</p>;

  return (
    <WebSocketProvider subscriptions={[]}>
      <div className="max-w-7xl mx-auto p-6">
        <Tabs value={quote} onValueChange={handleQuoteChange}>
          <TabsList className="grid w-full grid-cols-3 bg-muted">
            <TabsTrigger value="KRW">KRW</TabsTrigger>
            <TabsTrigger value="BTC">BTC</TabsTrigger>
            <TabsTrigger value="USDT">USDT</TabsTrigger>
          </TabsList>
        </Tabs>
        <ClientPage markets={markets.slice(0, size)} />

        <div className="flex justify-center mt-6">
          <PaginationComponent
            currentPage={page}
            totalPages={pagination.totalPages ?? 1}
            maxPageButtons={5}
            onPageChange={(newPage) => setPage(newPage)}
            size={size}
            onSizeChange={(newSize) => setSize(newSize)}
            totalElements={pagination.totalElements ?? 0}
            pageSizeList={[9, 15, 21]}
          />
        </div>
      </div>
    </WebSocketProvider>
  );
}
