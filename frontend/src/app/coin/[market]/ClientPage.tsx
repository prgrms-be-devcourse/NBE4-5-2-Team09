"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import OrderbookList from "@/app/coin/components/OrderbookList";
import CandleChart from "@/app/coin/components/CandleChart";
import TradeList from "@/app/coin/components/TradeList";
import NewsList from "@/app/coin/components/NewsList";
import {
  generateMockOrderbook,
  generateMockTrades,
  generateMockNews,
} from "@/app/utils/mockData";
import { useWebSocket } from "@/app/context/WebSocketContext";
import type { CandleItem, CandleChartDto } from "@/app/types";
import { Client } from "@stomp/stompjs";

export default function ClientPage() {
  const { market } = useParams() as { market: string };
  const { ticker } = useWebSocket();

  // 보정된 1초봉 데이터를 저장합니다.
  const [candles, setCandles] = useState<CandleItem[]>([]);

  useEffect(() => {
    const client = new Client({
      brokerURL: "ws://localhost:8080/websocket", // 백엔드 웹소켓 엔드포인트
      reconnectDelay: 500,
      debug: (str) => console.log("[STOMP DEBUG]:", str),
    });

    client.onConnect = () => {
      console.log("STOMP 연결 성공");
      // 서버는 "/sub/coin/candles/{market}/candle.1s" 토픽으로 보정된 1초봉 데이터를 전송합니다.
      const topic = `/sub/coin/candles/${market}/candle.1s`;
      console.log("구독할 토픽:", topic);
      const subscription = client.subscribe(topic, (message) => {
        try {
          // 보정된 CandleDto를 파싱합니다.
          const dto: CandleChartDto = JSON.parse(message.body);
          console.log("수신한 보정된 DTO:", dto);
          // 이제 서버가 보정한 DTO의 필드는 다음과 같이 가정합니다:
          // { code, timestamp, open, high, low, close, volume }
          const newCandle: CandleItem = {
            // timestamp가 string이면 new Date(dto.timestamp).getTime() 사용, 아니면 그대로 사용
            time: typeof dto.timestamp === "string" ? new Date(dto.timestamp).getTime() : dto.timestamp,
            open: dto.open,
            high: dto.high,
            low: dto.low,
            close: dto.close,
            volume: dto.volume,
          };
          // 새로운 캔들을 상태에 추가합니다. (최대 50건 유지)
          setCandles((prev) => {
            const updated = [...prev, newCandle];
            return updated.length > 50 ? updated.slice(updated.length - 50) : updated;
          });
        } catch (error) {
          console.error("보정된 캔들 DTO 파싱 오류:", error);
        }
      });
      return () => subscription.unsubscribe();
    };

    client.onStompError = (frame) => {
      console.error("STOMP 에러:", frame);
    };

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [market]);

  return (
      <div>
        <div className="mb-4">
          <h1 className="text-2xl font-bold flex items-center">
            <span className="mr-2">{market.split("-")[1]}</span>
            {ticker && (
                <span
                    className={`text-xl ${
                        ticker.change === "RISE"
                            ? "text-green-500"
                            : ticker.change === "FALL"
                                ? "text-red-500"
                                : "text-gray-500"
                    }`}
                >
              ₩{ticker.tradePrice.toLocaleString()}
                  <span className="ml-2 text-sm">
                {ticker.signedChangeRate > 0 ? "+" : ""}
                    {(ticker.signedChangeRate * 100).toFixed(2)}%
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
                  {ticker ? Math.floor(ticker.accTradeVolume24h).toLocaleString() : "-"}
                  <span className="text-sm">{market.split("-")[1]}</span>
                </div>
              </div>
              <div className="bg-white p-4 rounded-lg shadow-sm">
                <div className="text-sm text-gray-500">전일 종가</div>
                <div className="text-xl font-bold text-blue-600">
                  {ticker ? ticker.prevClosingPrice.toLocaleString() : "-"}
                  <span className="text-sm">{market.split("-")[0]}</span>
                </div>
              </div>
              <div className="bg-white p-4 rounded-lg shadow-sm">
                <div className="text-sm text-gray-500">도미넌스</div>
                <div className="text-xl font-bold text-blue-600">52.1%</div>
              </div>
              <div className="bg-white p-4 rounded-lg shadow-sm">
                <div className="text-sm text-gray-500">전일대비</div>
                <div className="text-xl font-bold text-blue-600">
                  {ticker ? (ticker.signedChangeRate * 100).toFixed(2) + "%" : "-"}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="space-y-4">
          <div className="w-full">
            <CandleChart candles={candles} />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <TradeList trades={generateMockTrades()} />
            <OrderbookList orderbook={generateMockOrderbook()} currentPrice={ticker?.tradePrice || 0} />
          </div>

          <div className="w-full">
            <NewsList news={generateMockNews()} />
          </div>
        </div>
      </div>
  );
}
