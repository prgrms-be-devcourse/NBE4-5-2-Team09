"use client";

import { fetchJSON } from "@/lib/api/client";
import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationPrevious,
  PaginationNext,
  PaginationEllipsis,
  PaginationLink,
} from "@/components/ui/pagination";
import { useParams, useRouter } from "next/navigation";
import { MarketList } from "@/components/types";
import { useMultiValue } from "@/components/providers/multivalue-provider";

export default function DashboardClientPage() {
  const router = useRouter();
  const { setValues } = useMultiValue();
  const [markets, setMarkets] = useState<MarketList | null>(null);
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(10);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const maxPageButtons = 5;

  useEffect(() => {
    async function fetchMarkets() {
      setLoading(true);
      setError(null);
      try {
        const url = `/api/markets?page=${page - 1}&size=${size}`;
        const response = await fetch(url);
        const data: MarketList = await response.json();
        setMarkets(data);
      } catch (err: any) {
        console.error("Fetch error:", err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }
    fetchMarkets();
  }, [page, size]);

  if (loading) return <Skeleton className="h-96 w-full rounded-md" />;
  if (error) return <p className="text-red-500">{error}</p>;
  if (!markets || !markets.content) return <p>No data found</p>;

  const totalPages = markets.totalPages ?? 1;
  const currentPageGroup = Math.floor((page - 1) / maxPageButtons);
  const startPage = currentPageGroup * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);

  return (
    <div className="max-w-7xl mx-auto p-6">
      <div className="flex justify-between items-center mb-4">
        <Select
          value={String(size)}
          onValueChange={(value) => setSize(Number(value))}
        >
          <SelectTrigger className="w-32">
            <SelectValue>{size}개</SelectValue>
          </SelectTrigger>
          <SelectContent>
            {[5, 10, 20].map((num) => (
              <SelectItem key={num} value={String(num)}>
                {num}개
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <p className="text-muted-foreground">
          총 {markets.totalElements ?? 0}개 코인
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-6">
        {markets.content.map((coin) => (
          <Card
            key={coin.market}
            className="cursor-pointer bg-white shadow-md hover:shadow-lg transition"
            onClick={() => {
              setValues(coin);
              router.push(`/dashboard/${coin.market}`);
            }}
          >
            <CardHeader className="flex justify-between items-center">
              <div>
                <CardTitle>{coin.koreanName}</CardTitle>
                <p className="text-muted-foreground text-sm">
                  {coin.englishName}
                </p>
              </div>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold text-blue-700">{coin.market}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="flex justify-center mt-6">
        <Pagination>
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                className="cursor-pointer"
                onClick={() => setPage(Math.max(1, page - 1))}
              />
            </PaginationItem>
            {startPage > 1 && (
              <PaginationItem>
                <PaginationEllipsis />
              </PaginationItem>
            )}
            {Array.from(
              { length: endPage - startPage + 1 },
              (_, i) => startPage + i
            ).map((num) => (
              <PaginationItem key={num}>
                <PaginationLink
                  className={
                    page === num
                      ? "bg-primary text-blue-700 px-3 py-1 rounded-md"
                      : "px-3 py-1 cursor-pointer"
                  }
                  onClick={() => setPage(num)}
                >
                  {num}
                </PaginationLink>
              </PaginationItem>
            ))}
            {endPage < totalPages && (
              <PaginationItem>
                <PaginationEllipsis />
              </PaginationItem>
            )}
            <PaginationItem>
              <PaginationNext
                className="cursor-pointer"
                onClick={() => setPage(Math.min(totalPages, page + 1))}
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      </div>
    </div>
  );
}
