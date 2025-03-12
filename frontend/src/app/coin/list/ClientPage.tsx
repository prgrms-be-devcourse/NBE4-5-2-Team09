'use client';

import { useEffect, useMemo, useRef } from 'react';
import { useWebSocket } from '@/context/WebSocketContext';
import { MarketDto } from '@/types';
import { useBookmarkToggle } from '@/hooks/useBookmarkToggle'; // 훅 불러오기
import MarketCard from '../components/MarketCard';

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
