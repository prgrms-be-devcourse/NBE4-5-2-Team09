"use client";

import { useEffect, useState } from "react";
import { useWebSocket } from "@/app/context/WebSocketContext";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { components } from "@/lib/api/generated/schema";
import { useAuth } from "@/app/context/auth-context";
import client from "@/lib/api/client";
import Link from "next/link";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

type PageBookmarkResponse = components["schemas"]["PageBookmarkResponse"];

interface ClientPageProps {
  bookmarks: PageBookmarkResponse;
  markets: string[];
}

export default function ClientPage({ bookmarks, markets }: ClientPageProps) {
  const { tickers, updateSubscriptions } = useWebSocket();
  const { accessToken } = useAuth();

  // ✅ 기본 필터링 "KRW"
  const [prefix, setPrefix] = useState("KRW");
  const [sortBy, setSortBy] = useState<"price" | "changeRate" | "volume">(
    "price"
  );
  const [page, setPage] = useState(bookmarks.number ? bookmarks.number + 1 : 1);
  const [loading, setLoading] = useState(false);
  const [bookmarksData, setBookmarksData] =
    useState<PageBookmarkResponse>(bookmarks);

  // ✅ 북마크 리스트
  const bookmarkList = bookmarksData.content ?? [];
  const totalPages = bookmarksData.totalPages || 1;
  const itemsPerPage = 9;

  // ✅ API 호출 및 WebSocket 구독 업데이트
  useEffect(() => {
    async function fetchBookmarks() {
      if (!accessToken) return;
      setLoading(true);
      try {
        const { data, error } = await client.GET("/api/bookmarks/{prefix}", {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
          params: {
            path: { prefix },
            query: {
              page: page - 1,
              size: itemsPerPage,
            },
          },
        });

        if (error || !data)
          throw new Error("북마크 데이터를 불러오는 중 오류 발생");

        setBookmarksData(data);

        // ✅ WebSocket 재구독
        if (data.content) {
          const newMarketCodes = data.content
            .map((b) => b.code!)
            .filter(Boolean);
          updateSubscriptions([{ type: "ticker", markets: newMarketCodes }]);
        }
      } catch (error) {
        console.error("북마크 데이터를 불러오는 중 오류 발생:", error);
      } finally {
        setLoading(false);
      }
    }

    fetchBookmarks();
  }, [page, prefix]); // ✅ `fetchBookmarks`를 `useEffect` 내부에 선언하여 의존성 배열에서 제외

  const sortedData = [...bookmarkList].sort((a, b) => {
    const priceA = tickers[a.code ?? ""]?.tradePrice || 0;
    const priceB = tickers[b.code ?? ""]?.tradePrice || 0;
    const changeRateA = tickers[a.code ?? ""]?.signedChangeRate || 0;
    const changeRateB = tickers[b.code ?? ""]?.signedChangeRate || 0;
    const volumeA = tickers[a.code ?? ""]?.accTradeVolume || 0;
    const volumeB = tickers[b.code ?? ""]?.accTradeVolume || 0;

    if (sortBy === "price") return priceB - priceA; // 가격 내림차순
    if (sortBy === "changeRate") return changeRateB - changeRateA; // 변동률 내림차순
    if (sortBy === "volume") return volumeB - volumeA; // 거래량 내림차순
    return 0;
  });

  return (
    <div className="p-6">
      {/* ✅ 필터링 탭 */}
      <Tabs
        value={prefix}
        onValueChange={(newQuote) => {
          setPrefix(newQuote);
          setPage(1);
        }}
      >
        <TabsList className="grid w-full grid-cols-3 bg-gray-100">
          <TabsTrigger value="KRW">KRW</TabsTrigger>
          <TabsTrigger value="BTC">BTC</TabsTrigger>
          <TabsTrigger value="USDT">USDT</TabsTrigger>
        </TabsList>
      </Tabs>

      {/* ✅ 정렬 드롭다운 */}
      <div className="flex justify-end space-x-2 my-4">
        <Select
          onValueChange={(value) =>
            setSortBy(value as "price" | "changeRate" | "volume")
          }
        >
          <SelectTrigger className="w-32">
            <SelectValue placeholder="정렬 기준" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="price">현재가</SelectItem>
            <SelectItem value="changeRate">변동률</SelectItem>
            <SelectItem value="volume">거래량</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* ✅ 로딩 상태 */}
      {loading ? (
        <div className="text-center py-6">로딩 중...</div>
      ) : (
        <>
          {/* ✅ 북마크 코인 리스트 */}
          <div className="grid grid-cols-3 gap-4 mt-5">
            {sortedData.map((bookmark) => {
              const ticker = tickers[bookmark.code!!];

              if (!ticker) {
                return (
                  <Link key={bookmark.code} href={`/coin/${bookmark.code}`}>
                    <Card className="bg-white shadow-sm rounded-sm border-0">
                      <CardContent className="p-4">
                        <div className="flex items-center space-x-1">
                          <h2 className="text-base font-bold">
                            {bookmark.koreanName}
                          </h2>
                          <h3 className="text-sm text-gray-500">
                            {bookmark.englishName}
                          </h3>
                        </div>
                        <div className="flex justify-between items-end mt-1">
                          <p className="text-xl font-semibold">
                            0{" "}
                            <span className="text-xs">
                              {bookmark.code?.split("-")[0]}
                            </span>
                          </p>
                          <p className="text-sm text-gray-500">0%</p>
                        </div>
                        <p className="text-xs text-gray-500 mt-2">거래량: 0</p>
                      </CardContent>
                    </Card>
                  </Link>
                );
              }

              return (
                <Link key={bookmark.code} href={`/coin/${bookmark.code}`}>
                  <Card className="bg-white shadow-sm rounded-sm border-0">
                    <CardContent className="p-4">
                      <div className="flex items-center space-x-1">
                        <h2 className="text-base font-bold">
                          {bookmark.koreanName}
                        </h2>
                        <h3 className="text-sm text-gray-500">
                          {bookmark.englishName}
                        </h3>
                      </div>
                      <div className="flex justify-between items-end mt-1">
                        <p className="text-xl font-semibold">
                          {new Intl.NumberFormat(undefined, {
                            minimumFractionDigits:
                              ticker.tradePrice <= 1
                                ? 8
                                : ticker.tradePrice < 1000
                                ? 1
                                : 0,
                            maximumFractionDigits:
                              ticker.tradePrice <= 1
                                ? 8
                                : ticker.tradePrice < 1000
                                ? 1
                                : 0,
                          }).format(ticker.tradePrice)}
                          <span className="text-xs">
                            {bookmark.code?.split("-")[0]}
                          </span>
                        </p>
                        <p
                          className={
                            ticker.signedChangeRate >= 0
                              ? "text-sm text-red-500"
                              : "text-sm text-blue-500"
                          }
                        >
                          {ticker.signedChangeRate
                            ? `${ticker.signedChangeRate >= 0 ? "+" : ""}${(
                                ticker.signedChangeRate * 100
                              ).toFixed(2)}%`
                            : "0%"}
                        </p>
                      </div>
                      <p className="text-xs text-gray-500 mt-2">
                        거래량:{" "}
                        {new Intl.NumberFormat().format(
                          parseFloat(ticker.accTradeVolume?.toFixed(3) || "0")
                        )}
                      </p>
                    </CardContent>
                  </Card>
                </Link>
              );
            })}
          </div>

          {/* ✅ 페이지네이션 */}
          <div className="flex justify-center items-center mt-6 space-x-2">
            <Button
              onClick={() => setPage((p) => Math.max(1, p - 1))}
              disabled={page === 1}
            >
              이전
            </Button>
            <span>
              {page} / {totalPages}
            </span>
            <Button
              onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
              disabled={page === totalPages}
            >
              다음
            </Button>
          </div>
        </>
      )}
    </div>
  );
}
