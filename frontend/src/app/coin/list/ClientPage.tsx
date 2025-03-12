'use client';

import { useEffect, useMemo, useRef } from 'react';
import { useAuth } from '@/context/AuthContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import Link from 'next/link';
import { useWebSocket } from '@/context/WebSocketContext';
import { MarketDto } from '@/types';

interface ClientPageProps {
  markets: MarketDto[];
}

export default function ClientPage({ markets }: ClientPageProps) {
  const { tickers, updateSubscriptions } = useWebSocket();
  const { accessToken } = useAuth();

  // markets 배열에서 시장 코드 배열을 useMemo로 계산 (불필요한 재계산 방지)
  const marketCodes = useMemo(() => {
    return markets.map((market) => market.code).filter((code) => Boolean(code));
  }, [markets]);

  // 이전 구독 배열을 저장할 ref
  const prevMarketCodesRef = useRef<string[]>([]);

  // marketCodes가 실제로 변경되었을 때만 updateSubscriptions 호출
  useEffect(() => {
    if (marketCodes.length > 0) {
      const newCodes = JSON.stringify(marketCodes);
      const prevCodes = JSON.stringify(prevMarketCodesRef.current);
      if (newCodes !== prevCodes) {
        prevMarketCodesRef.current = marketCodes;
        updateSubscriptions([{ type: 'ticker', markets: marketCodes }]);
      }
    }
  }, [marketCodes, updateSubscriptions]);

  const formatTradePrice = (tradePrice: number): string => {
    const decimalPlaces = tradePrice <= 1 ? 8 : tradePrice < 1000 ? 1 : 0;

    return new Intl.NumberFormat(undefined, {
      minimumFractionDigits: decimalPlaces,
      maximumFractionDigits: decimalPlaces,
    }).format(tradePrice);
  };

  const formatSignedChangeRate = (rate: number): string => {
    return `${rate >= 0 ? '+' : ''}${(rate * 100).toFixed(2)}%`;
  };

  const formatTradeVolume = (volume: number): string => {
    return new Intl.NumberFormat(undefined, {
      minimumFractionDigits: 3,
      maximumFractionDigits: 3,
    }).format(volume);
  };

  // 북마크 상태를 토글하고 서버에 요청을 보내는 함수
  const handleBookmarkToggle = async (market: MarketDto) => {
    const { isBookmarked } = market;
    const endpoint = isBookmarked ? `/api/bookmark/${market.code}` : '/api/bookmark'; // URL을 북마크 추가/삭제에 따라 다르게 설정
    const requestBody = {
      coinCode: market.code, // s필요한 coinCode를 넣어줍니다
    };

    try {
      const response = await fetch(process.env.NEXT_PUBLIC_API_URL + endpoint, {
        method: isBookmarked ? 'DELETE' : 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${accessToken}`,
        },
        credentials: 'include',
        body: isBookmarked ? null : JSON.stringify(requestBody), // 북마크 추가일 경우 body에 시장 코드 전달
      });

      if (response.ok) {
        // 요청 성공 시 북마크 상태를 반영
        market.isBookmarked = !isBookmarked;
      } else {
        console.error('Failed to update bookmark');
      }
    } catch (error) {
      console.error('Error occurred while updating bookmark:', error);
    }
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6 mt-5 px-2">
      {markets.map((market) => {
        const ticker = tickers[market.code];
        return (
          <Card
            key={market.code}
            className="flex flex-col gap-4 bg-card rounded-sm border-0 cursor-pointer shadow-md hover:shadow-lg"
          >
            <div className="relative flex flex-col h-full">
              <Link href={`/coin/${market.code}`} className="flex flex-col">
                <CardHeader className="mb-4">
                  <div className="flex flex-row flex-wrap">
                    <CardTitle className="text-lg font-bold mr-2">{market.koreanName}</CardTitle>
                    <div className="text-muted-foreground text-sm font-light self-end my-1">
                      {market.englishName}
                    </div>
                  </div>
                  <div className="text-xs text-gray-400">({market.code})</div>
                </CardHeader>
                <CardContent className="mt-auto">
                  <div className="flex flex-wrap justify-between items-end mt-1">
                    <p
                      className={`text-xl font-semibold ${ticker ? (ticker.signedChangeRate >= 0 ? 'text-red-500' : 'text-blue-500') : ''}`}
                    >
                      {ticker ? formatTradePrice(ticker.tradePrice) : '0'}
                      <span className="ml-1 text-xs">{market.code.split('-')[0]}</span>
                    </p>

                    <p
                      className={`text-sm ${ticker ? (ticker.signedChangeRate >= 0 ? 'text-red-500' : 'text-blue-500') : ''}`}
                    >
                      {ticker?.signedChangeRate
                        ? formatSignedChangeRate(ticker.signedChangeRate)
                        : '0%'}
                    </p>
                  </div>

                  <p className="text-xs text-muted-foreground mt-2">
                    거래량 {ticker ? formatTradeVolume(ticker.accTradeVolume) : '0'}
                    <span className="text-xs">{market.code.split('-')[1]}</span>
                  </p>
                </CardContent>
              </Link>

              {/* Bookmark 버튼 */}
              <button
                className="absolute top-1 right-5.5 text-gray-300 hover:text-gray-600 transition-colors"
                onClick={(e) => {
                  e.stopPropagation(); // 링크 클릭을 방지
                  handleBookmarkToggle(market);
                }}
              >
                <svg
                  className="w-5 h-5"
                  fill={market.isBookmarked ? '#FFCC33' : 'currentColor'}
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"
                  />
                </svg>
              </button>
            </div>
          </Card>
        );
      })}
    </div>
  );
}
