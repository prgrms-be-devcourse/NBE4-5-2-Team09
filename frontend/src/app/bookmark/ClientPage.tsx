"use client";

import { useEffect, useState } from "react";
import { useWebSocket } from "@/context/WebSocketContext";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { components } from "@/lib/api/generated/schema";
import { useAuth } from "@/context/AuthContext";
import client from "@/lib/api/client";
import Link from "next/link";

type PageBookmarkResponse =
  components["schemas"]["PagedResponseBookmarkResponse"];

interface ClientPageProps {
  bookmarks: PageBookmarkResponse;
  markets: string[];
}

export default function ClientPage({ bookmarks, markets }: ClientPageProps) {
  const { tickers, updateSubscriptions } = useWebSocket();
  const { accessToken } = useAuth();

  // 기본 필터링 "KRW"
  const [quote, setQuote] = useState("KRW");
  const [page, setPage] = useState(bookmarks.page ? bookmarks.page + 1 : 1);
  const [loading, setLoading] = useState(false);
  const [bookmarksData, setBookmarksData] =
    useState<PageBookmarkResponse>(bookmarks);

  // 북마크 리스트
  const bookmarkList = bookmarksData.content ?? [];
  const totalPages = bookmarksData.totalPages || 1;
  const itemsPerPage = 9;

  // API 호출 및 WebSocket 구독 업데이트
  useEffect(() => {
    async function fetchBookmarks() {
      if (!accessToken) return;
      setLoading(true);
      try {
        const { data, error } = await client.GET("/api/bookmarks/{quote}", {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
          params: {
            path: { quote },
            query: {
              page: page - 1,
              size: itemsPerPage,
            },
          },
        });

        if (error || !data)
          throw new Error("북마크 데이터를 불러오는 중 오류 발생");

        setBookmarksData(data);

        // WebSocket 재구독
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
  }, [page, quote]); // `fetchBookmarks`를 `useEffect` 내부에 선언하여 의존성 배열에서 제외

  return (
    <div className="p-6">
      {/* 필터링 탭 */}
      <Tabs
        value={quote}
        onValueChange={(newQuote) => {
          setQuote(newQuote);
          setPage(1);
        }}
      >
        <TabsList className="grid w-full grid-cols-3 bg-gray-100">
          <TabsTrigger value="KRW">KRW</TabsTrigger>
          <TabsTrigger value="BTC">BTC</TabsTrigger>
          <TabsTrigger value="USDT">USDT</TabsTrigger>
        </TabsList>
      </Tabs>

      {/* 로딩 상태 */}
      {loading ? (
        <div className="text-center py-6">로딩 중...</div>
      ) : (
        <>
          {/* 북마크 코인 리스트 */}
          <div className="grid grid-cols-3 gap-4 mt-5">
            {bookmarkList.map((bookmark) => {
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

          {/* 페이지네이션 */}
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
