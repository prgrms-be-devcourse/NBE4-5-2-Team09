'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { useWebSocket } from '@/context/WebSocketContext';
import OrderbookList from '../components/orderbook/OrderbookList';
import CandleChart from '../components/CandleChart';
import NewsList from '../components/NewsList';
import { generateMockNews } from '@/lib/utils';
import type { CandleItem } from '@/types';
import TradeList from '@/app/coin/components/TradeList';
import Ticker from '@/app/coin/components/Ticker';
import { fetchApi } from '@/lib/api';

export default function ClientPage() {
  const { market } = useParams() as { market: string };
  const { tickers } = useWebSocket();
  const ticker = tickers?.[market] ?? null;
  const { trades } = useWebSocket();
  const trade = trades?.[market] ?? null;
  const { orderbooks } = useWebSocket();
  const orderbook = orderbooks?.[market] ?? null;

  // 보정된 캔들 데이터를 저장
  const [candles, setCandles] = useState<CandleItem[]>([]);
  // 선택한 봉 단위: seconds, minutes, days, weeks, months, years
  const [candleType, setCandleType] = useState<
    'seconds' | 'minutes' | 'days' | 'weeks' | 'months' | 'years'
  >('seconds');
  // 분봉일 경우 단위 선택 (예: 1, 3, 5, 10, 15, 30, 60, 240)
  const [minuteUnit, setMinuteUnit] = useState(1);

  // 봉 단위에 따른 폴링 간격(ms)을 결정하는 함수
  const getPollingInterval = (type: string): number => {
    switch (type) {
      case 'seconds':
        return 1000; // 초봉은 1초
      case 'minutes':
        return 30000; // 분봉은 30초
      case 'days':
        return 3600000; // 일봉은 1시간
      case 'weeks':
      case 'months':
      case 'years':
        return 86400000; // 주, 월, 연봉은 하루 주기 업데이트
      default:
        return 1000;
    }
  };

  useEffect(() => {
    const fetchCandles = async () => {
      try {
        const unitQuery = candleType === 'minutes' && minuteUnit ? `&unit=${minuteUnit}` : '';
        const data = await fetchApi<CandleItem[]>(
          `/api/candle?market=${market}&candleType=${candleType}${unitQuery}`,
          {
            method: 'GET',
          },
        );
        setCandles(data);
      } catch (err) {
        console.error('캔들 데이터 호출 오류:', err);
      }
    };

    // 폴링 간격을 봉 단위에 따라 동적으로 결정
    const pollingInterval = getPollingInterval(candleType);
    fetchCandles();
    const interval = setInterval(fetchCandles, pollingInterval);
    return () => clearInterval(interval);
  }, [market, candleType, minuteUnit]);

  return (
    <div>
      <Ticker market={market} ticker={ticker} />

      <div className="space-y-4">
        {/* CandleChart 컴포넌트에 setCandleType, minuteUnit, setMinuteUnit 전달 */}
        <CandleChart
          candles={candles}
          candleType={candleType}
          setCandleType={setCandleType}
          minuteUnit={minuteUnit}
          setMinuteUnit={setMinuteUnit}
        />
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <TradeList market={market} trade={trade} />
          <OrderbookList market={market} orderbook={orderbook} />
        </div>
        <div className="w-full">
          <NewsList news={generateMockNews()} />
        </div>
      </div>
    </div>
  );
}
