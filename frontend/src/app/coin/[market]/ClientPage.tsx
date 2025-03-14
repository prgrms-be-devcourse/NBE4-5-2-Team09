'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { useWebSocket } from '@/context/WebSocketContext';
import OrderbookList from '../components/orderbook/OrderbookList';
import CandleChart from '../components/CandleChart';
import NewsList from '../components/NewsList';
import type { CandleItem, NewsItem } from '@/types';
import TradeList from '@/app/coin/components/TradeList';
import Ticker from '@/app/coin/components/Ticker';
import { fetchApi } from '@/lib/api';

export default function ClientPage() {
  const { market } = useParams() as { market: string };
  const { tickers, trades, orderbooks } = useWebSocket();
  const ticker = tickers?.[market] ?? null;
  const trade = trades?.[market] ?? null;
  const orderbook = orderbooks?.[market] ?? null;

  // 보정된 캔들 데이터를 저장
  const [candles, setCandles] = useState<CandleItem[]>([]);
  // 선택한 봉 단위: seconds, minutes, days, weeks, months, years
  const [candleType, setCandleType] = useState<
      'seconds' | 'minutes' | 'days' | 'weeks' | 'months' | 'years'
  >('seconds');
  // 분봉일 경우 단위 선택 (예: 1, 3, 5, 10, 15, 30, 60, 240)
  const [minuteUnit, setMinuteUnit] = useState(1);

  // 뉴스 데이터 state (NewsItem 배열)
  const [news, setNews] = useState<NewsItem[]>([]);

  // 봉 단위에 따른 폴링 간격(ms) 결정 함수
  const getPollingInterval = (type: string): number => {
    switch (type) {
      case 'seconds':
        return 1000;
      case 'minutes':
        return 30000;
      case 'days':
        return 3600000;
      case 'weeks':
      case 'months':
      case 'years':
        return 86400000;
      default:
        return 1000;
    }
  };

  // 캔들 데이터 fetch
  useEffect(() => {
    const fetchCandles = async () => {
      try {
        const unitQuery =
            candleType === 'minutes' && minuteUnit ? `&unit=${minuteUnit}` : '';
        const data = await fetchApi<CandleItem[]>(
            `/api/candle?market=${market}&candleType=${candleType}${unitQuery}`,
            { method: 'GET' }
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

  // 뉴스 데이터 fetch (API 라우트 통해 변환된 데이터 사용)
  useEffect(() => {
    const fetchNews = async () => {
      try {
        const url = `/api/news?market=${encodeURIComponent(market)}`;
        const data = await fetchApi<NewsItem[]>(url, { method: 'GET' });
        setNews(data);
      } catch (err) {
        console.error('뉴스 데이터 호출 오류:', err);
      }
    };

    if (market) {
      fetchNews();
    }
  }, [market]);

  return (
      <div>
        <Ticker market={market} ticker={ticker} />
        <div className="space-y-4">
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
            <NewsList news={news} />
          </div>
        </div>
      </div>
  );
}
