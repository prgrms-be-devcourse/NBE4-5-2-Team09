'use client';

import { useState, useEffect, useRef } from 'react';
import { AskBid, TradeDto } from '@/types';

interface TradeListProps {
  market: string;
  trade: TradeDto | null;
}

type TradeResponse = {
  trades: TradeDto[];
};

export default function TradeList({ market, trade }: TradeListProps) {
  const [clientTrades, setClientTrades] = useState<TradeDto[]>([]);
  const isMounted = useRef(false);

  useEffect(() => {
    async function fetchTradeList() {
      try {
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/trade/${market}`);

        if (!response.ok) {
          throw new Error('체결 데이터를 불러오는 중 오류 발생');
        }

        const data: TradeResponse = await response.json();
        setClientTrades((prevTrades) => {
          // 기존 데이터와 새 데이터를 병합하면서 중복 제거
          const mergedTrades = [...data.trades, ...prevTrades].filter(
            (trade, index, self) =>
              index === self.findIndex((t) => t.sequentialId === trade.sequentialId),
          );

          // 최신 거래가 맨 위에 오도록 정렬
          return mergedTrades.sort((a, b) => b.sequentialId - a.sequentialId).slice(0, 20);
        });
      } catch (err) {
        console.error('체결 데이터를 불러오는 중 오류 발생:', err);
      } finally {
        isMounted.current = true;
      }
    }

    fetchTradeList();
  }, []);

  useEffect(() => {
    if (!isMounted.current) return;

    if (trade) {
      setClientTrades((prevTrades) => {
        // 중복 확인 (기존 배열에 같은 id가 있으면 추가하지 않음)
        if (prevTrades.some((t) => t.sequentialId === trade.sequentialId)) {
          return prevTrades;
        }

        const updatedTrades = [trade, ...prevTrades];
        return updatedTrades.sort((a, b) => b.sequentialId - a.sequentialId).slice(0, 20);
      });
    }
  }, [trade]);

  const formatTime = (timestamp: number) => {
    return new Date(timestamp).toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
    });
  };

  const formatPrice = (price: number) => {
    return price.toLocaleString();
  };

  return (
    <div className="bg-card rounded-lg shadow-sm overflow-hidden">
      <div className="p-4 border-b border-muted">
        <h2 className="text-lg font-semibold">체결 내역</h2>
      </div>
      {clientTrades.length === 0 ? (
        <div className="p-4 text-center text-muted-foreground">체결 내역 없음</div>
      ) : (
        <div className="overflow-y-auto max-h-[400px]">
          <table className="w-full">
            <thead className="bg-muted">
              <tr>
                <th className="px-4 py-2 text-xs text-muted-foreground text-left">시간</th>
                <th className="px-4 py-2 text-xs text-muted-foreground text-right">가격(KRW)</th>
                <th className="px-4 py-2 text-xs text-muted-foreground text-right">수량(BTC)</th>
                <th className="px-4 py-2 text-xs text-muted-foreground text-center">구분</th>
              </tr>
            </thead>
            <tbody>
              {clientTrades.map((trade) => (
                <tr key={trade.sequentialId} className="border-b border-muted">
                  <td className="px-4 py-2 text-sm text-gray-600">{formatTime(trade.timestamp)}</td>
                  <td
                    className={`px-4 py-2 text-sm text-right ${
                      trade.askBid === AskBid.ASK ? 'text-red-500' : 'text-green-500'
                    }`}
                  >
                    {formatPrice(trade.tradePrice)}
                  </td>
                  <td className="px-4 py-2 text-sm text-right">{trade.tradeVolume.toFixed(4)}</td>
                  <td className="px-4 py-2 text-sm text-center">
                    <span
                      className={`inline-block px-2 py-1 rounded-full text-xs ${
                        trade.askBid === AskBid.ASK
                          ? 'bg-light-red text-red-800'
                          : 'bg-light-green text-green-800'
                      }`}
                    >
                      {trade.askBid === AskBid.ASK ? '매도' : '매수'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
