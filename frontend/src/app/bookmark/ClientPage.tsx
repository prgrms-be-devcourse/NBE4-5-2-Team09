'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import { useWebSocket } from '@/context/WebSocketContext';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { components } from '@/lib/api/generated/schema';
import { useAuth } from '@/context/AuthContext';
import Link from 'next/link';
import { client } from '@/lib/api';
import RequireAuthenticated from '@/components/RequireAutenticated';

type PageBookmarkResponse = components['schemas']['PagedResponseBookmarkResponse'];

interface ClientPageProps {
  bookmarks: PageBookmarkResponse;
}

export default function ClientPage({ bookmarks }: ClientPageProps) {
  const { tickers, updateSubscriptions } = useWebSocket();
  const { accessToken } = useAuth();

  // 기본 필터링: "KRW" (탭의 값으로 사용)
  const [quote, setQuote] = useState('KRW');
  const [page, setPage] = useState(bookmarks.page ? bookmarks.page + 1 : 1);
  const [loading, setLoading] = useState(false);
  const [bookmarksData, setBookmarksData] = useState<PageBookmarkResponse>(bookmarks);

  const totalPages = bookmarksData.totalPages || 1;
  const itemsPerPage = 9;

  // API 호출 (페이지나 quote가 변경될 때)
  useEffect(() => {
    async function fetchBookmarks() {
      if (!accessToken) return;
      setLoading(true);
      try {
        const { data, error } = await client.GET('/api/bookmarks/{quote}', {
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

        if (error || !data) {
          throw new Error('북마크 데이터를 불러오는 중 오류 발생');
        }
        setBookmarksData(data);
      } catch (err) {
        console.error('북마크 데이터를 불러오는 중 오류 발생:', err);
      } finally {
        setLoading(false);
      }
    }
    fetchBookmarks();
  }, [accessToken, page, quote]);

  // WebSocket 구독용 마켓 리스트를 useMemo로 계산 (불필요한 재계산 방지)
  const marketsForWS = useMemo(() => {
    return bookmarksData.content.map((bookmark) => bookmark.code!).filter((code) => Boolean(code));
  }, [bookmarksData.content]);

  // 이전 구독 배열을 저장할 ref
  const prevMarketsRef = useRef<string[]>([]);

  // 마켓 리스트가 변경되었을 때만 구독 업데이트 실행
  useEffect(() => {
    if (marketsForWS.length > 0) {
      const newMarketsJSON = JSON.stringify(marketsForWS);
      const prevMarketsJSON = JSON.stringify(prevMarketsRef.current);
      if (newMarketsJSON !== prevMarketsJSON) {
        prevMarketsRef.current = marketsForWS;
        updateSubscriptions([{ type: 'ticker', markets: marketsForWS }]);
      }
    }
  }, [marketsForWS, updateSubscriptions]);

  if (!accessToken) {
    return renderError('로그인이 필요합니다.');
  }
  if (loading) {
    return <div className="p-6 flex justify-center items-center">로딩 중...</div>;
  }
  if (!bookmarksData) {
    return renderError('북마크 데이터를 불러올 수 없습니다.');
  }

  // 탭 전환: quote 변경 시 페이지 리셋
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

      {/* 북마크 코인 리스트 */}
      <div className="grid grid-cols-3 gap-4 mt-5">
        {bookmarksData.content.map((bookmark) => {
          const ticker = tickers[bookmark.code!];
          if (!ticker) {
            return (
              <Link key={bookmark.code} href={`/coin/${bookmark.code}`}>
                <Card className="bg-white shadow-sm rounded-sm border-0">
                  <CardContent className="p-4">
                    <div className="flex items-center space-x-1">
                      <h2 className="text-base font-bold">{bookmark.koreanName}</h2>
                      <h3 className="text-sm text-gray-500">{bookmark.englishName}</h3>
                    </div>
                    <div className="flex justify-between items-end mt-1">
                      <p className="text-xl font-semibold">
                        0 <span className="text-xs">{bookmark.code?.split('-')[0]}</span>
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
                    <h2 className="text-base font-bold">{bookmark.koreanName}</h2>
                    <h3 className="text-sm text-gray-500">{bookmark.englishName}</h3>
                  </div>
                  <div className="flex justify-between items-end mt-1">
                    <p className="text-xl font-semibold">
                      {new Intl.NumberFormat(undefined, {
                        minimumFractionDigits:
                          ticker.tradePrice <= 1 ? 8 : ticker.tradePrice < 1000 ? 1 : 0,
                        maximumFractionDigits:
                          ticker.tradePrice <= 1 ? 8 : ticker.tradePrice < 1000 ? 1 : 0,
                      }).format(ticker.tradePrice)}
                      <span className="text-xs">{bookmark.code?.split('-')[0]}</span>
                    </p>
                    <p
                      className={
                        ticker.signedChangeRate >= 0
                          ? 'text-sm text-red-500'
                          : 'text-sm text-blue-500'
                      }
                    >
                      {ticker.signedChangeRate
                        ? `${ticker.signedChangeRate >= 0 ? '+' : ''}${(
                            ticker.signedChangeRate * 100
                          ).toFixed(2)}%`
                        : '0%'}
                    </p>
                  </div>
                  <p className="text-xs text-gray-500 mt-2">
                    거래량:{' '}
                    {new Intl.NumberFormat().format(
                      parseFloat(ticker.accTradeVolume?.toFixed(3) || '0'),
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
        <Button onClick={() => setPage((p) => Math.max(1, p - 1))} disabled={page === 1}>
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
    </div>
  );
}

// 에러 메시지 렌더링 함수
function renderError(message: string) {
  return (
    <RequireAuthenticated>
      <div className="p-6 flex justify-center items-center">
        <p className="text-red-500">{message}</p>
      </div>
    </RequireAuthenticated>
  );
}
