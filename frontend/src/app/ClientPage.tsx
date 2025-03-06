"use client";

import { useState, useEffect } from "react";
import { Client } from "@stomp/stompjs";
import OrderBookList from "./components/OrderBookList";
import CandleChart from "./components/CandleChart";
import TradeList from "./components/TradeList";
import NewsList from "./components/NewsList";
import type { TickerDto } from "./types";
import {
  generateMockOrderBook,
  generateMockTrades,
  generateMockCandles,
  generateMockNews,
} from "./utils/mockData";

export default function ClientPage() {
  const [ticker, setTicker] = useState<TickerDto | undefined>(undefined);
  const [isConnected, setIsConnected] = useState(false);
  const [connectionError, setConnectionError] = useState<string | undefined>(
    undefined
  );

  useEffect(() => {
    const client = new Client({
      brokerURL: "ws://localhost:8080/websocket",
      debug: (str) => {
        console.log(str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      setIsConnected(true);
      setConnectionError(undefined);

      client.subscribe("/sub/coin/ticker", (message) => {
        try {
          const tickerData: TickerDto = JSON.parse(message.body);
          setTicker(tickerData);
        } catch (error) {
          console.error("Failed to parse ticker data:", error);
        }
      });
    };

    client.onStompError = (frame) => {
      setConnectionError(`STOMP error: ${frame.headers.message}`);
      setIsConnected(false);
    };

    client.onWebSocketError = (event) => {
      setConnectionError("WebSocket connection error");
      setIsConnected(false);
    };

    client.activate();

    return () => {
      if (client.active) {
        client.deactivate();
      }
    };
  }, []);

  // 모의 데이터 생성 함수
  const orderBook = generateMockOrderBook();
  const trades = generateMockTrades();
  const candles = generateMockCandles();
  const news = generateMockNews();

  return (
    <div>
      {connectionError && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {connectionError}
        </div>
      )}

      <div className="mb-4">
        <h1 className="text-2xl font-bold flex items-center">
          <span className="mr-2">이오스(EOS)</span>
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
                <text className="text-sm">EOS</text>
              </div>
            </div>
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <div className="text-sm text-gray-500">전일 종가</div>
              <div className="text-xl font-bold text-blue-600">
                {ticker ? ticker.prevClosingPrice.toLocaleString() : "-"}
                <text className="text-sm">KRW</text>
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
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <TradeList trades={trades} />
          <OrderBookList
            orderBook={orderBook}
            currentPrice={ticker?.tradePrice || 0}
          />
        </div>

        <div className="w-full">
          <NewsList news={news} />
        </div>
      </div>
    </div>
  );
}
