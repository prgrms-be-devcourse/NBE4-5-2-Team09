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

  // 모의 데이터 (호가, 체결, 뉴스)
  const orderbook = generateMockOrderbook();
  const trades = generateMockTrades();
  const news = generateMockNews();

  // 캔들 데이터: 웹소켓을 통해 백엔드에서 받아옴
  const [candles, setCandles] = useState<CandleItem[]>([]);

  useEffect(() => {
    const client = new Client({
      brokerURL: "ws://localhost:8080/websocket", // 백엔드 웹소켓 엔드포인트
      reconnectDelay: 5000,
      debug: (str) => console.log("[STOMP DEBUG]:", str),
    });

    client.onConnect = () => {
      console.log("STOMP 연결 성공");
      // 백엔드에서 "/sub/coin/candles/{market}" 토픽으로 캔들 데이터를 전송한다고 가정합니다.
      const topic = `/sub/coin/candles/${market}/${"day"}`;
      console.log("구독할 토픽:", topic);
      const subscription = client.subscribe(topic, (message) => {
        try {
          const dto: CandleChartDto = JSON.parse(message.body);
          console.log("수신한 DTO:", dto);
          // 변환: CandleChartDto → CandleItem
          const newCandle: CandleItem = {
            time: new Date(dto.candleDateTime).getTime(),
            open: dto.openingPrice,
            high: dto.highPrice,
            low: dto.lowPrice,
            close: dto.closingPrice,
            volume: dto.volume,
          };
          console.log("변환된 candleItem:", newCandle);
          setCandles((prev) => {
            const updated = [...prev, newCandle];
            // 최대 50건 유지
            const limited = updated.length > 50 ? updated.slice(updated.length - 50) : updated;
            console.log("현재 candles 상태:", limited);
            return limited;
          });
        } catch (error) {
          console.error("캔들 데이터 파싱 오류:", error);
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

  // 임시로 캔들 데이터 상태를 화면에 JSON으로 출력 (디버깅용)
  // <pre>{JSON.stringify(candles, null, 2)}</pre>

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
                  {ticker
                      ? Math.floor(ticker.accTradeVolume24h).toLocaleString()
                      : "-"}
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
                  {ticker
                      ? (ticker.signedChangeRate * 100).toFixed(2) + "%"
                      : "-"}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="space-y-4">
          <div className="w-full">
            <CandleChart candles={candles} />
            {/* 디버그용: 수신한 캔들 데이터 JSON 출력 */}
            {/* <pre>{JSON.stringify(candles, null, 2)}</pre> */}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <TradeList trades={generateMockTrades()} />
            <OrderbookList
                orderbook={generateMockOrderbook()}
                currentPrice={ticker?.tradePrice || 0}
            />
          </div>

          <div className="w-full">
            <NewsList news={generateMockNews()} />
          </div>
        </div>
      </div>
  );
}
