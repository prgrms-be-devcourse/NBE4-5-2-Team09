'use client';

import { useEffect, useMemo, useRef } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import Link from 'next/link';
import { useWebSocket } from '@/context/WebSocketContext';
import { MarketDto } from '@/types';

interface ClientPageProps {
  markets: MarketDto[];
}

export default function ClientPage({ markets }: ClientPageProps) {
  const { tickers, updateSubscriptions } = useWebSocket();

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

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6 mt-5 px-2">
      {markets.map((market) => {
        const ticker = tickers[market.code];
        return (
          <Card
            key={market.code}
            className="flex flex-col gap-4 bg-white shadow-sm rounded-sm border-0 cursor-pointer shadow-md hover:shadow-lg"
          >
            <Link href={`/coin/${market.code}`} className="flex flex-col h-full">
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
          </Card>
        );
      })}
    </div>
  );
}
