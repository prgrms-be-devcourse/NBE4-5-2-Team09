"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import Link from "next/link";
import { useWebSocket } from "@/context/WebSocketContext";
import { MarketDto } from "@/types";

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
        updateSubscriptions([{ type: "ticker", markets: marketCodes }]);
      }
    }
  }, [marketCodes, updateSubscriptions]);

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 px-2 mt-5">
      {markets.map((market) => {
        const ticker = tickers[market.code];
        return (
          <Link key={market.code} href={`/coin/${market.code}`}>
            <Card className="bg-white shadow-sm rounded-sm border-0">
              <CardContent className="p-4">
                <div className="flex items-center space-x-1">
                  <h2 className="text-base font-bold truncate">{market.koreanName}</h2>
                  <h3 className="text-sm text-gray-500 truncate">{market.englishName}</h3>
                </div>
                <div className="flex justify-between items-end mt-1">
                  <p className="text-xl font-semibold">
                    {ticker
                      ? new Intl.NumberFormat(undefined, {
                        minimumFractionDigits:
                          ticker.tradePrice <= 1
                            ? 8
                            : ticker.tradePrice < 1000
                              ? 1
                              : 0,
                        maximumFractionDigits:
                          ticker.tradePrice <= 1
                            ? 8
                            : ticker.tradePrice < 1000
                              ? 1
                              : 0,
                      }).format(ticker.tradePrice)
                      : "0"}{" "}
                    <span className="text-xs">
                      {market.code.split("-")[0]}
                    </span>
                  </p>
                  <p
                    className={
                      ticker && ticker.signedChangeRate >= 0
                        ? "text-sm text-red-500"
                        : "text-sm text-blue-500"
                    }
                  >
                    {ticker && ticker.signedChangeRate
                      ? `${ticker.signedChangeRate >= 0 ? "+" : ""}${(
                        ticker.signedChangeRate * 100
                      ).toFixed(2)}%`
                      : "0%"}
                  </p>
                </div>
                <p className="text-xs text-gray-500 mt-2">
                  거래량{" "}
                  {ticker
                    ? new Intl.NumberFormat().format(
                      parseFloat(ticker.accTradeVolume?.toFixed(3) || "0")
                    )
                    : "0"}{" "}
                  <span className="text-xs">
                    {market.code.split("-")[1]}
                  </span>
                </p>
              </CardContent>
            </Card>
          </Link>
        );
      })}
    </div>
  );
}
