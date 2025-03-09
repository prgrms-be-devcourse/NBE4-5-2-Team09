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
import axios from "axios";

export default function ClientPage() {
  const { market } = useParams() as { market: string };
  const { ticker } = useWebSocket();

  // 보정된 캔들 데이터를 저장
  const [candles, setCandles] = useState<CandleItem[]>([]);
  // 선택한 봉 단위: seconds, minutes, days, weeks, months, years
  const [candleType, setCandleType] = useState<"seconds" | "minutes" | "days" | "weeks" | "months" | "years">("seconds");
  // 분봉일 경우 단위 선택 (예: 1, 3, 5, 10, 15, 30, 60, 240)
  const [minuteUnit, setMinuteUnit] = useState(1);

  // 봉 단위에 따른 폴링 간격(ms)을 결정하는 함수
  const getPollingInterval = (type: string): number => {
    switch (type) {
      case "seconds":
        return 1000; // 초봉은 1초
      case "minutes":
        return 30000; // 분봉은 30초
      case "days":
        return 3600000; // 일봉은 1시간
      case "weeks":
      case "months":
      case "years":
        return 86400000; // 주, 월, 연봉은 하루 주기 업데이트
      default:
        return 1000;
    }
  };

  useEffect(() => {
    const fetchCandles = async () => {
      try {
        const unitQuery = candleType === "minutes" ? `?unit=${minuteUnit}` : "";
        const url = `http://localhost:8080/api/candles/${market}/${candleType}${unitQuery}`;
        const response = await axios.get(url);
        const data: CandleChartDto[] = response.data;
        console.log("REST로 수신한 DTO:", data);
        const mapped: CandleItem[] = data.map((dto) => ({
          time: new Date(dto.candle_date_time_utc).getTime(),
          open: dto.opening_price,
          high: dto.high_price,
          low: dto.low_price,
          close: dto.trade_price,
          volume: dto.candle_acc_trade_volume,
        }));
        setCandles(mapped);
      } catch (error) {
        console.error("캔들 데이터 호출 오류:", error);
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
        <div className="mb-4">
          <h1 className="text-2xl font-bold flex items-center">
            <span className="mr-2">{market.split("-")[1]}</span>
            {ticker && (
                <span className={`text-xl ${ticker.change === "RISE" ? "text-green-500" : ticker.change === "FALL" ? "text-red-500" : "text-gray-500"}`}>
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

        {/* 분봉일 경우 분봉 단위 선택 드롭다운 (차트 바로 위에 오버레이) */}
        {candleType === "minutes" && (
            <div className="flex items-center mb-4 space-x-2">
              <label className="text-sm font-medium">분봉 단위:</label>
              <select
                  value={minuteUnit}
                  onChange={e => setMinuteUnit(parseInt(e.target.value))}
                  className="text-sm p-1 border rounded"
              >
                <option value={1}>1분</option>
                <option value={3}>3분</option>
                <option value={5}>5분</option>
                <option value={10}>10분</option>
                <option value={15}>15분</option>
                <option value={30}>30분</option>
                <option value={60}>60분</option>
                <option value={240}>240분</option>
              </select>
            </div>
        )}

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
