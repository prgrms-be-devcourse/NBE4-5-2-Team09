'use client';

import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { MarketDto, TickerDto } from '@/types';

interface MarketCardProps {
  market: MarketDto;
  ticker?: TickerDto | null;
  onBookmarkToggle: (market: MarketDto) => void;
}

export default function MarketCard({ market, ticker, onBookmarkToggle }: MarketCardProps) {
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
    <Card className="flex flex-col gap-4 bg-white shadow-sm rounded-sm border-0 cursor-pointer shadow-md hover:shadow-lg">
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
                {ticker?.signedChangeRate ? formatSignedChangeRate(ticker.signedChangeRate) : '0%'}
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
          className="cursor-pointer absolute top-1 right-5.5 text-gray-300 hover:text-gray-600 transition-colors"
          onClick={(e) => {
            e.stopPropagation(); // 링크 클릭을 방지
            onBookmarkToggle(market);
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
}
