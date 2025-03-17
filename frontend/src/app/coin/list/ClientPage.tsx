'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import { useWebSocket } from '@/context/WebSocketContext';
import { MarketDto, TickerDto } from '@/types';
import { useBookmarkToggle } from '@/hooks/useBookmarkToggle'; // 훅 불러오기
import MarketCard from '../components/MarketCard';
import { client } from '@/lib/api';

interface ClientPageProps {
  markets: MarketDto[];
}

export default function ClientPage({ markets }: ClientPageProps) {
  const { tickers: wsTickers, updateSubscriptions } = useWebSocket();
  const [tickers, setTickers] = useState<Record<string, TickerDto | null>>({});

  // 처음 마운트 시 API에서 초기 데이터 가져오기
  useEffect(() => {
    async function fetchInitialTickers() {
      try {
        const requestBody = {
          markets: marketCodes,
        };

        const { data, error } = await client.POST('/api/ticker', {
          body: requestBody,
        });

        if (error || !data) {
          throw new Error('Ticker 데이터를 불러오는 중 오류 발생');
        }

        // ticker를 code 기반의 객체로 변환
        const tickerMap: Record<string, TickerDto> = (data.tickers as TickerDto[]).reduce(
          (acc, ticker) => {
            acc[ticker.code] = ticker;
            return acc;
          },
          {} as Record<string, TickerDto>,
        );

        setTickers(tickerMap);
      } catch (err) {
        console.error('Ticker 데이터를 불러오는 중 오류 발생:', err);
      }
    }

    fetchInitialTickers();
  }, []);

  // 웹소켓 데이터가 변경될 때 API에서 가져온 tickers를 웹소켓 데이터로 갱신
  useEffect(() => {
    if (Object.keys(wsTickers).length > 0) {
      setTickers((prevTickers) => ({
        ...prevTickers,
        ...wsTickers, // 기존 API 데이터에 웹소켓 데이터 덮어쓰기
      }));
    }
  }, [wsTickers]);

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

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6 mt-5 px-2">
      {markets.map((market) => {
        const handleBookmarkToggle = useBookmarkToggle(market);

        return (
          <MarketCard
            key={market.code}
            market={market}
            ticker={tickers[market.code]}
            onBookmarkToggle={handleBookmarkToggle}
          />
        );
      })}
    </div>
  );
}
