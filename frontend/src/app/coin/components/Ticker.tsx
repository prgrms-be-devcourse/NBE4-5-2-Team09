'use client';

import { useEffect, useState, useRef } from 'react';

interface TickerProps {
  market: string;
  ticker: TickerDto | null;
}

type TickerResponse = {
  ticker: TickerDto | null;
};

export type TickerDto = {
  tradePrice: number;
  prevClosingPrice: number;
  accTradeVolume24h: number;
  signedChangeRate: number;
  change: 'RISE' | 'FALL' | 'EVEN';
};

export default function Ticker({ market, ticker }: TickerProps) {
  const isMounted = useRef(false);
  const [currentTicker, setCurrentTicker] = useState<TickerDto | null>(null);

  // 마운트 시 Ticker 데이터가 없으면 API 요청
  useEffect(() => {
    async function fetchTicker() {
      try {
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/ticker/${market}`);

        if (!response.ok) {
          throw new Error('Ticker 데이터를 불러오는 중 오류 발생');
        }

        const data: TickerResponse = await response.json();
        setCurrentTicker(data.ticker);
      } catch (err) {
        console.error('Ticker 데이터를 불러오는 중 오류 발생:', err);
      } finally {
        isMounted.current = true;
      }
    }

    fetchTicker();
  }, []);

  // `ticker` 업데이트 감지하여 상태 반영
  useEffect(() => {
    if (!isMounted.current) return;

    if (ticker) {
      setCurrentTicker(ticker);
    }
  }, [ticker]);

  return (
    <div>
      <div className="mb-4">
        <h1 className="text-2xl font-bold flex items-center">
          <span className="mr-2">{market.split('-')[1]}</span>
          {currentTicker && (
            <span
              className={`text-xl ${
                currentTicker.change === 'RISE'
                  ? 'text-green-500'
                  : currentTicker.change === 'FALL'
                    ? 'text-red-500'
                    : 'text-gray-500'
              }`}
            >
              ₩{currentTicker.tradePrice.toLocaleString()}
              <span className="ml-2 text-sm">
                {currentTicker.signedChangeRate > 0 ? '+' : ''}
                {(currentTicker.signedChangeRate * 100).toFixed(2)}%
              </span>
            </span>
          )}
        </h1>
      </div>

      <div className="bg-blue-50 py-4 mb-4 rounded-lg">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-4 gap-4">
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <div className="text-sm text-gray-500">24시간 거래량</div>
              <div className="text-xl font-bold text-blue-600">
                {currentTicker ? Math.floor(currentTicker.accTradeVolume24h).toLocaleString() : '-'}
                <span className="text-sm">{market.split('-')[1]}</span>
              </div>
            </div>
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <div className="text-sm text-gray-500">전일 종가</div>
              <div className="text-xl font-bold text-blue-600">
                {currentTicker ? currentTicker.prevClosingPrice.toLocaleString() : '-'}
                <span className="text-sm">{market.split('-')[0]}</span>
              </div>
            </div>
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <div className="text-sm text-gray-500">도미넌스</div>
              <div className="text-xl font-bold text-blue-600">52.1%</div>
            </div>
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <div className="text-sm text-gray-500">전일대비</div>
              <div className="text-xl font-bold text-blue-600">
                {currentTicker ? (currentTicker.signedChangeRate * 100).toFixed(2) + '%' : '-'}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
